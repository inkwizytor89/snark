package org.enoch.snark.action.processor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.LoadColoniesCommand;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.instance.si.module.consumer.gi.BaseGameInfoGIR;
import org.enoch.snark.instance.model.action.DiffLists;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.DEFENSES;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FACILITIES;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.LFBUILDINGS;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.SUPPLIES;
import static org.enoch.snark.instance.model.types.ColonyType.PLANET;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class LoadColoniesProcessor {

    private final ColonyRepository colonyRepository;
    private final FleetRepository fleetRepository;
    private BaseGameInfoGIR baseGameInfoGIR;

    @Transactional
    public boolean execute(GI gi, LoadColoniesCommand command) {
        try {
            baseGameInfoGIR = new BaseGameInfoGIR(gi);
            DiffLists<ColonyEntity> diff = new DiffLists<>(colonyRepository.findAll(), baseGameInfoGIR.loadPlanetList());

            diff.added().forEach(colony -> {
                System.out.println(LoadColoniesProcessor.class.getSimpleName()+" add " + colony);
                colony.level = colony.formsLevel = 1L;
                Optional<ColonyEntity> cpmEntity = colonyRepository.findByCp(colony.cpm);
                cpmEntity.ifPresent(colonyEntity -> colonyEntity.cpm = colony.cp);
                colonyRepository.save(colony);
                updateColony(colony);
            });

            diff.removed().forEach(colony -> {
                System.out.println(LoadColoniesProcessor.class.getSimpleName()+" remove " + colony);
                fleetRepository.deleteBySource(colony);
                colonyRepository.delete(colony);
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
