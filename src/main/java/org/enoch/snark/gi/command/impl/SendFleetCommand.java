package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.SendFleetGIR;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.instance.model.technology.Ship;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.to.*;
import org.enoch.snark.instance.model.exception.FleetCantStart;
import org.enoch.snark.instance.model.exception.ShipDoNotExists;
import org.enoch.snark.instance.model.exception.ToStrongPlayerException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.enoch.snark.gi.command.impl.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.gi.types.Mission.ATTACK;
import static org.enoch.snark.gi.types.Mission.SPY;
import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.model.to.ShipsMap.ALL_SHIPS;

public class SendFleetCommand extends AbstractCommand {

    public static final Long TIME_BUFFER = 3L;
    protected boolean autoComplete;
    protected FleetPromise fleetPromise = new FleetPromise();
    protected Resources resources;

    public FleetEntity fleet;
    private final SendFleetGIR gir = new SendFleetGIR();

    public SendFleetCommand(FleetEntity fleet) {
        super();
        this.fleet = fleet;
    }

    public boolean prepere() {
        GIUrl.openSendFleetView(fleet.source, fleet.getDestination(), fleet.mission);
        fleet.source = ColonyDAO.getInstance().fetch(fleet.source);
        if(!fleet.source.hasEnoughShips(Ship.createShipsMap(fleet))) {
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
        }
        if(!promise().fit()) return true;
        Long durationSeconds;
        fleet.hash = this.hash();
        fleet.source = ColonyDAO.getInstance().fetch(fleet.source);
        //Scroll down till the bottom of the page
        ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(0,document.body.scrollHeight)");


        Set<Map.Entry<Ship, Long>> entries = buildShipsMap().entrySet();
        if(isAllShips() && (promise().getLeaveShipsMap() == null || promise().getLeaveShipsMap().isEmpty())) {
            gir.selectAllShips();
        } else if (promise().getShipsMap() != null) {
            promise().setSource(fleet.source);
            ShipsMap realCanToSend = promise().normalizeShipMap();
            for (Map.Entry<Ship, Long> entry : realCanToSend.entrySet()) {
                Long value = typeShip(entry.getKey(), entry.getValue());
                fleet.setShips(ShipsMap.createSingle(entry.getKey(), value));

            }
        } else { //todo: nie powinno byc wyciagania z fleetEntity statkow i wybieranie ich
            // tylko z shipMap powinno byc wybierane. Do Fleet entity powinno byc zapisywane to co zostalo ostatecznie typowane
            for (Map.Entry<Ship, Long> entry : entries) {
                typeShip(entry.getKey(), entry.getValue());
            }
        }
        try {
            next();
            SleepUtil.sleep();
        } catch (ShipDoNotExists e) {
            System.err.println("fleet.id "+fleet.id+" required on planet "+fleet.source);
            if(isAllShips()) {
                System.err.println("allShips selected");
            } else {
                for (Map.Entry<Ship, Long> entry : entries) {
                    System.err.print(entry.getKey().name() + " " + entry.getValue() + "/");
                    System.err.println(fleet.source.getShipsMap().get(entry.getKey()));
                }
            }
            fleet.start = LocalDateTime.now();
            fleet.code = 0L;
            FleetDAO.getInstance().saveOrUpdate(fleet);
            throw e;
        }


        if(!autoComplete) {
            WebElement coordsElement = webDriver.findElement(By.id("target")).findElement(By.className("coords"));
            coordsElement.findElement(By.id("galaxy")).sendKeys(fleet.targetGalaxy.toString());
            coordsElement.findElement(By.id("system")).sendKeys(fleet.targetSystem.toString());
            coordsElement.findElement(By.id("position")).sendKeys(fleet.targetPosition.toString());
            SleepUtil.pause();
            SleepUtil.sleep();
        }

        gir.setSpeed(fleet.speed);
        gir.setResources(promise().getResources(), fleet.source);

        durationSeconds = gir.parseDurationSecounds().toSecondOfDay() + TIME_BUFFER;
        fleet.start = LocalDateTime.now();
        fleet.visited = gir.parseFleetVisited();
        fleet.back = gir.parseFleetBack();

        if(webDriver.findElements(By.className("status_abbr_noob")).size() != 0) {//player is green - too weak
            Optional<TargetEntity> target = TargetDAO.getInstance().find(fleet.getTarget());
            if (target.isPresent()) {
                PlayerEntity player = target.get().player;
                player.type = TargetEntity.WEAK;
                PlayerDAO.getInstance().saveOrUpdate(player);
            } else {
                // look at galaxy to reload player
                GIUrl.openGalaxy(new SystemView(fleet.targetGalaxy, fleet.targetSystem), null);
            }
            return true;
        }

        try {
            gir.sendFleet(fleet);
        } catch(FleetCantStart e) {
            e.printStackTrace();
            Planet target = new Planet(fleet.targetGalaxy, fleet.targetSystem, fleet.targetPosition);
            System.err.println("Can not send fleet to target " + target);
            new GalaxyAnalyzeCommand(new SystemView(fleet.targetGalaxy, fleet.targetSystem))
            .setRunType(QueueRunType.NORMAL).push();
//            instance.removePlanet(new Planet(fleet.getCoordinate()));
            if(fleet.code != null) fleet.code = - fleet.code;
            clearNext();
            return true;
        } catch(ToStrongPlayerException e) {
            System.err.println(e.getMessage());
            clearNext();
            if(fleet.code != null) fleet.code = -fleet.code;
        }
        FleetDAO.getInstance().saveOrUpdate(fleet);

        if(SPY.equals(fleet.mission)) {
            Optional<TargetEntity> targetEntity = TargetDAO.getInstance().find(fleet.getTarget());
            if(targetEntity.isPresent()) {
                targetEntity.get().lastSpiedOn = fleet.visited;
                TargetDAO.getInstance().saveOrUpdate(targetEntity.get());
            }
        }

        if(ATTACK.equals(fleet.mission)) {
            Optional<TargetEntity> targetEntity = TargetDAO.getInstance().find(fleet.getTarget());
            if(targetEntity.isPresent()) {
                targetEntity.get().lastAttacked = fleet.visited;
                TargetDAO.getInstance().saveOrUpdate(targetEntity.get());
            }
        }
        updateDelayForAction(durationSeconds);
        reloadColony();
        return true;
    }

