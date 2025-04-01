package org.enoch.snark.action.command;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent;

public class OpenPageCommand extends AbstractCommand {

    public final UrlComponent component;
    public final ColonyEntity colony;

    public OpenPageCommand(UrlComponent component, ColonyEntity colony) {
        super();
        this.component = component;
        this.colony = colony;
    }

    public AbstractCommand sourceHash(String sourceHash) {
        return super.hash(sourceHash+"_"+component+"_"+colony);
    }

    @Override
    public String toString() {
        return "On " + colony + " open page component " + component ;
    }
}
