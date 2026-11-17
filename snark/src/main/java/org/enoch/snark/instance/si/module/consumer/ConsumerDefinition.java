package org.enoch.snark.instance.si.module.consumer;

import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;

import java.util.ArrayList;
import java.util.List;

public class ConsumerDefinition {

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


    public static final ConfigDefinition URL_CONFIG = new ConfigDefinition("consumer", threadType, URL, "", false,
            "Game URL used to start the browser session.");
    public static final ConfigDefinition HASH_CONFIG = new ConfigDefinition("consumer", threadType, HASH, "", false,
            "Hash identifier for the target server session.");
    public static final ConfigDefinition SERVER_CONFIG = new ConfigDefinition("consumer", threadType, SERVER, "", false,
            "Server code or name used when opening the game session.");
    public static final ConfigDefinition LOGIN_CONFIG = new ConfigDefinition("consumer", threadType, LOGIN, "", false,
            "Login name used for the player session.");
    public static final ConfigDefinition PASSWORD_CONFIG = new ConfigDefinition("consumer", threadType, PASSWORD, "", false,
            "Password used for the player session.");
    public static final ConfigDefinition WEBDRIVER_PATH_CONFIG =
            new ConfigDefinition("consumer", threadType, WEBDRIVER_PATH,
                    "C:\\global\\selenium\\chromedriver.exe", false,
                    "Custom path to the ChromeDriver used by the browser session.");
    public static final ConfigDefinition RESTART_DURATION_CONFIG =
            new ConfigDefinition("consumer", threadType, RESTART_DURATION, "300S", false,
                    "How long the session stays alive before a restart is triggered.");
    public static final List<ConfigDefinition> CONFIGS = List.of(
            URL_CONFIG,
            HASH_CONFIG,
            SERVER_CONFIG,
            LOGIN_CONFIG,
            PASSWORD_CONFIG,
            WEBDRIVER_PATH_CONFIG,
            RESTART_DURATION_CONFIG
    );

    public static ThreadDefinition threadDefinition() {
        return new ThreadDefinition(threadType,
                "Runs the browser-backed game consumer that processes queued commands and keeps the game session alive.",
                new ArrayList<>(CONFIGS));
    }
}
