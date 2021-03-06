package org.enoch.snark.module.farm;

import org.enoch.snark.gi.command.request.AttackReportWaiter;
import org.enoch.snark.gi.command.request.AttackRequest;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.AttackInfo;
import org.enoch.snark.model.AttackPlan;
import org.enoch.snark.model.SpyInfo;
import org.enoch.snark.module.AbstractModule;
import org.enoch.snark.module.ModuleStatus;
import org.enoch.snark.gi.command.request.SpyReportWaiter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.stream.Collectors;

public class FarmModule extends AbstractModule implements SpyReportWaiter, AttackReportWaiter {

    public FarmModule(Instance instance) {
        super(instance);
        this.priority = 200;
    }

    @Override
    public void run() {
        setStatus(ModuleStatus.IN_PROGRESS);
        throw new NotImplementedException();
//        List<Planet> targets = PlanetFromFileReader.get(targetsFile.getAbsolutePath());
//        if(targets == null ) throw new RuntimeException("Error when load targets from file");
//        System.err.println("Do szukania:");
//        for (Planet planet : targets) {
//            System.err.println(planet);
//        }
//        new SpyRequest(instance, targets, this);
    }

    @Override
    public void saveSpyReport(Collection<SpyInfo> values) {
        Set<SpyInfo> spyInfoSet = new TreeSet<>(values);
        final int fleetCount = calculateAvailableFleetCount();
        List<SpyInfo> toAttack = new ArrayList<>();
        int index = 0;
        for (SpyInfo info : spyInfoSet) {
            System.err.println("Entry: "+info);
            if( info.getSumResourceCount() > 10000) {
                toAttack.add(info);
            }
            if(index>=fleetCount) break;
        }

        final List<AttackPlan> attackPlans = toAttack.stream().map(AttackPlan::new).collect(Collectors.toList());
        new AttackRequest(instance, attackPlans, this);
        setStatus(ModuleStatus.WAITING);
    }

    private int calculateAvailableFleetCount() {
        return 10;
    }

    @Override
    public void saveAttackReport(Collection<AttackInfo> values) {

    }

    @Override
    public boolean isReady() {
        if(isInProgress()) return false;

        final int fleetFreeSlots = instance.commander.getFleetFreeSlots();
        final int fleetMax = instance.commander.getFleetMax();

        return ((double) fleetFreeSlots / (double) fleetMax) > 0.6;
    }
}
