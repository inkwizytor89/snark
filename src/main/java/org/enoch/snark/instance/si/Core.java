package org.enoch.snark.instance.si;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.common.Debug;
import org.enoch.snark.config.ConfigurationScheduledTask;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.FleetRepository;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.si.module.*;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.instance.si.module.AbstractThread.PROCESSING_SUFFIX;
import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Service
@RequiredArgsConstructor
public class Core {

    private final ApplicationContext applicationContext;
    private final DefaultListableBeanFactory beanFactory;
    private final ConfigurationScheduledTask configurationScheduledTask;
    private final CacheEntryRepository cacheEntryRepository;

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

    @Scheduled(fixedDelay = 10000)
    public void loadConfig() throws IOException {
        PropertiesMap propertiesMap = configurationScheduledTask.loadConfig();
        if(propertiesMap == null) return;
        configurationUpdate(propertiesMap);
    }

    public void configurationUpdate(PropertiesMap propertiesMap) {
        for(ModuleMap moduleMap : propertiesMap.modules()) {
            String moduleName = moduleMap.getName();
            AbstractModule module = registerModuleBeanIfAbsent(moduleName, moduleMap);
            modules.putIfAbsent(moduleName, module);
            module.updateMap(moduleMap);

            for(ThreadMap threadMap : module.getModuleMap().threads()) {
                String threadName = threadMap.name();
                if(threadName.endsWith(MAIN)) {
                    module.setMainMap(threadMap);
                    continue;
                }

                AbstractThread thread = registerThreadBeanIfAbsent(threadName, threadMap.getTypeClass());
                thread.updateMap(threadMap);
                thread.setModule(module);
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

    public String status() {
        StringBuilder threadsStatus = new StringBuilder();
        for(Entry<String, AbstractModule> moduleEntry : modules.entrySet()) {
            threadsStatus.append(moduleEntry.getKey()).append(":");
            for (Entry<String, ThreadMap> threadEntry : moduleEntry.getValue().getModuleMap().entrySet()) {
                threadsStatus.append(threadEntry.getKey()).append("=").append(threadEntry.getValue().get(TIME)).append(",");
            }
            threadsStatus.append(" ");
        }
//        List<AbstractThread> threads = modules.values().stream().flatMap(m -> m.getThreadsMap().values().stream()).toList();
//        for(AbstractThread thread : threads) threadsStatus.append(thread).append(" ");
        return Navigator.getInstance().getStatus()+" Q:"+commandDeque+"\n"+threadsStatus;

    }

//    public synchronized void push(AbstractCommand command, String action) {
//        if (!fleetRepository.isBlockedWithExpiredTime(command.getHash(), action)) push(command);
//    }
}
