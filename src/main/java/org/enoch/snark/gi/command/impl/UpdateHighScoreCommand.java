package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.HighScoreGIR;

import java.util.Arrays;

import static org.enoch.snark.db.entity.CacheEntryEntity.HIGH_SCORE;

public class UpdateHighScoreCommand extends AbstractCommand {

    private HighScoreGIR gir = new HighScoreGIR();

    public UpdateHighScoreCommand() {
        super();
        this.addTag(HIGH_SCORE);
    }

    @Override
    public boolean execute() {
        for(String area : Arrays.asList("xxx", "yyy", "zzz")){
            update(area);
        }
        return true;
    }

    private void update(String area) {
        for(String page : Arrays.asList("1", "2")) {
            // openHighScore area page
            gir.update(area, page);
        }
    }

    @Override
    public String toString() {
        return HIGH_SCORE + "_Command";
    }
}
