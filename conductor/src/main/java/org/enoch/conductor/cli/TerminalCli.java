package org.enoch.conductor.cli;

import org.enoch.conductor.command.CommandMessage;
import org.enoch.conductor.instance.InstanceProfile;
import org.enoch.conductor.instance.InstanceRuntime;
import org.enoch.conductor.instance.ProfileRepository;
import org.enoch.conductor.process.ProcessService;
import org.enoch.conductor.ws.WorkerSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

@Component
public class TerminalCli implements CommandLineRunner {

    private final ProfileRepository repository;
    private final ProcessService processService;
    private final WorkerSocketHandler socketHandler;

    private final ObjectMapper mapper = new ObjectMapper();

    public TerminalCli(
            ProfileRepository repository,
            ProcessService processService,
            WorkerSocketHandler socketHandler
    ) {
        this.repository = repository;
        this.processService = processService;
        this.socketHandler = socketHandler;
    }

    @Override
    public void run(String... args) {

        new Thread(this::terminalLoop).start();
    }

    public String executeCommand(String line) {
        if (line == null || line.isBlank()) {
            return "";
        }

        String[] split = line.trim().split("\\s+");

        try {
            return switch (split[0]) {
                case "create" -> createCommand(split);
                case "start" -> startCommand(split);
                case "stop" -> stopCommand(split);
                case "restart" -> restartCommand(split);
                case "status" -> statusCommand();
                case "send" -> sendCommand(split);
                case "broadcast" -> broadcastCommand(split);
                case "delete" -> deleteCommand(split);
                case "help" -> helpCommand();
                default -> "Unknown command: " + split[0] + "\nType 'help' for available commands.";
            };
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void terminalLoop() {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            try {

                System.out.print("manager> ");

                String line = scanner.nextLine();
                if (line == null || line.isBlank()) {
                    continue;
                }

                String output = executeCommand(line);
                if (!output.isBlank()) {
                    System.out.println(output);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private String createCommand(String[] split) throws Exception {
        if (split.length < 4) {
            throw new IllegalArgumentException("Usage: create <login> <password> <server>");
        }

        String login = split[1];
        String password = split[2];
        String server = split[3];

        InstanceProfile profile = new InstanceProfile();
        profile.setId(login + "#" + server);
        profile.setAutoStart(true);

        repository.save(profile);
        return "Created instance: " + profile.getId();
    }

    private String startCommand(String[] split) throws Exception {
        if (split.length < 2) {
            throw new IllegalArgumentException("Usage: start <id>");
        }

        String id = split[1];

        for (InstanceProfile profile : repository.loadAll()) {
            if (profile.getId().equals(id)) {
                processService.start(profile);
                return "Started: " + id;
            }
        }

        return "Instance not found: " + id;
    }

    private String stopCommand(String[] split) {
        if (split.length < 2) {
            throw new IllegalArgumentException("Usage: stop <id>");
        }

        processService.stop(split[1]);
        return "Stopped: " + split[1];
    }

    private String restartCommand(String[] split) throws Exception {
        if (split.length < 2) {
            throw new IllegalArgumentException("Usage: restart <id>");
        }

        String id = split[1];

        for (InstanceProfile profile : repository.loadAll()) {
            if (profile.getId().equals(id)) {
                processService.restart(profile);
                return "Restarted: " + id;
            }
        }

        return "Instance not found: " + id;
    }

    private String statusCommand() throws Exception {
        List<InstanceProfile> profiles = repository.loadAll();
        Map<String, InstanceRuntime> runtimes = processService.getAllRuntimes();

        if (profiles.isEmpty()) {
            return "No instances found.";
        }

        StringBuilder output = new StringBuilder();
        output.append(System.lineSeparator())
                .append("ID\t\t\t\t\t\t\t\t\t\tRUNNING\t\tAUTOSTART\tPID")
                .append(System.lineSeparator())
                .append("â”€".repeat(70))
                .append(System.lineSeparator());

        for (InstanceProfile profile : profiles) {
            boolean isRunning = processService.isRunning(profile.getId());
            InstanceRuntime runtime = runtimes.get(profile.getId());
            long pid = runtime != null ? runtime.getPid() : -1;

            String id = profile.getId();
            String running = isRunning ? "yes" : "no";
            String autoStart = profile.isAutoStart() ? "yes" : "no";
            String pidStr = pid == -1 ? "-" : String.valueOf(pid);

            output.append(String.format("%-24s\t%-15s\t%-15s\t\t%s%n", id, running, autoStart, pidStr));
        }

        return output.toString().trim();
    }

    private String sendCommand(String[] split) throws Exception {
        if (split.length < 3) {
            throw new IllegalArgumentException("Usage: send <id> <command>");
        }

        String workerId = split[1];
        String command = split[2];

        CommandMessage msg = new CommandMessage();
        msg.setId(UUID.randomUUID().toString());
        msg.setType("request:" + command);
        msg.setFrom("conductor");
        msg.setTo(workerId);
        msg.setTimestamp(System.currentTimeMillis());
        msg.setStatus(100);

        socketHandler.send(workerId, mapper.writeValueAsString(msg));
        return "Sent command to " + workerId + ": " + command;
    }

    private String broadcastCommand(String[] split) throws Exception {
        if (split.length < 2) {
            throw new IllegalArgumentException("Usage: broadcast <command>");
        }

        String command = split[1];

        CommandMessage msg = new CommandMessage();
        msg.setId(UUID.randomUUID().toString());
        msg.setType("request:" + command);
        msg.setFrom("conductor");
        msg.setTo("broadcast");
        msg.setTimestamp(System.currentTimeMillis());
        msg.setStatus(100);

        socketHandler.broadcast(mapper.writeValueAsString(msg));
        return "Broadcasted command: " + command;
    }

    private String deleteCommand(String[] split) {
        if (split.length < 2) {
            throw new IllegalArgumentException("Usage: delete <id>");
        }

        return "TODO delete profile + data for " + split[1];
    }

    private String helpCommand() {
        return """
                Usage: conductor <command> [options]

                Commands:
                  create <login> <server>     Create a new instance profile
                  start <id>                  Start an instance
                  stop <id>                   Stop a running instance
                  restart <id>                Restart an instance (stop and start)
                  status                      Show status of all instances
                  send <id> <command>         Send a command to a specific worker
                  broadcast <command>         Send a command to all workers
                  delete <id>                 Delete an instance profile and its data
                  help                        Display this help message

                Examples:
                  conductor create user@example.com gmail
                  conductor start user@example.com#gmail
                  conductor status
                  conductor send user@example.com#gmail restart
                """;
    }

    private void create(String[] split) throws Exception {

        String login = split[1];
        String password = split[2];
        String server = split[3];

        InstanceProfile profile = new InstanceProfile();

        profile.setId(login+ "#" + server);
        profile.setAutoStart(true);

        repository.save(profile);

        System.out.println("Created instance: " + profile.getId());
    }

    private void start(String[] split) throws Exception {

        String id = split[1];

        List<InstanceProfile> profiles = repository.loadAll();

        for (InstanceProfile profile : profiles) {

            if (profile.getId().equals(id)) {
                processService.start(profile);
                System.out.println("Started: " + id);
            }
        }
    }

    private void stop(String[] split) {

        processService.stop(split[1]);

        System.out.println("Stopped: " + split[1]);
    }

    private void restart(String[] split) throws Exception {

        String id = split[1];

        for (InstanceProfile profile : repository.loadAll()) {

            if (profile.getId().equals(id)) {
                processService.restart(profile);
            }
        }
    }

    private void list() throws Exception {

        for (InstanceProfile profile : repository.loadAll()) {
            System.out.println( profile.getId());
        }
    }

    private void status() throws Exception {
        List<InstanceProfile> profiles = repository.loadAll();
        Map<String, InstanceRuntime> runtimes = processService.getAllRuntimes();

        if (profiles.isEmpty()) {
            System.out.println("No instances found.");
            return;
        }

        System.out.println("\nID\t\t\t\t\t\t\t\tRUNNING\t\tAUTOSTART\tPID");
        System.out.println("â”€".repeat(70));

        for (InstanceProfile profile : profiles) {
            boolean isRunning = processService.isRunning(profile.getId());
            InstanceRuntime runtime = runtimes.get(profile.getId());
            long pid = runtime != null ? runtime.getPid() : -1;

            String id = profile.getId();
            String running = isRunning ? "yes" : "no";
            String autoStart = profile.isAutoStart() ? "yes" : "no";
            String pidStr = pid == -1 ? "-" : String.valueOf(pid);

            System.out.printf("%-24s\t%-15s\t%-15s\t\t%s%n", id, running, autoStart, pidStr);
        }

        System.out.println();
    }

    private void send(String[] split) throws Exception {

        String workerId = split[1];
        String command = split[2];

        CommandMessage msg = new CommandMessage();
        msg.setId(UUID.randomUUID().toString());
        msg.setType("request:" + command);
        msg.setFrom("conductor");
        msg.setTo(workerId);
        msg.setTimestamp(System.currentTimeMillis());
        msg.setStatus(100);

        socketHandler.send(workerId, mapper.writeValueAsString(msg));
    }

    private void broadcast(String[] split) throws Exception {

        String command = split[1];

        CommandMessage msg = new CommandMessage();
        msg.setId(UUID.randomUUID().toString());
        msg.setType("request:" + command);
        msg.setFrom("conductor");
        msg.setTo("broadcast");
        msg.setTimestamp(System.currentTimeMillis());
        msg.setStatus(100);

        socketHandler.broadcast(mapper.writeValueAsString(msg));
    }

    private void delete(String[] split) {

        System.out.println("TODO delete profile + data");
    }

    private void help() {

        System.out.println("""
                Usage: conductor <command> [options]

                Commands:
                  create <login> <server>     Create a new instance profile
                  start <id>                  Start an instance
                  stop <id>                   Stop a running instance
                  restart <id>                Restart an instance (stop and start)
                  status                      Show status of all instances
                  send <id> <command>         Send a command to a specific worker
                  broadcast <command>         Send a command to all workers
                  delete <id>                 Delete an instance profile and its data
                  help                        Display this help message

                Examples:
                  conductor create user@example.com gmail
                  conductor start user@example.com#gmail
                  conductor status
                  conductor send user@example.com#gmail restart
                """);
    }
}



