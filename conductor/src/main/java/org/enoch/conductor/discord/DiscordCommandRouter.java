package org.enoch.conductor.discord;

import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class DiscordCommandRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordCommandRouter.class);

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

        if (normalized.contains("help")) {
            return DiscordCommandResult.info("help",
                    "Available commands: help, get context, show context, set active instance <id>, list instances, start <id>, stop <id>, status <id>");
        }

        if (normalized.startsWith("get context") || normalized.startsWith("show context")) {
            return describeContext();
        }

        if (normalized.startsWith("set active instance ")) {
            return setActiveInstance(extractArgument(command, "set active instance"));
        }

        if (normalized.startsWith("list instances")) {
            return listInstances();
        }

        if (normalized.startsWith("start ")) {
            return startInstance(extractArgument(command, "start"));
        }

        if (normalized.startsWith("stop ")) {
            return stopInstance(extractArgument(command, "stop"));
        }

        if (normalized.startsWith("status ")) {
            return statusInstance(extractArgument(command, "status"));
        }

        if (normalized.startsWith("set active") || normalized.startsWith("assign") || normalized.startsWith("clear")
                || normalized.startsWith("restart") || normalized.startsWith("create instance")) {
            LOGGER.warn("discord.bot.command.failed command={} reason=inactive_or_not_supported", command);
            return DiscordCommandResult.inactive(command,
                    "This command is not available in the current conductor Discord bridge. Use help to view supported commands.");
        }

        LOGGER.warn("discord.bot.command.failed command={} reason=not_recognized", command);
        return DiscordCommandResult.failure("discord-command",
                "Command not recognized.", "Use help to list supported commands.");
    }

    public DiscordCommandResult describeContext() {
        String activeInstanceId = context.getActiveInstanceId();
        if (activeInstanceId == null || activeInstanceId.isBlank()) {
            String defaultInstance = resolveDefaultActiveInstanceId();
            if (defaultInstance != null && !defaultInstance.isBlank()) {
                context.setActiveInstanceId(defaultInstance);
                activeInstanceId = defaultInstance;
            }
        }
        if (activeInstanceId == null || activeInstanceId.isBlank()) {
            return DiscordCommandResult.info("context", "No active instance is set. Use 'set active instance <instanceId>' to choose one.");
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
            return DiscordCommandResult.failure("set active instance", "Missing instance id.", "Use set active instance <instanceId>");
        }

        try {
            InstanceProfile profile = profileRepository.findById(instanceId).orElse(null);
            if (profile == null) {
                LOGGER.warn("discord.bot.command.failed command=set active instance instanceId={} reason=instance_not_found", instanceId);
                return DiscordCommandResult.failure("set active instance", "Instance not found.", instanceId);
            }
            context.setActiveInstanceId(instanceId);
            LOGGER.info("discord.bot.context.changed instanceId={}", instanceId);
            return DiscordCommandResult.success("set active instance", "Active instance set to " + instanceId);
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=set active instance instanceId={} reason={}", instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("set active instance", "Unable to set active instance.", e.getMessage());
        }
    }

    public DiscordCommandResult listInstances() {
        try {
            StringBuilder builder = new StringBuilder();
            var profiles = profileRepository.loadAll();
            if (profiles.isEmpty()) {
                return DiscordCommandResult.info("list instances", "No instances are configured.");
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
            return DiscordCommandResult.info("list instances", builder.toString().trim());
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=list instances reason={}", e.getMessage(), e);
            return DiscordCommandResult.failure("list instances", "Unable to list instances.", e.getMessage());
        }
    }

    public DiscordCommandResult startInstance(String instanceId) {
        if (instanceId == null || instanceId.isBlank()) {
            return DiscordCommandResult.failure("start", "Missing instance id.", "Use start <instanceId>");
        }

        try {
            InstanceProfile profile = profileRepository.findById(instanceId).orElse(null);
            if (profile == null) {
                LOGGER.warn("discord.bot.command.failed command=start instanceId={} reason=instance_not_found", instanceId);
                return DiscordCommandResult.failure("start", "Instance does not exist.", instanceId);
            }
            processService.start(profile);
            context.setActiveInstanceId(instanceId);
            LOGGER.info("discord.bot.command.dispatched command=start instanceId={} result=accepted", instanceId);
            return DiscordCommandResult.success("start", "Start request accepted for instance " + instanceId);
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=start instanceId={} reason={}", instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("start", "Start action failed.", e.getMessage());
        }
    }

    public DiscordCommandResult stopInstance(String instanceId) {
        if (instanceId == null || instanceId.isBlank()) {
            return DiscordCommandResult.failure("stop", "Missing instance id.", "Use stop <instanceId>");
        }

        try {
            InstanceProfile profile = profileRepository.findById(instanceId).orElse(null);
            if (profile == null) {
                LOGGER.warn("discord.bot.command.failed command=stop instanceId={} reason=instance_not_found", instanceId);
                return DiscordCommandResult.failure("stop", "Instance does not exist.", instanceId);
            }
            processService.stop(instanceId);
            LOGGER.info("discord.bot.command.dispatched command=stop instanceId={} result=accepted", instanceId);
            return DiscordCommandResult.success("stop", "Stop request accepted for instance " + instanceId);
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=stop instanceId={} reason={}", instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("stop", "Stop action failed.", e.getMessage());
        }
    }

    public DiscordCommandResult statusInstance(String instanceId) {
        if (instanceId == null || instanceId.isBlank()) {
            instanceId = context.getActiveInstanceId();
        }
        if (instanceId == null || instanceId.isBlank()) {
            return DiscordCommandResult.failure("status", "No active instance selected.", "Use set active instance <instanceId> or pass an instance id.");
        }

        try {
            InstanceProfile profile = profileRepository.findById(instanceId).orElse(null);
            if (profile == null) {
                LOGGER.warn("discord.bot.command.failed command=status instanceId={} reason=instance_not_found", instanceId);
                return DiscordCommandResult.failure("status", "Instance does not exist.", instanceId);
            }
            String state = processService.getState(instanceId);
            return DiscordCommandResult.success("status",
                    "Instance " + instanceId + " is in state " + state + ". Discord channel: " +
                            (profile.getDiscordChannelId() == null || profile.getDiscordChannelId().isBlank() ? "none" : profile.getDiscordChannelId()));
        } catch (Exception e) {
            LOGGER.warn("discord.bot.command.failed command=status instanceId={} reason={}", instanceId, e.getMessage(), e);
            return DiscordCommandResult.failure("status", "Status request failed.", e.getMessage());
        }
    }

    private String extractArgument(String rawMessage, String command) {
        String trimmed = rawMessage.trim();
        String prefix = command.trim();
        if (trimmed.regionMatches(true, 0, prefix, 0, prefix.length())) {
            String rest = trimmed.substring(prefix.length()).trim();
            if (rest.startsWith("instance")) {
                return rest.substring("instance".length()).trim();
            }
            return rest;
        }
        return null;
    }
}
