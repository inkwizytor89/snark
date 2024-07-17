package org.enoch.snark.instance.model.action.filter;

import org.enoch.snark.gi.command.impl.SendFleetPromiseCommand;

import java.util.List;

public class HighestCapacityFilter extends AbstractFilter {

    @Override
    public List<SendFleetPromiseCommand> filter(List<SendFleetPromiseCommand> promises) {
        return promises;
    }
}
