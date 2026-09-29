package org.enoch.snark.action.processor;

import lombok.RequiredArgsConstructor;
import org.enoch.snark.action.command.UpdateFleetEventsCommand;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.discord.DiscordBotService;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.consumer.gi.EventContentGIR;
import org.enoch.snark.instance.si.module.consumer.gi.Wd;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;

@RequiredArgsConstructor
@Component
@Scope("prototype")
public class UpdateFleetEventsProcessor {

    private final Core core;
    private final ColonyRepository colonyRepository;
    private final DiscordBotService discordBotService;

    public ExecutionIssue execute(Wd wd, UpdateFleetEventsCommand command) {
        ColonyEntity colony = wd.url().openComponent(FLEETDISPATCH, null);
        colonyRepository.save(colony);

        List<EventFleet> eventFleetList = new EventContentGIR(wd).readEventFleet();
        Navigator.getInstance().informAboutEventFleets(eventFleetList);
        discordBotService.sendMessage(core.status());
        return ExecutionIssue.NO_ISSUE;
    }

    @Override
    public String toString() {
        return "UpdateFleetEventsCommand";
    }
}
