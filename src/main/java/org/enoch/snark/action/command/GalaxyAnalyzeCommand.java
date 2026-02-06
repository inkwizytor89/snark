package org.enoch.snark.action.command;

import io.micrometer.common.util.StringUtils;
import lombok.Data;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.si.QueueRunType;
import org.enoch.snark.instance.model.to.SystemView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GalaxyAnalyzeCommand extends AbstractCommand {

    public SystemView systemView;
    private Map<Planet, Boolean> spyPositions = new HashMap<>();
    private ColonyEntity source;
    private String spyNew;

    public GalaxyAnalyzeCommand(GalaxyEntity galaxyEntity) {
        this(galaxyEntity.toSystemView());
    }

    public GalaxyAnalyzeCommand(SystemView systemView) {
        super();
        this.systemView = systemView;
        hash(systemView.toString());
        setRunType(QueueRunType.SPAM);
    }

    public void setSpyPositions(List<Planet> list) {
        spyPositions = new HashMap<>();
        for(Planet position : list) spyPositions.put(position, false);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("GAC_"+systemView);
        if(source != null) builder.append("_"+source);
        if(!spyPositions.isEmpty()) builder.append("_SP"+spyPositions.size());
        if(!StringUtils.isEmpty(spyNew)) builder.append("_"+spyNew);
        return builder.toString();
    }
}
