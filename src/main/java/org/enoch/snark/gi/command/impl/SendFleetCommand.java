package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.SendFleetGIR;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SystemView;
import org.enoch.snark.model.exception.FleetCantStart;
import org.enoch.snark.model.exception.ShipDoNotExists;
import org.enoch.snark.model.exception.ToStrongPlayerException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.enoch.snark.gi.command.impl.CommandType.PRIORITY_REQUIERED;
import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;

public class SendFleetCommand extends GICommand {

    public Mission mission;
    protected GIUrlBuilder giUrlBuilder;
    protected boolean autoComplete;

    public FleetEntity fleet;
    private final SendFleetGIR gir;

    public SendFleetCommand(FleetEntity fleet) {
        super(PRIORITY_REQUIERED);
        this.priority = 40;
        this.fleet = fleet;
        this.mission = fleet.mission;
        gir = new SendFleetGIR();
        giUrlBuilder = new GIUrlBuilder();
    }

    public boolean prepere() {
        giUrlBuilder.openFleetView(fleet.source, new Planet(fleet.getCoordinate()), mission);
        if(!fleet.source.hasEnoughShips(ShipEnum.createShipsMap(fleet))) {
            if(fleet.code == null) {
                fleet.code = 0L;
            } else {
                fleet.code = - fleet.code;
            }
            fleet.start = fleet.back = LocalDateTime.now();
            FleetDAO.getInstance().saveOrUpdate(fleet);
            return false;
        }
        autoComplete = true;
        return true;
    }

    @Override
    public boolean execute() {
        if(fleet.visited != null || fleet.back != null) {
            System.err.println("Fleet already send "+fleet);
            return true;
        }

        if(!prepere()) {
            return true;
        };
        fleet.source = ColonyDAO.getInstance().fetch(fleet.source);
        //Scroll down till the bottom of the page
        ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(0,document.body.scrollHeight)");

        Set<Map.Entry<ShipEnum, Long>> entries = buildShipsMap().entrySet();
        for(Map.Entry<ShipEnum, Long> entry : entries) {
            typeShip(entry.getKey(), entry.getValue());
        }
        try {
            next();
        } catch (ShipDoNotExists e) {
            System.err.println("fleet.id "+fleet.id+" required on planet "+fleet.source);
            for(Map.Entry<ShipEnum, Long> entry : entries) {
                System.err.print(entry.getKey().getId()+" "+entry.getValue()+"/");
                        System.err.println(fleet.source.getShipsMap().get(entry.getKey()));
            }

            fleet.start = LocalDateTime.now();
            fleet.code = 0L;
            FleetDAO.getInstance().saveOrUpdate(fleet);
            throw e;
        }


        SleepUtil.pause();
        if(!autoComplete) {
            WebElement coordsElement = webDriver.findElement(By.id("target")).findElement(By.className("coords"));
            coordsElement.findElement(By.id("galaxy")).sendKeys(fleet.targetGalaxy.toString());
            coordsElement.findElement(By.id("system")).sendKeys(fleet.targetSystem.toString());
            coordsElement.findElement(By.id("position")).sendKeys(fleet.targetPosition.toString());
            SleepUtil.pause();
            SleepUtil.sleep();
        }

        gir.setResources(fleet);
        gir.setSpeed(fleet);

        final String duration = instance.gi.findElement("span", "id", "duration", "").getText();
        //Text '' could not be parsed at index 0 - popular error, shoud wait for not null time
        final LocalTime durationTime = DateUtil.parseDuration(duration);
         String arrivalTimeString = webDriver.findElement(By.id("arrivalTime")).getText();
        if(arrivalTimeString.contains("-")) {
            SleepUtil.sleep();
            arrivalTimeString = webDriver.findElement(By.id("arrivalTime")).getText();
        }
        fleet.start = LocalDateTime.now();
        fleet.visited = DateUtil.parseToLocalDateTime(arrivalTimeString);
        final String returnTimeString = webDriver.findElement(By.id("returnTime")).getText();
        fleet.back = DateUtil.parseToLocalDateTime(returnTimeString);
//        setSecoundToDelayAfterCommand(durationTime.toSecondOfDay()+ 5L);
//        fleetSelector.next();
        if(webDriver.findElements(By.className("status_abbr_noob")).size() != 0) {//player is green - too weak
            Optional<TargetEntity> target = TargetDAO.getInstance().find(fleet.targetGalaxy, fleet.targetSystem, fleet.targetPosition);
            if (target.isPresent()) {
                PlayerEntity player = target.get().player;
                player.type = TargetEntity.WEAK;
                PlayerDAO.getInstance().saveOrUpdate(player);
            } else {
                // look at galaxy to reload player
                giUrlBuilder.openGalaxy(new SystemView(fleet.targetGalaxy, fleet.targetSystem), null);
            }
            return true;
        }
        if(Mission.SPY.equals(mission)) {
            int secondToCheck = durationTime.toSecondOfDay()+3;
            setSecondToDelayAfterCommand(secondToCheck*2);
            setAfterCommand(new OpenPageCommand(PAGE_BASE_FLEET, fleet.source));
        }

        try {
            gir.sendFleet(fleet);
        } catch(FleetCantStart e) {
            e.printStackTrace();
            Planet target = new Planet(fleet.targetGalaxy, fleet.targetSystem, fleet.targetPosition);
            System.err.println("Can not send fleer to target " + target);
            instance.push(new GalaxyAnalyzeCommand(new SystemView(fleet.targetGalaxy, fleet.targetSystem)));
//            instance.removePlanet(new Planet(fleet.getCoordinate()));
            if(fleet.code != null) fleet.code = - fleet.code;
            setAfterCommand(null);
            return true;
        }
        catch(ToStrongPlayerException e) {
            System.err.println(e);
            setAfterCommand(null);
            if(fleet.code != null) fleet.code = -fleet.code;
        }
        FleetDAO.getInstance().saveOrUpdate(fleet);
        SleepUtil.secondsToSleep(1); //without it many strange problems with send fleet - random active planet

        //open after pause to wait for game to reload fleet and expedition statuses
        new GIUrlBuilder().open(PAGE_BASE_FLEET, fleet.source);
//        int expeditionCount = instance.commander.getExpeditionCount();
//        System.err.println("Sent fleet and read expedition count to "+ expeditionCount);
        return true;
    }

    public void typeShip(ShipEnum shipEnum, Long count) {
        WebElement element = webDriver.findElement(By.name(shipEnum.getId()));
        if(!element.isEnabled()) {
            throw new ShipDoNotExists("Missings ships " + shipEnum.getId());
        }
        element.sendKeys(count.toString());
    }

    public void next() {
        SleepUtil.pause();
        // to button continue to recalculate
        session.getWebDriver().findElement(By.className("planet-header")).click();

        final WebElement continueButton = session.getWebDriver().findElement(By.id("continueToFleet2"));
        if(continueButton.getAttribute("class").equals("continue off")) {
            throw new ShipDoNotExists();
        }
//        continueButton.click();


        Actions actions = new Actions(session.getWebDriver());

        actions.moveToElement(continueButton).click().perform();
    }

    public Map<ShipEnum, Long> buildShipsMap() {
        return ShipEnum.createShipsMap(fleet);
    }

    @Override
    public void onInterrupt() {
        super.onInterrupt();
        fleet.code = -1L;
        FleetDAO.getInstance().saveOrUpdate(fleet);
    }

    @Override
    public String toString() {
        return mission.name()+" "+fleet.getCoordinate()+" form "+fleet.source;
    }
}
