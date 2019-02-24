package org.enoch.snark.module.farm;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.FarmDAO;
import org.enoch.snark.db.dao.impl.FarmDAOImpl;
import org.enoch.snark.db.entity.FarmEntity;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThred;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class FarmThred extends AbstractThred {

    private final FarmDAO farmDAO;
    private FarmEntity farmEntity;

    public FarmThred(SI si) {
        super(si);
        farmDAO = new FarmDAOImpl(si.getInstance().universeEntity);
        farmEntity = farmDAO.getActualState();
    }

    @Override
    public void run() {
        super.run();
        while(true) {

            if(farmEntity.warRequestCode != null) {
                // x = przylot ostatniego statku
                // stworz nowy farm entity
                // ustaw jego start na x-5min
                // aktualny farmEntity ustaw jako nowy
            } else if (farmEntity.spyRequestCode !=null) {
                int fleetNum = si.getAvailableFleetCount(this);
                // zapytaj o 50 najnowszych skanow
                // posortuj je wg korzystnosci i bez obrony
                // wybierz fleetNum najbardziej korzystnych
                // posegreguj je wg najdluższego lotu
                // stworz war request - niech ma konstruktor ktoremu podajesz cele
                // i podajesz ile ma z tego ruszyc, bo jak by jakis byl nie wypalem z powodu
                // bledu albo pozniej ze z falangi sie nie oplaca to zeby wziol nastepny
            } else if(farmEntity.start.before( new Timestamp(System.currentTimeMillis()))) {
                // wyciagnij poprzedni zbior celow
                // wysylac sondy 50 sond do planet ktore maja najwieszy porencjal a nie bylu ostatnio oblatywane
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
