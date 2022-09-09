package org.enoch.snark.instance.commander;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class QueueManger {

    public static final String RESEARCH = "productionboxresearchcomponent";
    public static final String BUILDING = "productionboxbuildingcomponent";
    public static final String SHIPYARD = "productionboxshipyardcomponent";
    public static final String LIFEFORM_BUILDINGS = "productionboxlfbuildingcomponent";
    public static final String LIFEFORM_RESEARCH = "productionboxlfresearchcomponent";
    private static QueueManger INSTANCE;
    private final ColonyDAO colonyDAO;

    private Map<ColonyEntity,Map<String, QueueMonitor>> queueMap;
    private final QueueMonitor researchMonitor;


    private QueueManger() {
        colonyDAO = ColonyDAO.getInstance();
        queueMap = new HashMap<>();
        researchMonitor = new QueueMonitor();

    }

    private void updateQueueMap(ColonyEntity colony) {
        if(!queueMap.containsKey(colony)) {
            HashMap<String, QueueMonitor> planetQueues = new HashMap<>();
            planetQueues.put(RESEARCH, researchMonitor);
            planetQueues.put(BUILDING, new QueueMonitor());
            planetQueues.put(SHIPYARD, new QueueMonitor());
            planetQueues.put(LIFEFORM_BUILDINGS, new QueueMonitor());
            planetQueues.put(LIFEFORM_RESEARCH, new QueueMonitor());
            queueMap.put(colony, planetQueues);

            if(queueMap.size() > 20) {
                throw new RuntimeException("QueueManger leak: too many queue");
            }
        }
    }

    public static QueueManger getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new QueueManger();
        }
        return INSTANCE;
    }

    public boolean isFree(ColonyEntity colony, String name) {
        updateQueueMap(colony);
        return queueMap.get(colony).get(name).isFree();
    }

    public void clean(ColonyEntity colony, String name) {
        updateQueueMap(colony);
        queueMap.get(colony).get(name).clean();
    }

    public void set(ColonyEntity colony, String name, LocalDateTime date) {
        updateQueueMap(colony);
        queueMap.get(colony).get(name).setDate(date);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("QueueManger"+queueMap.keySet().size()+" :\n");
        for(ColonyEntity colony : queueMap.keySet()) {
            result.append(colony+" BUILDING queue is free "+ queueMap.get(colony).get(BUILDING).isFree());
            result.append("\n");
        }
        return result.toString();
    }
}
