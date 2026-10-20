package org.enoch.conductor.process;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class InstanceLogService {

    private static final DateTimeFormatter LOG_FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS");
    private static final Duration LOG_RETENTION = Duration.ofDays(14);

    public void prepareLogs(Path instanceDir) throws IOException {
        Path logsDir = instanceDir.resolve("logs");
        Files.createDirectories(logsDir);

        Instant cutoff = Instant.now().minus(LOG_RETENTION);
        try (Stream<Path> paths = Files.list(logsDir)) {
            for (Path path : paths
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".log"))
                    .toList()) {
                if (Files.getLastModifiedTime(path).toInstant().isBefore(cutoff)) {
                    Files.delete(path);
                }
            }
        }
    }

    public void capture(String instanceId, Process process, Path instanceDir) throws IOException {
        Path logsDir = instanceDir.resolve("logs");
        String fileName = "snark-" + LOG_FILE_TIMESTAMP.format(LocalDateTime.now())
                + "-" + UUID.randomUUID() + ".log";
        Path logFile = logsDir.resolve(fileName);
        OutputStream logOutput = Files.newOutputStream(logFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

        AtomicInteger activeStreams = new AtomicInteger(2);
        Object logLock = new Object();
        startOutputCaptureThread(instanceId, "stdout", process.getInputStream(), System.out,
                logOutput, logLock, activeStreams);
        startOutputCaptureThread(instanceId, "stderr", process.getErrorStream(), System.err,
                logOutput, logLock, activeStreams);
    }

    private void startOutputCaptureThread(String instanceId, String streamName, InputStream input,
                                          PrintStream console, OutputStream logOutput, Object logLock,
                                          AtomicInteger activeStreams) {
        Thread thread = new Thread(() -> {
            byte[] buffer = new byte[8192];
            boolean logWriteFailed = false;
            try (InputStream processOutput = input) {
                int bytesRead;
                while ((bytesRead = processOutput.read(buffer)) != -1) {
                    console.write(buffer, 0, bytesRead);
                    console.flush();
                    if (!logWriteFailed) {
                        try {
                            synchronized (logLock) {
                                logOutput.write(buffer, 0, bytesRead);
                            }
                        } catch (IOException e) {
                            logWriteFailed = true;
                            System.err.println("Failed to write logs for instance " + instanceId);
                            e.printStackTrace(System.err);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to read " + streamName + " for instance " + instanceId);
                e.printStackTrace(System.err);
            } finally {
                if (activeStreams.decrementAndGet() == 0) {
                    try {
                        synchronized (logLock) {
                            logOutput.close();
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to close log file for instance " + instanceId);
                        e.printStackTrace(System.err);
                    }
                }
            }
        }, "instance-" + instanceId + "-" + streamName);
        thread.setDaemon(true);
        thread.start();
    }
}
