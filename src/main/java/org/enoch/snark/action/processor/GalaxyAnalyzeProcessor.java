package org.enoch.snark.action.processor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.action.command.GalaxyAnalyzeCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.db.entity.*;
import org.enoch.snark.db.repository.GalaxyRepository;
import org.enoch.snark.db.repository.PlayerRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.GalaxyGIR;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.enoch.snark.action.command.status.ExecutionIssue.NO_ISSUE;
import static org.enoch.snark.action.command.status.ExecutionIssue.RETRY;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class GalaxyAnalyzeProcessor{

    private final PlayerRepository playerRepository;
    private final TargetRepository targetRepository;
    private final GalaxyRepository galaxyRepository;

    public ExecutionIssue execute(GI gi, GalaxyAnalyzeCommand command) {
        GalaxyGIR gir = new GalaxyGIR(gi);
        Integer galaxy = command.getSystemView().getGalaxy();
        Integer system = command.getSystemView().getSystem();

        ColonyEntity colony = gi.url().openGalaxy(command.getSystemView(), command.getSource());
        List<TargetEntity> fromDB = targetRepository.findByGalaxyAndSystem(galaxy, system);
        gir.updateGalaxy(command.getSystemView(), fromDB);

        removeNotUpdated(fromDB);
        List<TargetEntity> newTargets = fromDB.stream().filter(targetEntity -> targetEntity.updated != null && targetEntity.id == null).toList();
        addNewTargets(newTargets, command.getSpyPositions(), command.getSpyNew());

        Optional<GalaxyEntity> galaxyEntityOptional = galaxyRepository.findByGalaxyAndSystem(galaxy, system);
        if(galaxyEntityOptional.isPresent()) galaxyEntityOptional.get().updated = LocalDateTime.now();
        else {
            GalaxyEntity galaxyEntity = new GalaxyEntity();
            galaxyEntity.galaxy = galaxy;
            galaxyEntity.system = system;
            galaxyEntity.updated = LocalDateTime.now();
            galaxyRepository.save(galaxyEntity);
        }
        Map<Planet, Boolean> spyPositions = command.getSpyPositions();
        System.err.println(command.getSystemView()+" spy to click "+spyPositions.size());
        gir.takeAction(spyPositions, Mission.SPY);

        boolean anySpyFailed = spyPositions.values().stream().anyMatch(aBoolean -> !aBoolean);
        if(anySpyFailed) {
            System.err.println("------------------\n"+command.getSystemView());
            spyPositions.forEach((planet, aBoolean) -> System.err.println(planet.position+" "+aBoolean));
            System.err.println("------------------");
            return RETRY;
        }
        return NO_ISSUE;
    }

    private void removeNotUpdated(List<TargetEntity> targets) {

        List<TargetEntity> list = targets.stream().filter(targetEntity -> targetEntity.updated == null).toList();
        if(!list.isEmpty()) {
            System.err.print(list.size()+" targets to remove: ");
            list.forEach(System.err::print);
            System.err.println();
        }
        targetRepository.deleteAll(list);
    }

    @Transactional
    private void addNewTargets(List<TargetEntity> targets, Map<Planet, Boolean> spyPositions, String spyPattern) {
        Map<String, PlayerEntity> playerCache = new HashMap<>();
        for (TargetEntity target : targets) {
            PlayerEntity player = target.getPlayer();
            playerCache.computeIfAbsent(player.getCode(), code ->
                    playerRepository.findFirstByCode(code)
                            .orElseGet(() -> playerRepository.save(player))
            );
            target.setPlayer(playerCache.get(player.getCode()));
        }
        List<Planet> list = targets.stream()
                .filter(targetEntity -> targetEntity.id == null)
                .map(PlanetEntity::toPlanet).toList();
        if(!list.isEmpty()) {
            System.err.print(list.size()+" targets to add: ");
            list.forEach(System.err::print);
            System.err.println();
        }

        if(!StringUtils.isEmpty(spyPattern)) {
            targets.forEach(target -> {
                String targetProperties = target.player.status;
                if(targetProperties.toLowerCase().contains(spyPattern)) {
                    spyPositions.put(target.toPlanet(), false);
                    System.err.println("Match "+spyPattern+" to "+targetProperties.toLowerCase()+" for "+target.toPlanet());
                }
            });
        }
        targetRepository.saveAll(targets);
    }

}
