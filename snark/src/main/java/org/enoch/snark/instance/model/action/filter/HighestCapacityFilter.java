package org.enoch.snark.instance.model.action.filter;

import org.enoch.snark.action.command.SendCommand;

import java.util.List;

public class HighestCapacityFilter extends AbstractFilter {

    @Override
    public List<SendCommand> filter(List<SendCommand> promises) {
        return promises;
    }
}
