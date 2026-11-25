package org.enoch.snark.instance.si.module.event;

import lombok.RequiredArgsConstructor;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.repository.ConfigRepository;
import org.enoch.snark.instance.si.module.AbstractThread;

@RequiredArgsConstructor
public class EventThread extends AbstractThread {

    public static final String threadType = EventDefinition.THREAD_TYPE;
    private final ConfigRepository configRepository;
    private EventDefinition config;
    private Long elements;

    @Override
    protected void onStart() {
        super.onStart();
        config = new EventDefinition();
        config.update(configRepository.findByThread_Module_NameAndThread_Name(
                threadEntity.module.name, "main"));
        config.update(threadEntity.configs);
        elements = config.getElements();
    }

    @Override
    protected void onStep() {
        Long eventsCount = getTargets();
        if (elements == null || eventsCount < elements) elements = eventsCount;
        if(eventsCount > elements) {
            NotificationSoundPlayer.getInstance().play();
            elements = eventsCount;
        }
    }

    @Override
    public ThreadDefinition threadConfiguration(String thread, String module) {
        return config.threadConfiguration(threadEntity.name, threadEntity.module.name);
    }

    private Long getTargets() {
        return 0L;
    }

}
