package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.BaseGameInfoGIR;
import org.enoch.snark.instance.model.types.ColonyType;

import javax.transaction.Transactional;
import java.util.*;

public class LoadColoniesCommand extends AbstractCommand {

    private final ColonyDAO colonyDAO;
    private final BaseGameInfoGIR baseGameInfoGIR;

    public LoadColoniesCommand() {
        super();
        colonyDAO = ColonyDAO.getInstance();
        baseGameInfoGIR = new BaseGameInfoGIR();
    }

    @Override
    @Transactional
    public boolean execute() {
        System.out.println("LoadColoniesCommand.execute");
        try {
            Map<ColonyEntity, Boolean> stillExistMap = new HashMap<>();
            colonyDAO.fetchAll().forEach(colony -> stillExistMap.put(colony, false));

            List<ColonyEntity> loadColonyList = baseGameInfoGIR.loadPlanetList();

            loadColonyList.stream().filter(loadedColony -> loadedColony.isPlanet)
                    .forEach(loadedColony -> {
                        ColonyEntity colonyEntity = colonyDAO.find(loadedColony.cp);

                        if(colonyEntity == null) {
                            // new planet
                            loadedColony.type = ColonyType.PLANET;
                            colonyDAO.saveOrUpdate(loadedColony);
                        } else {
                            // old planet
                            stillExistMap.put(colonyEntity, true);
                        }
                    });

            loadColonyList.stream().filter(loadedColony -> !loadedColony.isPlanet)
                    .forEach(loadedColony -> {
                        ColonyEntity colonyEntity = colonyDAO.find(loadedColony.cp);

                        if(colonyEntity == null) {
                            // new moon
                            loadedColony.type = ColonyType.MOON;
                            colonyDAO.saveOrUpdate(loadedColony);
                            //update planet cpm
                            ColonyEntity planet = colonyDAO.find(loadedColony.cpm);
                            planet.cpm = loadedColony.cp;
                            colonyDAO.saveOrUpdate(planet);
                        } else {
                            // old moon
                            stillExistMap.put(colonyEntity, true);
                        }
                    });

            //remove missing colony
            stillExistMap.forEach((colonyEntity, stillExist) -> {
                if(!stillExist) {
                    System.err.println("Colony remove because do not exist " + colonyEntity);
                    FleetDAO.getInstance().clean(colonyEntity);
                    colonyDAO.remove(colonyEntity);
                }
            });

            for(ColonyEntity colony : colonyDAO.fetchAll()) {
                if(colony.level == null) {
                    baseGameInfoGIR.updateColony(colony);
                    colony.level = 1L;
                    colony.formsLevel = 1L;
                }
                colonyDAO.saveOrUpdate(colony);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public String toString() {
        return LoadColoniesCommand.class.getName();
    }
}
