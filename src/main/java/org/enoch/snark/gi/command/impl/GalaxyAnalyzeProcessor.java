package org.enoch.snark.gi.command.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.instance.model.to.SystemView;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GalaxyAnalyzeProcessor{

    private GalaxyEntity galaxyEntity;

    public GalaxyAnalyzeProcessor(GalaxyEntity galaxyEntity) {
        super();
        init(galaxyEntity);
    }

    public GalaxyAnalyzeProcessor(SystemView systemView) {
        super();
        Optional<GalaxyEntity> galaxyOptional = GalaxyDAO.getInstance().find(systemView);
        galaxyOptional.ifPresent(this::init);
        galaxyOptional.orElseThrow(() -> new RuntimeException("All galaxy should be present"));
    }

    private void init(GalaxyEntity galaxyEntity) {
        this.galaxyEntity = galaxyEntity;
    }

    public boolean execute(GalaxyAnalyzeCommand command) {
        GIUrl.openGalaxy(command.galaxyEntity.toSystemView(), null);
        return true;
    }

    @Override
    public String toString() {
        return "Look at " + galaxyEntity + " updated at "+galaxyEntity.updated;
    }
}
