package org.enoch.snark.instance.si.module.event;

import lombok.RequiredArgsConstructor;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadMap;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class EventThread extends AbstractThread {

    public static final String threadType = "event";
    public Long elements = null;

//    public EventThread(ThreadMap map) {
//        super(map);
//    }

    @Override
    public ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition("event", threadType, "elements", "0", false,
                "Stored event count used to detect a change in event volume."));
        configs.addAll(commonConfigDefinitions());
        return new ThreadDefinition(threadType, "Tracks important in-game events and raises a notification when the volume changes.", configs);
    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected String defaultPause() {
        return "4S";
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

    private Long getTargets() {
        return 0L;
    }

}
