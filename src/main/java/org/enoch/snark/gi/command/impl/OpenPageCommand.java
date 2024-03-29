package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.gi.types.UrlComponent;

public class OpenPageCommand extends AbstractCommand {

    private final UrlComponent component;
    private ColonyEntity colony;

    public OpenPageCommand(UrlComponent component, ColonyEntity colony) {
        super();
        this.component = component;
        this.colony = colony;
    }

    public OpenPageCommand(UrlComponent component) {
        super();
        this.component = component;
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
