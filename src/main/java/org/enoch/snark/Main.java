package org.enoch.snark;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.config.Universe;
import org.flywaydb.core.Flyway;

import java.io.File;
import java.io.FilenameFilter;

import static org.enoch.snark.db.entity.JPAUtility.H2_PERSISTENCE;
import static org.enoch.snark.db.entity.JPAUtility.H2_URL;
import static org.enoch.snark.instance.config.Universe.DATABASE;
import static org.enoch.snark.instance.config.Universe.configPath;

public class Main {

    public static void main(String[] args) {
        setConfigPath(args);
        Instance instance = Instance.getInstance();
        String config = Instance.config.getConfig(DATABASE);
        if(config == null || config.isEmpty() || config.equals("off")) {
            JPAUtility.buildDefaultEntityManager(H2_PERSISTENCE);
            migrateH2Database();
        }
        new Thread(instance::run).start();
    }

    public static void setConfigPath(String[] args) {
        if(args.length > 0) {
            Universe.setConfigPath(args[0]);
        } else {
            File defaultProperties = new File(configPath);
            if(!defaultProperties.exists()) {
                File[] files = defaultProperties.getParentFile().listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".properties");
                    }
                });

                if(files!= null && files.length > 0) {
                    Universe.setConfigPath(files[0].getName());
                } else {
                    throw new RuntimeException("Missing "+configPath+" and "+
                            defaultProperties.getParentFile().getAbsolutePath()+" do not contains file *.properties ");
                }
            }
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
