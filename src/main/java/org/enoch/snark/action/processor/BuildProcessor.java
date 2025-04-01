package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.action.command.BuildCommand;
import org.enoch.snark.action.command.SendMessageToPlayerCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.TechnologyGIR;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.service.TechnologyService;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.BuildRequirements;
import org.enoch.snark.instance.si.module.building.BuildingCost;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.enoch.snark.instance.si.module.ThreadMap.MASTER;

@Component
@Scope("prototype")
public class BuildProcessor {

    public TechnologyGIR gir;
    private GI gi;

    public boolean execute(GI gi, BuildCommand command) {
        this.gi = gi;
        gir = new TechnologyGIR(gi);
        ColonyEntity colony = command.colony;
        BuildRequirements requirements = command.requirements;

        gi.url().openComponent(command.requirements.request.technology.getPage(), colony);

        if(isBuildQueueBlockedForBuildRequest(colony, requirements.request)) return true;

        boolean isUpgraded = gir.upgradeBuilding(requirements);
        if(isUpgraded) {
            refreshColonyWhenBuildingIsDone(colony, requirements);
            return true;
        }
        if(requirements.isResourceUnknown()) {
            Resources costs = gir.findTechnologyCosts(requirements.request.technology.name());
            BuildingCost.getInstance().put(requirements.request, costs);

            String masterHref = Instance.getGlobalMainConfigMap().getConfig(MASTER, StringUtils.EMPTY);
            if (masterHref != null && !masterHref.isEmpty()) {
                new SendMessageToPlayerCommand(masterHref, "Master poprosze " + costs + " na " + colony).push();
            }
        }
        return true;
    }

    private void refreshColonyWhenBuildingIsDone(ColonyEntity colony, BuildRequirements requirements) {
        gi.url().openComponent(requirements.request.technology.getPage(), colony);
        Long seconds = gir.updateQueue(colony, TechnologyService.BUILDING);
        //todo: spring update setNext
        //        if(seconds != null) setNext(new OpenPageCommand(requirements.request.technology.getPage(), colony)
//                    .sourceHash(this.getClass().getSimpleName()), seconds);
    }

    private boolean isBuildQueueBlockedForBuildRequest(ColonyEntity colony, BuildRequest buildRequest) {
        return TechnologyService.getInstance().isBlocked(colony, buildRequest.technology);
    }
}
