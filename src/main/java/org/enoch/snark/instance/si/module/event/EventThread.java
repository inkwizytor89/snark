package org.enoch.snark.instance.si.module.event;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

public class EventThread extends AbstractThread {

    public static final String threadType = "event";
    public Long elements = null;

    public EventThread(ConfigMap map) {
        super(map);
    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected int getPauseInSeconds() {
        return 4;
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
