package org.enoch.snark.instance.si.module;

import org.enoch.snark.db.entity.ThreadEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Component
public class DatabaseThreadBeanFactory {

    private static final String BEAN_NAME_PREFIX = "databaseThreadPrototype.";

    private final DefaultListableBeanFactory beanFactory;

    public DatabaseThreadBeanFactory(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public synchronized AbstractThread create(
            Class<? extends AbstractThread> threadClass,
            ThreadEntity entity
    ) {
        AbstractThread thread = create(threadClass);
        initializeDatabaseThread(thread, entity);
        return thread;
    }

    public synchronized AbstractThread create(Class<? extends AbstractThread> threadClass) {
        String beanName = BEAN_NAME_PREFIX + threadClass.getName();
        if (!beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.registerBeanDefinition(beanName, BeanDefinitionBuilder
                    .genericBeanDefinition(threadClass)
                    .setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
                    .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR)
                    .getBeanDefinition());
        }
        return beanFactory.getBean(beanName, threadClass);
    }

    public void initializeDatabaseThread(AbstractThread thread, ThreadEntity entity) {
        String moduleName = entity.module.name;
        String threadName = entity.name;
        String threadType = entity.type;
        ThreadMap moduleMainMap = identityMap(moduleName, MAIN, MAIN);
        ModuleMap moduleMap = new ModuleMap(moduleName);
        moduleMap.put(MAIN, moduleMainMap);
        AbstractModule module = new AbstractModule(moduleMap);
        module.setMainMap(moduleMainMap);

        thread.setThreadEntity(entity);
        ThreadMap threadMap = identityMap(moduleName, threadName, threadType);
        thread.updateMap(threadMap);
        thread.setModule(module);
    }

    private ThreadMap identityMap(String moduleName, String threadName, String threadType) {
        ThreadMap map = new ThreadMap();
        map.put(MODULE, moduleName);
        map.put(NAME, threadName);
        map.put(TYPE, threadType);
        return map;
    }

    public void destroy(AbstractThread thread) {
        thread.destroy();
        beanFactory.destroyBean(thread);
    }
}
