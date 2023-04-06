package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.Instance;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;

public class RefreshColoniesStateCommand extends AbstractCommand {

    public RefreshColoniesStateCommand() {
        super(Instance.getInstance(), CommandType.NORMAL_REQUIERED);
    }

    @Override
    public boolean execute() {

        for(ColonyEntity colony : instance.flyPoints) {
            OpenPageCommand openPageCommand = new OpenPageCommand(PAGE_BASE_FLEET, colony);
            openPageCommand.execute();
        }
        return true;
    }

    @Override
    public String toString() {
        return RefreshColoniesStateCommand.class.getName();
    }
}
