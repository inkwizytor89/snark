package org.enoch.snark.action.command;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.si.module.consumer.gi.TechnologyGIR;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.service.TechnologyService;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.si.module.building.BuildRequest;
import org.enoch.snark.instance.si.module.building.BuildRequirements;
import org.enoch.snark.instance.si.module.building.BuildingCost;

import static org.enoch.snark.instance.si.module.ThreadMap.MASTER;

public class BuildCommand extends AbstractCommand {

    public final TechnologyService technologyService;
    public final ColonyEntity colony;
    public final BuildRequirements requirements;
//    public final TechnologyGIR gir = new TechnologyGIR();

    public BuildCommand(ColonyEntity colony, BuildRequirements requirements) {
        super();
        this.colony = colony;
        this.requirements = requirements;
        technologyService = TechnologyService.getInstance();
        hash("build_"+colony+"_"+requirements);
    }

    @Override
    public String toString() {
        return "build " + requirements + " on " + colony;
    }
}
