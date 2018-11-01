package org.enoch.snark.gi.macro;

public enum Mission {
    EXPEDITION("id=missionButton15", 15),
    COLONIZATION("id=missionButton7", 7),
    RECYCLE("id=missionButton8", 8),
    TRANSPORT("id=missionButton3", 3),
    STATIONED("id=missionButton4", 4),
    SPY("id=missionButton6", 6),
    STOP("id=missionButton5", 5),
    ATTACK("id=missionButton1", 1),
    GROUP_ATTACK("id=missionButton2", 2),
    DESTROY("id=missionButton9", 9);

    private String id;
    private int value;

    Mission(String id, int value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public int getValue() {
        return value;
    }
}
