package org.enoch.snark.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.enoch.common.config.ConfigEntry;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ModuleDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.db.repository.ConfigRepository;
import org.enoch.snark.db.repository.ModuleRepository;
import org.enoch.snark.db.repository.ThreadRepository;
import org.enoch.snark.instance.si.ThreadManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class ConfigService implements ApplicationRunner {

    private static final String GLOBAL_MODULE = "global";
    private static final String SERVER_KEY = "server";
    public static final String TEMPLATE = "template";
    private static List<ConfigEntry> configs = new ArrayList<>();

    private final ModuleRepository moduleRepository;
    private final ThreadRepository threadRepository;
    private final ConfigRepository configRepository;

    @Value("${properties:}")
    private String propertiesPath;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        if (StringUtils.isNotBlank(propertiesPath)) {
            loadFromFile(propertiesPath);
        }
    }

    @Transactional
    public void updateThreads(List<ConfigEntry> configTerms) {
        for (ConfigEntry term : configTerms) {
            ThreadEntity threadEntity = threadRepository.find(term.getModule(), term.getThread())
                    .orElseGet(() -> createThreadEntity(term));
            threadEntity.update(term);
            threadEntity.updated = LocalDateTime.now();
            threadRepository.save(threadEntity);
            configRepository.saveAll(threadEntity.configs);
        }
    }

    @Transactional
    public void changeThreadModule(ConfigEntry term, String moduleName) {
        ThreadEntity thread = threadRepository.find(term.getModule(), term.getThread())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find thread '" + term.getThread() + "' in module '" + term.getModule() + "'"));
        ModuleEntity module = moduleRepository.findByName(moduleName)
                .orElseGet(() -> moduleRepository.save(new ModuleEntity(moduleName)));

        module.updated = LocalDateTime.now();
        moduleRepository.save(module);

        moduleRepository.findByName(term.getModule()).ifPresent(sourceModule -> {
            sourceModule.updated = LocalDateTime.now();
            moduleRepository.save(sourceModule);
        });

        thread.module = module;
        thread.updated = LocalDateTime.now();
        threadRepository.save(thread);
    }

    @Transactional
    public void deleteModule(ConfigEntry term) {
        moduleRepository.findByName(term.getModule()).ifPresent(module -> {
            List<ThreadEntity> threads = threadRepository.findByModule(module);
            for (ThreadEntity thread : threads) {
                List<ConfigEntity> configs = configRepository.findByThread_Module_NameAndThread_Name(
                        term.getModule(), thread.name);
                configRepository.deleteAll(configs);
            }
            threadRepository.deleteAll(threads);
            moduleRepository.delete(module);
            log.info("Deleted module '{}' and all its threads and configurations", term.getModule());
        });
    }

    @Transactional(readOnly = true)
    public List<ModuleDefinition> listModuleDefinitions() {
        return moduleRepository.findAll().stream().map(module -> {
            ModuleDefinition definition = new ModuleDefinition(module.name, module.time, module.conditions);
            definition.setCreatedOn(module.createdOn);
            definition.setUpdated(module.updated);
            definition.setConfigs(List.of(
                    new ConfigDefinition("time", module.time, false,
                            "Schedule controlling when the module can run."),
                    new ConfigDefinition("conditions", module.conditions, false,
                            "Conditions that must be satisfied before the module can run.")
            ));
            return definition;
        }).toList();
    }

    @Transactional
    public ModuleDefinition updateModule(ConfigEntry term) {
        if (term == null || term.getModule() == null || term.getModule().isBlank()
                || term.getKey() == null || term.getKey().isBlank()) {
            throw new IllegalArgumentException("Module, key, and value are required");
        }

        ModuleEntity module = moduleRepository.findByName(term.getModule())
                .orElseGet(() -> moduleRepository.save(new ModuleEntity(term.getModule())));
        switch (term.getKey()) {
            case "time" -> module.time = term.getValue();
            case "conditions" -> module.conditions = term.getValue();
            default -> throw new IllegalArgumentException("Unsupported module configuration key: " + term.getKey());
        }
        module.updated = LocalDateTime.now();
        moduleRepository.save(module);

        ModuleDefinition definition = new ModuleDefinition(module.name, module.time, module.conditions);
        definition.setCreatedOn(module.createdOn);
        definition.setUpdated(module.updated);
        definition.setConfigs(List.of(
                new ConfigDefinition("time", module.time, false,
                        "Schedule controlling when the module can run."),
                new ConfigDefinition("conditions", module.conditions, false,
                        "Conditions that must be satisfied before the module can run.")
        ));
        return definition;
    }

    public List<ConfigDefinition> listConfigs() {
        return configRepository.findAll().stream()
                .map(config -> new ConfigDefinition(
                        config.thread.module.name,
                        config.thread.name,
                        config.key,
                        config.value,
                        String.class,
                        false,
                        ""))
                .toList();
    }

    @Transactional
    public void deleteConfig(ConfigEntry term) {
        threadRepository.find(term.getModule(), term.getThread()).ifPresent(thread ->
                configRepository.findByThreadAndKey(thread, term.getKey())
                        .ifPresent(config -> {
                            configRepository.delete(config);
                            thread.updated = LocalDateTime.now();
                            threadRepository.save(thread);
                        }));
    }

    @Transactional
    public void deleteThread(ConfigEntry term) {
        threadRepository.find(term.getModule(), term.getThread()).ifPresent(thread -> {
            List<ConfigEntity> configs = configRepository.findByThread_Module_NameAndThread_Name(
                    term.getModule(), term.getThread());
            configRepository.deleteAll(configs);
            threadRepository.delete(thread);
            log.info("Deleted thread '{}' from module '{}' and its {} configurations",
                    term.getThread(), term.getModule(), configs.size());
        });
    }

    private ThreadEntity createThreadEntity(ConfigEntry term) {
        ModuleEntity module = moduleRepository.findByName(term.getModule())
                .orElseGet(() -> moduleRepository.save(new ModuleEntity(term.getModule())));
        ThreadEntity thread = new ThreadEntity(
                term.getThread(), module, resolveThreadType(term.getThread()));
        ThreadEntity savedThread = threadRepository.save(thread);
        log.info("Created thread '{}' in module '{} as {}'", savedThread.name, savedThread.module.name, savedThread.type);
        return savedThread;
    }

    private String resolveThreadType(String threadName) {
        return ThreadManager.availableThreadTypes().stream()
                .filter(threadName::contains)
                .findFirst()
                .orElse(threadName);
    }

    @Transactional
    public void loadFromFile(String propertiesPath) throws IOException {
        List<ConfigEntry> loadedConfigs = loadConfig(propertiesPath);
        if (loadedConfigs != null) {
            updateThreads(loadedConfigs);
            log.info("Loaded {} configurations from file '{}'", loadedConfigs.size(), propertiesPath);
        } else {
            log.info("No changes detected in configurations from file '{}'", propertiesPath);
        }
    }

    private List<ConfigEntry> loadConfig(String propertiesPath) throws IOException {
        List<ConfigEntry> globals = loadConfigsFromFile(propertiesPath, GLOBAL_MODULE);
        List<ConfigEntry> result = new ArrayList<>(globals);
        for (ConfigEntry term : globals)
            if(TEMPLATE.equals(term.getKey()) && !StringUtils.isEmpty(term.getValue())) {
                result.addAll(loadConfigsFromFile(term.getValue(), term.getModule()));
            }
        if (areConfigsChanged(configs, result)) {
            configs = result;
            return new ArrayList<>(configs);
        } else return null;
    }

    private boolean areConfigsChanged(List<ConfigEntry> configs, List<ConfigEntry> result) {
        if(configs.size() != result.size()) return true;
        for(int i = 0; i < configs.size(); i++)
            if (!configs.get(i).getValue().equals(result.get(i).getValue()))
                return true;
        return false;
    }

    private static List<ConfigEntry> loadConfigsFromFile(String propertiesPath, String defaultModule) throws IOException {
        File file = new File(propertiesPath);
        if(!file.exists() || !file.isFile()) throw new RuntimeException("File "+file.getAbsolutePath()+" is incorrect");

        FileInputStream fileInputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fileInputStream);
        fileInputStream.close();
        return properties.stringPropertyNames().stream()
                .map(s -> ConfigEntry.parse(s, properties.getProperty(s), defaultModule)).toList();
    }
}
