package org.enoch.snark.instance.si.module.consumer.gi.types;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.consumer.gi.Wd;
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

    private final Wd wd;

    public GIUrl(Wd wd) {
        this.wd = wd;
    }

    private void openUrl(String url) {
        wd.getWebDriver().get(url);
    }

    public void openMessages() {
        openUrl(new UrlBuilder(wd, MESSAGES).get());
    }

    public void openHighScore(String site) {
        openUrl(new UrlBuilder(wd, HIGHSCORE).param(SITE_PARAM, site).get());
    }

    public ColonyEntity openSendFleetView(ColonyEntity source, Planet target, Mission mission) {
        openUrl(new UrlBuilder(wd, FLEETDISPATCH)
                .param(CP_PARAM, source.cp)
                .param(GALAXY_PARAM, target.galaxy)
                .param(SYSTEM_PARAM, target.system)
                .param(POSITION_PARAM, target.position)
                .param(TYPE_PARAM, generateTargetType(target.type))
                .param(MISSION_PARAM, mission.getValue())
                .get());

        updateColony(source);
        loadFleetStatus();
        wd.updateFleet(source);
        return source;
    }

    public ColonyEntity openGalaxy(SystemView systemView, ColonyEntity colony) {
        if(colony == null) {
            colony = Core.getLastVisited();
        }
        openUrl(new UrlBuilder(wd, GALAXY)
                .param(GALAXY_PARAM, systemView.galaxy)
                .param(SYSTEM_PARAM, systemView.system)
                .param(CP_PARAM, colony.cp)
                .get());

        Core.setLastVisited(colony);
        updateColony(colony);
        return colony;
    }

    public void openResearch(PlayerEntity mainPlayer) {
        openUrl(new UrlBuilder(wd, RESEARCH).get());

        wd.updateResearch(mainPlayer);
        new TechnologyGIR(wd).updateQueue(null, TechnologyService.RESEARCH);
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
        String url = new UrlBuilder(wd, component).param(CP_PARAM, colony.cp).get();
        openUrl(url);
        if(debug) System.err.println("GET URL: "+url);
        Core.setLastVisited(colony);

        updateColony(colony);
        if (SUPPLIES.equals(component)) {
            wd.updateResourcesProducers(colony);
            new TechnologyGIR(wd).updateQueue(colony, TechnologyService.BUILDING);
            new TechnologyGIR(wd).updateQueue(colony, TechnologyService.SHIPYARD);
        } else if (FACILITIES.equals(component)) {
            wd.updateFacilities(colony);
            new TechnologyGIR(wd).updateQueue(colony, TechnologyService.BUILDING);
        } else if (LFBUILDINGS.equals(component)) {
            wd.updateLifeform(colony);
            new TechnologyGIR(wd).updateQueue(colony, TechnologyService.LIFE_FORM_BUILDINGS);
        } else if (FLEETDISPATCH.equals(component)) {
            loadFleetStatus();
            wd.updateFleet(colony);
        } else if (DEFENSES.equals(component)) {
            wd.updateDefence(colony);
            new TechnologyGIR(wd).updateQueue(colony, TechnologyService.SHIPYARD);
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
            final WebElement slotsLabel = wd.getWebDriver().findElement(By.id("slots"));
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
        wd.updateResources(colony);
        colony.updated = LocalDateTime.now();
    }
}
