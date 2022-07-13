package org.enoch.snark.gi.macro;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.Instance;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class GIUrlBuilder {

    public static final String COMPONENT_TERM = "component=";
    public static final String PAGE_TERM = "page=";

    private static final String PAGE_INGAME = "ingame";
    private static final String PAGE_OVERVIEW = "overview";
    private static final String PAGE_BASE_FLEET = "fleetdispatch";
    private static final String PAGE_MESSAGES = "messages";
    private static final String PAGE_SPACE = "galaxy";

    private Instance instance;

    public GIUrlBuilder(Instance instance) {
        this.instance = instance;
    }

    public void openFleetView(ColonyEntity source, TargetEntity target, Mission mission) {
        String builder = instance.universeEntity.url + "?" +
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
        String builder = instance.universeEntity.url + "?" +
                PAGE_TERM + PAGE_INGAME + "&" +
                COMPONENT_TERM + PAGE_BASE_FLEET ;
        instance.session.getWebDriver().get(builder);

        loadFleetStatus();
    }

    private void loadFleetStatus() {
        final WebElement slotsLabel = instance.session.getWebDriver().findElement(By.id("slots"));
        final String[] split = slotsLabel.getText().split("\\s");
        instance.commander.setFleetStatus(returnValue(split[2]), returnMax(split[2]));
        instance.commander.setExpeditionStatus(returnValue(split[4]), returnMax(split[4]));
    }

    private int returnValue(String status) {
        return Integer.parseInt(status.split("/")[0]);
    }

    private int returnMax(String status) {
        return Integer.parseInt(status.split("/")[1]);
    }

    public void openMessages() {
        String builder = instance.universeEntity.url + "?" +
                PAGE_TERM + PAGE_MESSAGES;
        instance.session.getWebDriver().get(builder);
    }

    public void openOverview() {
        String builder = instance.universeEntity.url + "?" +
                PAGE_TERM + PAGE_INGAME + "&" +
                COMPONENT_TERM + PAGE_OVERVIEW;
        instance.session.getWebDriver().get(builder);
    }

    public void openGalaxy(int galaxy, int system) {
        openGalaxy(galaxy, system, 1);
    }

    public void openGalaxy(int galaxy, int system, int position) {
        String builder = instance.universeEntity.url + "?" +
                PAGE_TERM + PAGE_INGAME + "&" +
                COMPONENT_TERM + PAGE_SPACE +
                "&galaxy=" + galaxy +
                "&system=" + system +
                "&position=" + position;
        instance.session.getWebDriver().get(builder);
    }
}
