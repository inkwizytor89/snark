package org.enoch.snark.action.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.GalaxyAnalyzeCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.MessageEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.MessageRepository;
import org.enoch.snark.db.repository.PlayerRepository;
import org.enoch.snark.db.repository.TargetRepository;
import org.enoch.snark.instance.service.MessageService;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.SpyReportGIR;
import org.enoch.snark.instance.model.to.SystemView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.OVERVIEW;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class ReadMessageProcessor {

    private final Core core;
    private final ColonyRepository colonyRepository;
    private final TargetRepository targetRepository;
    private final PlayerRepository playerRepository;
    private final MessageRepository messageRepository;

    private GI gi;

    public ExecutionIssue execute(GI gi) {
        this.gi = gi;
        gi.url().openMessages();
        SleepUtil.secondsToSleep(8L);

        List<String> spyReports = loadMessagesLinks();
        storeSpyMessage(spyReports);
//        MessageService.getInstance().update(duration);
        return ExecutionIssue.NO_ISSUE;
    }

    private List<String> loadMessagesLinks() {
        final WebDriver chromeDriver = gi.getWebDriver();
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
        ColonyEntity colony = gi.url().openComponent(OVERVIEW, null);
        colonyRepository.save(colony);
    }

    // TODO: 12.03.2019 przegladanie wiadommosci w osobnym oknie i jak jest duplikat to przerywanie
    private boolean storeSpyMessage(String link) {
        Long messageId = Long.parseLong(getMessageIdFromLink(link));

        boolean alreadyExists =messageRepository.findAll().stream().anyMatch(
                messageEntity -> messageEntity.messageId.equals(messageId));
        if(alreadyExists) return false;

        MessageEntity messageEntity = MessageEntity.create(gi.getWebDriver().getPageSource());
        messageEntity.messageId = messageId;
        messageRepository.save(messageEntity);
        if(MessageEntity.SPY.equals(messageEntity.type)) {

            SpyReportGIR spyReportGIR = new SpyReportGIR(gi);
            TargetEntity spyTarget = spyReportGIR.readTargetFromReport(link);
            MessageService.getInstance().release(spyTarget.toPlanet());
            Optional<TargetEntity> targetOptional = targetRepository.find(spyTarget.toPlanet());
            if(targetOptional.isEmpty()) {
                System.err.println("Spy report for "+spyTarget.toPlanet()+" have no association for TargetEntity");
                core.push(new GalaxyAnalyzeCommand(new SystemView(spyTarget.galaxy, spyTarget.galaxy)));

                return true;
            }
            TargetEntity targetEntity = targetOptional.get();
            if(spyTarget.metal == null) { // not enough spy probe
                targetEntity.player.spyLevel*=2;
                playerRepository.save(targetEntity.player);
            } else {
                targetEntity.update(spyTarget);
                targetRepository.save(targetEntity);
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
