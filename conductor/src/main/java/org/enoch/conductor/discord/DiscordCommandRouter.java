package org.enoch.conductor.discord;

import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.conductor.config.InstanceThreadConfigurationService;
import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.process.ProcessService;
import org.enoch.common.status.NavigatorStatus;
import org.enoch.common.status.ServerStatus;
import org.enoch.conductor.config.InstanceConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class DiscordCommandRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordCommandRouter.class);
    private static final DateTimeFormatter STATUS_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_THREAD_STATUS_LENGTH = 1800;
    private static final Pattern SET_CONFIG_COMMAND =
            Pattern.compile("^set\\s+config(?=\\s|$)\\s*(.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern CONFIG_ENTRY =
            Pattern.compile("^([^\\s.=]+)\\.([^\\s.=]+)\\.([^\\s.=]+)\\s*=\\s*(.*)$", Pattern.DOTALL);

    private final ProfileRepository profileRepository;
    private final ProcessService processService;
    private final InstanceThreadConfigurationService threadConfigurationService;
    private final InstanceConfigService instanceConfigService;
    private final DiscordCommandContext context = new DiscordCommandContext();
    private final CommandNode commands = createCommandTree();

    @Autowired
    public DiscordCommandRouter(ProfileRepository profileRepository,
                                ProcessService processService,
                                InstanceThreadConfigurationService threadConfigurationService,
                                InstanceConfigService instanceConfigService) {
        this.profileRepository = profileRepository;
        this.processService = processService;
        this.threadConfigurationService = threadConfigurationService;
        this.instanceConfigService = instanceConfigService;
    }

    public DiscordCommandRouter(ProfileRepository profileRepository,
                                ProcessService processService,
                                InstanceThreadConfigurationService threadConfigurationService) {
        this(profileRepository, processService, threadConfigurationService, null);
    }

    public DiscordCommandRouter(ProfileRepository profileRepository, ProcessService processService) {
        this(profileRepository, processService, null, null);
    }

    private CommandNode createCommandTree() {
        CommandNode root = new CommandNode(
                "Available commands: help, ping, context, set <id>, set config <module.thread.key=value>, list, start, stop, restart, status, status thread, status deque");
        root.add("ping", "Replies with pong.",
                runWithoutArguments(() -> DiscordCommandResult.info("ping", "pong")));
        root.add("context", "Shows the currently selected instance.",
                runWithoutArguments(this::describeContext));
        root.add("set", "Selects the active instance by its ID.", this::setInstanceFromArguments);
        root.add("list", "Lists all configured instances with their state and Discord channel.",
                runWithoutArguments(this::listInstances));
        root.add("start", "Starts the selected instance.", runWithoutArguments(this::startInstance));
        root.add("stop", "Stops the selected instance.", runWithoutArguments(this::stopInstance));
        root.add("restart", "Restarts the selected instance.", runWithoutArguments(this::restartInstance));

        CommandNode status = root.add("status", "Shows the selected instance status.", runWithoutArguments(this::statusInstance));
        status.add("thread", "Shows active thread configurations of the selected instance.",
                runWithoutArguments(this::statusThreads));
        status.add("deque", "Shows the selected instance command deque.",
                runWithoutArguments(this::statusDeque));
        root.add("assign", "This command is not available.", this::unsupportedCommand);
        root.add("clear", "This command is not available.", this::unsupportedCommand);
        root.add("create", "This command is not available.", arguments -> {
            if (!arguments.isEmpty() && arguments.get(0).equalsIgnoreCase("instance")) {
                return DiscordCommandResult.inactive("create instance",
                        "This command is not available in the current conductor Discord bridge.");
            }
            return unknownAction("create " + String.join(" ", arguments));
        });
        return root;
    }

    private DiscordCommandResult setInstanceFromArguments(List<String> arguments) {
        String instanceId = String.join(" ", arguments);
        if (instanceId.toLowerCase(Locale.ROOT).startsWith("active instance ")) {
            return DiscordCommandResult.failure("set",
                    "Use the shortened set command.",
                    "Use 'set <id>' to choose the active instance.");
        }
        return setActiveInstance(instanceId);
    }

    private DiscordCommandResult unsupportedCommand(List<String> arguments) {
        return DiscordCommandResult.inactive(String.join(" ", arguments),
                "This command is not available in the current conductor Discord bridge.");
    }

    private DiscordCommandResult instanceArgumentNotAllowed() {
        return DiscordCommandResult.failure("discord-command",
                "Instance IDs are not accepted by this command.",
                "Select the instance with 'set <id>' first.");
    }

    private Function<List<String>, DiscordCommandResult> runWithoutArguments(
            Supplier<DiscordCommandResult> action) {
        return arguments -> {
            if (!arguments.isEmpty()) {
                return instanceArgumentNotAllowed();
            }
            return action.get();
        };
    }

    public String resolveDefaultActiveInstanceId() {
        try {
            Optional<String> defaultInstance = profileRepository.loadAll().stream()
                    .filter(profile -> profile.getDiscordChannelId() != null && !profile.getDiscordChannelId().isBlank())
                    .map(InstanceProfile::getId)
                    .findFirst();
            String resolved = defaultInstance.orElse(null);
            if (resolved != null) {
                context.setActiveInstanceId(resolved);
            }
            return resolved;
        } catch (Exception e) {
            LOGGER.warn("Failed to resolve default Discord active instance", e);
            return null;
        }
    }

    public DiscordCommandResult handle(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            LOGGER.info("discord.bot.command.failed reason=empty_message");
            return DiscordCommandResult.failure("discord-command", "No command was received.", "Message payload is empty.");
        }

        String command = rawMessage.trim();
        LOGGER.info("discord.bot.command.dispatched command={}", redactConfigValue(command));
        return dispatch(command);
    }

    private DiscordCommandResult dispatch(String command) {
        Matcher setConfigMatcher = SET_CONFIG_COMMAND.matcher(command);
        if (setConfigMatcher.matches()) {
            return setConfigEntry(setConfigMatcher.group(1));
        }

        List<String> tokens = new ArrayList<>(List.of(command.split("\\s+")));
        CommandNode current = commands;
        if (tokens.get(0).equalsIgnoreCase("help")) {
            return DiscordCommandResult.info("help", commands.help);
        }

        for (int index = 0; index < tokens.size(); index++) {
            String token = tokens.get(index);
            if (token.equalsIgnoreCase("help")) {
                return DiscordCommandResult.info("help", current.help);
            }

            CommandNode child = current.children.get(token.toLowerCase(Locale.ROOT));
            if (child != null) {
                current = child;
                continue;
            }

            if (!current.children.isEmpty()) {
                return unknownAction(command);
            }
            if (current.action != null) {
                if (tokens.get(tokens.size() - 1).equalsIgnoreCase("help")) {
                    return DiscordCommandResult.info("help", current.help);
                }
                return current.action.apply(tokens.subList(index, tokens.size()));
            }
            return unknownAction(command);
        }

        if (current.action != null) {
            return current.action.apply(List.of());
        }
        return unknownAction(command);
    }

    private DiscordCommandResult setConfigEntry(String entry) {
        if (entry.equalsIgnoreCase("help")) {
            return DiscordCommandResult.info("set config",
                    "Sets one active instance configuration. Usage: set config <module.thread.key=value>");
        }

        Matcher matcher = CONFIG_ENTRY.matcher(entry);
        if (!matcher.matches()) {
            return DiscordCommandResult.failure("set config",
                    "Invalid configuration entry.",
                    "Use 'set config <module.thread.key=value>'.");
        }

        String instanceId = getActiveInstanceId();
        if (instanceId == null) {
            return noActiveInstance("set config");
        }

        try {
            if (profileRepository.findById(instanceId).isEmpty()) {
                LOGGER.warn("discord.bot.command.failed command=set config instanceId={} reason=active_instance_not_found",
                        instanceId);
                return DiscordCommandResult.failure("set config", "Active instance does not exist.", instanceId);
            }
            if (instanceConfigService == null) {
                return DiscordCommandResult.failure("set config",
                        "Unable to update instance configuration.", null);
            }

            ConfigDefinition config = new ConfigDefinition(
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3),
                    matcher.group(4),
                    false,
                    null);
            instanceConfigService.updateConfigEntry(instanceId, config);
            LOGGER.info("discord.bot.command.dispatched command=set config instanceId={} entry={}.{}.{} result=accepted",
                    instanceId, config.getModule(), config.getThread(), config.getKey());
            return DiscordCommandResult.success("set config",
                    "Configuration updated: " + config.getModule() + "." + config.getThread() + "." + config.getKey());
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=set config instanceId={} reason={}",
                    instanceId, e.getClass().getSimpleName());
            return DiscordCommandResult.failure("set config", "Configuration update failed.", e.getMessage());
        }
    }

    public static String redactConfigValue(String command) {
        Matcher matcher = SET_CONFIG_COMMAND.matcher(command == null ? "" : command);
        if (!matcher.matches()) {
            return command;
        }
        String entry = matcher.group(1);
        int equalsIndex = entry.indexOf('=');
        return equalsIndex < 0
                ? "set config [redacted]"
                : "set config " + entry.substring(0, equalsIndex + 1) + "[redacted]";
    }

    private DiscordCommandResult unknownAction(String command) {
        LOGGER.warn("discord.bot.command.failed command={} reason=not_recognized", command);
        return DiscordCommandResult.failure("discord-command",
                "Unknown action: " + command + ". Type help to see the available commands.",
                null);
    }

    public DiscordCommandResult describeContext() {
        String activeInstanceId = getActiveInstanceId();
        if (activeInstanceId == null || activeInstanceId.isBlank()) {
            return DiscordCommandResult.info("context", "No active instance is set. Use 'set <id>' to choose one.");
        }

        try {
            InstanceProfile profile = profileRepository.findById(activeInstanceId).orElse(null);
            if (profile == null) {
                return DiscordCommandResult.failure("context", "Active instance no longer exists.", activeInstanceId);
            }

            String state = processService.getState(activeInstanceId);
            return DiscordCommandResult.success("context",
                    "Active instance: " + activeInstanceId + "; state: " + state + "; discordChannelId: " +
                            (profile.getDiscordChannelId() == null || profile.getDiscordChannelId().isBlank() ? "not configured" : profile.getDiscordChannelId()));
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=context instanceId={} reason={}", activeInstanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("context", "Unable to read active instance context.", e.getMessage());
        }
    }

    public DiscordCommandResult setActiveInstance(String instanceId) {
        if (instanceId == null || instanceId.isBlank()) {
            return DiscordCommandResult.failure("set", "Missing instance id.", "Use set <id>");
        }

        try {
            InstanceProfile profile = profileRepository.findById(instanceId).orElse(null);
            if (profile == null) {
                LOGGER.warn("discord.bot.command.failed command=set instanceId={} reason=instance_not_found", instanceId);
                return DiscordCommandResult.failure("set", "Instance not found.", instanceId);
            }
            context.setActiveInstanceId(instanceId);
            LOGGER.info("discord.bot.context.changed instanceId={}", instanceId);
            return DiscordCommandResult.success("set", "Active instance set to " + instanceId);
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=set instanceId={} reason={}", instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("set", "Unable to set active instance.", e.getMessage());
        }
    }

    public DiscordCommandResult listInstances() {
        try {
            StringBuilder builder = new StringBuilder();
            var profiles = profileRepository.loadAll();
            if (profiles.isEmpty()) {
                return DiscordCommandResult.info("list", "No instances are configured.");
            }
            for (InstanceProfile profile : profiles) {
                boolean active = profile.getId().equals(context.getActiveInstanceId());
                builder.append(profile.getId())
                        .append(" | state=")
                        .append(processService.getState(profile.getId()))
                        .append(" | discord=")
                        .append(profile.getDiscordChannelId() == null || profile.getDiscordChannelId().isBlank() ? "none" : profile.getDiscordChannelId())
                        .append(active ? " | active" : "")
                        .append(System.lineSeparator());
            }
            return DiscordCommandResult.info("list", builder.toString().trim());
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=list reason={}", e.getMessage(), e);
            return DiscordCommandResult.failure("list", "Unable to list instances.", e.getMessage());
        }
    }

    public DiscordCommandResult startInstance() {
        String instanceId = getActiveInstanceId();
        if (instanceId == null) {
            return noActiveInstance("start");
        }

        try {
            InstanceProfile profile = profileRepository.findById(instanceId).orElse(null);
            if (profile == null) {
                LOGGER.warn("discord.bot.command.failed command=start instanceId={} reason=active_instance_not_found", instanceId);
                return DiscordCommandResult.failure("start", "Active instance does not exist.", instanceId);
            }
            processService.start(profile);
            LOGGER.info("discord.bot.command.dispatched command=start instanceId={} result=accepted", instanceId);
            return DiscordCommandResult.success("start", "Start request accepted for instance " + instanceId);
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=start instanceId={} reason={}", instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("start", "Start action failed.", e.getMessage());
        }
    }

    public DiscordCommandResult stopInstance() {
        String instanceId = getActiveInstanceId();
        if (instanceId == null) {
            return noActiveInstance("stop");
        }

        try {
            InstanceProfile profile = profileRepository.findById(instanceId).orElse(null);
            if (profile == null) {
                LOGGER.warn("discord.bot.command.failed command=stop instanceId={} reason=active_instance_not_found", instanceId);
                return DiscordCommandResult.failure("stop", "Active instance does not exist.", instanceId);
            }
            processService.stop(instanceId);
            LOGGER.info("discord.bot.command.dispatched command=stop instanceId={} result=accepted", instanceId);
            return DiscordCommandResult.success("stop", "Stop request accepted for instance " + instanceId);
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=stop instanceId={} reason={}", instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("stop", "Stop action failed.", e.getMessage());
        }
    }

    public DiscordCommandResult restartInstance() {
        String instanceId = getActiveInstanceId();
        if (instanceId == null) {
            return noActiveInstance("restart");
        }

        try {
            InstanceProfile profile = profileRepository.findById(instanceId).orElse(null);
            if (profile == null) {
                LOGGER.warn("discord.bot.command.failed command=restart instanceId={} reason=active_instance_not_found", instanceId);
                return DiscordCommandResult.failure("restart", "Active instance does not exist.", instanceId);
            }
            processService.restart(profile);
            LOGGER.info("discord.bot.command.dispatched command=restart instanceId={} result=accepted", instanceId);
            return DiscordCommandResult.success("restart", "Restart request accepted for instance " + instanceId);
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=restart instanceId={} reason={}", instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("restart", "Restart action failed.", e.getMessage());
        }
    }

    public DiscordCommandResult statusInstance() {
        String instanceId = getActiveInstanceId();
        if (instanceId == null) {
            return noActiveInstance("status");
        }

        try {
            InstanceProfile profile = profileRepository.findById(instanceId).orElse(null);
            if (profile == null) {
                LOGGER.warn("discord.bot.command.failed command=status instanceId={} reason=active_instance_not_found", instanceId);
                return DiscordCommandResult.failure("status", "Active instance does not exist.", instanceId);
            }
            String state = processService.getState(instanceId);
            if ("running".equals(state)) {
                ServerStatus serverStatus = processService.getLastServerStatus(instanceId);
                NavigatorStatus navigatorStatus = serverStatus == null ? null : serverStatus.getNavigatorStatus();
                String statusText = formatNavigatorStatus(instanceId, navigatorStatus);
                String updateTime = navigatorStatus == null || navigatorStatus.getLastUpdate() == null
                        ? ""
                        : " as of " + navigatorStatus.getLastUpdate().format(STATUS_TIME_FORMAT);
                return DiscordCommandResult.success("status",
                        instanceId + " is running. State " + statusText + updateTime + ".");
            }
            return DiscordCommandResult.success("status",
                    "Instance " + instanceId + " is in state " + state + ". Discord channel: " +
                            (profile.getDiscordChannelId() == null || profile.getDiscordChannelId().isBlank() ? "none" : profile.getDiscordChannelId()));
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=status instanceId={} reason={}", instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("status", "Status request failed.", e.getMessage());
        }
    }

    private DiscordCommandResult statusThreads() {
        String instanceId = getActiveInstanceId();
        if (instanceId == null) {
            return noActiveInstance("status thread");
        }

        try {
            if (profileRepository.findById(instanceId).isEmpty()) {
                LOGGER.warn("discord.bot.command.failed command=status thread instanceId={} reason=active_instance_not_found", instanceId);
                return DiscordCommandResult.failure("status thread", "Active instance does not exist.", instanceId);
            }
            if (threadConfigurationService == null) {
                return DiscordCommandResult.failure("status thread",
                        "Unable to request active thread configurations.", null);
            }
            List<ThreadDefinition> definitions = threadConfigurationService.fetchManagedDefinitions(instanceId);
            return DiscordCommandResult.success("status thread", formatThreadConfigurations(definitions));
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=status thread instanceId={} reason={}",
                    instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("status thread",
                    "Unable to read active thread configurations.", e.getMessage());
        }
    }

    private String formatThreadConfigurations(List<ThreadDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return "No active threads.";
        }

        StringBuilder result = new StringBuilder();
        for (ThreadDefinition definition : definitions) {
            List<ConfigDefinition> configs = definition.getConfigs();
            String module = definition.getModule();
            if (module == null || module.isBlank()) {
                module = "unknown";
                if (!configs.isEmpty()) {
                    module = configs.get(0).getModule();
                }
            }
            StringBuilder line = new StringBuilder(module+"."+definition.getThread());
            List<String> configValues = configs.stream()
                    .map(this::formatConfigValue)
                    .toList();
            if (!configValues.isEmpty()) {
                line.append(": ").append(String.join(", ", configValues));
            }
            if (result.length() + line.length() + System.lineSeparator().length() > MAX_THREAD_STATUS_LENGTH) {
                result.append(System.lineSeparator()).append("...");
                break;
            }
            if (!result.isEmpty()) {
                result.append(System.lineSeparator());
            }
            result.append(line);
        }
        return result.toString();
    }

    private String formatConfigValue(ConfigDefinition config) {
        String key = config.getKey();
        if (isSensitiveConfigKey(key)) {
            return "******";
        }
        String value = String.valueOf(config.getValue()).replaceAll("[\\r\\n]+", " ");
        if (value.length() > 80) {
            value = value.substring(0, 77) + "...";
        }
        return key + "=" + value;
    }

    private boolean isSensitiveConfigKey(String key) {
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        return normalizedKey.contains("password")
                || normalizedKey.contains("login")
                || normalizedKey.contains("hash")
                || normalizedKey.contains("token")
                || normalizedKey.contains("url")
                || normalizedKey.contains("secret")
                || normalizedKey.contains("credential");
    }

    private DiscordCommandResult statusDeque() {
        String instanceId = getActiveInstanceId();
        if (instanceId == null) {
            return noActiveInstance("status deque");
        }

        try {
            if (profileRepository.findById(instanceId).isEmpty()) {
                LOGGER.warn("discord.bot.command.failed command=status deque instanceId={} reason=active_instance_not_found", instanceId);
                return DiscordCommandResult.failure("status deque", "Active instance does not exist.", instanceId);
            }
            ServerStatus serverStatus = processService.getLastServerStatus(instanceId);
            String commandDeque = serverStatus == null ? null : serverStatus.getCommandDeque();
            return DiscordCommandResult.success("status deque",
                    commandDeque == null || commandDeque.isBlank() ? "-" : commandDeque);
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=status deque instanceId={} reason={}", instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("status deque", "Unable to read the command deque.", e.getMessage());
        }
    }

    private String formatNavigatorStatus(String instanceId, NavigatorStatus navigatorStatus) {
        if (navigatorStatus == null) {
            String statusMessage = processService.getLastStatusMessage(instanceId);
            return statusMessage == null || statusMessage.isBlank()
                    ? "Awaiting the next status update..."
                    : statusMessage;
        }

        StringBuilder text = new StringBuilder()
                .append(valueOrZero(navigatorStatus.getFleetCount()))
                .append(" / ").append(valueOrZero(navigatorStatus.getFleetMax()))
                .append(' ')
                .append(valueOrZero(navigatorStatus.getExpeditionCount()))
                .append(" / ").append(valueOrZero(navigatorStatus.getExpeditionMax()));
        Map<String, Long> missions = navigatorStatus.getMissions();
        if (missions != null) {
            missions.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        if (!entry.getKey().isEmpty()) {
                            text.append(' ')
                                    .append(entry.getKey().charAt(0))
                                    .append(':')
                                    .append(entry.getValue());
                        }
                    });
        }
        return text.toString();
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String getActiveInstanceId() {
        String activeInstanceId = context.getActiveInstanceId();
        if (activeInstanceId == null || activeInstanceId.isBlank()) {
            activeInstanceId = resolveDefaultActiveInstanceId();
        }
        return activeInstanceId;
    }

    private DiscordCommandResult noActiveInstance(String command) {
        return DiscordCommandResult.failure(command, "No active instance selected.",
                "Use 'set <id>' to choose the context instance.");
    }

    private static final class CommandNode {
        private final String help;
        private final Map<String, CommandNode> children = new HashMap<>();
        private final Function<List<String>, DiscordCommandResult> action;

        private CommandNode(String help) {
            this(help, null);
        }

        private CommandNode(String help, Function<List<String>, DiscordCommandResult> action) {
            this.help = help;
            this.action = action;
        }

        private CommandNode add(String name, String help, Function<List<String>, DiscordCommandResult> action) {
            CommandNode child = new CommandNode(help, action);
            children.put(name, child);
            return child;
        }
    }
}
