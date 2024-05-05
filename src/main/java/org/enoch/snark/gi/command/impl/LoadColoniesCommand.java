package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.BaseGameInfoGIR;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.action.DiffLists;
import org.enoch.snark.instance.model.to.Planet;

import javax.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.gi.types.UrlComponent.*;
import static org.enoch.snark.instance.model.types.ColonyType.PLANET;
import static org.enoch.snark.instance.si.module.ConfigMap.GALAXY_MAX;
import static org.enoch.snark.instance.si.module.ConfigMap.TRIP;

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
//        colonyDAO.fetchAll().forEach(colonyEntity -> System.err.println(colonyEntity+" -> "+nextStop(colonyEntity)));
        try {
            DiffLists<ColonyEntity> diff = new DiffLists<>(colonyDAO.fetchAll(), baseGameInfoGIR.loadPlanetList());

            diff.added().forEach(colony -> {
                System.out.println(LoadColoniesCommand.class.getSimpleName()+" add " + colony);
                colony.level = colony.formsLevel = 1L;
                ColonyEntity cpmEntity = colonyDAO.find(colony.cpm);
                if(cpmEntity != null) cpmEntity.cpm = colony.cp;
                colonyDAO.saveOrUpdate(colony);
                updateColony(colony);
            });

            diff.removed().forEach(colony -> {
                System.out.println(LoadColoniesCommand.class.getSimpleName()+" remove " + colony);
                FleetDAO.getInstance().clean(colony);
                colonyDAO.remove(colony);
            });
        } catch (Throwable e) {
            System.err.println(this+" with error "+e.getMessage());
            return false;
        }
        return true;
    }

    public void updateColony(ColonyEntity colony) {
        new OpenPageCommand(SUPPLIES, colony).push();
        new OpenPageCommand(FACILITIES, colony).push();
        if(colony.is(PLANET)) new OpenPageCommand(LFBUILDINGS, colony).push();
        new OpenPageCommand(FLEETDISPATCH, colony).push();
        new OpenPageCommand(DEFENSES, colony).push();
    }

//    public List<ColonyEntity> uniTrip() {
//        List<ColonyEntity> configTrip = Instance.getMainConfigMap().getColonies(TRIP, null);
//        if(configTrip != null) return configTrip;
//        List<ColonyEntity> moons = ColonyDAO.getInstance().getColonies("moon");
//        Instance.getMainConfigMap().getConfigInteger(GALAXY_MAX, 6);
//
//        return configTrip;
//    }
//

    @Override
    public String toString() {
        return LoadColoniesCommand.class.getSimpleName();
    }
}
