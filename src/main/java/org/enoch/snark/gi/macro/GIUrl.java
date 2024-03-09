package org.enoch.snark.gi.macro;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.gi.GI;
import org.enoch.snark.gi.GeneralGIR;
import org.enoch.snark.gi.command.impl.LoadColoniesCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.instance.commander.QueueManger;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SystemView;
import org.enoch.snark.model.types.ColonyType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.enoch.snark.gi.macro.UrlBuilder.*;
import static org.enoch.snark.gi.macro.UrlComponent.*;
import static org.enoch.snark.gi.macro.UrlPage.HIGHSCORE;
import static org.enoch.snark.gi.macro.UrlPage.MESSAGES;
import static org.enoch.snark.instance.config.Config.*;

public class GIUrl {

    public static final String COMPONENT_TERM = "component=";
    public static final String PAGE_TERM = "page=";
    private static final String SITE_TERM = "site=";

    public static final String PAGE_INGAME = "ingame";
    public static final String PAGE_OVERVIEW = "overview";
    public static final String PAGE_RESOURCES = "supplies";
    public static final String PAGE_LIFEFORM = "lfbuildings";
    public static final String PAGE_FACILITIES = "facilities";
    public static final String PAGE_RESEARCH = "research";
    public static final String PAGE_BASE_FLEET = "fleetdispatch";
    public static final String PAGE_DEFENSES = "defenses";
    public static final String PAGE_MESSAGES = "messages";
    public static final String PAGE_SPACE = "galaxy";
    public static final String PAGE_HIGH_SCORE = "highscore";

    private Instance instance;
    private String url;

    private static boolean shouldUpdateCurrentState = false;

    public GIUrl() {
        instance = Instance.getInstance();
        url = Instance.getMainConfigMap().getConfig(URL);
    }

    public static void openMessages() {
        new UrlBuilder(MESSAGES).get();
    }

    public static void openHighScore(String site) {
        new UrlBuilder(HIGHSCORE).param(SITE_PARAM, site).get();
    }

    @Deprecated
    public static void openFleetView(ColonyEntity source, Planet target, Mission mission) {
        new UrlBuilder(FLEETDISPATCH)
                .param(CP_PARAM, source.cp)
                .param(GALAXY_PARAM, target.galaxy)
                .param(SYSTEM_PARAM, target.system)
                .param(POSITION_PARAM, target.position)
                .param(TYPE_PARAM, generateTargetType(target.type))
                .param(MISSION_PARAM, mission.getValue())
                .get();

        updateColony(source);
        loadFleetStatus();
        GI.getInstance().updateFleet(source);
    }

    public static void openGalaxy(SystemView systemView, ColonyEntity colony) {
        if(colony == null) {
            colony = Instance.getInstance().lastVisited;
            if(colony == null) colony = ColonyDAO.getInstance().getOldestUpdated();
        }
        new UrlBuilder(GALAXY)
                .param(GALAXY_PARAM, systemView.galaxy)
                .param(SYSTEM_PARAM, systemView.system)
                .param(CP_PARAM, colony.cp)
                .get();

        Instance.getInstance().lastVisited = colony;
        updateColony(colony);
        GI.getInstance().updateGalaxy(systemView);
    }

    public static void openResearch() {
        new UrlBuilder(RESEARCH).get();

        PlayerEntity player = PlayerDAO.getInstance().fetch(PlayerEntity.mainPlayer());
        GI.getInstance().updateResearch(player);
        GI.getInstance().updateQueue(null, QueueManger.RESEARCH);
        player.updated = LocalDateTime.now();
        PlayerDAO.getInstance().saveOrUpdate(player);
    }

    public static void openComponent(UrlComponent component, ColonyEntity colony) {
        if(colony == null) {
            colony = selectColony();
        }
        new UrlBuilder(component).param(CP_PARAM, colony.cp).get();
        Instance.getInstance().lastVisited = colony;

        updateColony(colony);
        if (SUPPLIES.equals(component)) {
            GI.getInstance().updateResourcesProducers(colony);
            GI.getInstance().updateQueue(colony, QueueManger.BUILDING);
            GI.getInstance().updateQueue(colony, QueueManger.SHIPYARD);
        } else if (FACILITIES.equals(component)) {
            GI.getInstance().updateFacilities(colony);
            GI.getInstance().updateQueue(colony, QueueManger.BUILDING);
        } else if (LFBUILDINGS.equals(component)) {
            GI.getInstance().updateLifeform(colony);
            GI.getInstance().updateQueue(colony, QueueManger.LIFEFORM_BUILDINGS);
        } else if (FLEETDISPATCH.equals(component)) {
            if (Instance.commander != null) {
                loadFleetStatus();
            }
            GI.getInstance().updateFleet(colony);
        } else if (DEFENSES.equals(component)) {
            GI.getInstance().updateDefence(colony);
            GI.getInstance().updateQueue(colony, QueueManger.SHIPYARD);
        }
    }

    private static String generateTargetType(ColonyType type) {
        switch (type) {
            case MOON: return "3";
            case DEBRIS: return "2";
            default: return "1";
        }
    }

    public static void loadFleetStatus() {
        try {
            Pattern fleetStatusPattern = Pattern.compile("\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)");
            final WebElement slotsLabel = GI.getInstance().getWebDriver().findElement(By.id("slots"));
            Matcher m = fleetStatusPattern.matcher(slotsLabel.getText());
            if (m.find()) {
                Instance.commander.setFleetStatus(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
                int expeditionCount = Integer.parseInt(m.group(3));
                Instance.commander.setExpeditionStatus(expeditionCount, Integer.parseInt(m.group(4)));
            }
        } catch (Exception e) {
            System.err.println("Can not load slots, maybe temporary planet is removed reloadColonies");
            new LoadColoniesCommand().execute();
        }
    }

    private static ColonyEntity selectColony() {
        Boolean isHidingActivity = Instance.getMainConfigMap().getConfigBoolean(HIDING_ACTIVITY, false);
        if(isHidingActivity) {
            Optional<ColonyEntity> temporaryPlanet = ColonyDAO.getInstance().fetchAll().stream()
                    .filter(colonyEntity -> colonyEntity.isPlanet)
                    .filter(colonyEntity -> colonyEntity.cpm == null)
                    .max(Comparator.comparing(o -> o.created));

            if (temporaryPlanet.isPresent()) {
                return temporaryPlanet.get();
            }
        }
        ColonyEntity colony = Instance.getInstance().lastVisited;
        if(colony == null) colony = ColonyDAO.getInstance().getOldestUpdated();
        ColonyDAO.getInstance().fetchAll();
        return colony;
    }

    private static void updateColony(ColonyEntity colony) {
        GI.getInstance().updateResources(colony);
        colony.updated = LocalDateTime.now();
        colony.save();
    }

    public GIUrl updateCurrentState(boolean shouldUpdateCurrentState) {
        this.shouldUpdateCurrentState = shouldUpdateCurrentState;
        return this;
    }
}
