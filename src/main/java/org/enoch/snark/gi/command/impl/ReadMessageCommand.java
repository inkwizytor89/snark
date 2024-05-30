package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.MessageDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.MessageEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.SpyReportGIR;
import org.enoch.snark.gi.types.GIUrl;
import org.enoch.snark.instance.model.to.SystemView;
import org.enoch.snark.instance.service.MessageService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.enoch.snark.gi.types.UrlComponent.OVERVIEW;

public class ReadMessageCommand extends AbstractCommand {

    public ReadMessageCommand() {
        super();
    }

    @Override
    public boolean execute() {
        new GIUrl().openMessages();
        SleepUtil.secondsToSleep(8L);

        List<String> spyReports = loadMessagesLinks();
        storeSpyMessage(spyReports);
        MessageService.getInstance().update();
        return true;
    }

    private List<String> loadMessagesLinks() {
        final WebDriver chromeDriver = GI.getInstance().getWebDriver();
        final List<WebElement> elements = chromeDriver.findElements(By.tagName("a"));
        List<String> spyReports = new ArrayList<>();
        for (WebElement element : elements) {
            final String href = element.getAttribute("href");
            if(href != null && href.contains("messageId")) {
                spyReports.add(href);
            }
        }
        return spyReports;
    }

    public void storeSpyMessage(List<String> links) {
        for(String link : links) {
            if(!storeSpyMessage(link)) {
                break;
            }
        }
        GIUrl.openComponent(OVERVIEW, null);
    }

    // TODO: 12.03.2019 przegladanie wiadommosci w osobnym oknie i jak jest duplikat to przerywanie
    private boolean storeSpyMessage(String link) {
        Long messageId = Long.parseLong(getMessageIdFromLink(link));

        boolean alreadyExists = MessageDAO.getInstance().fetchAll().stream().anyMatch(
                messageEntity -> messageEntity.messageId.equals(messageId));
        if(alreadyExists) return false;

        MessageEntity messageEntity = MessageEntity.create(GI.getInstance().getWebDriver().getPageSource());
        messageEntity.messageId = messageId;
        MessageDAO.getInstance().saveOrUpdate(messageEntity);
        if(MessageEntity.SPY.equals(messageEntity.type)) {

            SpyReportGIR spyReportGIR = new SpyReportGIR();
            TargetEntity spyTarget = spyReportGIR.readTargetFromReport(link);
            Optional<TargetEntity> targetOptional = TargetDAO.getInstance().find(spyTarget.toPlanet());
            if(targetOptional.isEmpty()) {
                System.err.println("Spy report for "+spyTarget.toPlanet()+" have no association for TargetEntity");
                new GalaxyAnalyzeCommand(new SystemView(spyTarget.galaxy, spyTarget.galaxy)).push();
                return true;
            }
            TargetEntity targetEntity = targetOptional.get();
            if(spyTarget.metal == null) { // not enough spy probe
                targetEntity.player.spyLevel*=2;
                PlayerDAO.getInstance().saveOrUpdate(targetEntity.player);
            } else {
                targetEntity.update(spyTarget);
                TargetDAO.getInstance().saveOrUpdate(targetEntity);
            }
        }
        return true;
    }

    private String getMessageIdFromLink(String link) {
        return link.substring(link.indexOf("messageId=")+10);
    }

    @Override
    public String toString() {
        return "load messages";
    }
}
