package org.enoch.snark.instance.si;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.common.RunningState;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.si.module.*;
import org.enoch.snark.instance.si.module.consumer.Consumer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class Core {

    private final DefaultListableBeanFactory beanFactory;

    private Map<String, AbstractModule> modules = new ConcurrentHashMap<>();
    private CommandDeque queue;
    private CommandDeque commandDeque;
    private boolean isDequeReady;

//    private BaseSI() {
//        waitForEndOfInitialActions();
//        start();
//    }

    private void waitForEndOfInitialActions() {
        while(!Consumer.getInstance().peekQueues().isEmpty()) SleepUtil.pause();
    }

//    public void configurationUpdate(PropertiesMap propertiesMap) {
//        propertiesMap.forEach((s, moduleMap) -> {
//            moduleMap.forEach((s1, configMap) -> {
//                Integer pause = configMap.getConfigInteger(ConfigMap.PAUSE, 10);
//                String name = configMap.name();
//                if(taskExists(name)) {
//                    removeTask(name);
//                }
//                createTask(name, pause*1000);
//            });
//        });
//    }

    public void configurationUpdate(PropertiesMap propertiesMap) {
        for(ModuleMap moduleMap : propertiesMap.modules()) {
            String moduleName = moduleMap.getName();
            AbstractModule module = registerBeanIfAbsent(moduleName, moduleMap.getTypeClass(), modules);
            module.updateMap(moduleMap);

            for(ThreadMap threadMap : module.getModuleMap().threads()) {
                String threadName = threadMap.name();
                AbstractThread thread = registerBeanIfAbsent(threadName, threadMap.getTypeClass(), module.getThreadsMap());
                thread.setMap(threadMap);
            }
           // brakuje usuwania modułów i wygaszanie beanow i co tmajeszcze potrzeba
        }


//        propertiesMap.forEach((moduleName, moduleMap) -> {
//            if(modules.containsKey(moduleName)) {
//                modules.get(moduleName).updateMap(moduleMap);
//            } else {
//                AbstractModule module = AbstractModule.create(moduleName, moduleMap);
//                modules.put(moduleName, module);
//            }
//        });
//        modules.entrySet().stream()
//                .filter(entry -> !propertiesMap.containsKey(entry.getKey()))
//                .forEach(thread -> thread.getValue().destroy());
//        SleepUtil.secondsToSleep(10L);
    }

    public <T> T registerBeanIfAbsent(String name, Class<? extends T> beanClass, Map<String, T> map) {
        T bean = map.get(name);
        if (bean == null) {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
            beanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getBeanDefinition());
            bean = beanFactory.getBean(name, beanClass);
            map.put(name, bean);
        }
        return bean;
    }

    public int getAvailableFleetCount(String withOutThreadName) {
        int fleetMax = Instance.consumer.getFleetMax();
        if(fleetMax == 0) return 0;

        int fleetInUse = 0;
        List<AbstractThread> threads = modules.values().stream()
                .flatMap(abstractModule -> abstractModule.getThreadsMap().values().stream()).toList();
        for(AbstractThread thread : threads) {
            if(RunningState.isRunning(thread.getActualState()) && !thread.getName().equals(withOutThreadName)) {
                fleetInUse += thread.getRequestedFleetCount();
            }
        }
        return fleetMax - fleetInUse;
    }

    public void register(CommandDeque commandDeque) {
        this.commandDeque = commandDeque;
    }

    public synchronized boolean isDequeReady() {
        if(isDequeReady) return true;
        isDequeReady = commandDeque != null && commandDeque.isEmpty();
        return isDequeReady;
    }
}
