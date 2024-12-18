package org.enoch.snark.gi.command.impl;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.TechnologyGIR;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.service.TechnologyService;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.BuildRequirements;
import org.enoch.snark.instance.si.module.building.BuildingCost;

import static org.enoch.snark.instance.si.module.ThreadMap.MASTER;

public class BuildCommand extends AbstractCommand {

    private final TechnologyService technologyService;
    private final ColonyEntity colony;
    private final BuildRequirements requirements;
    private final TechnologyGIR gir = new TechnologyGIR();

    public BuildCommand(ColonyEntity colony, BuildRequirements requirements) {
        super();
        this.colony = colony;
        this.requirements = requirements;
        technologyService = TechnologyService.getInstance();
        hash("build_"+colony+"_"+requirements);
    }

    @Override
    public boolean execute() {
        GIUrl.openComponent(requirements.request.technology.getPage(), colony);

        if(isBuildQueueBlockedForBuildRequest(colony, requirements.request)) return true;

        boolean isUpgraded = gir.upgradeBuilding(requirements);
        if(isUpgraded) {
            refreshColonyWhenBuildingIsDone();
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

    private void refreshColonyWhenBuildingIsDone() {
        GIUrl.openComponent(requirements.request.technology.getPage(), colony);
        Long seconds = gir.updateQueue(colony, TechnologyService.BUILDING);
        if(seconds != null) {
            setNext(new OpenPageCommand(requirements.request.technology.getPage(), colony)
                    .sourceHash(this.getClass().getSimpleName()), seconds);
        }
    }

    private boolean isBuildQueueBlockedForBuildRequest(ColonyEntity colony, BuildRequest buildRequest) {
        return technologyService.isBlocked(colony, buildRequest.technology);
    }

    @Override
    public String toString() {
        return "build " + requirements + " on " + colony;
    }
}
