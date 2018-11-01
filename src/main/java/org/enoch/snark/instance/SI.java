package org.enoch.snark.instance;

import org.enoch.snark.module.AbstractModule;
import org.enoch.snark.module.explore.SpaceModule;
import org.enoch.snark.module.farm.FarmModule;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SI {

    private static final Logger log = Logger.getLogger( SI.class.getName() );

    Set<AbstractModule> modules = new TreeSet<>();
    private Date nearestActionDate;
    private AbstractModule nearestModule;
    private Instance instance;

    public SI(Instance instance) {
        this.instance = instance;
//        modules.add(new FarmModule(instance));
        modules.add(new SpaceModule(instance));
    }

    public void run() {
        while(true) {
            for (AbstractModule module : modules) {
                if (module.isReady()) {
                    module.run();
                } if (module.isInProgress()) {
                    break;
                }
            }
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

//    public void run() {
//        while(true) {
//            nearestActionDate = initTime();
//            for (AbstractModule module : modules) {
//                if (module.isReady()) {
//                    module.run();
//                }
//                updateNearestActionDate(module.getReadyOn(), module);
//            }
//            sleep();
//        }
//    }

    private void updateNearestActionDate(Date readyOn, AbstractModule module) {
        if(nearestActionDate.after(readyOn)) {
            nearestActionDate = readyOn;
            nearestModule = module;
        }
    }

    private Date initTime() {
        return new Date(new Date().getTime() + 24*3600*1000);
    }

    private void sleep() {
        long timeToSleep = nearestActionDate.getTime() - new Date().getTime()+1000;
        log.log(Level.FINE, "Next {0} on {1} that is {2}ms",
                new Object[]{nearestModule.getName(), nearestActionDate.toString(), timeToSleep});
        try {
            TimeUnit.MILLISECONDS.sleep(timeToSleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
