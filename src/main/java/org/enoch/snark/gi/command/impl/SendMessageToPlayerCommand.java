package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.command.AbstractCommand;
import org.enoch.snark.instance.Instance;
import org.openqa.selenium.JavascriptExecutor;

import static org.enoch.snark.gi.command.CommandType.INTERFACE_REQUIERED;

public class SendMessageToPlayerCommand extends AbstractCommand {

    private final Instance instance;
    private final String herf;
    private final String message;

    public SendMessageToPlayerCommand(Instance instance, String herf, String message) {
        super(instance, INTERFACE_REQUIERED);
        this.instance = instance;
        this.herf = herf;
        this.message = message;
    }

    @Override
    public boolean execute() {
        instance.gi.webDriver.get(herf);

        //Scroll down till the bottom of the page
        ((JavascriptExecutor) instance.gi.webDriver).executeScript("window.scrollBy(0,document.body.scrollHeight)");

        instance.gi.findElement("textarea", "name", "text").sendKeys(message);
        instance.gi.findElement("a", "class", "btn_blue fright send_new_msg").click();

        return true;
    }


    @Override
    public String toString() {
        return "write message to player";
    }
}
