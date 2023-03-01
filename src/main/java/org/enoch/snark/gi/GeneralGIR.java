package org.enoch.snark.gi;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.model.types.ColonyType;
import org.enoch.snark.model.types.FleetDirectionType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class GeneralGIR extends GraphicalInterfaceReader {


    public List<EventFleet> readEventFleet() {
        List<EventFleet> eventFleets = new ArrayList<>();
        try {
            List<WebElement> eventHeader = wd.findElements(By.id("eventHeader"));
            if(!eventHeader.isEmpty()) {
                if(eventHeader.get(0).isDisplayed())
                    wd.findElement(By.className("event_list")).click();
            }
            List<WebElement> event_list = wd.findElements(By.className("event_list"));
            if(event_list.isEmpty()) {
                return eventFleets;
            }
            event_list.get(0).click();

            SleepUtil.sleep();
            List<WebElement> tableRows = null;
            WebDriverWait wait = new WebDriverWait(wd, 15);
            WebElement eventContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("eventContent")));
            tableRows = wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(eventContent, By.tagName(TR_TAG)));

            for (WebElement we : tableRows) {

                EventFleet event = new EventFleet();
                WebElement countDown = we.findElement(By.className("countDown"));
                event.isHostile = !countDown.findElements(By.className("hostile")).isEmpty();
                event.countDown =  countDown.getText();
                event.arrivalTime =  DateUtil.parseStringTimeToDateTime(we.findElement(By.className("arrivalTime")).getText());
                String missionText = we.findElement(By.className("missionFleet")).findElement(By.className("tooltipHTML")).getAttribute(TITLE_ATTRIBUTE);
                event.mission = Mission.convert(missionText);
                WebElement figure = we.findElement(By.className("originFleet")).findElement(By.tagName("figure"));
                event.originFleet = figure.getAttribute(CLASS_ATTRIBUTE).contains("moon") ? ColonyType.MOON : ColonyType.PLANET;
                event.coordsOrigin =  we.findElement(By.className("coordsOrigin")).getText();
                event.detailsFleet =  we.findElement(By.className("detailsFleet")).getText();
                event.iconMovement =  missionText.contains("(R)") ? FleetDirectionType.BACK : FleetDirectionType.THERE;
                WebElement destFigure = we.findElement(By.className("destFleet")).findElement(By.tagName("figure"));
                event.destFleet =  destFigure.getAttribute(CLASS_ATTRIBUTE).contains("moon") ? ColonyType.MOON : ColonyType.PLANET;
                event.destCoords =  we.findElement(By.className("destCoords")).getText();
                event.sendProbe =  we.findElement(By.className("sendProbe")).getText();
                List<WebElement> player = we.findElements(By.className("sendMail"));
                if(player.size() > 1) {
                    event.sendMail =  player.get(1).getAttribute(HREF_ATTRIBUTE);
                } else {
                    event.sendMail = "";
                }

                eventFleets.add(event);
            }
            wd.findElement(By.className("event_list")).click();
        } catch (Exception e) {
            System.err.println("Can not load EventFleet "+e.getClass().getName()+" cause:" +e.getMessage());
            return null;
        }
        return eventFleets;
    }
}
