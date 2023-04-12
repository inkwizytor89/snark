package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.GISession;
import org.openqa.selenium.WebDriver;

public abstract class GICommand extends AbstractCommand {

    protected final GISession session;
    protected final WebDriver webDriver;

    protected GICommand() {
        super();
        session = instance.session;
        webDriver = session.getWebDriver();
    }
}
