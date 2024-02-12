package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.model.SystemView;

import java.time.LocalDateTime;
import java.util.Optional;

public class GalaxyAnalyzeCommand extends AbstractCommand {

    private GalaxyEntity galaxyEntity;
    private LocalDateTime lastUpdated;
    private GalaxyDAO galaxyDAO;
    private GIUrlBuilder giUrlBuilder;

    public GalaxyAnalyzeCommand(GalaxyEntity galaxyEntity) {
        super();
        init(galaxyEntity);
    }

    public GalaxyAnalyzeCommand(SystemView systemView) {
        super();
        Optional<GalaxyEntity> galaxyOptional = GalaxyDAO.getInstance().find(systemView);
        galaxyOptional.ifPresent(this::init);
        galaxyOptional.orElseThrow(() -> new RuntimeException("All galaxy should be present"));
    }

    private void init(GalaxyEntity galaxyEntity) {
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
