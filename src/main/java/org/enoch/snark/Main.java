package org.enoch.snark;

import org.enoch.snark.instance.Instance;
import org.flywaydb.core.Flyway;

import static org.enoch.snark.db.entity.JPAUtility.H2_URL;

public class Main {

    public static void main(String[] args) {
        migrateDatabase();
        Instance.setServerProperties(setServerProperties(args));
        new Thread(Instance.getInstance()::run).start();
    }

    public static String setServerProperties(String[] args) {
        String serverConfigPath = "server.properties";
        if(args.length > 0) {
            serverConfigPath = args[0];
        }
         return serverConfigPath;
    }

    private static void migrateDatabase() {
        Flyway flyway = Flyway.configure().dataSource(H2_URL, "sa", null).load();
        flyway.migrate();
    }
}
