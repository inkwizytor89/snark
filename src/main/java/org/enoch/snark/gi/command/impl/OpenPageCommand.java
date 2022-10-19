package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;

public class OpenPageCommand extends AbstractCommand {

    private final String page;
    private final ColonyEntity colony;

    private boolean checkEventFleet = false;

    public OpenPageCommand(String page, ColonyEntity colony) {
        super(Instance.getInstance(), CommandType.INTERFACE_REQUIERED);
        this.page = page;
        this.colony = colony;
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
