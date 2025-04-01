package org.enoch.snark.action.processor;

import org.enoch.snark.action.command.SendMessageToPlayerCommand;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.openqa.selenium.JavascriptExecutor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SendMessageToPlayerProcessor {

    public boolean execute(GI gi, SendMessageToPlayerCommand command ) {
        gi.webDriver.get(command.herf);

        //Scroll down till the bottom of the page
        ((JavascriptExecutor) gi.webDriver).executeScript("window.scrollBy(0,document.body.scrollHeight)");

        gi.findElement("textarea", "name", "text").sendKeys(command.message);
        gi.findElement("a", "class", "btn_blue fright send_new_msg").click();

        return true;
    }


    @Override
    public String toString() {
        return "write message to player";
    }
}
