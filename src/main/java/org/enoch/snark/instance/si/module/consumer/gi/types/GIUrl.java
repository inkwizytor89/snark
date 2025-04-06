package org.enoch.snark.instance.si.module.consumer.gi.types;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.PlayerDAO;
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

    public void openSendFleetView(ColonyEntity source, Planet target, Mission mission) {
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
    }

    public void openGalaxy(SystemView systemView, ColonyEntity colony) {
        if(colony == null) {
            colony = Core.getLastVisited();
            if(colony == null) colony = ColonyDAO.getInstance().getOldestUpdated();
        }
        openUrl(new UrlBuilder(gi, GALAXY)
                .param(GALAXY_PARAM, systemView.galaxy)
                .param(SYSTEM_PARAM, systemView.system)
                .param(CP_PARAM, colony.cp)
                .get());

        Core.setLastVisited(colony);
        updateColony(colony);
        gi.updateGalaxy(systemView);
    }

    public void openResearch() {
        openUrl(new UrlBuilder(gi, RESEARCH).get());

        PlayerEntity player = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer());
        gi.updateResearch(player);
        new TechnologyGIR(gi).updateQueue(null, TechnologyService.RESEARCH);
        player.updated = LocalDateTime.now();
//        PlayerDAO.getInstance().saveOrUpdate(player);
    }

    public void openComponent(UrlComponent component, ColonyEntity colony) {
        openComponent(component, colony, false);
    }

    public void openComponent(UrlComponent component, ColonyEntity colony, boolean debug) {
        if(colony == null) {
            colony = selectColony();
        }
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

                Navigator.setExpeditionCount(Integer.parseInt(m.group(3)));
                Navigator.setExpeditionMax(Integer.parseInt(m.group(4)));
            }
        } catch (Exception e) {
            System.err.println("Can not load slots, maybe temporary planet is removed reloadColonies");
//            new LoadColoniesCommand().execute();
        }
    }

    private ColonyEntity selectColony() {
        ColonyEntity colony = Core.getLastVisited();
        if(colony == null) colony = ColonyDAO.getInstance().getOldestUpdated();
        ColonyDAO.getInstance().fetchAll();
        return colony;
    }

    private void updateColony(ColonyEntity colony) {
        gi.updateResources(colony);
        colony.updated = LocalDateTime.now();
        colony.save();
    }
}
