package org.enoch.snark.gi;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.model.HighScorePosition;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class HighScoreGIR extends GraphicalInterfaceReader {

    public static final String POINTS_AREA = "points";
    public static final String ECONOMY_AREA = "economy";
    public static final String FLEET_AREA = "fleet";

    public void loadHighScore(List<String> areas, Integer maxPages) {

        HashMap<String, HighScorePosition> highScore = new HashMap<>();
        for (String page : generatePagesToCheck(maxPages)) {
            new GIUrlBuilder().openHighScore(page);
            for (String area : areas) {
                update(highScore, area);
            }
        }

        highScore.values().forEach(highScorePosition -> {
            System.err.println(highScorePosition);
            PlayerEntity playerEntity = PlayerDAO.getInstance().find(highScorePosition.code);
            playerEntity.update(highScorePosition);
        });

    }

    private void update(HashMap<String, HighScorePosition> highScore, String area) {
        wd.findElement(By.id("typeButtons")).findElement(By.id(area)).click();
        SleepUtil.sleep();
        final WebElement ranks = wd.findElement(By.id("ranks"));
        List<WebElement> rankPositions = new WebDriverWait(wd, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(ranks, By.tagName(TR_TAG)));

        rankPositions = rankPositions.stream()
                .filter(webElement -> !webElement.getAttribute("class").trim().contains("rank"))
                .filter(webElement -> webElement.getAttribute("id").trim().length() > 8)
                .collect(Collectors.toList());

//        rankPositions.forEach(webElement -> {
//            System.err.println(webElement.getAttribute("id").substring(8)+" "+webElement.getAttribute("class").trim());
//        });

        switch (area) {
            case POINTS_AREA :
                rankPositions.forEach(webElement -> {
                    String playerCode = webElement.getAttribute("id").substring(8);
                    if(!highScore.containsKey(playerCode)) highScore.put(playerCode, new HighScorePosition(playerCode));
                    HighScorePosition highScorePosition = highScore.get(playerCode);
                    highScorePosition.points = toLong(webElement.findElement(By.className("score")).getText());
                    highScorePosition.name = webElement.findElement(By.className("playername")).getText();
                });
                break;
            case ECONOMY_AREA :
                rankPositions.forEach(webElement -> {
                    String playerCode = webElement.getAttribute("id").substring(8);
                    if(!highScore.containsKey(playerCode)) highScore.put(playerCode, new HighScorePosition(playerCode));
                    HighScorePosition highScorePosition = highScore.get(playerCode);
                    highScorePosition.economy = toLong(webElement.findElement(By.className("score")).getText());
                });
                break;
            case FLEET_AREA :
                rankPositions.forEach(webElement -> {
                    String playerCode = webElement.getAttribute("id").substring(8);
                    if(!highScore.containsKey(playerCode)) highScore.put(playerCode, new HighScorePosition(playerCode));
                    HighScorePosition highScorePosition = highScore.get(playerCode);
                    highScorePosition.fleet = toLong(webElement.findElement(By.className("score")).getText());
                });
                break;
            default:
                break;
         }
    }

    public List<String> generatePagesToCheck(Integer maxPages) {
        return IntStream.range(1, maxPages+1).boxed().map(Object::toString).collect(Collectors.toList());
    }
}
