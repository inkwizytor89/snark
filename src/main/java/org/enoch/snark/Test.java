package org.enoch.snark;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.instance.Instance;
import org.flywaydb.core.Flyway;

import static org.enoch.snark.Main.setServerProperties;
import static org.enoch.snark.db.entity.JPAUtility.H2_PERSISTENCE;
import static org.enoch.snark.db.entity.JPAUtility.H2_URL;
import static org.enoch.snark.model.Universe.DATABASE;

public class Test {

    public static void main(String[] args) {
        Instance.setServerProperties(setServerProperties(args));
        Instance instance = Instance.getInstance();
        String config = Instance.universe.getConfig(DATABASE);
        if(config == null || config.isEmpty() || config.equals("off")) {
            JPAUtility.setPersistence(H2_PERSISTENCE);
        } else {
            JPAUtility.setPersistence(Instance.universe.name);
            migrateDatabase();
        }
        new Thread(instance::run).start();
    }

    private static void migrateDatabase() {
        Flyway flyway = Flyway.configure().dataSource("jdbc:postgresql://localhost/"+Instance.universe.name, "postgres", "").load();
        flyway.migrate();
    }
}
