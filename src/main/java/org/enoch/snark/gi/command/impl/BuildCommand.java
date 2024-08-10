package org.enoch.snark.gi.command.impl;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.action.QueueManger;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.si.module.building.BuildRequirements;
import org.enoch.snark.instance.si.module.building.BuildingCost;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.enoch.snark.gi.GI.TECHNOLOGIES;
import static org.enoch.snark.instance.si.module.ConfigMap.MASTER;

public class BuildCommand extends AbstractCommand {

    private final QueueManger queueManger;
    private ColonyEntity colony;
    private BuildRequirements requirements;
    private GI gi;

    public BuildCommand(ColonyEntity colony, BuildRequirements requirements) {
        super();
        this.colony = colony;
        this.requirements = requirements;
        queueManger = QueueManger.getInstance();
        this.gi = GI.getInstance();
        hash("build_"+colony+"_"+requirements);
    }

    @Override
    public boolean execute() {
        GIUrl.openComponent(requirements.request.building.getPage(), colony);
        boolean isUpgraded = gi.upgradeBuilding(requirements);
        if(isUpgraded) {
            refreshColonyWhenBuildingIsDone();
        } else {
            WebElement technologies = gi.webDriver.findElement(By.id(TECHNOLOGIES));
            WebElement buildingIcon = technologies.findElement(By.className(requirements.request.building.name()));
            buildingIcon.click();
            SleepUtil.sleep();
            WebElement costsWe = gi.webDriver.findElement(By.className("costs"));
            Resources costs = new Resources(
                    getCost(costsWe, "metal"),
                    getCost(costsWe, "crystal"),
                    getCost(costsWe, "deuterium"));
            BuildingCost.getInstance().put(requirements.request, costs);
            String masterHref = Instance.getMainConfigMap().getConfig(MASTER, StringUtils.EMPTY);
            if(masterHref != null && !masterHref.isEmpty()) {
                new SendMessageToPlayerCommand(masterHref, "Master poprosze "+costs+ " na "+colony).push();
            }
        }
        return true;
    }

    private boolean noDuplication() {
        return false;
    }

    private void refreshColonyWhenBuildingIsDone() {
        GIUrl.openComponent(requirements.request.building.getPage(), colony);
        Long seconds = GI.getInstance().updateQueue(colony, QueueManger.BUILDING);
        if(seconds != null) {
            System.out.println(colony+" build "+ requirements.request + ", refresh after "+ seconds);
            setNext(new OpenPageCommand(requirements.request.building.getPage(), colony), seconds);
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

    @Override
    public String toString() {
        return "build " + requirements + " on " + colony;
    }
}
