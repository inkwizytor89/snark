package org.enoch.snark.instance;

import org.enoch.snark.instance.si.module.PropertiesMap;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class Core {

    private final DefaultListableBeanFactory beanFactory;

    public Core(ApplicationContext context) {
        this.beanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
    }

    public void configurationUpdate(PropertiesMap map) {

    }

    private void registerBean(String beanName, Class<?> beanClass) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        beanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
    }

    private void removeBean(String beanName) {
        beanFactory.removeBeanDefinition(beanName);
    }
}
