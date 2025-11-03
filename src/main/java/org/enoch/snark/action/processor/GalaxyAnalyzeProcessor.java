package org.enoch.snark.action.processor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.GalaxyAnalyzeCommand;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.repository.GalaxyRepository;
import org.enoch.snark.db.repository.PlayerRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.GalaxyGIR;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.enoch.snark.instance.model.to.SystemView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class GalaxyAnalyzeProcessor{

    private final PlayerRepository playerRepository;
    private final TargetRepository targetRepository;
    private final GalaxyRepository galaxyRepository;

    public boolean execute(GI gi, GalaxyAnalyzeCommand command) {
        GalaxyGIR gir = new GalaxyGIR(gi);
        Integer galaxy = command.getSystemView().getGalaxy();
        Integer system = command.getSystemView().getSystem();

        ColonyEntity colony = gi.url().openGalaxy(command.getSystemView(), null);
        List<TargetEntity> fromDB = targetRepository.findByGalaxyAndSystem(galaxy, system);
        gir.updateGalaxy(command.getSystemView(), fromDB);
        removeNotUpdated(fromDB);
        addNewTargets(fromDB);

        Optional<GalaxyEntity> galaxyEntityOptional = galaxyRepository.findByGalaxyAndSystem(galaxy, system);
        if(galaxyEntityOptional.isPresent()) galaxyEntityOptional.get().updated = LocalDateTime.now();
        else {
            GalaxyEntity galaxyEntity = new GalaxyEntity();
            galaxyEntity.galaxy = galaxy;
            galaxyEntity.system = system;
            galaxyEntity.updated = LocalDateTime.now();
            galaxyRepository.save(galaxyEntity);
        }
        return true;
    }

    private void removeNotUpdated(List<TargetEntity> targets) {
        targetRepository.deleteAll(targets.stream().filter(targetEntity -> targetEntity.updated == null).toList());
    }

    private void addNewTargets(List<TargetEntity> targets) {
        targets.forEach(targetEntity -> {
            if(targetEntity.player.id == null) {
                Optional<PlayerEntity> byCode = playerRepository.findFirstByCode(targetEntity.player.code);
                targetEntity.player = byCode.orElseGet(() -> playerRepository.save(targetEntity.player));
            }
        });


        List<TargetEntity> newTargets = targets.stream().filter(targetEntity -> targetEntity.id == null).toList();


        targetRepository.saveAllAndFlush(newTargets);
    }

}
