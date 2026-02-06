package org.enoch.snark.action.processor;

import org.enoch.snark.action.command.SendMessageToPlayerCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.instance.si.module.consumer.gi.Wd;
import org.openqa.selenium.JavascriptExecutor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SendMessageToPlayerProcessor {

    public ExecutionIssue execute(Wd wd, SendMessageToPlayerCommand command ) {
        wd.webDriver.get(command.herf);

        //Scroll down till the bottom of the page
        ((JavascriptExecutor) wd.webDriver).executeScript("window.scrollBy(0,document.body.scrollHeight)");

        wd.findElement("textarea", "name", "text").sendKeys(command.message);
        wd.findElement("a", "class", "btn_blue fright send_new_msg").click();

        return ExecutionIssue.NO_ISSUE;
    }


    @Override
    public String toString() {
        return "write message to player";
    }
}
