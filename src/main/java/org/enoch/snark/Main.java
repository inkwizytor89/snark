package org.enoch.snark;

import org.enoch.snark.db.dao.impl.UniverseDAOImpl;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.instance.Instance;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<UniverseEntity> universes = new UniverseDAOImpl().fetchAllUniverses();
        for(UniverseEntity universeEntity : universes) {
                new Thread(new Instance(universeEntity)::runSI).start();
        }
    }
}
