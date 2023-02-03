package org.enoch.snark.instance.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Universe {

    public static String configPath = "server.properties";

    public static void setConfigPath(String path) {
        configPath = path;
    }

    public synchronized static Config load() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(configPath);
        Properties properties = new java.util.Properties();
        properties.load(fileInputStream);
        fileInputStream.close();

        return new Config(properties);
    }
}
