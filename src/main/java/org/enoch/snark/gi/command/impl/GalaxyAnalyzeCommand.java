package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.gi.command.CommandType;
import org.enoch.snark.gi.command.GICommand;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Utils;

public class GalaxyAnalyzeCommand extends GICommand {

    private final GalaxyEntity galaxyEntity;
    private GalaxyDAO galaxyDAO;
    private final GIUrlBuilder giUrlBuilder;

    public GalaxyAnalyzeCommand(GalaxyEntity galaxyEntity) {
        super(CommandType.INTERFACE_REQUIERED);
        this.galaxyEntity = galaxyEntity;
        galaxyDAO = GalaxyDAO.getInstance();
        giUrlBuilder = new GIUrlBuilder();
    }

    @Override
    public boolean execute() {
        if(galaxyEntity.updated != null && DateUtil.lessThanHours(22, galaxyEntity.updated)) {
            return true;
        }
        giUrlBuilder.openGalaxy(galaxyEntity.toSystemView(), null);
        Utils.sleep();
        return true;
    }

    @Override
    public String toString() {
        return "Look at " + galaxyEntity;
    }
}
