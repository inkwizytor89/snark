package org.enoch.snark.instance.si.module.update;

import lombok.Data;
import org.enoch.common.config.ConfigDefinition;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.db.entity.ConfigEntity;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateDefinition {

    private String refresh;
    private String checkSpyReportsDuration;

    public UpdateDefinition() {
        for (ConfigDefinition config : threadDefinition().getConfigs()) {
            switch (config.getKey()) {
                case UpdateThread.REFRESH -> refresh = config.getValue();
                case UpdateThread.CHECK_SPY_REPORTS_DURATION -> checkSpyReportsDuration = config.getValue();
                default -> {
                }
            }
        }
    }

    public void update(List<ConfigEntity> configs) {
        for (ConfigEntity config : configs) {
            switch (config.key) {
                case UpdateThread.REFRESH -> refresh = config.value;
                case UpdateThread.CHECK_SPY_REPORTS_DURATION -> checkSpyReportsDuration = config.value;
                default -> {
                }
            }
        }
    }

    public static ThreadDefinition threadDefinition() {
        List<ConfigDefinition> configs = new ArrayList<>();
        configs.add(new ConfigDefinition("update", UpdateThread.threadType, UpdateThread.REFRESH, "12M", false,
                "How often the navigator and colony state should be refreshed."));
        configs.add(new ConfigDefinition("update", UpdateThread.threadType,
                UpdateThread.CHECK_SPY_REPORTS_DURATION, "2M", false,
                "Interval between checks for new spy reports and message processing."));
        return new ThreadDefinition(UpdateThread.threadType,
                "Refreshes navigator, colony, and message state so other threads operate on a current view of the game.",
                configs);
    }
}
