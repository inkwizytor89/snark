package org.enoch.snark.instance.si.module.consumer.gi;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.to.HighScorePosition;
import org.enoch.snark.instance.model.to.SystemView;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class GalaxyGIR extends GraphicalInterfaceReader {

    public GalaxyGIR(GI gi) {
        super(gi);
    }

    public void updateGalaxy(SystemView systemView, List<TargetEntity> targets) {
//        List<TargetEntity> targets
        for(WebElement row : wd().findElements(By.className("galaxyRow"))) {
            // skip table headers
            if(row.findElements(By.className("cellPosition")).isEmpty()) {
                continue;
            }

            //skip lear rows
            final int position = Integer.parseInt(row.findElement(By.className("cellPosition")).getText());
            Optional<TargetEntity> targetFromDb = targets.stream()
                    .filter(t -> t.position.equals(position))
                    .findAny();
            WebElement cellPlayerName = row.findElement(By.className("cellPlayerName"));
            if(targetFromDb.isPresent() && cellPlayerName.getText().trim().isEmpty()) {
                targetFromDb.get().updated = null;
                continue;
            }
            if(cellPlayerName.getText().trim().isEmpty()) {
                continue;
            }
            List<WebElement> targetElement = cellPlayerName.findElements(By.className("tooltipRel"));
            // me on player list
            if(targetElement.isEmpty()) {
                continue;
            }
            final WebElement playerElement = targetElement.get(0);
            final String playerName = playerElement.getText().trim();
            final String playerCode = playerElement.getAttribute("rel").substring(6);
            List<WebElement> isStatus = cellPlayerName.findElements(By.tagName("pre"));
            String status = "";
            if(!isStatus.isEmpty()) {
                status = isStatus.get(0).getText();
            }
            final String alliance = row.findElement(By.className("cellAlliance")).getText();
            if(StringUtils.isEmpty(playerName) && !targetFromDb.isPresent()) {
                continue;
            }
            // nothing changed, nothing to process
            if(targetFromDb.isPresent() && status.equals(targetFromDb.get().player.status)) {
                continue;
            }
            TargetEntity entity;
            if(targetFromDb.isPresent()) {
                entity = targetFromDb.get();
            } else {
                entity = new TargetEntity();
                entity.galaxy = systemView.galaxy;
                entity.system = systemView.system;
                entity.position = position;
                entity.type = ColonyType.PLANET;
                targets.add(entity);
            }
            if(entity.player == null || entity.player.code.equals(playerCode)) {
                PlayerEntity playerEntity = new PlayerEntity();
                playerEntity.name = playerName;
                playerEntity.code = playerCode;
                playerEntity.alliance = alliance;
                playerEntity.status = status;
                playerEntity.type = setStatus(status);
                entity.player = playerEntity;
            }
        }
    }

    public static String setStatus(String status) {
        if (status.contains("A")) {
            return TargetEntity.ADMIN;
        } else if (status.contains("u")) {
            return TargetEntity.ABSENCE;
        } else if (status.contains("i") || status.contains("I")) {
            return TargetEntity.IN_ACTIVE;
        } else if (status.contains("s")) {
            return TargetEntity.WEAK;
        } else {
            return TargetEntity.NORMAL;
        }
    }
}
