package org.enoch.snark;

import org.enoch.snark.db.dao.impl.UniverseDAOImpl;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.instance.Instance;

public class Main {

    public static void main(String[] args) {
        for(UniverseEntity universeEntity : new UniverseDAOImpl().fetchAllUniverses()) {
            if(universeEntity.getName().equals("Fenrir")){
				Runnable task = new Instance(universeEntity)::runSI;
                new Thread(task).start();
            }
        }
    }
}
