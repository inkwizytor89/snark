package org.enoch.snark;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.instance.Instance;
import org.flywaydb.core.Flyway;

import static org.enoch.snark.db.entity.JPAUtility.H2_PERSISTENCE;
import static org.enoch.snark.db.entity.JPAUtility.H2_URL;
import static org.enoch.snark.model.Universe.DATABASE;

public class Main {

    public static void main(String[] args) {
        Instance.setServerProperties(loadServerProperties(args));
        Instance instance = Instance.getInstance();
        String config = Instance.universe.getConfig(DATABASE);
        if(config == null || config.isEmpty() || config.equals("off")) {
            JPAUtility.setPersistence(H2_PERSISTENCE);
            migrateH2Database();
        } else {
            JPAUtility.setPersistence(Instance.universe.name);
            migratePostgresDatabase();
        }
        new Thread(instance::run).start();
    }

    public static String loadServerProperties(String[] args) {
        String serverConfigPath = "server.properties";
        if(args.length > 0) {
            serverConfigPath = args[0];
        }
         return serverConfigPath;
    }

    private static void migrateH2Database() {
        Flyway flyway = Flyway.configure().dataSource(H2_URL, "sa", null).load();
        flyway.migrate();
    }

    private static void migratePostgresDatabase() {
        Flyway flyway = Flyway.configure().dataSource("jdbc:postgresql://localhost/"+Instance.universe.name, "postgres", "").load();
        flyway.migrate();
    }
}
