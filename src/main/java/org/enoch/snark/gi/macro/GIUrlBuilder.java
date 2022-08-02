package org.enoch.snark.gi.macro;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void openFleetView(ColonyEntity source, Planet target, Mission mission) {
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

//    public static void main(String[] args) {
//
//        Pattern pattern = Pattern.compile("^(.*)");
//        String input = "Floty:17/20\n" + "Ekspedycje: 4/7";
//        Matcher m = fleetStatusPattern.matcher(input);
//        if(m.find()) {
//            System.out.println(m.group(0));
//            System.out.println(m.group(1));
//            System.out.println(m.group(2));
//            System.out.println(m.group(3));
//            System.out.println(m.group(4));
//            System.out.println(m.group(5));
//            System.out.println(m.group(6));
//            System.out.println(m.group(7));
//            System.out.println(m.group(8));
//        }
//    }

    private void loadFleetStatus() {
        Pattern fleetStatusPattern = Pattern.compile("\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)");
        final WebElement slotsLabel = instance.session.getWebDriver().findElement(By.id("slots"));
        Matcher m = fleetStatusPattern.matcher(slotsLabel.getText());
//        final String[] split = slotsLabel.getText().split("\\s");
        if(m.find()) {
            instance.commander.setFleetStatus(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            instance.commander.setExpeditionStatus(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
        }
    }
//
//    private int returnValue(String status) {
//        return Integer.parseInt(status.split("/")[0]);
//    }
//
//    private int returnMax(String status) {
//        return Integer.parseInt(status.split("/")[1]);
//    }

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
