package org.enoch.snark.action.processor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.AbstractCommand;
import org.enoch.snark.action.command.RecallCommand;
import org.enoch.snark.action.command.UpdateResearchCommand;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.repository.PlayerRepository;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Scope("prototype")
public class UpdateResearchProcessor {

    private final PlayerRepository playerRepository;

    public boolean execute(GI gi, UpdateResearchCommand command) {
        PlayerEntity mainPlayer = playerRepository.mainPlayer();
        new GIUrl(gi).openResearch(mainPlayer);
        return true;
    }

    @Override
    public String toString() {
        return "UpdateResearchCommand";
    }
}
