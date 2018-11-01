package org.enoch.snark.gi.command.request;

import org.enoch.snark.gi.command.SpyObserver;
import org.enoch.snark.gi.command.impl.ReadSpyInfoCommand;
import org.enoch.snark.gi.command.impl.SpyCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SpyInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SpyRequest implements SpyObserver{

    private final List<Planet> targets;
    private final SpyReportWaiter waiter;
    private final List<SpyInfo> spyReport = new ArrayList<>();
    private final LocalDateTime startTimestamp = LocalDateTime.now();

    public SpyRequest(Instance instance, List<Planet> targets, SpyReportWaiter waiter) {
        this.targets = targets;
        this.waiter = waiter;
        for(Planet target : targets) {

            final SpyCommand spyCommand = new SpyCommand(instance, target);
            spyCommand.setAfterCommand(new ReadSpyInfoCommand(instance, target, this));
            instance.commander.push(spyCommand);
        }

        returnSpyReport();
    }

    private void returnSpyReport() {
        Runnable task = () -> {
             do {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (areNotAll());

            waiter.saveSpyReport(spyReport);
        };

        new Thread(task).start();
    }

    private boolean areNotAll() {
        LocalDateTime toLate = startTimestamp.plusMinutes(20);
        return !LocalDateTime.now().isAfter(toLate) && spyReport.size() < targets.size();

    }

    public void report(SpyInfo info) {
        spyReport.add(info);
    }
}
