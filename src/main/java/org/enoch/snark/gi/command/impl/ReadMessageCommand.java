package org.enoch.snark.gi.command.impl;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.db.entity.MessageEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SpyInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.enoch.snark.gi.command.CommandType.INTERFACE_REQUIERED;

public class ReadMessageCommand extends AbstractCommand {

    private final Instance instance;
    private Planet planet;

    public ReadMessageCommand(Instance instance) {
        super(instance, INTERFACE_REQUIERED);
        this.instance = instance;
    }

    @Override
    public boolean execute() {
//        SpyInfo lastSpyInfo = instance.messageService.getLastSpyInfo(planet);
//
//        if(lastSpyInfo == null || !lastSpyInfo.isStillAvailable(10)) {
            new GIUrlBuilder(instance).openMessages();
            instance.session.sleep(TimeUnit.SECONDS, 5);

            List<String> spyReports = loadMessagesLinks();
            storeSpyMessage(spyReports);
//            instance.messageService.getLastSpyInfo(planet);
//        }
        return true;
    }

    private List<String> loadMessagesLinks() {
        final WebDriver chromeDriver = instance.session.getWebDriver();
        final List<WebElement> elements = chromeDriver.findElements(By.tagName("a"));
        List<String> spyReports = new ArrayList<>();
        for (WebElement element : elements) {
            final String href = element.getAttribute("href");
            if(href != null && href.contains("messageId") && href.contains("ajax=1")) {
                spyReports.add(href);
            }
        }
        return spyReports;
    }

    public void storeSpyMessage(List<String> links) {
        for(String link : links) {
            storeSpyMessage(link);
        }
        new GIUrlBuilder(instance).openOverview();
    }

    // TODO: 12.03.2019 przegladanie wiadommosci w osobnym oknie i jak jest duplikat to przerywanie
    private void storeSpyMessage(String link) {
        Long messageId = Long.parseLong(getMessageIdFromLink(link));
        boolean alreadyExists = instance.daoFactory.messageDAO.fetchAll().stream().anyMatch(
                messageEntity -> messageEntity.messageId.equals(messageId));
        if(alreadyExists) {
            return;
        }
        instance.session.getWebDriver().get(link);
        MessageEntity messageEntity = MessageEntity.create(instance.session.getWebDriver().getPageSource());
        messageEntity.messageId = messageId;
        messageEntity.universe = instance.universeEntity;

        instance.daoFactory.messageDAO.saveOrUpdate(messageEntity);

        if(MessageEntity.SPY.equals(messageEntity.type)) {
            TargetEntity planet = messageEntity.getPlanet();
            Optional<TargetEntity> targetEntity = instance.daoFactory.targetDAO.find(planet.galaxy, planet.system, planet.position);
            targetEntity.get().update(planet);
            instance.daoFactory.targetDAO.saveOrUpdate(targetEntity.get());
            if(targetEntity.get().resources == 0L) {// jesli nie mamy informacji o resource to trzeba wysalac ponownie informacje o sondzie na wyzszy poziom bo to jest za slaby poziom informacji
                targetEntity.get().spyLevel += 4;
                instance.daoFactory.targetDAO.saveOrUpdate(targetEntity.get());
            }
        }
    }

    private String getMessageIdFromLink(String link) {
        Pattern pattern = Pattern.compile("messageId=(.*?)&tabid");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String toString() {
        return "load messages";
    }
}
