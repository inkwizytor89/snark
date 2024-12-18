package org.enoch.snark.instance;

import org.enoch.snark.instance.si.module.ThreadMap;
import org.enoch.snark.instance.si.module.PropertiesMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//@Service
public class DynamicTaskManager {

    private final Map<String, ExecutorService> taskExecutors = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    // Tworzenie zadania
    public void createTask(String name, long interval) {
        if (taskExecutors.containsKey(name)) {
            throw new IllegalArgumentException("Task with name " + name + " already exists.");
        }

        TaskBean taskBean = new TaskBean(name, interval); // Tworzenie nowego beana
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(taskBean);

        taskExecutors.put(name, executor);
    }

    // Usuwanie zadania
    public void removeTask(String name) {
        ExecutorService executor = taskExecutors.remove(name);
        if (executor != null) {
            executor.shutdownNow();
            System.out.println("Task [" + name + "] stopped.");
        }
    }

    // Sprawdzenie istnienia zadania
    public boolean taskExists(String name) {
        return taskExecutors.containsKey(name);
    }

    public void configurationUpdate(PropertiesMap propertiesMap) {
        propertiesMap.forEach((s, moduleMap) -> {
            moduleMap.forEach((s1, configMap) -> {
                Integer pause = configMap.getConfigInteger(ThreadMap.PAUSE, 10);
                String name = configMap.name();
                if(taskExists(name)) {
                    removeTask(name);
                }
                createTask(name, pause*1000);
            });
        });
    }
}
