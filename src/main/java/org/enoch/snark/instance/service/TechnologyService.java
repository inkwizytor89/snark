package org.enoch.snark.instance.service;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.technology.*;
import org.enoch.snark.instance.model.uc.TechnologyUC;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.enoch.snark.instance.model.uc.TechnologyUC.*;

public class TechnologyService {

    public static final String RESEARCH = "productionboxresearchcomponent";
    public static final String BUILDING = "productionboxbuildingcomponent";
    public static final String SHIPYARD = "productionboxshipyardcomponent";
    public static final String LIFE_FORM_BUILDINGS = "productionboxlfbuildingcomponent";
    public static final String LIFE_FORM_RESEARCH = "productionboxlfresearchcomponent";
    private static TechnologyService INSTANCE;

    private final Map<ColonyEntity,Map<String, QueueMonitor>> queueMap;
    private final QueueMonitor researchMonitor;


    private TechnologyService() {
        queueMap = new HashMap<>();
        researchMonitor = new QueueMonitor();

    }

    private void updateQueueMap(ColonyEntity colony) {
        if(!queueMap.containsKey(colony)) {
            HashMap<String, QueueMonitor> colonyQueues = new HashMap<>();
            colonyQueues.put(RESEARCH, researchMonitor);
            colonyQueues.put(BUILDING, new QueueMonitor());
            colonyQueues.put(SHIPYARD, new QueueMonitor());
            colonyQueues.put(LIFE_FORM_BUILDINGS, new QueueMonitor());
            colonyQueues.put(LIFE_FORM_RESEARCH, new QueueMonitor());
            queueMap.put(colony, colonyQueues);

            if(queueMap.size() > 30) {
                throw new RuntimeException("QueueManger leak: too many queue "+queueMap.size());
            }
        }
    }

    public static TechnologyService getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new TechnologyService();
        }
        return INSTANCE;
    }

    public boolean isBlocked(ColonyEntity colony, Technology technology) {
        updateQueueMap(colony);
        QueueMonitor queueMonitor = queueMap.get(colony).get(findQueueName(technology));
        if(!queueMonitor.isFree()) return true;
        return isTechnologyBlockedFromOtherReasons(colony, technology);
    }

    public void clean(ColonyEntity colony, String name) {
        updateQueueMap(colony);
        queueMap.get(colony).get(name).clean();
    }

    public void update(ColonyEntity colony, String name, LocalDateTime date, Technology technology) {
        updateQueueMap(colony);
        queueMap.get(colony).get(name).update(technology, date);
    }

    private String findQueueName(Technology technology) {
        if(technology instanceof Building) return BUILDING;
        if(technology instanceof Research) return RESEARCH;
        if(technology instanceof LFBuilding) return LIFE_FORM_BUILDINGS;
        if(technology instanceof LFResearch) return LIFE_FORM_RESEARCH;
        if(technology instanceof Ship) return SHIPYARD;
        if(technology instanceof Defense) return SHIPYARD;
        throw new IllegalStateException("TechnologyService can not find queue name "+technology.name());
    }

    private boolean isTechnologyBlockedFromOtherReasons(ColonyEntity colony, Technology technology) {
        if(technology instanceof Research) {
            return getMonitorsFromAllColonies(BUILDING).stream()
                    .map(QueueMonitor::getTechnology)
                    .anyMatch(TechnologyUC::isLaboratory);
        }
        if(isLaboratory(technology)) return !queueMap.get(colony).get(RESEARCH).isFree();

        if(technology instanceof LFResearch) {
            Technology buldingTechnology = queueMap.get(colony).get(LIFE_FORM_BUILDINGS).getTechnology();
            return isLFLaboratory(buldingTechnology);
        }
        if(isLFLaboratory(technology)) return !queueMap.get(colony).get(LIFE_FORM_RESEARCH).isFree();

        if(technology instanceof LFBuilding) {
            Technology buldingTechnology = queueMap.get(colony).get(BUILDING).getTechnology();
            return isFactory(buldingTechnology);
        }
        if(isFactory(technology)) return !queueMap.get(colony).get(LIFE_FORM_BUILDINGS).isFree();
        return false;
    }

    private List<QueueMonitor> getMonitorsFromAllColonies(String queueName) {
        List<QueueMonitor> result = new ArrayList<>();
        for(Map<String,QueueMonitor> map : queueMap.values()) {
            result.add(map.get(queueName));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("QueueManger "+queueMap.keySet().size()+" :\n");
        for(ColonyEntity colony : queueMap.keySet()) {
            result.append(colony).append(" BUILDING queue is free ").append(queueMap.get(colony).get(BUILDING).isFree());
            result.append("\n");
        }
        return result.toString();
    }
}
