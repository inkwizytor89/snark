package org.enoch.snark.module.collector;

import org.enoch.snark.db.entity.CollectionEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThred;

import java.time.LocalDateTime;
import java.util.Optional;
//todo nieskonczone
public class CollectorThred extends AbstractThred {

    private final Instance instance;

    public CollectorThred(SI si) {
        super(si);
        instance = si.getInstance();
    }

    @Override
    protected int getPauseInSeconds() {
        return 10; //pauza wynika z zawartosci bazy danych
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStep() {

        // z bazy danych znajdz obecna planetę
        Optional<CollectionEntity> optionalCollection = instance.daoFactory.collectionDAO.fetchAll().stream()
                .filter(collectionEntity -> collectionEntity.universe.name.equals(instance.universeEntity.name))
                .findAny();

        if(!optionalCollection.isPresent()) {
            throw new RuntimeException("Missing entry in table Collections for universe "+instance.universeEntity.name);
        }
        CollectionEntity collectionEntity = optionalCollection.get();
        if(LocalDateTime.now().isBefore(collectionEntity.start)) { // jeszcze flota nie przyszła
            return;
        }

//        instance.daoFactory.fleetDAO.saveOrUpdate();

        //todo powinien leciec przez ksiezyce

        // za pomocą algorytmu wyznacz nastepny source
        // wyslij transportery z zawartoscia na nastepny source
    }
}
