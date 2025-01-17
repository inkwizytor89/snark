package org.enoch.snark.gi.command.impl;

import jakarta.transaction.Transactional;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.BaseGameInfoGIR;
import org.enoch.snark.instance.model.action.DiffLists;
import static org.enoch.snark.gi.types.UrlComponent.DEFENSES;
import static org.enoch.snark.gi.types.UrlComponent.FACILITIES;
import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.gi.types.UrlComponent.LFBUILDINGS;
import static org.enoch.snark.gi.types.UrlComponent.SUPPLIES;
import static org.enoch.snark.instance.model.types.ColonyType.PLANET;

public class LoadColoniesProcessor {

    private BaseGameInfoGIR baseGameInfoGIR = new BaseGameInfoGIR();

    @Transactional
    public boolean execute(LoadColoniesCommand command) {
        try {
            DiffLists<ColonyEntity> diff = new DiffLists<>(ColonyDAO.getInstance().fetchAll(), baseGameInfoGIR.loadPlanetList());

            diff.added().forEach(colony -> {
                System.out.println(LoadColoniesProcessor.class.getSimpleName()+" add " + colony);
                colony.level = colony.formsLevel = 1L;
                ColonyEntity cpmEntity = ColonyDAO.getInstance().find(colony.cpm);
                if(cpmEntity != null) cpmEntity.cpm = colony.cp;
                ColonyDAO.getInstance().saveOrUpdate(colony);
                updateColony(colony);
            });

            diff.removed().forEach(colony -> {
                System.out.println(LoadColoniesProcessor.class.getSimpleName()+" remove " + colony);
                FleetDAO.getInstance().clean(colony);
                ColonyDAO.getInstance().remove(colony);
            });
        } catch (Throwable e) {
            System.err.println(this+" with error "+e.getMessage());
            return false;
        }
        return true;
    }

    public void updateColony(ColonyEntity colony) {
        new OpenPageCommand(SUPPLIES, colony).sourceHash(this.getClass().getSimpleName()).push();
        new OpenPageCommand(FACILITIES, colony).sourceHash(this.getClass().getSimpleName()).push();
        if(colony.is(PLANET)) new OpenPageCommand(LFBUILDINGS, colony).sourceHash(this.getClass().getSimpleName()).push();
        new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName()).push();
        new OpenPageCommand(DEFENSES, colony).sourceHash(this.getClass().getSimpleName()).push();
    }

    @Override
    public String toString() {
        return LoadColoniesProcessor.class.getSimpleName();
    }
}
