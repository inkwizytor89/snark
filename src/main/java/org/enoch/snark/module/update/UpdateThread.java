package org.enoch.snark.module.update;

import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;
import org.enoch.snark.module.AbstractThread;

import java.util.logging.Logger;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;

public class UpdateThread extends AbstractThread {

    public static final String threadName = "update";
    protected static final Logger LOG = Logger.getLogger( UpdateThread.class.getName());

    public UpdateThread(SI si) {
        super(si);
        pause = 300;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return pause;
    }

    @Override
    protected void onStep() {
        System.err.println("PAGE_BASE_FLEET pushed");
        Instance.getInstance().push(
                new OpenPageCommand(PAGE_BASE_FLEET, null).setCheckEventFleet(true));
    }
}
