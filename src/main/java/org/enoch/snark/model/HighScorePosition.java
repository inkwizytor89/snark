package org.enoch.snark.model;

public class HighScorePosition {
    public String code;
    public String name;
    public Long points;
    public Long economy;
    public Long fleet;
    public Long ships;

    public HighScorePosition(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code + "\t" + name + "\t" + points + "\t" + economy + "\t"+ fleet + "\t"+ ships;
    }
}
