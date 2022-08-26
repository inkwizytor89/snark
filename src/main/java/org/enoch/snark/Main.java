package org.enoch.snark;

import org.enoch.snark.db.entity.JPAUtility;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.instance.AppProperties;
import org.enoch.snark.instance.Instance;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) throws IOException {
        String serverConfigPath = "server.properties";
        if(args.length > 0) {
            serverConfigPath = args[0];
        }
        UniverseEntity universeProperties = UniverseEntity.loadPrperties(new AppProperties(serverConfigPath));

        EntityManager onStartEntityManager = JPAUtility.getEntityManager();
        List<UniverseEntity> universes = onStartEntityManager
                .createQuery("from UniverseEntity", UniverseEntity.class).getResultList();

        String serverName = universeProperties.name;
        Optional<UniverseEntity> any = universes.stream()
                .filter(universeEntity -> universeEntity.name.equals(serverName))
                .findFirst();

        if(any.isPresent()) {
            universeProperties = any.get();
        } else {
            onStartEntityManager.getTransaction().begin();
            onStartEntityManager.persist(universeProperties);
            onStartEntityManager.getTransaction().commit();
            onStartEntityManager.close();
        }

        new Thread(new Instance(universeProperties.id)::runSI).start();
    }
}
