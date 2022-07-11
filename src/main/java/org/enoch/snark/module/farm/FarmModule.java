package org.enoch.snark.module.farm;

import org.enoch.snark.instance.Instance;
import org.enoch.snark.module.AbstractModule;

import java.util.*;
import java.util.stream.Collectors;

public class FarmModule extends AbstractModule {

    public FarmModule(Instance instance) {
        super(instance);
        this.priority = 200;
    }

    @Override
    public void run() {
        
    }

//    @Override
//    public void run() {
//        setStatus(ModuleStatus.IN_PROGRESS);
//        throw new NotImplementedException();
////        List<Planet> targets = PlanetFromFileReader.get(targetsFile.getAbsolutePath());
////        if(targets == null ) throw new RuntimeException("Error when load targets from file");
////        System.err.println("Do szukania:");
////        for (Planet target : targets) {
////            System.err.println(target);
////        }
////        new SendFleetRequest(instance, targets, this);
//    }
//
//    @Override
//    public void saveSpyReport(Collection<SpyInfo> values) {
//        Set<SpyInfo> spyInfoSet = new TreeSet<>(values);
//        final int fleetCount = calculateAvailableFleetCount();
//        List<SpyInfo> toAttack = new ArrayList<>();
//        int index = 0;
//        for (SpyInfo info : spyInfoSet) {
//            System.err.println("Entry: "+info);
//            if( info.getSumResourceCount() > 10000) {
//                toAttack.add(info);
//            }
//            if(index>=fleetCount) break;
//        }
//
//        final List<AttackPlan> attackPlans = toAttack.stream().map(AttackPlan::new).collect(Collectors.toList());
//        new AttackRequest(instance, attackPlans, this);
//        setStatus(ModuleStatus.WAITING);
//    }
//
//    private int calculateAvailableFleetCount() {
//        return 10;
//    }
//
//    @Override
//    public void saveAttackReport(Collection<AttackInfo> values) {
//
//    }
//
//    @Override
//    public boolean isReady() {
//        if(isInProgress()) return false;
//
//        final int fleetFreeSlots = instance.commander.getFleetFreeSlots();
//        final int fleetMax = instance.commander.getFleetMax();
//
//        return ((double) fleetFreeSlots / (double) fleetMax) > 0.6;
//    }
}
