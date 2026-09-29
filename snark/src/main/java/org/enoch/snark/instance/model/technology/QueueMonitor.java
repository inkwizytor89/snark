package org.enoch.snark.instance.model.technology;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class QueueMonitor {
    private LocalDateTime date;
    private Technology technology;

    public QueueMonitor() {
        clean();
    }

    public boolean isFree() {
        return LocalDateTime.now().isAfter(date);
    }

    public void clean() {
        date = LocalDateTime.MIN;
    }

    public void update(Technology technology, LocalDateTime date) {
        this.technology = technology;
        this.date = date;
    }

    @Override
    public String toString() {
        return "technology: " + technology + " time: " +date;
    }
}
