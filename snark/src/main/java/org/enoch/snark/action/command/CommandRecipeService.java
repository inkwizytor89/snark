package org.enoch.snark.action.command;

import org.enoch.common.command.CommandCatalogResponse;
import org.enoch.common.command.CommandSubmitRequest;
import org.enoch.common.command.CommandSubmitResponse;
import org.enoch.snark.expression.ExpressionService;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.si.Core;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CommandRecipeService {

    private final CommandRegistryService commandRegistryService;
    private final ExpressionService expressionService;
    private final Core core;

    public CommandRecipeService(CommandRegistryService commandRegistryService,
                                ExpressionService expressionService,
                                Core core) {
        this.commandRegistryService = commandRegistryService;
        this.expressionService = expressionService;
        this.core = core;
    }

    public CommandCatalogResponse listCommands() {
        return CommandCatalogResponse.builder()
                .commands(commandRegistryService.listAvailableCommands())
                .build();
    }

    public CommandSubmitResponse submitRecipe(CommandSubmitRequest request) {
        if (request == null || request.getCommandName() == null || request.getCommandName().isBlank()) {
            return CommandSubmitResponse.builder()
                    .commandName(request == null ? "" : request.getCommandName())
                    .generatedCount(0)
                    .queuedCount(0)
                    .message("Command name is required")
                    .build();
        }

        Class<? extends AbstractCommand> commandType = commandRegistryService.resolveCommand(request.getCommandName());
        if (commandType == null) {
            return CommandSubmitResponse.builder()
                    .commandName(request.getCommandName())
                    .generatedCount(0)
                    .queuedCount(0)
                    .message("Unknown command: " + request.getCommandName())
                    .build();
        }

        List<Map<String, Object>> combinations = buildParameterCombinations(request.getValues());
        if (combinations.isEmpty()) {
            return CommandSubmitResponse.builder()
                    .commandName(request.getCommandName())
                    .generatedCount(0)
                    .queuedCount(0)
                    .message("No values were provided for command " + request.getCommandName())
                    .build();
        }

        List<AbstractCommand> generated = new ArrayList<>();
        for (Map<String, Object> combo : combinations) {
            try {
                generated.add(instantiateCommand(commandType, combo));
            } catch (RuntimeException e) {
                return CommandSubmitResponse.builder()
                        .commandName(request.getCommandName())
                        .generatedCount(generated.size())
                        .queuedCount(0)
                        .message("Failed to generate command: " + e.getMessage())
                        .build();
            }
        }

        for (AbstractCommand command : generated) {
            core.push(command);
        }

        return CommandSubmitResponse.builder()
                .commandName(request.getCommandName())
                .generatedCount(generated.size())
                .queuedCount(generated.size())
                .message("Queued " + generated.size() + " command(s)")
                .build();
    }

    private List<Map<String, Object>> buildParameterCombinations(Map<String, String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of(Map.of());
        }

        Map<String, List<Object>> expanded = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : rawValues.entrySet()) {
            String fieldName = entry.getKey();
            List<Object> values = expandExpression(fieldName, entry.getValue());
            if (values.isEmpty()) {
                values = new ArrayList<>();
                values.add(entry.getValue() == null ? null : entry.getValue());
            }
            expanded.put(fieldName, values);
        }

        List<String> fieldOrder = new ArrayList<>(expanded.keySet());
        List<Map<String, Object>> combinations = new ArrayList<>();
        int total = 1;
        for (List<Object> values : expanded.values()) {
            total *= values.size();
        }

        for (int index = 0; index < total; index++) {
            Map<String, Object> combination = new LinkedHashMap<>();
            int divisor = 1;
            for (String fieldName : fieldOrder) {
                List<Object> values = expanded.get(fieldName);
                int fieldIndex = (index / divisor) % values.size();
                combination.put(fieldName, values.get(fieldIndex));
                divisor *= values.size();
            }
            combinations.add(combination);
        }
        return combinations;
    }

    private List<Object> expandExpression(String fieldName, String rawValue) {
        if (rawValue == null) {
            return List.of((Object) null);
        }
        String value = rawValue.trim();
        if (value.isEmpty()) {
            return List.of("");
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return List.of(Boolean.parseBoolean(value));
        }
        if (value.equalsIgnoreCase("null")) {
            return List.of((Object) null);
        }
        Integer asInt = tryParseInt(value);
        if (asInt != null) {
            return List.of(asInt);
        }
        Long asLong = tryParseLong(value);
        if (asLong != null) {
            return List.of(asLong);
        }

        String lowered = fieldName.toLowerCase();
        if ((value.startsWith("#") || value.contains("(") || value.contains("[") || value.contains(";"))
                && (lowered.contains("colony") || lowered.contains("planet") || lowered.contains("source") || lowered.contains("target"))) {
            List<PlanetData> coordinates = expressionService.evaluateCoordinates(value);
            if (coordinates != null && !coordinates.isEmpty()) {
                List<Object> result = new ArrayList<>();
                for (PlanetData coordinate : coordinates) {
                    result.add(coordinate.getColony() != null ? coordinate.getColony() : coordinate);
                }
                return result;
            }
        }
        return List.of(value);
    }

    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private AbstractCommand instantiateCommand(Class<? extends AbstractCommand> commandType, Map<String, Object> values) {
        try {
            Constructor<? extends AbstractCommand> defaultConstructor = commandType.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            AbstractCommand instance = defaultConstructor.newInstance();
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldValue = entry.getValue();
                if (fieldValue == null) {
                    continue;
                }
                applyFieldValue(instance, fieldName, fieldValue);
            }
            return instance;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Command " + commandType.getSimpleName() + " does not define a no-arg constructor.", e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to instantiate command " + commandType.getSimpleName(), e);
        }
    }

    private void applyFieldValue(AbstractCommand instance, String fieldName, Object fieldValue) {
        Class<?> targetType = findFieldType(instance.getClass(), fieldName);
        if (targetType == null) {
            return;
        }
        Object normalized = normalizeValue(targetType, fieldValue);
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, normalized);
        } catch (NoSuchFieldException e) {
            if (fieldName.equals("requirements") && normalized != null) {
                try {
                    Field field = instance.getClass().getDeclaredField("requirements");
                    field.setAccessible(true);
                    field.set(instance, normalized);
                } catch (NoSuchFieldException | IllegalAccessException ignored) {
                    throw new IllegalArgumentException("Unsupported field assignment for " + fieldName, e);
                }
            } else {
                throw new IllegalArgumentException("Command field not found: " + fieldName, e);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not assign field " + fieldName, e);
        }
    }

    private Class<?> findFieldType(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    return field.getType();
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private Object normalizeValue(Class<?> targetType, Object fieldValue) {
        if (fieldValue == null) {
            return null;
        }
        if (targetType.isInstance(fieldValue)) {
            return fieldValue;
        }
        if (targetType == String.class) {
            return String.valueOf(fieldValue);
        }
        if (targetType == Long.class || targetType == long.class) {
            if (fieldValue instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(String.valueOf(fieldValue));
        }
        if (targetType == Integer.class || targetType == int.class) {
            if (fieldValue instanceof Number number) {
                return number.intValue();
            }
            return Integer.parseInt(String.valueOf(fieldValue));
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(String.valueOf(fieldValue));
        }
        if (targetType == Double.class || targetType == double.class) {
            if (fieldValue instanceof Number number) {
                return number.doubleValue();
            }
            return Double.parseDouble(String.valueOf(fieldValue));
        }
        if (targetType.getSimpleName().equals("BuildRequirements")) {
            return buildRequirementsFromString(String.valueOf(fieldValue));
        }
        return fieldValue;
    }

    private Object buildRequirementsFromString(String rawValue) {
        String[] parts = rawValue.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Build requirements must use format <technology>:<level>");
        }
        try {
            Class<?> requestType = Class.forName("org.enoch.snark.instance.si.module.building.BuildRequest");
            Class<?> requirementsType = Class.forName("org.enoch.snark.instance.si.module.building.BuildRequirements");
            Object technology = Enum.valueOf((Class<? extends Enum>) Class.forName("org.enoch.snark.instance.model.technology.Technology"), parts[0]);
            Constructor<?> requestCtor = requestType.getDeclaredConstructor(Class.forName("org.enoch.snark.instance.model.technology.Technology"), long.class);
            requestCtor.setAccessible(true);
            Object request = requestCtor.newInstance(technology, Long.parseLong(parts[1]));
            Constructor<?> requirementsCtor = requirementsType.getDeclaredConstructor(requestType, Class.forName("org.enoch.snark.instance.model.to.Resources"));
            requirementsCtor.setAccessible(true);
            return requirementsCtor.newInstance(request, Class.forName("org.enoch.snark.instance.model.to.Resources").getField("unknown").get(null));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported requirements value: " + rawValue, e);
        }
    }
}
