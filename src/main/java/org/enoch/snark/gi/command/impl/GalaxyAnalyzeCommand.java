package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.gi.command.CommandType;
import org.enoch.snark.gi.command.GICommand;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.Utils;
import org.enoch.snark.model.SystemView;

import java.util.Optional;

import static org.enoch.snark.model.Universe.GALAXY_MAX;

public class GalaxyAnalyzeCommand extends GICommand {

    private GalaxyDAO galaxyDAO;
    private final GIUrlBuilder giUrlBuilder;
    private SystemView systemView;

    public GalaxyAnalyzeCommand(Instance instance, SystemView systemView) {
        super(instance, CommandType.INTERFACE_REQUIERED);
        galaxyDAO = GalaxyDAO.getInstance();
        giUrlBuilder = new GIUrlBuilder();
        this.systemView = systemView;
        normalize(this.systemView);
    }

    private void normalize(SystemView systemView) {
        systemView.galaxy = Math.floorMod(systemView.galaxy, Integer.parseInt(instance.universe.getConfig(GALAXY_MAX)));
    }

    @Override
    public boolean execute() {
        final Optional<GalaxyEntity> galaxyEntity = galaxyDAO.find(systemView);

        if(galaxyEntity.isPresent() && DateUtil.lessThanHours(22, galaxyEntity.get().updated)) {
            return true;
        }
        giUrlBuilder.openGalaxy(systemView, null);
        Utils.sleep();
        return true;
    }
}
