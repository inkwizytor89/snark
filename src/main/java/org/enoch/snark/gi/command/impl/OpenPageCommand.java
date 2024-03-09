package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.macro.GIUrl;
import org.enoch.snark.gi.macro.UrlComponent;

public class OpenPageCommand extends AbstractCommand {

    private final UrlComponent component;
    private ColonyEntity colony;

    private boolean updateCurrentState = false;

    public OpenPageCommand(UrlComponent component, ColonyEntity colony) {
        super();
        this.component = component;
        this.colony = colony;
    }

    public OpenPageCommand(UrlComponent component) {
        super();
        this.component = component;
    }

    public OpenPageCommand updateCurrentState(boolean updateCurrentState) {
        this.updateCurrentState = updateCurrentState;
        return this;
    }

    @Override
    public boolean execute() {
            GIUrl.openComponent(component, colony);
        return true;
    }

    @Override
    public String toString() {
        return "open " + component + " on " + colony;
    }
}
