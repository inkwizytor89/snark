package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.BuildCommand;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.TechnologyGIR;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.service.TechnologyService;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.BuildRequirements;
import org.enoch.snark.instance.si.module.building.BuildingCost;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.enoch.snark.action.command.status.ExecutionIssue.NO_ISSUE;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class BuildProcessor {

    private final ColonyRepository colonyRepository;

    public TechnologyGIR gir;
    private GI gi;

    public ExecutionIssue execute(GI gi, BuildCommand command) {
        this.gi = gi;
        gir = new TechnologyGIR(gi);
        BuildRequirements requirements = command.requirements;

        ColonyEntity colony = gi.url().openComponent(command.requirements.request.technology.getPage(), command.colony);
        colonyRepository.save(colony);

        if(isBuildQueueBlockedForBuildRequest(colony, requirements.request)) return NO_ISSUE;

        boolean isUpgraded = gir.upgradeBuilding(requirements);
        if(isUpgraded) {
            refreshColonyWhenBuildingIsDone(command, requirements);
            return NO_ISSUE;
        }
        if(requirements.isResourceUnknown()) {
            Resources costs = gir.findTechnologyCosts(requirements.request.technology.name());
            BuildingCost.getInstance().put(requirements.request, costs);

//            String masterHref = Instance.getGlobalMainConfigMap().getConfig(MASTER, StringUtils.EMPTY);
//            if (masterHref != null && !masterHref.isEmpty()) {
//                new SendMessageToPlayerCommand(masterHref, "Master poprosze " + costs + " na " + colony).push();
//            }
        }
        return NO_ISSUE;
    }

    private void refreshColonyWhenBuildingIsDone(BuildCommand command, BuildRequirements requirements) {
        colonyRepository.save(gi.url().openComponent(requirements.request.technology.getPage(), command.colony));
        Long seconds = gir.updateQueue(command.colony, TechnologyService.BUILDING);
                if(seconds != null)
                    command.setNext(new OpenPageCommand(requirements.request.technology.getPage(), command.colony)
                    .sourceHash(this.getClass().getSimpleName()), seconds);
    }

    private boolean isBuildQueueBlockedForBuildRequest(ColonyEntity colony, BuildRequest buildRequest) {
        return TechnologyService.getInstance().isBlocked(colony, buildRequest.technology);
    }
}
