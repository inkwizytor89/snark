package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.common.WaitingThread;
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
import org.enoch.snark.model.Resources;
import org.enoch.snark.model.SystemView;
import org.enoch.snark.model.exception.FleetCantStart;
import org.enoch.snark.model.exception.ShipDoNotExists;
import org.enoch.snark.model.exception.ToStrongPlayerException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.Adler32;

import static org.enoch.snark.gi.command.impl.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;
import static org.enoch.snark.model.Resources.everything;

public class SendFleetCommand extends GICommand {

    public static final Long TIME_BUFFOR = 3L;
    public Mission mission;
    protected GIUrlBuilder giUrlBuilder;
    protected boolean autoComplete;
    protected Resources resources;
    protected boolean allShips;

    public FleetEntity fleet;
    private final SendFleetGIR gir;

    public SendFleetCommand(FleetEntity fleet) {
        super();
        this.priority = 40;
        this.fleet = fleet;
        this.mission = fleet.mission;
        gir = new SendFleetGIR();
        giUrlBuilder = new GIUrlBuilder();
    }

    public boolean prepere() {
        giUrlBuilder.openFleetView(fleet.source, fleet.getDestination(), mission);
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
        Long durationSeconds;
        fleet.source = ColonyDAO.getInstance().fetch(fleet.source);
        //Scroll down till the bottom of the page
        ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(0,document.body.scrollHeight)");


        Set<Map.Entry<ShipEnum, Long>> entries = buildShipsMap().entrySet();
        if(allShips) {
            gir.selectAllShips();
        } else {
            for (Map.Entry<ShipEnum, Long> entry : entries) {
                typeShip(entry.getKey(), entry.getValue());
            }
        }
        try {
            next();
            SleepUtil.sleep();
        } catch (ShipDoNotExists e) {
            System.err.println("fleet.id "+fleet.id+" required on planet "+fleet.source);
            if(allShips) {
                System.err.println("allShips selected");
            } else {
                for (Map.Entry<ShipEnum, Long> entry : entries) {
                    System.err.print(entry.getKey().getId() + " " + entry.getValue() + "/");
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

        gir.setSpeed(fleet);
        gir.setResources(resources, fleet);

        durationSeconds = gir.parseDurationSecounds().toSecondOfDay() + TIME_BUFFOR;
        fleet.start = LocalDateTime.now();
        fleet.visited = gir.parseFleetVisited();
        fleet.back = gir.parseFleetBack();

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

        try {
            gir.sendFleet(fleet);
        } catch(FleetCantStart e) {
            e.printStackTrace();
            Planet target = new Planet(fleet.targetGalaxy, fleet.targetSystem, fleet.targetPosition);
            System.err.println("Can not send fleet to target " + target);
            new GalaxyAnalyzeCommand(new SystemView(fleet.targetGalaxy, fleet.targetSystem)).push();
//            instance.removePlanet(new Planet(fleet.getCoordinate()));
            if(fleet.code != null) fleet.code = - fleet.code;
            clearNext();
            return true;
        } catch(ToStrongPlayerException e) {
            System.err.println(e);
            clearNext();
            if(fleet.code != null) fleet.code = -fleet.code;
        }
        FleetDAO.getInstance().saveOrUpdate(fleet);
        reloadColonyAfterFleetIsBack(durationSeconds);
        updateDelayForAction(durationSeconds);
        reloadColony();
        return true;
    }

    private void reloadColonyAfterFleetIsBack(Long durationSeconds) {
        if(mission.isComingBack()) {
            OpenPageCommand command = new OpenPageCommand(PAGE_BASE_FLEET, fleet.source);
            long secondsToBack = durationSeconds * 2;
            if(Mission.EXPEDITION.equals(fleet.mission)) {
                secondsToBack+=3600;
                System.out.println("Expedition is probably back "+LocalDateTime.now().plusSeconds(secondsToBack)+
                        " back is "+fleet.back);
            }
            new WaitingThread(new FollowingAction(command, secondsToBack)).start();
        }
    }

    private void reloadColony() {
        SleepUtil.sleep();
        new GIUrlBuilder().openComponent(PAGE_BASE_FLEET, fleet.source);
    }

    private void updateDelayForAction(Long durationSeconds) {
        if(isRequiredAction(DELAY_TO_FLEET_BACK)) {
            getFollowingAction().setSecondsToDelay(durationSeconds*2);
        }
    }

    public void typeShip(ShipEnum shipEnum, Long count) {
        WebElement element = webDriver.findElement(By.name(shipEnum.getId()));
        if(!element.isEnabled()) {
            throw new ShipDoNotExists("Missing ships " + shipEnum.getId());
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

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public void setAllShips(boolean allShips) {
        this.allShips = allShips;
    }

    @Override
    public String toString() {
        return mission.name()+" "+fleet.getDestination()+" form "+fleet.source;
    }
}
