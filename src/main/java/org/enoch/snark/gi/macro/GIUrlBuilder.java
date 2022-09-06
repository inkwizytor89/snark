package org.enoch.snark.gi.macro;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.concurrent.TimeUnit;
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
    public static final String PAGE_MESSAGES = "messages";
    public static final String PAGE_SPACE = "galaxy";

    private Instance instance;

    public GIUrlBuilder() {
        instance = Instance.getInstance();
    }

    public GIUrlBuilder(Instance instance) {
        this();
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
        instance.session.getWebDriver().get(builder);

        loadFleetStatus();
    }

    public void updateFleetStatus() {
        String builder = instance.universe.url + "?" +
                PAGE_TERM + PAGE_INGAME + "&" +
                COMPONENT_TERM + PAGE_BASE_FLEET ;
        instance.session.getWebDriver().get(builder);

        loadFleetStatus();
    }

    private void loadFleetStatus() {
        Pattern fleetStatusPattern = Pattern.compile("\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)");
        final WebElement slotsLabel = instance.session.getWebDriver().findElement(By.id("slots"));
        Matcher m = fleetStatusPattern.matcher(slotsLabel.getText());
        if(m.find()) {
            instance.commander.setFleetStatus(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            instance.commander.setExpeditionStatus(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
        }
    }

    public void openMessages() {
        String builder = instance.universe.url + "?" +
                PAGE_TERM + PAGE_MESSAGES;
        instance.session.getWebDriver().get(builder);
    }

    public void openOverview() {
        this.openOverview(null);
    }

    public void openOverview(ColonyEntity source) {
        StringBuilder builder = new StringBuilder( instance.universe.url + "?");
        builder.append(PAGE_TERM + PAGE_INGAME + "&");
        builder.append(COMPONENT_TERM + PAGE_OVERVIEW);
        if(source != null) {
            builder.append("&cp=" + source.cp);
        }
        instance.session.getWebDriver().get(builder.toString());
    }

    public void open(ColonyEntity source, String page) {
        StringBuilder builder = new StringBuilder( instance.universe.url + "?");
        builder.append(PAGE_TERM + PAGE_INGAME + "&");
        builder.append(COMPONENT_TERM + page);
        if(source != null) {
            builder.append("&cp=" + source.cp);
        }
        instance.session.getWebDriver().get(builder.toString());
        if(PAGE_RESOURCES.equals(page)) {
            instance.gi.updateResources(source);
        } else if(PAGE_FACILITIES.equals(page)) {
            instance.gi.updateFacilities(source);
        } else if(PAGE_LIFEFORM.equals(page)) {
            instance.gi.updateLifeform(source);
        }
    }

    public void open(String page, PlayerEntity player) {
        StringBuilder builder = new StringBuilder( instance.universe.url + "?");
        builder.append(PAGE_TERM + PAGE_INGAME + "&");
        builder.append(COMPONENT_TERM + page);
        instance.session.getWebDriver().get(builder.toString());
        if(PAGE_RESEARCH.equals(page) && player != null) {
            instance.gi.updateResearch(player);
        }
    }

    public void openGalaxy(int galaxy, int system) {
        openGalaxy(galaxy, system, 1);
    }

    public void openGalaxy(int galaxy, int system, int position) {
        String builder = instance.universe.url + "?" +
                PAGE_TERM + PAGE_INGAME + "&" +
                COMPONENT_TERM + PAGE_SPACE +
                "&galaxy=" + galaxy +
                "&system=" + system +
                "&position=" + position;
        instance.session.getWebDriver().get(builder);
    }
}
