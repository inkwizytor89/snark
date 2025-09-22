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
        System.err.println("Create module bean "+name+" as "+moduleClass.getSimpleName());
        return beanFactory.getBean(name, moduleClass);
    }

    public <T> T registerThreadBeanIfAbsent(String name, Class<? extends T> beanClass) {
        if (applicationContext.containsBean(name)) return applicationContext.getBean(name, beanClass);

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        beanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getBeanDefinition());
        System.err.println("Create thread bean "+name+" as "+beanClass.getSimpleName());
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
}
