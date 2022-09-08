package org.enoch.snark.instance.ommander;

import java.time.LocalDateTime;

public class QueueMonitor {
    private LocalDateTime date;

    public QueueMonitor() {
        clean();
    }

    public boolean isFree() {
        return LocalDateTime.now().isAfter(date);
    }

    public void clean() {
        date = LocalDateTime.now();
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

}
