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
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class FarmThread extends AbstractThread {

    private static final Logger log = Logger.getLogger(FarmThread.class.getName());

    private final FarmDAO farmDAO;
    private final FleetDAO fleetDAO;
    private final TargetDAO targetDAO;
    private FarmEntity actualFarm;
    private FarmEntity previousFarm;

    public FarmThread(SI si) {
        super(si);
        farmDAO = new FarmDAOImpl(si.getInstance().universeEntity);
        targetDAO = new TargetDAOImpl(si.getInstance().universeEntity);
        fleetDAO = new FleetDAOImpl(si.getInstance().universeEntity);
    }

    @Override
    protected int getPauseInSeconds() {
        return 1;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(farmDAO.getActualState() == null) {
//            farmDAO.saveOrUpdate(createFarmEntity());
            farmDAO.saveOrUpdate(createFarmEntity());
        }
        actualFarm = farmDAO.getActualState();
        previousFarm = farmDAO.getPreviousState();
    }

    private FarmEntity createFarmEntity() {
        FarmEntity farmEntity = new FarmEntity();
        farmEntity.universe = si.getInstance().universeEntity;
        farmEntity.start = LocalDateTime.now();
        return farmEntity;
    }

    @Override
    public void onStep() {
        if(targetDAO.findFarms(100).size()<80) {
            si.getInstance().gi.sleep(TimeUnit.SECONDS, 60);
            return;
        }

        if(actualFarm.warRequestCode != null) {
            previousFarm = actualFarm;
            actualFarm = createFarmEntity();
            farmDAO.saveOrUpdate(actualFarm);
            // x = przylot ostatniego statku
            // stworz nowy farm entity
            // ustaw jego start na x-5min
            // aktualny actualFarm ustaw jako nowy
        } else if (actualFarm.spyRequestCode != null) {
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
            List<TargetEntity> farmTargets = targetDAO.findFarms(20);
            actualFarm.spyRequestCode = new SendFleetRequest(si.getInstance(), FleetEntity.SPY, farmTargets)
                    .sendAndWait();
            farmDAO.saveOrUpdate(actualFarm);
            // wyciagnij poprzedni zbior celow

            // wysylac sondy 50 sond do target ktore maja najwieszy porencjal a nie bylu ostatnio oblatywane
            // spyrequest zrobic mu konsrtuktor ktory dziala na tych zbiorach lub lepiej
        }
            // niech czeka ile trzeba a nie aktywuje sie co sekunde
    }
}
