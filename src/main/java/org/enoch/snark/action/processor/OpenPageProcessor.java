package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.types.GIUrl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class OpenPageProcessor {

    private final ColonyRepository colonyRepository;

    public boolean execute(GI gi, OpenPageCommand command) {
        ColonyEntity colonyEntity = gi.url().openComponent(command.component, command.colony);
        System.err.println(command+" "+colonyEntity.getShipsMap());
        colonyRepository.save(colonyEntity);
        return true;
    }
}
