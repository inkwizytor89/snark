package org.enoch.snark.instance.si;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.common.status.ColonyStatus;
import org.enoch.common.status.CoordinatesStatus;
import org.enoch.snark.common.RunningState;
import org.enoch.common.status.ServerStatus;
import org.enoch.snark.config.ConfigurationScheduledTask;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.si.module.*;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static org.enoch.snark.instance.si.module.AbstractThread.PROCESSING_SUFFIX;
import static org.enoch.snark.instance.model.types.ColonyType.PLANET;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Service
@RequiredArgsConstructor
public class Core {

    private final ApplicationContext applicationContext;
    private final DefaultListableBeanFactory beanFactory;
    private final CacheEntryRepository cacheEntryRepository;
    private final ColonyRepository colonyRepository;
//    private final RemotePropertiesMap remotePropertiesMap;

    private Map<String, AbstractModule> modules = new ConcurrentHashMap<>();
    private CommandDeque queue;
    private CommandDeque commandDeque;
    private boolean isDequeReady;
    public static boolean isSomethingAttacking;

    @Getter
    @Setter
    private static ColonyEntity lastVisited;

    @PostConstruct
    public void init() {
        cacheEntryRepository.setUnknownForSuffix(PROCESSING_SUFFIX);
    }

    public void configurationUpdate(PropertiesMap propertiesMap) {
        for(ModuleMap moduleMap : propertiesMap.modules()) {
            String moduleName = moduleMap.getName();
            AbstractModule module = registerModuleBeanIfAbsent(moduleName, moduleMap);
            modules.putIfAbsent(moduleName, module);
            module.updateMap(moduleMap);

            for(ThreadMap threadMap : module.getModuleMap().threads()) {
                String threadName = threadMap.get(NAME);
                if(threadName.endsWith(MAIN)) {
                    module.setMainMap(threadMap);
                    continue;
                }

                AbstractThread thread = registerThreadBeanIfAbsent(threadMap.name(), threadMap.getTypeClass());
                thread.updateMap(threadMap);
                thread.setModule(module);
                module.getThreadsMap().put(threadName, thread);
                threadMap.put(TYPE, threadMap.getTypeClass().getSimpleName());
                Executors.newSingleThreadExecutor().submit(thread);
            }
            // brakuje usuwania modułów i wygaszanie beanow i co tmajeszcze potrzeba
        }
    }

    public AbstractModule registerModuleBeanIfAbsent(String name, ModuleMap map) {
        Class<AbstractModule> moduleClass = AbstractModule.class;
        if (applicationContext.containsBean(name)) return applicationContext.getBean(name, moduleClass);

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(moduleClass)
                    .addConstructorArgValue(map);
        beanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getBeanDefinition());
        return beanFactory.getBean(name, moduleClass);
    }

    public <T> T registerThreadBeanIfAbsent(String name, Class<? extends T> beanClass) {
        if (applicationContext.containsBean(name)) return applicationContext.getBean(name, beanClass);

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        beanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getBeanDefinition());
        return beanFactory.getBean(name, beanClass);
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
        if(commandDeque == null)
            System.err.println("Unexpected error: "+command);
        commandDeque.push(command);
    }

    public ServerStatus status() {
        List<String> activeThreads = new ArrayList<>();
        List<String> notifications = new ArrayList<>();
        for(Map.Entry<String, AbstractModule> moduleEntry : modules.entrySet()) {
            moduleEntry.getValue().getThreadsMap().values().stream()
                    .filter(thread -> RunningState.ON.equals(thread.getActualState()))
                    .map(thread -> moduleEntry.getKey() + ":" + thread.map().get(NAME))
                    .forEach(activeThreads::add);
            moduleEntry.getValue().getThreadsMap().values().forEach(thread ->
                    thread.getNotifications().forEach((key, value) -> notifications.add(
                            thread.map().name() + System.lineSeparator() + key +
                                    System.lineSeparator() + value)));
        }
        List<ColonyEntity> colonies = colonyRepository.findAll().stream()
                .sorted(Comparator.comparing(colony -> colony.toPlanet().toString()))
                .toList();
        int planetCount = (int) colonies.stream().filter(colony -> colony.is(PLANET)).count();
        int moonCount = colonies.size() - planetCount;
        List<ColonyStatus> colonyStatuses = colonies.stream()
                .map(colony -> {
                    Map<String, Long> ships = new LinkedHashMap<>();
                    addShip(ships, Ship.fighterLight, colony.fighterLight);
                    addShip(ships, Ship.fighterHeavy, colony.fighterHeavy);
                    addShip(ships, Ship.cruiser, colony.cruiser);
                    addShip(ships, Ship.battleship, colony.battleship);
                    addShip(ships, Ship.interceptor, colony.interceptor);
                    addShip(ships, Ship.bomber, colony.bomber);
                    addShip(ships, Ship.destroyer, colony.destroyer);
                    addShip(ships, Ship.deathstar, colony.deathstar);
                    addShip(ships, Ship.reaper, colony.reaper);
                    addShip(ships, Ship.explorer, colony.explorer);
                    addShip(ships, Ship.transporterSmall, colony.transporterSmall);
                    addShip(ships, Ship.transporterLarge, colony.transporterLarge);
                    addShip(ships, Ship.colonyShip, colony.colonyShip);
                    addShip(ships, Ship.recycler, colony.recycler);
                    addShip(ships, Ship.espionageProbe, colony.espionageProbe);
                    addShip(ships, Ship.solarSatellite, colony.solarSatellite);
                    return new ColonyStatus(
                            colony.toPlanet().toString(),
                            colony.metal,
                            colony.crystal,
                            colony.deuterium,
                            colony.energy,
                            ships
                    );
                })
                .toList();
        return new ServerStatus(
                Navigator.getInstance().getStatus(),
                activeThreads,
                commandDeque == null ? "" : commandDeque.toString(),
                String.join(System.lineSeparator() + System.lineSeparator(), notifications),
                new CoordinatesStatus(planetCount, moonCount, colonyStatuses)
        );
    }

    private void addShip(Map<String, Long> ships, Ship ship, Long count) {
        if (count != null && count > 0) {
            ships.put(ship.name(), count);
        }
    }

//    public synchronized void push(AbstractCommand command, String action) {
//        if (!fleetRepository.isBlockedWithExpiredTime(command.getHash(), action)) push(command);
//    }
}
