package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class JacksonConditionConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        String basePackage = "org.enoch.snark.instance.model.action.condition";

        var scanner = new org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(AbstractCondition.class));

        Set<Class<?>> conditionClasses = scanner.findCandidateComponents(basePackage).stream()
                .map(BeanDefinition::getBeanClassName)
                .map(name -> {
                    try {
                        return ClassUtils.forName(name, getClass().getClassLoader());
                    } catch (Exception e) {
                        throw new RuntimeException("Can not load class: " + name, e);
                    }
                })
                .collect(Collectors.toSet());

        for (Class<?> conditionClass : conditionClasses) {
            if (AbstractCondition.class.isAssignableFrom(conditionClass)) {
                String typeName = conditionClass.getSimpleName()
                        .replace("Condition", "")
                        .replaceAll("([a-z])([A-Z]+)", "$1_$2")
                        .toUpperCase();
                mapper.registerSubtypes(new NamedType(conditionClass, typeName));
            }
        }

        return mapper;
    }
}
