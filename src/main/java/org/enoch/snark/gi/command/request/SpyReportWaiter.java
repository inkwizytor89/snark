package org.enoch.snark.gi.command.request;

import org.enoch.snark.model.SpyInfo;

import java.util.Collection;

public interface SpyReportWaiter {
    void saveSpyReport(Collection<SpyInfo> values);
}
