package org.enoch.snark.module.farm;

import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.dao.impl.FarmDAOImpl;
import org.enoch.snark.db.dao.impl.FleetDAOImpl;
import org.enoch.snark.db.dao.impl.TargetDAOImpl;
import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.command.request.SendFleetRequest;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThred;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FarmThred extends AbstractThred {

    private final FarmDAO farmDAO;
    private final FleetDAO fleetDAO;
    private final TargetDAO targetDAO;
    private FarmEntity actualFarm;
    private FarmEntity previousFarm;

    public FarmThred(SI si) {
        super(si);
        farmDAO = new FarmDAOImpl(si.getInstance().universeEntity);
        targetDAO = new TargetDAOImpl(si.getInstance().universeEntity);
        fleetDAO = new FleetDAOImpl(si.getInstance().universeEntity);
        actualFarm = farmDAO.getActualState();
        previousFarm = farmDAO.getPreviousState();
    }

    @Override
    public void run() {
        super.run();
        while(true) {

            if(targetDAO.findFarms(100).size()<100) {
                try {
                    TimeUnit.SECONDS.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            if(actualFarm.warRequestCode != null) {
                previousFarm = actualFarm;
                actualFarm = new FarmEntity();
                actualFarm.start = LocalDateTime.now();
                farmDAO.saveOrUpdate(actualFarm);
                // x = przylot ostatniego statku
                // stworz nowy farm entity
                // ustaw jego start na x-5min
                // aktualny actualFarm ustaw jako nowy
            } else if (actualFarm.spyRequestCode !=null) {
                int fleetNum = si.getAvailableFleetCount(this);
                List<TargetEntity> farmTargets = targetDAO.findTopFarms(fleetNum);
                actualFarm.warRequestCode = new SendFleetRequest(si.getInstance(), FleetEntity.ATTACK, farmTargets)
                        .setLimit(fleetNum)
                        .sendAndWait();
                farmDAO.saveOrUpdate(actualFarm);
                // zapytaj o 50 najnowszych skanow
                // posortuj je wg korzystnosci i bez obrony
                // wybierz fleetNum najbardziej korzystnych
                // posegreguj je wg najdluższego lotu
                // stworz war request - niech ma konstruktor ktoremu podajesz cele
                // i podajesz ile ma z tego ruszyc, bo jak by jakis byl nie wypalem z powodu
                // bledu albo pozniej ze z falangi sie nie oplaca to zeby wziol nastepny
            } else if(LocalDateTime.now().isAfter(actualFarm.start)) {
                List<TargetEntity> farmTargets = targetDAO.findFarms(50);
                actualFarm.spyRequestCode = new SendFleetRequest(si.getInstance(), FleetEntity.SPY, farmTargets)
                        .sendAndWait();
                farmDAO.saveOrUpdate(actualFarm);
                // wyciagnij poprzedni zbior celow
                
                // wysylac sondy 50 sond do target ktore maja najwieszy porencjal a nie bylu ostatnio oblatywane
                // spyrequest zrobic mu konsrtuktor ktory dziala na tych zbiorach lub lepiej
            } else {
                // niech czeka ile trzeba a nie aktywuje sie co sekunde
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
//        farmDAO.getLastState();
    }
}
