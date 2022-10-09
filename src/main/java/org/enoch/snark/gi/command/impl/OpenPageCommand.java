package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.gi.command.CommandType;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.QueueManger;
import org.enoch.snark.model.Resources;
import org.enoch.snark.module.building.BuildRequirements;
import org.enoch.snark.module.building.BuildingCost;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.enoch.snark.gi.GI.TECHNOLOGIES;

public class OpenPageCommand extends AbstractCommand {

    private String page;
    private ColonyEntity colony;
    private GI gi;

    private boolean checkEventFleet = false;

    public OpenPageCommand(String page, ColonyEntity colony) {
        super(Instance.getInstance(), CommandType.INTERFACE_REQUIERED);
        this.page = page;
        this.colony = colony;
        this.gi = GI.getInstance();
    }

    public OpenPageCommand setCheckEventFleet(boolean checkEventFleet) {
        this.checkEventFleet = checkEventFleet;
        return this;
    }

    @Override
    public boolean execute() {
        new GIUrlBuilder()
                .setCheckEventFleet(checkEventFleet)
                .open(page, colony);
        return true;
    }

    @Override
    public String toString() {
        return "open " + page + " on " + colony;
    }
}
