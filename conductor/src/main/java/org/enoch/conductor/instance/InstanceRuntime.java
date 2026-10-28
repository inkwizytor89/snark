package org.enoch.conductor.instance;

public class InstanceRuntime {

    private Process process;
    private boolean online;
    private long pid;
    private volatile boolean workerRegistered;
    private volatile boolean restartRequested;
    private volatile long disconnectedSince;
    private volatile long startedAt;

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public boolean hasWorkerRegistered() {
        return workerRegistered;
    }

    public void setWorkerRegistered(boolean workerRegistered) {
        this.workerRegistered = workerRegistered;
    }

    public boolean isRestartRequested() {
        return restartRequested;
    }

    public void setRestartRequested(boolean restartRequested) {
        this.restartRequested = restartRequested;
    }

    public long getDisconnectedSince() {
        return disconnectedSince;
    }

    public void setDisconnectedSince(long disconnectedSince) {
        this.disconnectedSince = disconnectedSince;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }
}