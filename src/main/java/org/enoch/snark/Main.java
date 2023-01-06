package org.enoch.snark;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.config.Universe;
import org.flywaydb.core.Flyway;

import static org.enoch.snark.db.entity.JPAUtility.H2_PERSISTENCE;
import static org.enoch.snark.db.entity.JPAUtility.H2_URL;
import static org.enoch.snark.instance.config.Universe.DATABASE;

public class Main {

    public static void main(String[] args) {
        setConfigPathIfDefined(args);
        Instance instance = Instance.getInstance();
        String config = Instance.config.getConfig(DATABASE);
        if(config == null || config.isEmpty() || config.equals("off")) {
            JPAUtility.buildDefaultEntityManager(H2_PERSISTENCE);
            migrateH2Database();
        }
        new Thread(instance::run).start();
    }

    public static void setConfigPathIfDefined(String[] args) {
        if(args.length > 0) {
            Universe.setConfigPath(args[0]);
        }
    }

    private static void migrateH2Database() {
        Flyway flyway = Flyway.configure().dataSource(H2_URL, "sa", null).load();
        flyway.migrate();
    }

    private static void migratePostgresDatabase() {
        Flyway flyway = Flyway.configure().dataSource("jdbc:postgresql://localhost/"+Instance.config.name, "postgres", "").load();
        flyway.migrate();
    }
}
