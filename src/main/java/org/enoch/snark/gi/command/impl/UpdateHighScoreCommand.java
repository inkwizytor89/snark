package org.enoch.snark.gi.command.impl;

import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.gi.HighScoreGIR;
import org.enoch.snark.instance.Instance;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.enoch.snark.db.entity.CacheEntryEntity.HIGH_SCORE;
import static org.enoch.snark.gi.HighScoreGIR.*;
import static org.enoch.snark.instance.config.Config.HIGH_SCORE_PAGES;
import static org.enoch.snark.instance.config.Config.MAIN;

public class UpdateHighScoreCommand extends AbstractCommand {

    private HighScoreGIR gir = new HighScoreGIR();
    private List<String> areasToCheck;
    private Integer maxPages;

    public UpdateHighScoreCommand(Integer maxPages) {
        this(Arrays.asList(POINTS_AREA, ECONOMY_AREA, FLEET_AREA), maxPages);
    }

    public UpdateHighScoreCommand(List<String> areasToCheck, Integer maxPages) {
        super();
        this.areasToCheck = areasToCheck;
        this.maxPages = maxPages;
        this.addTag(HIGH_SCORE);
    }

    public List<String> getAreas() {
        areasToCheck = Arrays.asList(POINTS_AREA, ECONOMY_AREA, FLEET_AREA);
        return areasToCheck;
    }

    @Override
    public boolean execute() {

        gir.loadHighScore(getAreas(), maxPages);

        CacheEntryDAO.getInstance().setValue(HIGH_SCORE, LocalDateTime.now().toString());
        return true;
    }

    @Override
    public String toString() {
        return HIGH_SCORE + "_Command";
    }
}
