package org.enoch.snark;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.instance.Instance;

import java.io.IOException;

import static org.enoch.snark.Main.setServerProperties;
import static org.enoch.snark.db.entity.JPAUtility.POSTGRES_PERSISTENCE;

public class Test {

    public static void main(String[] args) throws IOException {
        Instance.setServerProperties(setServerProperties(args));
        JPAUtility.setPersistence(POSTGRES_PERSISTENCE);
        new Thread(Instance.getInstance()::run).start();
    }
}
