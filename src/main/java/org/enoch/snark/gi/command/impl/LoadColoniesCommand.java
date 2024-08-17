package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.BaseGameInfoGIR;
import org.enoch.snark.gi.GI;
import org.enoch.snark.instance.model.action.DiffLists;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v122.network.Network;
import org.openqa.selenium.devtools.v122.network.model.RequestId;
import org.openqa.selenium.devtools.v122.network.model.Response;

import javax.transaction.Transactional;

import java.util.Optional;

import static org.enoch.snark.gi.types.UrlComponent.*;
import static org.enoch.snark.instance.model.types.ColonyType.PLANET;

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
        new OpenPageCommand(SUPPLIES, colony).sourceHash(this.getClass().getSimpleName()).push();
        new OpenPageCommand(FACILITIES, colony).sourceHash(this.getClass().getSimpleName()).push();
        if(colony.is(PLANET)) new OpenPageCommand(LFBUILDINGS, colony).sourceHash(this.getClass().getSimpleName()).push();
        new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName()).push();
        new OpenPageCommand(DEFENSES, colony).sourceHash(this.getClass().getSimpleName()).push();
    }

    @Override
    public String toString() {
        return LoadColoniesCommand.class.getSimpleName();
    }
}
