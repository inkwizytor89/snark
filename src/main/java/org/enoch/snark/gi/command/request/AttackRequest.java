package org.enoch.snark.gi.command.request;

import org.enoch.snark.gi.command.impl.SendFleetCommandOld;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.AttackInfo;
import org.enoch.snark.model.AttackPlan;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AttackRequest {

    private final AttackReportWaiter waiter;
    private final List<AttackInfo> spyReport = new ArrayList<>();
    private final LocalDateTime startTimestamp = LocalDateTime.now();

    public AttackRequest(Instance instance, List<AttackPlan> attacks, AttackReportWaiter waiter) {
        this.waiter = waiter;
        for(AttackPlan attack : attacks) {

            final SendFleetCommandOld attackCommand = new SendFleetCommandOld(instance, attack.target, Mission.ATTACK, attack.fleet);
            instance.commander.push(attackCommand);
        }
    }
}
