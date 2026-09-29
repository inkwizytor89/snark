package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.si.module.consumer.gi.Wd;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.enoch.snark.action.command.status.ExecutionIssue.NO_ISSUE;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class OpenPageProcessor {

    private final ColonyRepository colonyRepository;

    public ExecutionIssue execute(Wd wd, OpenPageCommand command) {
        ColonyEntity colonyEntity = wd.url().openComponent(command.component, command.colony);
        colonyRepository.save(colonyEntity);
        return NO_ISSUE;
    }
}
