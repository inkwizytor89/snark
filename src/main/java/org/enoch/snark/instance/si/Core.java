package org.enoch.snark.instance.si;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.si.module.*;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static org.enoch.snark.instance.si.module.ThreadMap.MAIN;
import static org.enoch.snark.instance.si.module.ThreadMap.TYPE;

@Service
@RequiredArgsConstructor
public class Core {

    private final ApplicationContext applicationContext;
    private final DefaultListableBeanFactory beanFactory;

    private Map<String, AbstractModule> modules = new ConcurrentHashMap<>();
    private CommandDeque queue;
    private CommandDeque commandDeque;
    private boolean isDequeReady;
    public static boolean isSomethingAttacking;

    @Getter
    @Setter
    private static ColonyEntity lastVisited;

    public void configurationUpdate(PropertiesMap propertiesMap) {
        for(ModuleMap moduleMap : propertiesMap.modules()) {
            String moduleName = moduleMap.getName();
            AbstractModule module = registerBeanIfAbsent(moduleName, moduleMap);
            modules.putIfAbsent(moduleName, module);
//            AbstractModule module = registerModuleBeanIfAbsent(moduleName, modules);
            module.updateMap(moduleMap);

            for(ThreadMap threadMap : module.getModuleMap().threads()) {
                String threadName = threadMap.name();
                if(threadName.endsWith(MAIN)) {
                    module.setMainMap(threadMap);
                    continue;
                }

                AbstractThread thread = registerBeanIfAbsent(threadName, threadMap.getTypeClass(), module.getThreadsMap());
                thread.updateMap(threadMap);
                thread.setModule(module);
                threadMap.put(TYPE, threadMap.getTypeClass().getSimpleName());
                Executors.newSingleThreadExecutor().submit(thread);
            }
            // brakuje usuwania modułów i wygaszanie beanow i co tmajeszcze potrzeba
        }
//    printAllBeans();

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

//    private BaseSI() {
//        waitForEndOfInitialActions();
//        start();
//    }

//    private void waitForEndOfInitialActions() {
//        while(!Consumer.getInstance().peekQueues().isEmpty()) SleepUtil.pause();
//    }

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

//    public <T> T registerBeanIfAbsent(
//            String name,
//            Class<? extends T> beanClass,
//            Map<String, T> map,
//            String... dependencies) {
//
//        T bean = map.get(name);
//        if (bean == null) {
//            // Tworzenie definicji beana
//            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
//
//            // Dodawanie zależności na podstawie nazw z dependencies
//            for (String dependency : dependencies) {
//                Object dependencyBean = beanFactory.getBean(dependency);
//                beanDefinitionBuilder.addConstructorArgValue(dependencyBean);
//            }
//            if(dependencies.length > 0) {
//                Object dependencyCore = beanFactory.getBean(Core.class);
//                beanDefinitionBuilder.addConstructorArgValue(dependencyCore);
//            }
//
//            // Automatyczne wstrzykiwanie pozostałych zależności
//            beanDefinitionBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
//
//            // Rejestracja beana
//            beanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getBeanDefinition());
//
//            // Pobieranie beana
//            System.err.println("getBean(" + name + ", " + beanClass + ")");
//            bean = beanFactory.getBean(name, beanClass);
//
//            // Dodawanie do mapy
//            map.put(name, bean);
//            System.err.println("Create bean " + name + " as " + beanClass.getSimpleName());
//        }
//        return bean;
//    }

    public <T> T registerBeanIfAbsent(String name, Class<? extends T> beanClass, Map<String, T> map) {
        T bean = map.get(name);
        if (bean == null) {

            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
            beanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getBeanDefinition());
            bean = beanFactory.getBean(name, beanClass);
            map.put(name, bean);
            Executors.newSingleThreadExecutor();
            System.err.println("Create bean "+name+" as "+beanClass.getSimpleName());
        }
        return bean;
    }

    public AbstractModule registerBeanIfAbsent(String moduleName, ModuleMap map) {
        // Jeśli już istnieje, zwracamy z cache
        return modules.computeIfAbsent(moduleName, key -> {
            Class<? extends AbstractModule> moduleClass = AbstractModule.class;
            if (moduleClass == null) {
                throw new IllegalArgumentException("Unknown module: " + key);
            }

            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(moduleClass)
                    .addConstructorArgValue(map); // Przekazujemy `ModuleMap`

            beanFactory.registerBeanDefinition(moduleName, beanDefinitionBuilder.getBeanDefinition());

            return beanFactory.getBean(moduleName, moduleClass);
        });
    }

//    public int getAvailableFleetCount(String withOutThreadName) {
//        int fleetMax = Instance.consumer.getFleetMax();
//        if(fleetMax == 0) return 0;
//
//        int fleetInUse = 0;
//        List<AbstractThread> threads = modules.values().stream()
//                .flatMap(abstractModule -> abstractModule.getThreadsMap().values().stream()).toList();
//        for(AbstractThread thread : threads) {
//            if(RunningState.isRunning(thread.getActualState()) && !thread.getName().equals(withOutThreadName)) {
//                fleetInUse += thread.getRequestedFleetCount();
//            }
//        }
//        return fleetMax - fleetInUse;
//    }

    public void printAllBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("Zarejestrowane beany:");
        for (String beanName : beanNames) {
            System.out.println(beanName + " -> " + applicationContext.getBean(beanName).getClass().getName());
        }
    }

    public void register(CommandDeque commandDeque) {
        this.commandDeque = commandDeque;
        System.err.println("Register CommandDeque");
    }

    public synchronized boolean isDequeReady() {
        if(isDequeReady) return true;
        isDequeReady = commandDeque != null && commandDeque.isEmpty();
        return isDequeReady;
    }

    public void push(AbstractCommand command) {
        commandDeque.push(command);
    }
}
