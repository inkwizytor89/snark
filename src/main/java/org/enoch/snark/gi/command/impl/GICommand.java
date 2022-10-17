package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.GISession;
import org.enoch.snark.instance.Instance;
import org.openqa.selenium.WebDriver;

public abstract class GICommand extends AbstractCommand {

    protected final GISession session;
    protected final WebDriver webDriver;

    protected GICommand(Instance instance, CommandType type) {
        super(instance, type);
        session = instance.session;
        webDriver = session.getWebDriver();
    }

    protected GICommand(CommandType type) {
        super(type);
        session = instance.session;
        webDriver = session.getWebDriver();
    }
}
