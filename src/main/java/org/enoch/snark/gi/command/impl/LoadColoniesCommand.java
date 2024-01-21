package org.enoch.snark.gi.command.impl;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.gi.BaseGameInfoGIR;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.types.ColonyType;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_RESEARCH;
import static org.enoch.snark.instance.config.Config.FLY_POINTS;
import static org.enoch.snark.instance.config.Config.MAIN;

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
            typeFlyPoints();

                        // update colonies
            instance.cachedPlaned = new ArrayList<>();
            for(ColonyEntity colony : colonyDAO.fetchAll()) {
                if(colony.isPlanet) {
                    instance.cachedPlaned.add(colony.toPlanet());
                }
                if(colony.level == null) {
                    baseGameInfoGIR.updateColony(colony);
                    colony.level = 1L;
                    colony.formsLevel = 1L;
                }
                colonyDAO.saveOrUpdate(colony);

                PlayerEntity mainPlayer = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer());
                if(mainPlayer.spyLevel == null) {
                    new GIUrlBuilder().openWithPlayerInfo(PAGE_RESEARCH, mainPlayer);
                    mainPlayer.spyLevel = 1L;
                }
            }
        } catch (Exception e) {
            typeFlyPoints();
            e.printStackTrace();
        }
        return true;
    }

    private synchronized void typeFlyPoints() {
        List<ColonyEntity> flyPoints = new ArrayList<>();
        String flyPointsConfig = Instance.getMainConfigMap().getConfig(FLY_POINTS, StringUtils.EMPTY);
        List<ColonyEntity> planetList = colonyDAO.fetchAll()
                .stream()
                .filter(colonyEntity -> colonyEntity.isPlanet)
                .sorted(Comparator.comparing(o -> -o.galaxy))
                .collect(Collectors.toList());

        if(flyPointsConfig.isEmpty()) {
            for (ColonyEntity planet : planetList) {
                ColonyEntity colony = planet;
                if (planet.cpm != null) {
                    colony = colonyDAO.find(planet.cpm);
                }
                flyPoints.add(colony);
            }
        } else if (flyPointsConfig.contains("moon")) {
            flyPoints = colonyDAO.fetchAll()
                    .stream()
                    .filter(colonyEntity -> !colonyEntity.isPlanet)
                    .sorted(Comparator.comparing(o -> -o.galaxy))
                    .collect(Collectors.toList());
        } else {
            flyPoints.addAll(planetList);
        }

//        System.err.println("\nCount of fly points: "+flyPoints.size());
//        flyPoints.forEach(System.err::println);
        Instance.getInstance().flyPoints = flyPoints;
    }

    @Override
    public String toString() {
        return LoadColoniesCommand.class.getName();
    }
}
