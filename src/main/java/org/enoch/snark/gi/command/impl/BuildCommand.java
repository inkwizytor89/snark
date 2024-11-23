package org.enoch.snark.gi.command.impl;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.TechnologyGIR;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.service.TechnologyService;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.BuildRequirements;
import org.enoch.snark.instance.si.module.building.BuildingCost;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.enoch.snark.gi.GI.TECHNOLOGIES;
import static org.enoch.snark.instance.si.module.ConfigMap.MASTER;

public class BuildCommand extends AbstractCommand {

    private final TechnologyService technologyService;
    private final ColonyEntity colony;
    private final BuildRequirements requirements;
    private final GI gi;

    public BuildCommand(ColonyEntity colony, BuildRequirements requirements) {
        super();
        this.colony = colony;
        this.requirements = requirements;
        technologyService = TechnologyService.getInstance();
        this.gi = GI.getInstance();
        hash("build_"+colony+"_"+requirements);
    }

    @Override
    public boolean execute() {
        GIUrl.openComponent(requirements.request.technology.getPage(), colony);

        if(isBuildQueueBlockedForBuildRequest(colony, requirements.request)) return true;

        boolean isUpgraded = gi.upgradeBuilding(requirements);
        if(isUpgraded) {
            refreshColonyWhenBuildingIsDone();
            return true;
        }
        if(requirements.isResourceUnknown()) {
            WebElement technologies = gi.webDriver.findElement(By.id(TECHNOLOGIES));
            WebElement buildingIcon = technologies.findElement(By.className(requirements.request.technology.name()));
            buildingIcon.click();
            SleepUtil.sleep();
            WebElement costsWe = gi.webDriver.findElement(By.className("costs"));
            Resources costs = new Resources(
                    getCost(costsWe, "metal"),
                    getCost(costsWe, "crystal"),
                    getCost(costsWe, "deuterium"));
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
        Long seconds = new TechnologyGIR().updateQueue(colony, TechnologyService.BUILDING);
        if(seconds != null) {
            System.out.println(colony+" build "+ requirements.request + ", refresh after "+ seconds);
            setNext(new OpenPageCommand(requirements.request.technology.getPage(), colony).sourceHash(this.getClass().getSimpleName()), seconds);
        }
    }

    private Long getCost(WebElement costsElement, String resourceName) {
        List<WebElement>resourceList = costsElement.findElements(By.className(resourceName));
        if(resourceList.isEmpty()) {
            return 0L;
        }
        WebElement resourceElement = resourceList.get(0);
        return Long.parseLong(resourceElement.getAttribute("data-value"));
    }

    private boolean isBuildQueueBlockedForBuildRequest(ColonyEntity colony, BuildRequest buildRequest) {
        return technologyService.isBlocked(colony, buildRequest.technology);
    }

    @Override
    public String toString() {
        return "build " + requirements + " on " + colony;
    }
}
