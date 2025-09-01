package org.enoch.snark.action.command;

import org.enoch.snark.action.command.status.CommandStatus;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;

import java.util.List;
import java.util.Map;

public interface SendCommand {
    ColonyEntity getSource();

    Planet getTarget();

    Mission getMission();

    ShipsMap getShipsMap();

    ShipsMap getLeaveShipsMap();

    Long getSpeed();

    Resources getResources();

    Resources getLeaveResources();

    List<AbstractCondition> getConditions();

    String hash();

    CommandStatus getStatus();

    void clearNext();

    boolean isRequiredAction(String action);

    FollowingAction getFollowingAction();

}
