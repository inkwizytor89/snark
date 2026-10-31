package org.enoch.conductor.settings;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class ConductorSettings {

    public static final Path SETTINGS_PATH = Path.of("settings");

    private static final String DEFAULT_BROWSER_AUTO_OPEN = "true";
    private static final String DEFAULT_INSTANCES_DIR = "instances";
    private static final String DEFAULT_START_PROPERTIES_DIR = "start-properties";
    private static final String DEFAULT_TEMPLATES_DIR = "templates";
    private static final String DEFAULT_WORKER_JAR = "";
    private static final String DEFAULT_INSTANCE_MONITOR_INTERVAL_MS = "5000";
    private static final String DEFAULT_WORKER_DISCONNECT_TIMEOUT_MS = "30000";
    private static final String DEFAULT_SERVER_PORT = "8080";
    private static final String DEFAULT_DISCORD_BOT_ENABLED = "false";
    private static final String DEFAULT_DISCORD_BOT_TOKEN = "";

    private boolean browserAutoOpen = Boolean.parseBoolean(DEFAULT_BROWSER_AUTO_OPEN);
    private String instancesDir = DEFAULT_INSTANCES_DIR;
    private String startPropertiesDir = DEFAULT_START_PROPERTIES_DIR;
    private String templatesDir = DEFAULT_TEMPLATES_DIR;
    private String workerJar = DEFAULT_WORKER_JAR;
    private long instanceMonitorIntervalMs = Long.parseLong(DEFAULT_INSTANCE_MONITOR_INTERVAL_MS);
    private long workerDisconnectTimeoutMs = Long.parseLong(DEFAULT_WORKER_DISCONNECT_TIMEOUT_MS);
    private int serverPort = Integer.parseInt(DEFAULT_SERVER_PORT);
    private boolean discordBotEnabled = Boolean.parseBoolean(DEFAULT_DISCORD_BOT_ENABLED);
    private String discordBotToken = DEFAULT_DISCORD_BOT_TOKEN;

    public ConductorSettings() {
        loadFromFile();
        applySystemProperties();
    }

    @PostConstruct
    public void initialize() {
        loadFromFile();
        applySystemProperties();
    }

    public static Map<String, Object> resolveSpringDefaults(String[] args) {
        ensureFileExists();
        Properties properties = loadProperties();
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("conductor.instances-dir", properties.getProperty("conductor.instances-dir", DEFAULT_INSTANCES_DIR));
        defaults.put("conductor.start-properties-dir", properties.getProperty("conductor.start-properties-dir", DEFAULT_START_PROPERTIES_DIR));
        defaults.put("conductor.templates-dir", properties.getProperty("conductor.templates-dir", DEFAULT_TEMPLATES_DIR));
        defaults.put("conductor.worker", properties.getProperty("conductor.worker", DEFAULT_WORKER_JAR));
        defaults.put("conductor.instances-monitor-interval", properties.getProperty("conductor.instances-monitor-interval", DEFAULT_INSTANCE_MONITOR_INTERVAL_MS));
        defaults.put("conductor.worker-disconnect-timeout", properties.getProperty("conductor.worker-disconnect-timeout", DEFAULT_WORKER_DISCONNECT_TIMEOUT_MS));
        defaults.put("server.port", properties.getProperty("server.port", DEFAULT_SERVER_PORT));
        defaults.put("browser.auto-open", properties.getProperty("browser.auto-open", DEFAULT_BROWSER_AUTO_OPEN));
        defaults.put("discord.bot.enabled", properties.getProperty("discord.bot.enabled", DEFAULT_DISCORD_BOT_ENABLED));
        defaults.put("discord.bot.token", properties.getProperty("discord.bot.token", DEFAULT_DISCORD_BOT_TOKEN));

        for (String arg : args) {
            if (arg.startsWith("--instances-dir=")) {
                defaults.put("conductor.instances-dir", arg.substring("--instances-dir=".length()));
            } else if (arg.startsWith("--start-properties-dir=")) {
                defaults.put("conductor.start-properties-dir", arg.substring("--start-properties-dir=".length()));
            } else if (arg.startsWith("--worker=")) {
                defaults.put("conductor.worker", arg.substring("--worker=".length()));
            }
        }

        return defaults;
    }

    public void loadFromFile() {
        ensureFileExists();
        Properties properties = loadProperties();
        this.browserAutoOpen = Boolean.parseBoolean(properties.getProperty("browser.auto-open", DEFAULT_BROWSER_AUTO_OPEN));
        this.instancesDir = normalize(properties.getProperty("conductor.instances-dir", DEFAULT_INSTANCES_DIR), DEFAULT_INSTANCES_DIR);
        this.startPropertiesDir = normalize(properties.getProperty("conductor.start-properties-dir", DEFAULT_START_PROPERTIES_DIR), DEFAULT_START_PROPERTIES_DIR);
        this.templatesDir = normalize(properties.getProperty("conductor.templates-dir", DEFAULT_TEMPLATES_DIR), DEFAULT_TEMPLATES_DIR);
        this.workerJar = properties.getProperty("conductor.worker", DEFAULT_WORKER_JAR);
        this.instanceMonitorIntervalMs = parseLong(properties.getProperty("conductor.instances-monitor-interval", DEFAULT_INSTANCE_MONITOR_INTERVAL_MS), 5000L);
        this.workerDisconnectTimeoutMs = parseLong(properties.getProperty("conductor.worker-disconnect-timeout", DEFAULT_WORKER_DISCONNECT_TIMEOUT_MS), 30000L);
        this.serverPort = parseInt(properties.getProperty("server.port", DEFAULT_SERVER_PORT), 8080);
        this.discordBotEnabled = Boolean.parseBoolean(properties.getProperty("discord.bot.enabled", DEFAULT_DISCORD_BOT_ENABLED));
        this.discordBotToken = properties.getProperty("discord.bot.token", DEFAULT_DISCORD_BOT_TOKEN);
        applySystemProperties();
    }

    public void apply(ConductorSettings other) throws IOException {
        this.browserAutoOpen = other.browserAutoOpen;
        this.instancesDir = normalize(other.instancesDir, DEFAULT_INSTANCES_DIR);
        this.startPropertiesDir = normalize(other.startPropertiesDir, DEFAULT_START_PROPERTIES_DIR);
        this.templatesDir = normalize(other.templatesDir, DEFAULT_TEMPLATES_DIR);
        this.workerJar = other.workerJar == null ? DEFAULT_WORKER_JAR : other.workerJar.trim();
        this.instanceMonitorIntervalMs = other.instanceMonitorIntervalMs > 0 ? other.instanceMonitorIntervalMs : 5000L;
        this.workerDisconnectTimeoutMs = other.workerDisconnectTimeoutMs > 0 ? other.workerDisconnectTimeoutMs : 30000L;
        this.serverPort = other.serverPort > 0 ? other.serverPort : 8080;
        this.discordBotEnabled = other.discordBotEnabled;
        this.discordBotToken = other.discordBotToken == null ? DEFAULT_DISCORD_BOT_TOKEN : other.discordBotToken.trim();

        Properties properties = new Properties();
        properties.setProperty("browser.auto-open", Boolean.toString(this.browserAutoOpen));
        properties.setProperty("conductor.instances-dir", this.instancesDir);
        properties.setProperty("conductor.start-properties-dir", this.startPropertiesDir);
        properties.setProperty("conductor.templates-dir", this.templatesDir);
        properties.setProperty("conductor.worker", this.workerJar);
        properties.setProperty("conductor.instances-monitor-interval", Long.toString(this.instanceMonitorIntervalMs));
        properties.setProperty("conductor.worker-disconnect-timeout", Long.toString(this.workerDisconnectTimeoutMs));
        properties.setProperty("server.port", Integer.toString(this.serverPort));
        properties.setProperty("discord.bot.enabled", Boolean.toString(this.discordBotEnabled));
        properties.setProperty("discord.bot.token", this.discordBotToken);
        writeProperties(properties);
        applySystemProperties();
    }

    public ConductorSettings copy() {
        ConductorSettings copy = new ConductorSettings();
        copy.browserAutoOpen = this.browserAutoOpen;
        copy.instancesDir = this.instancesDir;
        copy.startPropertiesDir = this.startPropertiesDir;
        copy.templatesDir = this.templatesDir;
        copy.workerJar = this.workerJar;
        copy.instanceMonitorIntervalMs = this.instanceMonitorIntervalMs;
        copy.workerDisconnectTimeoutMs = this.workerDisconnectTimeoutMs;
        copy.serverPort = this.serverPort;
        copy.discordBotEnabled = this.discordBotEnabled;
        copy.discordBotToken = this.discordBotToken;
        return copy;
    }

    public boolean isBrowserAutoOpen() {
        return browserAutoOpen;
    }

    public void setBrowserAutoOpen(boolean browserAutoOpen) {
        this.browserAutoOpen = browserAutoOpen;
    }

    public String getInstancesDir() {
        return instancesDir;
    }

    public void setInstancesDir(String instancesDir) {
        this.instancesDir = normalize(instancesDir, DEFAULT_INSTANCES_DIR);
    }

    public String getStartPropertiesDir() {
        return startPropertiesDir;
    }

    public void setStartPropertiesDir(String startPropertiesDir) {
        this.startPropertiesDir = normalize(startPropertiesDir, DEFAULT_START_PROPERTIES_DIR);
    }

    public String getTemplatesDir() {
        return templatesDir;
    }

    public void setTemplatesDir(String templatesDir) {
        this.templatesDir = normalize(templatesDir, DEFAULT_TEMPLATES_DIR);
    }

    public String getWorkerJar() {
        return workerJar == null ? DEFAULT_WORKER_JAR : workerJar;
    }

    public void setWorkerJar(String workerJar) {
        this.workerJar = workerJar == null ? DEFAULT_WORKER_JAR : workerJar.trim();
    }

    public long getInstanceMonitorIntervalMs() {
        return instanceMonitorIntervalMs;
    }

    public void setInstanceMonitorIntervalMs(long instanceMonitorIntervalMs) {
        this.instanceMonitorIntervalMs = instanceMonitorIntervalMs > 0 ? instanceMonitorIntervalMs : 5000L;
    }

    public long getWorkerDisconnectTimeoutMs() {
        return workerDisconnectTimeoutMs;
    }

    public void setWorkerDisconnectTimeoutMs(long workerDisconnectTimeoutMs) {
        this.workerDisconnectTimeoutMs = workerDisconnectTimeoutMs > 0 ? workerDisconnectTimeoutMs : 30000L;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort > 0 ? serverPort : 8080;
    }

    public boolean isDiscordBotEnabled() {
        return discordBotEnabled;
    }

    public void setDiscordBotEnabled(boolean discordBotEnabled) {
        this.discordBotEnabled = discordBotEnabled;
    }

    public String getDiscordBotToken() {
        return discordBotToken == null ? DEFAULT_DISCORD_BOT_TOKEN : discordBotToken;
    }

    public void setDiscordBotToken(String discordBotToken) {
        this.discordBotToken = discordBotToken == null ? DEFAULT_DISCORD_BOT_TOKEN : discordBotToken.trim();
    }

    public static void ensureFileExists() {
        try {
            if (Files.notExists(SETTINGS_PATH)) {
                Files.writeString(SETTINGS_PATH, buildDefaultPropertiesContent(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize conductor settings file", e);
        }
    }

    public static Properties loadProperties() {
        ensureFileExists();
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(SETTINGS_PATH)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read conductor settings file", e);
        }
        return properties;
    }

    private static void writeProperties(Properties properties) throws IOException {
        ensureFileExists();
        try (var writer = Files.newBufferedWriter(SETTINGS_PATH, StandardCharsets.UTF_8)) {
            properties.store(writer, "Conductor global settings");
        }
    }

    private static String buildDefaultPropertiesContent() {
        StringBuilder content = new StringBuilder();
        content.append("browser.auto-open=true\n");
        content.append("conductor.instances-dir=instances\n");
        content.append("conductor.start-properties-dir=start-properties\n");
        content.append("conductor.templates-dir=templates\n");
        content.append("conductor.worker=\n");
        content.append("conductor.instances-monitor-interval=5000\n");
        content.append("conductor.worker-disconnect-timeout=30000\n");
        content.append("server.port=8080\n");
        content.append("discord.bot.enabled=false\n");
        content.append("discord.bot.token=\n");
        return content.toString();
    }

    private static String normalize(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private static long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void applySystemProperties() {
        System.setProperty("browser.auto-open", Boolean.toString(this.browserAutoOpen));
        System.setProperty("conductor.instances-dir", this.instancesDir);
        System.setProperty("conductor.start-properties-dir", this.startPropertiesDir);
        System.setProperty("conductor.templates-dir", this.templatesDir);
        System.setProperty("conductor.worker", this.workerJar == null ? "" : this.workerJar);
        System.setProperty("conductor.instances-monitor-interval", Long.toString(this.instanceMonitorIntervalMs));
        System.setProperty("conductor.worker-disconnect-timeout", Long.toString(this.workerDisconnectTimeoutMs));
        System.setProperty("server.port", Integer.toString(this.serverPort));
        System.setProperty("discord.bot.enabled", Boolean.toString(this.discordBotEnabled));
        System.setProperty("discord.bot.token", this.discordBotToken == null ? DEFAULT_DISCORD_BOT_TOKEN : this.discordBotToken);
    }
}