    private void reloadColony() {
        SleepUtil.sleep();
        GIUrl.openComponent(FLEETDISPATCH, fleet.source);
    }

    private void updateDelayForAction(Long durationSeconds) {
        if(isRequiredAction(DELAY_TO_FLEET_BACK)) {
            getFollowingAction().setSecondsToDelay(durationSeconds*2);
        }
    }

    public Long typeShip(Ship ship, Long count) {
        WebElement element = webDriver.findElement(By.name(ship.name()));
        if(!element.isEnabled()) {
            throw new ShipDoNotExists("Missing ships " + ship.name());
        }
        element.sendKeys(count.toString());
        return count;
    }

    public void next() {
        SleepUtil.pause();
        // to button continue to recalculate
        webDriver.findElement(By.className("planet-header")).click();

        final WebElement continueButton = webDriver.findElement(By.id("continueToFleet2"));
        if(continueButton.getAttribute("class").equals("continue off")) {
            throw new ShipDoNotExists();
        }
//        continueButton.click();


        Actions actions = new Actions(webDriver);

        actions.moveToElement(continueButton).click().perform();
    }

    public Map<Ship, Long> buildShipsMap() {
        return Ship.createShipsMap(fleet);
    }

    @Override
    public void onInterrupt() {
        super.onInterrupt();
        fleet.code = -1L;
        FleetDAO.getInstance().saveOrUpdate(fleet);
    }

    public boolean isAllShips() {
        return ALL_SHIPS.equals(promise().getShipsMap());
    }

    public FleetPromise promise() {
        return fleetPromise;
    }

    @Override
    public String toString() {
        return fleet.mission.name()+" "+fleet.getDestination()+" form "+fleet.source;
    }

}
