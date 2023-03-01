package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.*;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.types.ColonyType;

import java.time.LocalDateTime;
import java.util.List;

public class FarmDAO extends AbstractDAO<FarmEntity> {

    private static FarmDAO INSTANCE;

    private FarmDAO() {
        super();
    }

    public static FarmDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new FarmDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<FarmEntity> getEntityClass() {
        return FarmEntity.class;
    }

    public FarmEntity getActualState() {
        List<FarmEntity> farmEntities = getSorted();
        if(farmEntities.size() > 0) {
            return farmEntities.get(0);
        } else {
            return null;

        }
    }

    public List<FarmEntity> getSorted() {
        synchronized (JPAUtility.dbSynchro) {
            return entityManager.createQuery("" +
                    "from FarmEntity order by start desc", FarmEntity.class)
                    .getResultList();
        }
    }

    public void createNewWave(Mission mission, List<TargetEntity> farmTargets, Long code) {
        synchronized (JPAUtility.dbSynchro) {
            entityManager.getTransaction().begin();
            for(TargetEntity farm : farmTargets) {
                FleetEntity fleet;
                if(Mission.SPY.equals(mission)) {
                    fleet = FleetEntity.createSpyFleet(farm);
                } else if(Mission.ATTACK.equals(mission)) {
                    fleet = FleetEntity.createFarmFleet(farm);
//                    Planet planetEntity = new Planet(fleet.getCoordinate());
//                    System.err.println(planetEntity+" lt "+fleet.transporterSmall);
                } else throw new RuntimeException("Unknown mission on farm wave");

                fleet.spaceTarget = ColonyType.PLANET;
                fleet.code = code;
                entityManager.persist(fleet);
                entityManager.flush();
                entityManager.clear();
            }

            entityManager.getTransaction().commit();
        }
    }
}
