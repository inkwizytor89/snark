package org.enoch.snark.gi.command.impl;

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
import java.util.concurrent.TimeUnit;

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
        SpyInfo lastSpyInfo = instance.messageService.getLastSpyInfo(planet);

        if(lastSpyInfo == null || !lastSpyInfo.isStillAvailable(10)) {
            new GIUrlBuilder(instance).openMessages();
            instance.session.sleep(TimeUnit.SECONDS, 5);

            List<String> spyReports = loadMessagesLinks();
            instance.messageService.storeSpyMessage(spyReports);
            instance.messageService.getLastSpyInfo(planet);
        }
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

    @Override
    public String toString() {
        return "load messages";
    }
}
