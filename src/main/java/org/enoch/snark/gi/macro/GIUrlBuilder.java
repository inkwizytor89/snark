package org.enoch.snark.gi.macro;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.instance.commander.QueueManger;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SystemView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GIUrlBuilder {

    public static final String COMPONENT_TERM = "component=";
    public static final String PAGE_TERM = "page=";

    public static final String PAGE_INGAME = "ingame";
    public static final String PAGE_OVERVIEW = "overview";
    public static final String PAGE_RESOURCES = "supplies";
    public static final String PAGE_LIFEFORM = "lfbuildings";
    public static final String PAGE_FACILITIES = "facilities";
    public static final String PAGE_RESEARCH = "research";
    public static final String PAGE_BASE_FLEET = "fleetdispatch";
    public static final String PAGE_DEFENSES = "defenses";
    public static final String PAGE_MESSAGES = "messages";
    public static final String PAGE_SPACE = "galaxy";

    private Instance instance;

    private boolean checkEventFleet = false;

    public GIUrlBuilder() {
        instance = Instance.getInstance();
    }

    public void openFleetView(ColonyEntity source, Planet target, Mission mission) {
        String builder = instance.universe.url + "?" +
                PAGE_TERM + PAGE_INGAME + "&" +
                COMPONENT_TERM + PAGE_BASE_FLEET +
                "&cp=" + source.cp +
                "&galaxy=" + target.galaxy +
                "&system=" + target.system +
                "&position=" + target.position +
                "&type=1&mission=" + mission.getValue();
        Instance.session.getWebDriver().get(builder);

        updateColony(source);
        loadFleetStatus();
        Instance.getInstance().gi.updateFleet(source);
    }

    public void loadFleetStatus() {
        Pattern fleetStatusPattern = Pattern.compile("\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)");
        final WebElement slotsLabel = instance.session.getWebDriver().findElement(By.id("slots"));
        Matcher m = fleetStatusPattern.matcher(slotsLabel.getText());
        if(m.find()) {
            Instance.commander.setFleetStatus(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            int expeditionCount = Integer.parseInt(m.group(3));
            Instance.commander.setExpeditionStatus(expeditionCount, Integer.parseInt(m.group(4)));
        }
    }

    public void openMessages() {
        String builder = Instance.universe.url + "?" +
                PAGE_TERM + PAGE_MESSAGES;
        Instance.session.getWebDriver().get(builder);
    }

    public void open(String page, ColonyEntity colony) {
        if(colony == null) {
            colony = instance.lastVisited;
            if(colony == null) colony = ColonyDAO.getInstance().getOldestUpdated();
        }
        StringBuilder builder = new StringBuilder( instance.universe.url + "?");
        builder.append(PAGE_TERM + PAGE_INGAME + "&");
        builder.append(COMPONENT_TERM + page);
        builder.append("&cp=" + colony.cp);
        instance.session.getWebDriver().get(builder.toString());
        instance.lastVisited = colony;

        updateColony(colony);
        if (PAGE_RESOURCES.equals(page)) {
            instance.gi.updateResourcesProducers(colony);
            instance.gi.updateQueue(colony, QueueManger.BUILDING);
            instance.gi.updateQueue(colony, QueueManger.SHIPYARD);
        } else if (PAGE_FACILITIES.equals(page)) {
            instance.gi.updateFacilities(colony);
            instance.gi.updateQueue(colony, QueueManger.BUILDING);
        } else if (PAGE_LIFEFORM.equals(page) && colony.isPlanet) {
            instance.gi.updateLifeform(colony);
            instance.gi.updateQueue(colony, QueueManger.LIFEFORM_BUILDINGS);
        } else if (PAGE_BASE_FLEET.equals(page)) {
            if (instance.commander != null) {
                loadFleetStatus();
            }
            Instance.getInstance().gi.updateFleet(colony);
        } else if (PAGE_DEFENSES.equals(page)) {
            Instance.getInstance().gi.updateDefence(colony);
            instance.gi.updateQueue(colony, QueueManger.SHIPYARD);
        }
        if (checkEventFleet) {
            Navigator.getInstance().informAboutEventFleets(Instance.gi.readEventFleet());
        }
    }

    public void updateColony(ColonyEntity colony) {
        instance.gi.updateResources(colony);
        colony.updated = LocalDateTime.now();
        colony.save();
    }

    public void openWithPlayerInfo(String page, PlayerEntity player) {
        StringBuilder builder = new StringBuilder( instance.universe.url + "?");
        builder.append(PAGE_TERM + PAGE_INGAME + "&");
        builder.append(COMPONENT_TERM + page);
        instance.session.getWebDriver().get(builder.toString());
        if(PAGE_RESEARCH.equals(page) && player != null) {
            instance.gi.updateResearch(player);
            instance.gi.updateQueue(instance.getMainColony(), QueueManger.RESEARCH);
        }
    }

    public void openGalaxy(SystemView systemView, ColonyEntity colony) {
        if(colony == null) {
            colony = instance.lastVisited;
            if(colony == null) colony = ColonyDAO.getInstance().getOldestUpdated();
        }
        String builder = Instance.universe.url + "?" +
                PAGE_TERM + PAGE_INGAME + "&" +
                COMPONENT_TERM + PAGE_SPACE +
                "&galaxy=" + systemView.galaxy +
                "&system=" + systemView.system +
                "&cp=" + colony.cp;
        Instance.session.getWebDriver().get(builder);
        instance.lastVisited = colony;
        updateColony(colony);
        Instance.gi.updateGalaxy(systemView);
    }

    public GIUrlBuilder setCheckEventFleet(boolean checkEventFleet) {
        this.checkEventFleet = checkEventFleet;
        return this;
    }
}
