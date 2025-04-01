package org.enoch.snark.action.processor;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.GalaxyAnalyzeCommand;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.enoch.snark.instance.model.to.SystemView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Scope("prototype")
public class GalaxyAnalyzeProcessor{

    private GalaxyEntity galaxyEntity;

//    public GalaxyAnalyzeProcessor(GalaxyEntity galaxyEntity) {
//        super();
//        init(galaxyEntity);
//    }
//
//    public GalaxyAnalyzeProcessor(SystemView systemView) {
//        super();
//        Optional<GalaxyEntity> galaxyOptional = GalaxyDAO.getInstance().find(systemView);
//        galaxyOptional.ifPresent(this::init);
//        galaxyOptional.orElseThrow(() -> new RuntimeException("All galaxy should be present"));
//    }

    private void init(GalaxyEntity galaxyEntity) {
        this.galaxyEntity = galaxyEntity;
    }

    public boolean execute(GI gi, GalaxyAnalyzeCommand command) {
        gi.url().openGalaxy(command.galaxyEntity.toSystemView(), null);
        return true;
    }

    @Override
    public String toString() {
        return "Look at " + galaxyEntity + " updated at "+galaxyEntity.updated;
    }
}
