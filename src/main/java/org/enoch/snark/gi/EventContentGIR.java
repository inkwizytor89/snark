package org.enoch.snark.gi;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.model.types.FleetDirectionType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.instance.model.types.FleetDirectionType.THERE;

public class EventContentGIR extends GraphicalInterfaceReader {

    public boolean recall(FleetPromise promise) {
        List<WebElement> toRecall = new ArrayList<>();
        try {
            List<WebElement> tableRows = activateEventTable();
            for(WebElement row : tableRows) {
                EventFleet event = createEventFleet(row);
                if (event == null) continue;
                if(isSimilar(event, promise)) {
                    toRecall.add(row);
                    System.err.println("Recall fleet "+event);
                }
            }
            for(WebElement row : toRecall) {
                row.findElement(By.className("recallFleet")).click();
                SleepUtil.sleep();
                wd.findElement(By.id("errorBoxDecisionYes")).click();
            }
            wd.findElement(By.className("event_list")).click();
        } catch (Exception e) {
            System.err.println("Can not load EventFleet "+e.getClass().getName()+" cause:" +e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean isSimilar(EventFleet row, FleetPromise promise) {
        return THERE.equals(row.iconMovement) &&
                row.mission.equals(promise.getMission()) &&
                row.getFrom().equals(promise.getSource().toPlanet()) &&
                row.getTo().equals(promise.getTarget());
    }

    public List<EventFleet> readEventFleet() {
        List<EventFleet> eventFleets;
        try {
            List<WebElement> tableRows = activateEventTable();
            eventFleets = readTableRows(tableRows);
            List<WebElement> eventList = wd.findElements(By.className("event_list"));
            if (!eventList.isEmpty()) {
                eventList.getFirst().click();
            }
        } catch (Exception e) {
            System.err.println("Can not load EventFleet "+e.getClass().getName()+" cause:" +e.getMessage());
            return null;
        }
        return eventFleets;
    }

    private List<WebElement> activateEventTable() {
        SleepUtil.pause(3);
        List<WebElement> eventHeader = wd.findElements(By.id("eventHeader"));
        if(!eventHeader.isEmpty()) {
            if(eventHeader.get(0).isDisplayed())
                wd.findElement(By.className("event_list")).click();
        }
        List<WebElement> event_list = wd.findElements(By.className("event_list"));
        if(event_list.isEmpty()) {
            return new ArrayList<>();
        }
        event_list.get(0).click();
        SleepUtil.sleep();
        List<WebElement> tableRows = null;
        WebDriverWait wait = new WebDriverWait(wd, Duration.ofSeconds(15));
        WebElement eventContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("eventContent")));
        return wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(eventContent, By.tagName(TR_TAG)));
    }

    private static List<EventFleet> readTableRows(List<WebElement> tableRows) {
        List<EventFleet> eventFleets = new ArrayList<>();
        for (WebElement we : tableRows) {
            EventFleet event = createEventFleet(we);
            if (event == null) continue;
            eventFleets.add(event);
        }
        return eventFleets;
    }

    private static EventFleet createEventFleet(WebElement we) {
        EventFleet event = new EventFleet();
        WebElement countDown = we.findElement(By.className("countDown"));
        event.isHostile = !countDown.findElements(By.className("hostile")).isEmpty();
        event.countDown =  countDown.getText();
        if(event.countDown.isEmpty()) return null;
        event.arrivalTime =  DateUtil.parseStringTimeToDateTime(we.findElement(By.className("arrivalTime")).getText());
        String missionText = "UNKNOWN";
        List<WebElement> missionTypeWE = we.findElement(By.className("missionFleet")).findElements(By.className("tooltipHTML"));
        if(!missionTypeWE.isEmpty()) {
            missionText = missionTypeWE.get(0).getAttribute(TITLE_ATTRIBUTE);
        } else {
            missionText = we.findElement(By.className("missionFleet")).findElement(By.className("tooltip")).getAttribute(TITLE_ATTRIBUTE);
        }
        event.mission = Mission.convert(missionText);
        List<WebElement> figure = we.findElement(By.className("originFleet")).findElements(By.tagName("figure"));
        if(!figure.isEmpty())
            event.originFleet = figure.get(0).getAttribute(CLASS_ATTRIBUTE).contains("moon") ? ColonyType.MOON : ColonyType.PLANET;
        else event.originFleet = ColonyType.UNKNOWN;
        event.coordsOrigin =  we.findElement(By.className("coordsOrigin")).getText();
        event.detailsFleet =  we.findElement(By.className("detailsFleet")).getText();
        event.iconMovement =  missionText.contains("(R)") ? FleetDirectionType.BACK : THERE;
        List<WebElement> destFigure = we.findElement(By.className("destFleet")).findElements(By.tagName("figure"));
        if(!destFigure.isEmpty())
            event.destFleet =  destFigure.get(0).getAttribute(CLASS_ATTRIBUTE).contains("moon") ? ColonyType.MOON : ColonyType.PLANET;
        else event.destFleet = ColonyType.UNKNOWN;
        event.destCoords =  we.findElement(By.className("destCoords")).getText();
        List<WebElement> sendProbe = we.findElements(By.className("sendProbe"));
        if(!sendProbe.isEmpty()) event.sendProbe =  sendProbe.get(0).getText();
        List<WebElement> player = we.findElements(By.className("sendMail"));
        if(player.size() > 1) {
            event.sendMail =  player.get(1).getAttribute(HREF_ATTRIBUTE);
        } else {
            event.sendMail = "";
        }
        return event;
    }
}
