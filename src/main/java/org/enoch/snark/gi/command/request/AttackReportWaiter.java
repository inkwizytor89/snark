package org.enoch.snark.gi.command.request;

import org.enoch.snark.model.AttackInfo;
import org.enoch.snark.model.SpyInfo;

import java.util.Collection;

public interface AttackReportWaiter {
    void saveAttackReport(Collection<AttackInfo> values);
}
