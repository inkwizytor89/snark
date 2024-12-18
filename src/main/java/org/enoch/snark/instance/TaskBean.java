package org.enoch.snark.instance;

public class TaskBean implements Runnable {

    private final String name;
    private final long interval;

    public TaskBean(String name, long interval) {
        this.name = name;
        this.interval = interval;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("Task: " + name+"("+interval+")" + ", Current Time: " + System.currentTimeMillis());
            try {
                Thread.sleep(interval); // Symuluje cykliczne działanie
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Task [" + name + "] interrupted.");
            }
        }
    }


}
