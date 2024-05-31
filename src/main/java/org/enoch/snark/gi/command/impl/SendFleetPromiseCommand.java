package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.common.WaitingThread;
import org.enoch.snark.db.dao.FleetDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.SendFleetGIR;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.exception.FleetCantStart;
import org.enoch.snark.instance.model.exception.ShipDoNotExists;
import org.enoch.snark.instance.model.exception.ToStrongPlayerException;
import org.enoch.snark.instance.model.to.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.enoch.snark.gi.command.impl.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.gi.types.Mission.ATTACK;
import static org.enoch.snark.gi.types.Mission.SPY;
import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.model.to.ShipsMap.ALL_SHIPS;

public class SendFleetPromiseCommand extends AbstractCommand {

    public static final Long TIME_BUFFER = 3L;
    protected FleetPromise promise;

//    public FleetEntity fleet;
    private final SendFleetGIR gir = new SendFleetGIR();

    public SendFleetPromiseCommand(FleetPromise promise) {
        super();
        this.promise = promise;
    }

    @Override
    public boolean execute() {
        GIUrl.openSendFleetView(promise.getSource(), promise.getTarget(), promise.getMission());

        if(!promise().fit()) return true;
        Long durationSeconds;
        //Scroll down till the bottom of the page
        ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(0,document.body.scrollHeight)");


        if(isAllShips() && (promise().getLeaveShipsMap() == null || promise().getLeaveShipsMap().isEmpty())) {
            gir.selectAllShips();
        } else if (promise().getShipsMap() != null) {
            promise().setSource(promise.getSource());
            ShipsMap realCanToSend = promise().normalizeShipMap();
            for (Map.Entry<ShipEnum, Long> entry : realCanToSend.entrySet()) {
                Long value = typeShip(entry.getKey(), entry.getValue());
                promise.setShipsMap(ShipsMap.createSingle(entry.getKey(), value));

            }
        }
        try {
            next();
            SleepUtil.sleep();
        } catch (ShipDoNotExists e) {
            System.err.println("promise required on planet "+promise.getSource());
            throw e;
        }

        gir.setSpeed(promise.getSpeed());
        gir.setResources(promise.getResources(), promise.getSource());

        FleetEntity fleet = new FleetEntity(promise);
        fleet.hash = this.hash();

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
                GIUrl.openGalaxy(new SystemView(promise.getTarget().galaxy, promise.getTarget().system), null);
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

        reloadColonyAfterFleetIsBack(durationSeconds);
        updateDelayForAction(durationSeconds);
        reloadColony();
        return true;
    }

    private void reloadColonyAfterFleetIsBack(Long durationSeconds) {
        if(promise.getMission().isComingBack()) {
            OpenPageCommand command = new OpenPageCommand(FLEETDISPATCH, promise.getSource());
            long secondsToBack = durationSeconds * 2;
            if(Mission.EXPEDITION.equals(promise.getMission())) {
                secondsToBack+=3600;
                System.out.println("Expedition is probably back "+LocalDateTime.now().plusSeconds(secondsToBack));
            }
            new WaitingThread(new FollowingAction(command, secondsToBack)).start();
        }
    }

    private void reloadColony() {
        SleepUtil.sleep();
        GIUrl.openComponent(FLEETDISPATCH, promise().getSource());
    }

    private void updateDelayForAction(Long durationSeconds) {
        if(isRequiredAction(DELAY_TO_FLEET_BACK)) {
            getFollowingAction().setSecondsToDelay(durationSeconds*2);
        }
    }

    public Long typeShip(ShipEnum shipEnum, Long count) {
        WebElement element = webDriver.findElement(By.name(shipEnum.getId()));
        if(!element.isEnabled()) {
            throw new ShipDoNotExists("Missing ships " + shipEnum.getId());
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

    public boolean isAllShips() {
        return ALL_SHIPS.equals(promise().getShipsMap());
    }

    public FleetPromise promise() {
        return promise;
    }

    @Override
    public String toString() {
        return promise.getMission().name()+" "+promise.getTarget()+" form "+promise.getSource();
    }

    public void generateHash(String hashPrefix, String code) {
        String prefix = hashPrefix != null ? hashPrefix+"_" : "";
        hash(prefix+promise.getSource()+"_"+promise.getMission()+"_"+promise.getTarget()+"_"+code);
    }
}