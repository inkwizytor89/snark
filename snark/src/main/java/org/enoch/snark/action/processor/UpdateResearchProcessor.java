package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.UpdateResearchCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.repository.PlayerRepository;
import org.enoch.snark.instance.si.module.consumer.gi.Wd;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Scope("prototype")
public class UpdateResearchProcessor {

    private final PlayerRepository playerRepository;

    public ExecutionIssue execute(Wd wd, UpdateResearchCommand command) {
        PlayerEntity mainPlayer = playerRepository.mainPlayer();
        new GIUrl(wd).openResearch(mainPlayer);
        return ExecutionIssue.NO_ISSUE;
    }

    @Override
    public String toString() {
        return "UpdateResearchCommand";
    }
}
