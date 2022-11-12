package org.enoch.snark.gi.command.impl;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.MessageDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.MessageEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.SpyReportGIR;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.enoch.snark.gi.command.impl.CommandType.NORMAL_REQUIERED;

public class ReadMessageCommand extends AbstractCommand {

    public ReadMessageCommand() {
        super(NORMAL_REQUIERED);
    }

    @Override
    public boolean execute() {
        new GIUrlBuilder().openMessages();
        SleepUtil.sleep();

        List<String> spyReports = loadMessagesLinks();
        storeSpyMessage(spyReports);
        return true;
    }

    private List<String> loadMessagesLinks() {
        final WebDriver chromeDriver = Instance.session.getWebDriver();
        final List<WebElement> elements = chromeDriver.findElements(By.tagName("a"));
        List<String> spyReports = new ArrayList<>();
        for (WebElement element : elements) {
            final String href = element.getAttribute("href");
            if(href != null && href.contains("messageId") && href.contains("ajax=1")) {
                spyReports.add(href);
//                System.err.println("link to messege "+href);
            }
        }
        return spyReports;
    }

    public void storeSpyMessage(List<String> links) {
        for(String link : links) {
            storeSpyMessage(link);
        }
        new GIUrlBuilder().open(GIUrlBuilder.PAGE_OVERVIEW, null);
    }

    // TODO: 12.03.2019 przegladanie wiadommosci w osobnym oknie i jak jest duplikat to przerywanie
    private void storeSpyMessage(String link) {
        Long messageId = Long.parseLong(getMessageIdFromLink(link));

        MessageDAO.getInstance().fetchAll().stream()
                .filter(message -> message.created.isBefore(LocalDateTime.now().minusDays(2)))
                .forEach(message -> MessageDAO.getInstance().remove(message));

        boolean alreadyExists = MessageDAO.getInstance().fetchAll().stream().anyMatch(
                messageEntity -> messageEntity.messageId.equals(messageId));
        if(alreadyExists) {
//            System.err.println("Skipping alredy existing message id "+messageId);
            return;
        }
//        System.err.println("parsing message id "+messageId);


        MessageEntity messageEntity = MessageEntity.create(Instance.session.getWebDriver().getPageSource());
        messageEntity.messageId = messageId;
        MessageDAO.getInstance().saveOrUpdate(messageEntity);
        if(MessageEntity.SPY.equals(messageEntity.type)) {

            SpyReportGIR spyReportGIR = new SpyReportGIR();
            TargetEntity planet = spyReportGIR.readTargetFromReport(link);
            Optional<TargetEntity> targetEntity = TargetDAO.getInstance().find(planet.galaxy, planet.system, planet.position);
            targetEntity.get().update(planet);
            TargetDAO.getInstance().saveOrUpdate(targetEntity.get());
//            if(targetEntity.get().resources == 0L) {// jesli nie mamy informacji o resource to trzeba wysalac ponownie informacje o sondzie na wyzszy poziom bo to jest za slaby poziom informacji
//                targetEntity.get().spyLevel += 4;
//                TargetDAO.getInstance().saveOrUpdate(targetEntity.get());
//            }
        }

//------------------------------------
//        instance.session.getWebDriver().get(link);
//        MessageEntity messageEntity = MessageEntity.create(instance.session.getWebDriver().getPageSource());
//        messageEntity.messageId = messageId;
//
//        MessageDAO.getInstance().saveOrUpdate(messageEntity);
//
//        if(MessageEntity.SPY.equals(messageEntity.type)) {
//            TargetEntity planet = messageEntity.getPlanet();
//            Optional<TargetEntity> targetEntity = TargetDAO.getInstance().find(planet.galaxy, planet.system, planet.position);
//            targetEntity.get().update(planet);
//            TargetDAO.getInstance().saveOrUpdate(targetEntity.get());
//            if(targetEntity.get().resources == 0L) {// jesli nie mamy informacji o resource to trzeba wysalac ponownie informacje o sondzie na wyzszy poziom bo to jest za slaby poziom informacji
//                targetEntity.get().spyLevel += 4;
//                TargetDAO.getInstance().saveOrUpdate(targetEntity.get());
//            }
//        }
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
