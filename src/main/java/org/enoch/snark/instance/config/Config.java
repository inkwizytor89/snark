package org.enoch.snark.instance.config;

import java.util.Properties;

public class Config {

    public static final String SERVER = "server";
    public static final String URL = "url";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String MODE = "mode";
    public static final String CONFIG = "config";
    public static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";

    public String name;
    public String url;
    public String login;
    public String pass;
    public String mode;
    public String config;
    public String pathToChromeWebdriver;

    public Config(Properties properties) {

        url = properties.getProperty(URL);
        name = properties.getProperty(SERVER);
        login = properties.getProperty(LOGIN);
        pass = properties.getProperty(PASSWORD);

        mode = properties.getProperty(MODE);
        config = properties.getProperty(CONFIG);

        pathToChromeWebdriver = properties.getProperty(WEBDRIVER_CHROME_DRIVER);
        System.setProperty(WEBDRIVER_CHROME_DRIVER, pathToChromeWebdriver);

    }

    public String getConfig(String key) {
        String[] configs = this.config.split(",");
        for (String s : configs) {
            String[] configuration = s.split("=");
            if (key.equals(configuration[0])) {
                return configuration.length > 1 ? configuration[1] : "";
            }
        }
        return null;
    }
}