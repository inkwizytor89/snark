package org.enoch.snark.instance.si.module.consumer.gi.types;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.consumer.gi.GI;
import org.enoch.snark.instance.si.module.consumer.gi.TechnologyGIR;
import org.enoch.snark.instance.service.TechnologyService;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.SystemView;
import org.enoch.snark.instance.model.types.ColonyType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlBuilder.*;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.*;
import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlPage.HIGHSCORE;

public class GIUrl {

    private final GI gi;

    public GIUrl(GI gi) {
        this.gi = gi;
    }

    private void openUrl(String url) {
        gi.getWebDriver().get(url);
    }

    public void openMessages() {
        openUrl(new UrlBuilder(gi, MESSAGES).get());
    }

    public void openHighScore(String site) {
        openUrl(new UrlBuilder(gi, HIGHSCORE).param(SITE_PARAM, site).get());
    }

    public ColonyEntity openSendFleetView(ColonyEntity source, Planet target, Mission mission) {
        openUrl(new UrlBuilder(gi, FLEETDISPATCH)
                .param(CP_PARAM, source.cp)
                .param(GALAXY_PARAM, target.galaxy)
                .param(SYSTEM_PARAM, target.system)
                .param(POSITION_PARAM, target.position)
                .param(TYPE_PARAM, generateTargetType(target.type))
                .param(MISSION_PARAM, mission.getValue())
                .get());

        updateColony(source);
        loadFleetStatus();
        gi.updateFleet(source);
        return source;
    }

    public void openGalaxy(SystemView systemView, ColonyEntity colony) {
        if(colony == null) {
            colony = Core.getLastVisited();
        }
        openUrl(new UrlBuilder(gi, GALAXY)
                .param(GALAXY_PARAM, systemView.galaxy)
                .param(SYSTEM_PARAM, systemView.system)
                .param(CP_PARAM, colony.cp)
                .get());

        Core.setLastVisited(colony);
        System.err.println("open galaxy setLastVisited="+colony);
        updateColony(colony);
        gi.updateGalaxy(systemView);
    }

    public void openResearch(PlayerEntity mainPlayer) {
        openUrl(new UrlBuilder(gi, RESEARCH).get());

        gi.updateResearch(mainPlayer);
        new TechnologyGIR(gi).updateQueue(null, TechnologyService.RESEARCH);
        mainPlayer.updated = LocalDateTime.now();
    }

    public ColonyEntity openComponent(UrlComponent component, ColonyEntity colony) {
        return openComponent(component, colony, false);
    }

    public ColonyEntity openComponent(UrlComponent component, ColonyEntity colony, boolean debug) {
        if(colony == null) {
            colony = Core.getLastVisited();
        }
        if(colony == null)
            System.err.println("yyyyyyyyyy");
        String url = new UrlBuilder(gi, component).param(CP_PARAM, colony.cp).get();
        openUrl(url);
        if(debug) System.err.println("GET URL: "+url);
        Core.setLastVisited(colony);

        updateColony(colony);
        if (SUPPLIES.equals(component)) {
            gi.updateResourcesProducers(colony);
            new TechnologyGIR(gi).updateQueue(colony, TechnologyService.BUILDING);
            new TechnologyGIR(gi).updateQueue(colony, TechnologyService.SHIPYARD);
        } else if (FACILITIES.equals(component)) {
            gi.updateFacilities(colony);
            new TechnologyGIR(gi).updateQueue(colony, TechnologyService.BUILDING);
        } else if (LFBUILDINGS.equals(component)) {
            gi.updateLifeform(colony);
            new TechnologyGIR(gi).updateQueue(colony, TechnologyService.LIFE_FORM_BUILDINGS);
        } else if (FLEETDISPATCH.equals(component)) {
            loadFleetStatus();
            gi.updateFleet(colony);
        } else if (DEFENSES.equals(component)) {
            gi.updateDefence(colony);
            new TechnologyGIR(gi).updateQueue(colony, TechnologyService.SHIPYARD);
        }
        return colony;
    }

    private String generateTargetType(ColonyType type) {
        switch (type) {
            case MOON: return "3";
            case DEBRIS: return "2";
            default: return "1";
        }
    }

    public void loadFleetStatus() {
        try {
            Pattern fleetStatusPattern = Pattern.compile("\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)");
            final WebElement slotsLabel = gi.getWebDriver().findElement(By.id("slots"));
            Matcher m = fleetStatusPattern.matcher(slotsLabel.getText());
            if (m.find()) {
                Navigator.setFleetCount(Integer.parseInt(m.group(1)));
                Navigator.setFleetMax(Integer.parseInt(m.group(2)));

                int expeditionCount = Integer.parseInt(m.group(3));
                int expeditionMax = Integer.parseInt(m.group(4));
                Navigator.setExpeditionCount(expeditionCount);
                Navigator.setExpeditionMax(expeditionMax);
            }
        } catch (Exception e) {
            System.err.println("Can not load slots, maybe temporary planet is removed reloadColonies");
//            new LoadColoniesCommand().execute();
        }
    }

    private void updateColony(ColonyEntity colony) {
        gi.updateResources(colony);
        colony.updated = LocalDateTime.now();
    }
}
