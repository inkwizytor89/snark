package org.enoch.snark.instance.model.action.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class NoMissionsCondition extends AbstractCondition {

    private final Planet source;
    private final List<Mission> blockingMissions;
}
