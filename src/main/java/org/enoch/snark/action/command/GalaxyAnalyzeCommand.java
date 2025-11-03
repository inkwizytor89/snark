package org.enoch.snark.action.command;

import lombok.Data;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.instance.si.QueueRunType;
import org.enoch.snark.instance.model.to.SystemView;

@Data
public class GalaxyAnalyzeCommand extends AbstractCommand {

    public SystemView systemView;

    public GalaxyAnalyzeCommand(GalaxyEntity galaxyEntity) {
        this(galaxyEntity.toSystemView());
    }

    public GalaxyAnalyzeCommand(SystemView systemView) {
        super();
        this.systemView = systemView;
        hash(systemView.toString());
        setRunType(QueueRunType.SPAM);
    }

    @Override
    public String toString() {
        return systemView + "_Command";
    }
}
