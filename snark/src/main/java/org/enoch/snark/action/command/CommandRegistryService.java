package org.enoch.snark.action.command;

import org.enoch.common.command.CommandDefinition;
import org.enoch.common.command.CommandFieldDefinition;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class CommandRegistryService {

    private static final String COMMAND_PACKAGE = "org.enoch.snark.action.command";

    public List<CommandDefinition> listAvailableCommands() {
        return discoverCommandClasses().stream()
                .map(this::toCommandDefinition)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CommandDefinition::getName))
                .toList();
    }

    public Class<? extends AbstractCommand> resolveCommand(String name) {
        for (Class<?> type : discoverCommandClasses()) {
            if (type.getSimpleName().equals(name) || type.getSimpleName().equals(name + "Command")) {
                return (Class<? extends AbstractCommand>) type;
            }
        }
        return null;
    }

    private List<Class<?>> discoverCommandClasses() {
        List<Class<?>> candidates = new ArrayList<>();
        String packagePath = COMMAND_PACKAGE.replace('.', '/');
        try {
            var resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (!"file".equals(resource.getProtocol())) {
                    continue;
                }
                Path root = Paths.get(resource.toURI());
                if (!Files.isDirectory(root)) {
                    continue;
                }
                try (var stream = Files.list(root)) {
                    stream.filter(path -> path.getFileName().toString().endsWith(".class"))
                            .map(path -> path.getFileName().toString().replace(".class", ""))
                            .forEach(name -> {
                                String className = COMMAND_PACKAGE + "." + name;
                                try {
                                    Class<?> type = Class.forName(className);
                                    if (isValidCommandClass(type)) {
                                        candidates.add(type);
                                    }
                                } catch (ClassNotFoundException ignored) {
                                    // ignored when scanning classpath entries that do not resolve cleanly
                                }
                            });
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Failed to scan command package " + COMMAND_PACKAGE, e);
        }
        return candidates;
    }

    private boolean isValidCommandClass(Class<?> type) {
        if (type.isEnum() || type.isInterface() || type.isAnnotation() || Modifier.isAbstract(type.getModifiers())) {
            return false;
        }
        return AbstractCommand.class.isAssignableFrom(type)
                && type.getSimpleName().endsWith("Command")
                && !AbstractCommand.class.equals(type);
    }

    private CommandDefinition toCommandDefinition(Class<?> type) {
        if (!isValidCommandClass(type)) {
            return null;
        }

        String simpleName = type.getSimpleName();
        List<CommandFieldDefinition> fields = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            fields.add(CommandFieldDefinition.builder()
                    .name(field.getName())
                    .label(toLabel(field.getName()))
                    .type(field.getType().getSimpleName())
                    .format(formatForType(field.getType()))
                    .description("Command field for " + field.getName())
                    .example("")
                    .required(true)
                    .expressionHint("string expression, e.g. #all_planets() or p[1:2:3]")
                    .build());
        }

        return CommandDefinition.builder()
                .name(simpleName)
                .label(toLabel(simpleName))
                .className(type.getName())
                .description(simpleName + " command metadata")
                .fields(fields)
                .build();
    }

    private String toLabel(String value) {
        String cleaned = value.endsWith("Command") ? value.substring(0, value.length() - "Command".length()) : value;
        String[] parts = cleaned.split("(?=\\p{Upper})");
        StringBuilder label = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (label.length() > 0) {
                label.append(' ');
            }
            label.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
        }
        return label.length() == 0 ? cleaned : label.toString();
    }

    private String formatForType(Class<?> type) {
        if (type == String.class) return "string";
        if (Number.class.isAssignableFrom(type) || type == int.class || type == long.class || type == double.class) {
            return "number";
        }
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (Enum.class.isAssignableFrom(type)) return "enum";
        return "expression";
    }
}
