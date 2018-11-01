package org.enoch.snark.module;

import org.enoch.snark.instance.Instance;

import java.util.Date;
import java.util.logging.Logger;

public abstract class AbstractModule implements Comparable<AbstractModule> {

    protected static final Logger log = Logger.getLogger( AbstractModule.class.getName() );

    protected Date readyOn = new Date();
    private ModuleStatus status = ModuleStatus.WAITING;
    protected Integer priority = 1000;
    private String name = this.getClass().getName();
    protected Instance instance;

    public AbstractModule(Instance instance) {
        this.instance = instance;
    }

    @Override
    public int compareTo(AbstractModule o) {
        return priority.compareTo(o.priority);
    }

    public boolean isReady() {
        return new Date().after(readyOn) && status.equals(ModuleStatus.WAITING);
    }

    public abstract void run();

    public Date getReadyOn() {
        return readyOn;
    }

    public String getName() {
        return name;
    }

    public ModuleStatus getStatus() {
        return status;
    }

    protected void setStatus(ModuleStatus status) {
        this.status = status;
    }

    public boolean isInProgress() {
        return this.status.equals(ModuleStatus.IN_PROGRESS);
    }
}
