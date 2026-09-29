package org.enoch.snark.action.command;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.service.TechnologyService;
import org.enoch.snark.instance.si.module.building.BuildRequirements;

public class BuildCommand extends AbstractCommand {

    public final TechnologyService technologyService;
    public final ColonyEntity colony;
    public final BuildRequirements requirements;

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
