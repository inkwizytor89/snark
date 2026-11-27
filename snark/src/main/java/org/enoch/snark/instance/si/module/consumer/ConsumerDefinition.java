package org.enoch.snark.instance.si.module.consumer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.instance.service.CoordinateExpressionService;
import org.enoch.snark.config.ThreadConfig;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class ConsumerDefinition implements ThreadConfig {

    public static final String threadType = "consumer";
    public static final String SERVER = "server";
    public static final String URL = "url";
    public static final String HASH = "hash";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String RESTART_DURATION = "restart_duration";
    public static final String MODE = "mode";
    public static final String CONFIG = "config";
    public static final String WEBDRIVER_PATH = "driver";
    public static final String SOURCE = "source";

    private String source;
    private String url;
    private String hash;
    private String server;
    private String login;
    private String password;
    private String webdriverPath;
    private String restartDuration;

    public ConsumerDefinition() {
        for (ConfigDefinition config : threadDefinition().getConfigs())
            switch (config.getKey()) {
                case SOURCE -> source = config.getValue();
                case URL -> url = config.getValue();
                case HASH -> hash = config.getValue();
                case SERVER -> server = config.getValue();
                case LOGIN -> login = config.getValue();
                case PASSWORD -> password = config.getValue();
                case WEBDRIVER_PATH -> webdriverPath = config.getValue();
                case RESTART_DURATION -> restartDuration = config.getValue();
                default -> log.warn("Config '{}' was not found in ConsumerDefinition", config.getKey());
            }
    }

    public void update(List<ConfigEntity> configs) {
        for (ConfigEntity config : configs) {
            switch (config.key) {
                case SOURCE -> source = config.value;
                case URL -> url = config.value;
                case HASH -> hash = config.value;
                case SERVER -> server = config.value;
                case LOGIN -> login = config.value;
                case PASSWORD -> password = config.value;
                case WEBDRIVER_PATH -> webdriverPath = config.value;
                case RESTART_DURATION -> restartDuration = config.value;
                default -> {
                }
            }
        }
    }

    public static ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition(SOURCE, CoordinateExpressionService.NONE, false,
                "Colony coordinate expression used to open fleet dispatch pages when the consumer starts."));
        configs.add(new ConfigDefinition(URL, null, false,
                "Game URL used to start the browser session."));
        configs.add(new ConfigDefinition(HASH, null, false,
                "Hash identifier for the target server session."));
        configs.add(new ConfigDefinition(SERVER, null, false,
                "Server code or name used when opening the game session."));
        configs.add(new ConfigDefinition(LOGIN, "missing_value", false,
                "Login name used for the player session."));
        configs.add(new ConfigDefinition(PASSWORD, "missing_value", false,
                "Password used for the player session."));
        configs.add(new ConfigDefinition(WEBDRIVER_PATH, "C:\\global\\selenium\\chromedriver.exe", false,
                "Custom path to the ChromeDriver used by the browser session."));
        configs.add(new ConfigDefinition(RESTART_DURATION, "300S", false,
                "How long the session stays alive before a restart is triggered."));
        return new ThreadDefinition(threadType, threadType, "Runs the browser-backed game consumer that processes queued commands and keeps the game session alive.", configs);
    }

    @Override
    public ThreadDefinition threadConfiguration(String thread, String module) {
        ThreadDefinition definition = threadDefinition();
        List<ConfigDefinition> configs = new ArrayList<>();
        for (ConfigDefinition config : definition.getConfigs()) {
            String value = switch (config.getKey()) {
                case SOURCE -> source;
                case URL -> url;
                case HASH -> hash;
                case SERVER -> server;
                case LOGIN -> login;
                case PASSWORD -> password;
                case WEBDRIVER_PATH -> webdriverPath;
                case RESTART_DURATION -> restartDuration;
                default -> config.getValue();
            };
            configs.add(new ConfigDefinition(
                    config.getKey(),
                    value,
                    config.getType(),
                    config.isTemporary(),
                    config.getDescription()));
        }
        ThreadDefinition configured = new ThreadDefinition(thread, definition.getType(), definition.getDescription(), configs);
        configured.setModule(module);
        return configured;
    }
}
