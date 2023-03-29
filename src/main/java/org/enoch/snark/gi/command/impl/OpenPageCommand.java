package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_RESEARCH;

public class OpenPageCommand extends AbstractCommand {

    private String page;
    private PlayerEntity player;
    private ColonyEntity colony;

    private boolean checkEventFleet = false;

    public OpenPageCommand(String page, ColonyEntity colony) {
        super(Instance.getInstance(), CommandType.NORMAL_REQUIERED);
        this.page = page;
        this.colony = colony;
    }

    public OpenPageCommand(String page, PlayerEntity player) {
        super(Instance.getInstance(), CommandType.NORMAL_REQUIERED);
        this.page = page;
        this.player = player;
    }

    public OpenPageCommand(String page) {
        super(Instance.getInstance(), CommandType.NORMAL_REQUIERED);
        this.page = page;
    }

    public OpenPageCommand setCheckEventFleet(boolean checkEventFleet) {
        this.checkEventFleet = checkEventFleet;
        return this;
    }

    @Override
    public boolean execute() {
        if(colony != null) {
            new GIUrlBuilder()
                    .setCheckEventFleet(checkEventFleet)
                    .open(page, colony);
        } else if(player != null) {
            new GIUrlBuilder()
                    .openWithPlayerInfo(PAGE_RESEARCH, player);
        } else {
            new GIUrlBuilder()
                    .setCheckEventFleet(checkEventFleet)
                    .open(page, null);
        }
        return true;
    }

    @Override
    public String toString() {
        return "open " + page + " on " + colony;
    }
}
