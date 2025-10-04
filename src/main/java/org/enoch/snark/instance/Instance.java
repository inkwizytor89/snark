package org.enoch.snark.instance;

import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.si.module.ThreadMap;
import org.enoch.snark.instance.si.module.PropertiesMap;

import java.util.List;
import java.util.logging.Logger;

import static org.enoch.snark.instance.si.module.ThreadMap.MAIN;

public class Instance {

    protected static final Logger LOG = Logger.getLogger(Instance.class.getName());

    private static Instance INSTANCE;

    @Getter
    private static PropertiesMap propertiesMap = new PropertiesMap();
    public static Integer level = 1;

    private Instance() {
    }

    public static Instance getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Instance();
        }
        return INSTANCE;
    }

    public static ThreadMap getGlobalMainConfigMap() {
        return getGlobalMainConfigMap(MAIN);
    }

    public static ThreadMap getGlobalMainConfigMap(String name) {
        throw new NotImplementedException("getGlobalMainConfigMap not to use");
//        ModuleMap moduleMap = propertiesMap.get(GLOBAL);
//        if(!moduleMap.containsKey(name)) return new ThreadMap();
//        return moduleMap.get(name);
    }

    public static List<ColonyEntity> getSources() {
        return getGlobalMainConfigMap().getSources();
    }
}
