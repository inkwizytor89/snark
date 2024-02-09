package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.GI;
import org.openqa.selenium.JavascriptExecutor;

public class SendMessageToPlayerCommand extends AbstractCommand {

    private final String herf;
    private final String message;

    public SendMessageToPlayerCommand(String herf, String message) {
        super();
        this.herf = herf;
        this.message = message;
    }

    @Override
    public boolean execute() {
        GI gi = GI.getInstance();
        gi.webDriver.get(herf);

        //Scroll down till the bottom of the page
        ((JavascriptExecutor) gi.webDriver).executeScript("window.scrollBy(0,document.body.scrollHeight)");

        gi.findElement("textarea", "name", "text").sendKeys(message);
        gi.findElement("a", "class", "btn_blue fright send_new_msg").click();

        return true;
    }


    @Override
    public String toString() {
        return "write message to player";
    }
}
