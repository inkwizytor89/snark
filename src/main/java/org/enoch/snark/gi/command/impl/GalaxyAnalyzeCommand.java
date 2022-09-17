package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.gi.command.CommandType;
import org.enoch.snark.gi.command.GICommand;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.common.SleepUtil;

import java.time.LocalDateTime;

public class GalaxyAnalyzeCommand extends GICommand {

    private final GalaxyEntity galaxyEntity;
    private final LocalDateTime lastUpdated;
    private GalaxyDAO galaxyDAO;
    private final GIUrlBuilder giUrlBuilder;

    public GalaxyAnalyzeCommand(GalaxyEntity galaxyEntity) {
        super(CommandType.INTERFACE_REQUIERED);
        this.galaxyEntity = galaxyEntity;
        lastUpdated = galaxyEntity.updated;
        galaxyDAO = GalaxyDAO.getInstance();
        giUrlBuilder = new GIUrlBuilder();
    }

    @Override
    public boolean execute() {
        giUrlBuilder.openGalaxy(galaxyEntity.toSystemView(), null);
        SleepUtil.sleep();
        return true;
    }

    @Override
    public String toString() {
        return "Look at " + galaxyEntity + " updated at "+lastUpdated;
    }
}
