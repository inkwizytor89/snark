package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.QueueManger;
import org.enoch.snark.model.Resources;
import org.enoch.snark.instance.config.Universe;
import org.enoch.snark.module.building.BuildRequirements;
import org.enoch.snark.module.building.BuildingCost;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.enoch.snark.gi.GI.*;

public class BuildCommand extends AbstractCommand {

    private final QueueManger queueManger;
    private ColonyEntity colony;
    private BuildRequirements requirements;
    private GI gi;

    public BuildCommand(ColonyEntity colony, BuildRequirements requirements) {
        super(Instance.getInstance(), CommandType.NORMAL_REQUIERED);
        this.colony = colony;
        this.requirements = requirements;
        queueManger = QueueManger.getInstance();
        this.gi = GI.getInstance();
    }

    @Override
    public boolean execute() {
        new GIUrlBuilder().open(requirements.request.building.getPage(), colony);
        boolean upgraded = gi.upgrade(requirements);
        if(upgraded) {
            // resource in database refresh
            new GIUrlBuilder().open(requirements.request.building.getPage(), colony);

            Integer seconds = instance.gi.updateQueue(colony, QueueManger.BUILDING);
            if(seconds == null) {
                System.err.println("wrong seconds read");
                this.setSecondToDelayAfterCommand(seconds);
                this.setAfterCommand(new OpenPageCommand(requirements.request.building.getPage(), colony));
            }
        } else {
            WebElement technologies = gi.webDriver.findElement(By.id(TECHNOLOGIES));
            WebElement buildingIcon = technologies.findElement(By.className(requirements.request.building.getName()));
            buildingIcon.click();
            SleepUtil.sleep();
            WebElement costs = gi.webDriver.findElement(By.className("costs"));
            Resources resources = new Resources(
                    getCost(costs, "metal"),
                    getCost(costs, "crystal"),
                    getCost(costs, "deuterium"));
            String masterHref = Instance.config.getConfig(Universe.MASTER);
            if(masterHref != null && !masterHref.isEmpty()) {
                SendMessageToPlayerCommand messageCommend = new SendMessageToPlayerCommand(masterHref,
                        "Master poprosze "+resources+ " na "+colony);
                Instance.commander.push(messageCommend);
            }
            BuildingCost.getInstance().put(requirements.request, resources);
        }
        return true;
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
