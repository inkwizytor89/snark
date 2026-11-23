package org.enoch.snark.instance.si.module;

import org.enoch.snark.db.entity.ThreadEntity;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class ThreadBeanFactory {

    private static final String BEAN_NAME_PREFIX = "databaseThreadPrototype.";

    private final DefaultListableBeanFactory beanFactory;

    public ThreadBeanFactory(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public synchronized AbstractThread create(
            Class<? extends AbstractThread> threadClass,
            ThreadEntity entity
    ) {
        AbstractThread thread = create(threadClass);
        thread.setThreadEntity(entity);
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

    public void destroy(AbstractThread thread) {
        thread.destroy();
        beanFactory.destroyBean(thread);
    }
}
