package org.enoch.snark.instance.ommander;

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

    private Map<ColonyEntity,Map<String, QueueMonitor>> queues;


    private QueueManger() {
        colonyDAO = ColonyDAO.getInstance();
        queues = new HashMap<>();
        QueueMonitor researchMonitor = new QueueMonitor();

        for(ColonyEntity colony : colonyDAO.fetchAll()) {
            HashMap<String, QueueMonitor> planetQueues = new HashMap<>();
            planetQueues.put(RESEARCH, researchMonitor);
            planetQueues.put(BUILDING, new QueueMonitor());
            planetQueues.put(SHIPYARD, new QueueMonitor());
            planetQueues.put(LIFEFORM_BUILDINGS, new QueueMonitor());
            planetQueues.put(LIFEFORM_RESEARCH, new QueueMonitor());
            queues.put(colony, planetQueues);
        }
    }

    public static QueueManger getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new QueueManger();
        }
        return INSTANCE;
    }

    public boolean isFree(ColonyEntity colony, String name) {
        return queues.get(colony).get(name).isFree();
    }

    public void clean(ColonyEntity colony, String name) {
        queues.get(colony).get(name).clean();
    }

    public void set(ColonyEntity colony, String name, LocalDateTime date) {
        queues.get(colony).get(name).setDate(date);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for(ColonyEntity colony : colonyDAO.fetchAll()) {
            result.append(colony+" BUILDING queue is free "+queues.get(colony).get(BUILDING).isFree());
            result.append("\n");
        }
        return result.toString();
    }
}
