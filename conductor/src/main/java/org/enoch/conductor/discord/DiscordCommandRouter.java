package org.enoch.conductor.discord;

import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.process.ProcessService;
import org.enoch.common.status.NavigatorStatus;
import org.enoch.common.status.ServerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class DiscordCommandRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordCommandRouter.class);
    private static final DateTimeFormatter STATUS_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ProfileRepository profileRepository;
    private final ProcessService processService;
    private final DiscordCommandContext context = new DiscordCommandContext();

    public DiscordCommandRouter(ProfileRepository profileRepository, ProcessService processService) {
        this.profileRepository = profileRepository;
        this.processService = processService;
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
        String normalized = command.toLowerCase(Locale.ROOT);
        LOGGER.info("discord.bot.command.dispatched command={}", command);

        if (normalized.equals("help")) {
            return DiscordCommandResult.info("help",
                    "Available commands: help, ping, context, set <id>, list, start, stop, restart, status");
        }

        if (normalized.equals("ping")) {
            return DiscordCommandResult.info("ping", "pong");
        }

        if (normalized.equals("context")) {
            return describeContext();
        }

        if (normalized.startsWith("set active instance ")) {
            return DiscordCommandResult.failure("set",
                    "Use the shortened set command.",
                    "Use 'set <id>' to choose the active instance.");
        }

        if (normalized.startsWith("set ")) {
            return setActiveInstance(command.substring("set".length()).trim());
        }

        if (normalized.equals("list")) {
            return listInstances();
        }

        if (normalized.equals("start")) {
            return startInstance();
        }

        if (normalized.equals("stop")) {
            return stopInstance();
        }

        if (normalized.equals("restart")) {
            return restartInstance();
        }

        if (normalized.equals("status")) {
            return statusInstance();
        }

        if (normalized.startsWith("start ") || normalized.startsWith("stop ")
                || normalized.startsWith("restart ") || normalized.startsWith("status ")) {
            return DiscordCommandResult.failure("discord-command",
                    "Instance IDs are not accepted by this command.",
                    "Set the active instance with 'set <id>', then use start, stop, restart, or status.");
        }

        if (normalized.startsWith("set active") || normalized.startsWith("assign") || normalized.startsWith("clear")
                || normalized.startsWith("create instance")) {
            LOGGER.warn("discord.bot.command.failed command={} reason=inactive_or_not_supported", command);
            return DiscordCommandResult.inactive(command,
                    "This command is not available in the current conductor Discord bridge. Use help to view supported commands.");
        }

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
}
