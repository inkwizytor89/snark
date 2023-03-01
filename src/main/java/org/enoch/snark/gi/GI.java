package org.enoch.snark.gi;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.exception.GIException;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.QueueManger;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SystemView;
import org.enoch.snark.module.building.BuildRequirements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.enoch.snark.gi.macro.GIUrlBuilder.*;

public class GI {
    private static GI INSTANCE;

    public static final String A_TAG = "a";
    public static final String DIV_TAG = "div";
    public static final String TR_TAG = "tr";
    public static final String SPAN_TAG = "span";

    public static final String HREF_ATTRIBUTE = "href";
    public static final String TITLE_ATTRIBUTE = "title";
    public static final String ID_ATTRIBUTE = "id";
    public static final String CLASS_ATTRIBUTE = "class";
    public static final String TECHNOLOGIES = "technologies";
    public final WebDriver webDriver;
    private final PlayerDAO playerDAO;
    private final Instance instance;
    private final GalaxyDAO galaxyDAO;
    private final TargetDAO targetDAO;
    private final QueueManger queueManger;

    private GI() {
        webDriver = new ChromeDriver();
        queueManger = QueueManger.getInstance();
        instance = Instance.getInstance();
        playerDAO = PlayerDAO.getInstance();
        galaxyDAO = GalaxyDAO.getInstance();
        targetDAO = TargetDAO.getInstance();
    }

    public static GI getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new GI();
        }
        return INSTANCE;
    }

    public static void restartInstance() {
        INSTANCE = null;
    }

    public void doubleClickText(String text) {
        try {
            WebElement serverElement = findTextByXPath(text);
            Actions actions = new Actions(webDriver);
            actions.doubleClick(serverElement).perform();
        } catch (NoSuchElementException e) {
            System.err.println("Skip text '" + text + "' to click");
        }
    }

    private WebElement findTextByXPath(String text) {
        List<WebElement> elements = ((ChromeDriver) webDriver).findElementsByXPath("//*[contains(text(), '" + text + "')]");
        int i = 1;
        while(elements.isEmpty() && i <= 10) {
            SleepUtil.pause(i);
            i++;
            elements = ((ChromeDriver) webDriver).findElementsByXPath("//*[contains(text(), '" + text + "')]");
        }

        if(elements.isEmpty()) {
            System.err.println("No element '" + text + "' to click");
            return ((ChromeDriver) webDriver).findElementsByXPath("//*[contains(text(), '" + text + "')]").get(0);
        } else if(elements.size() > 1) {
            System.err.println("Were many elements '" + text + "' to click");
        }
        return elements.get(0);
    }

    public WebElement findElement(String tag, String attribute, String value, String unacceptable) {
        WebElement element = findByXPath("//" + tag + "[@" + attribute + "='" + value + "']");
        for (int i = 1; i <= 10; i++) {
             element = findByXPath("//" + tag + "[@" + attribute + "='" + value + "']");
             if(!unacceptable.equals(element.getText())) {
                 break;
             }
            SleepUtil.pause(i);
        }
        if(unacceptable.equals(element.getText())) {
            throw new GIException(GIException.NOT_FOUND, "//"+tag+"[@" + attribute + "='"+value+"']",
                    "Found " + element.getText() + " but unacceptable was "+ unacceptable);
        }
        return element;
    }

    public WebElement findElement(String tag, String attribute, String value) {
        return findByXPath("//"+tag+"[@" + attribute + "='"+value+"']");
    }

    private WebElement findByXPath(String using) {
        List<WebElement> elements = ((ChromeDriver) webDriver).findElementsByXPath(using);
        int i = 1;
        while(elements.isEmpty() && i <= 10) {
            SleepUtil.pause(i);
            i++;
            elements = ((ChromeDriver) webDriver).findElementsByXPath(using);
        }

        if(elements.isEmpty()) {
            throw new GIException(GIException.NOT_FOUND, using, "No element " + using);
        } else if(elements.size() > 1) {
            throw new GIException(GIException.TOO_MANY, using, "No element " + using);
        }
        return elements.get(0);
    }

    public Long getRawValue(WebElement root, By id) {
        return Long.parseLong(root.findElement(id).getAttribute("data-raw"));
    }

    public void updateColony(ColonyEntity colony) {
        new GIUrlBuilder().open(PAGE_RESOURCES, colony);
        new GIUrlBuilder().open(PAGE_FACILITIES, colony);
        if(isLifeformAvailable() && colony.isPlanet) {
            new GIUrlBuilder().open(PAGE_LIFEFORM, colony);
        }
        new GIUrlBuilder().open(PAGE_BASE_FLEET, colony);
        new GIUrlBuilder().open(PAGE_DEFENSES, colony);
    }

    public void updateResources(ColonyEntity colony) {
        WebElement resources = webDriver.findElement(By.id("resources"));
        colony.metal = getRawValue(resources, By.id("resources_metal"));
        colony.crystal = getRawValue(resources, By.id("resources_crystal"));
        colony.deuterium = getRawValue(resources, By.id("resources_deuterium"));
        colony.energy = getRawValue(resources, By.id("resources_energy"));
        colony.save();
    }

    public void updateDefence(ColonyEntity colony) {
        WebElement technologies = webDriver.findElement(By.id(TECHNOLOGIES));
        colony.rocketLauncher = getAmount(technologies,"rocketLauncher");
        colony.laserCannonLight = getAmount(technologies,"laserCannonLight");
        colony.laserCannonHeavy = getAmount(technologies,"laserCannonHeavy");
        colony.gaussCannon = getAmount(technologies,"gaussCannon");
        colony.ionCannon = getAmount(technologies,"ionCannon");
        colony.plasmaCannon = getAmount(technologies,"plasmaCannon");
        colony.shieldDomeSmall = getAmount(technologies,"shieldDomeSmall");
        colony.shieldDomeLarge = getAmount(technologies,"shieldDomeLarge");
        colony.missileInterceptor = getAmount(technologies,"missileInterceptor");
        colony.missileInterplanetary = getAmount(technologies,"missileInterplanetary");
        colony.save();
    }

    public void updateFleet(ColonyEntity colony) {
        if (webDriver.findElements(By.id("warning")).size() == 0) {
            WebElement technologies = webDriver.findElement(By.id(TECHNOLOGIES));
            colony.fighterLight = getAmount(technologies,"fighterLight");
            colony.fighterHeavy = getAmount(technologies,"fighterHeavy");
            colony.cruiser = getAmount(technologies,"cruiser");
            colony.battleship = getAmount(technologies,"battleship");
            colony.interceptor = getAmount(technologies,"interceptor");
            colony.bomber = getAmount(technologies,"bomber");
            colony.destroyer = getAmount(technologies,"destroyer");
            colony.deathstar = getAmount(technologies,"deathstar");
            colony.reaper = getAmount(technologies,"reaper");
            colony.explorer = getAmount(technologies,"explorer");
            colony.transporterSmall = getAmount(technologies,"transporterSmall");
            colony.transporterLarge = getAmount(technologies,"transporterLarge");
            colony.colonyShip = getAmount(technologies,"colonyShip");
            colony.recycler = getAmount(technologies,"recycler");
            colony.espionageProbe = getAmount(technologies,"espionageProbe");
        } else {
            colony.fighterLight = 0L;
            colony.fighterHeavy = 0L;
            colony.cruiser = 0L;
            colony.battleship = 0L;
            colony.interceptor = 0L;
            colony.bomber = 0L;
            colony.destroyer = 0L;
            colony.deathstar = 0L;
            colony.reaper = 0L;
            colony.explorer = 0L;
            colony.transporterSmall = 0L;
            colony.transporterLarge = 0L;
            colony.colonyShip = 0L;
            colony.recycler = 0L;
            colony.espionageProbe = 0L;
        }
        colony.save();

    }

    public void updateLifeform(ColonyEntity colony) {
        List<WebElement> raceToChoose = webDriver.findElements(By.className("lfsettingsContent"));
        if(!raceToChoose.isEmpty()) {
            return;
        }
        WebElement technologies = webDriver.findElement(By.id(TECHNOLOGIES));
        List<WebElement> kaelsLifeFormChoosen = webDriver.findElements(By.id("lifeformTech14101"));
        if(!kaelsLifeFormChoosen.isEmpty()) {
            colony.lifeformTech14101 = getLevel(technologies, "lifeformTech14101");
            colony.lifeformTech14102 = getLevel(technologies, "lifeformTech14102");
            colony.lifeformTech14103 = getLevel(technologies, "lifeformTech14103");
            colony.lifeformTech14104 = getLevel(technologies, "lifeformTech14104");
            colony.lifeformTech14105 = getLevel(technologies, "lifeformTech14105");
            colony.lifeformTech14106 = getLevel(technologies, "lifeformTech14106");
            colony.lifeformTech14107 = getLevel(technologies, "lifeformTech14107");
            colony.lifeformTech14108 = getLevel(technologies, "lifeformTech14108");
            colony.lifeformTech14109 = getLevel(technologies, "lifeformTech14109");
            colony.lifeformTech14110 = getLevel(technologies, "lifeformTech14110");
            colony.lifeformTech14111 = getLevel(technologies, "lifeformTech14111");
            colony.lifeformTech14112 = getLevel(technologies, "lifeformTech14112");
            colony.save();
        }
    }

    public boolean isLifeformAvailable() {
        return !webDriver.findElements(By.id("lifeform")).isEmpty();
    }

    public void updateFacilities(ColonyEntity colony) {
        WebElement technologies = webDriver.findElement(By.id(TECHNOLOGIES));
        colony.roboticsFactory = getLevel(technologies,"roboticsFactory");
        colony.shipyard = getLevel(technologies,"shipyard");
        if(colony.isPlanet) {
            colony.researchLaboratory = getLevel(technologies, "researchLaboratory");
            colony.allianceDepot = getLevel(technologies, "allianceDepot");
            colony.missileSilo = getLevel(technologies, "missileSilo");
            colony.naniteFactory = getLevel(technologies, "naniteFactory");
            colony.terraformer = getLevel(technologies, "terraformer");
            colony.repairDock = getLevel(technologies, "repairDock");
        } else {
            colony.moonbase = getLevel(technologies, "moonbase");
            colony.sensorPhalanx = getLevel(technologies, "sensorPhalanx");
            colony.jumpGate = getLevel(technologies, "jumpGate");
        }
        colony.save();
    }

    public void updateResourcesProducers(ColonyEntity colony) {
        WebElement technologies = webDriver.findElement(By.id(TECHNOLOGIES));
        colony.metalMine = getLevel(technologies,"metalMine");
        colony.crystalMine = getLevel(technologies,"crystalMine");
        colony.deuteriumSynthesizer = getLevel(technologies,"deuteriumSynthesizer");
        colony.solarPlant = getLevel(technologies,"solarPlant");
        colony.fusionPlant = getLevel(technologies,"fusionPlant");
        colony.solarSatellite = getAmount(technologies,"solarSatellite");
        colony.metalStorage = getLevel(technologies,"metalStorage");
        colony.crystalStorage = getLevel(technologies,"crystalStorage");
        colony.deuteriumStorage = getLevel(technologies,"deuteriumStorage");
        colony.save();
    }

    public void updateResearch(PlayerEntity player) {
        WebElement technologies = webDriver.findElement(By.id(TECHNOLOGIES));
        player.energyTechnology = getLevel(technologies,"energyTechnology");
        player.laserTechnology = getLevel(technologies,"laserTechnology");
        player.ionTechnology = getLevel(technologies,"ionTechnology");
        player.hyperspaceTechnology = getLevel(technologies,"hyperspaceTechnology");
        player.plasmaTechnology = getLevel(technologies,"plasmaTechnology");
        player.combustionDriveTechnology = getLevel(technologies,"combustionDriveTechnology");
        player.impulseDriveTechnology = getLevel(technologies,"impulseDriveTechnology");
        player.hyperspaceDriveTechnology = getLevel(technologies,"hyperspaceDriveTechnology");
        player.espionageTechnology = getLevel(technologies,"espionageTechnology");
        player.computerTechnology = getLevel(technologies,"computerTechnology");
        player.astrophysicsTechnology = getLevel(technologies,"astrophysicsTechnology");
        player.researchNetworkTechnology = getLevel(technologies,"researchNetworkTechnology");
        player.gravitonTechnology = getLevel(technologies,"gravitonTechnology");
        player.weaponsTechnology = getLevel(technologies,"weaponsTechnology");
        player.shieldingTechnology = getLevel(technologies,"shieldingTechnology");
        player.armorTechnology = getLevel(technologies,"armorTechnology");
        PlayerDAO.getInstance().saveOrUpdate(player);
    }

    private Long getLevel(WebElement element, String name) {
        try {
            return Long.parseLong(element.findElement(By.className(name)).findElement(By.className("level")).getText());
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private Long getAmount(WebElement element, String name) {
        String amount = element.findElement(By.className(name)).findElement(By.className("amount")).getText();
        return Long.parseLong(amount.replaceAll("\\D", ""));
    }

    private Long getLong(String input) {
        String resultString = input.replace("Mln", "000000").replace(".", "");
        return Long.parseLong(resultString);
    }

    public List<ColonyEntity> loadPlanetList() {
        ArrayList<ColonyEntity> colonyEntities = new ArrayList<>();
        List<WebElement> coloniesWebElements = new WebDriverWait(webDriver, 10)
                .until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(webDriver.findElement(By.id("planetList")), By.tagName(DIV_TAG)));
        for(WebElement colonyWebElement : coloniesWebElements) {
            try {
                ColonyEntity colonyEntity = new ColonyEntity();

                colonyEntity.cp = Integer.parseInt(colonyWebElement.getAttribute(ID_ATTRIBUTE).split("-")[1]);
                Planet planet = new Planet(colonyWebElement.findElement(By.className("planet-koords ")).getText());
                colonyEntity.galaxy = planet.galaxy;
                colonyEntity.system = planet.system;
                colonyEntity.position = planet.position;
                colonyEntity.isPlanet = true;

                List<WebElement> moons = colonyWebElement.findElements(By.className("moonlink"));
                if(!moons.isEmpty()) {
                    String link = moons.get(0).getAttribute(HREF_ATTRIBUTE);
                    int i = link.indexOf("cp=");
                    colonyEntity.cpm = Integer.parseInt(link.substring(i+3));

                    ColonyEntity moonColonyEntity = new ColonyEntity();
                    moonColonyEntity.galaxy = planet.galaxy;
                    moonColonyEntity.system = planet.system;
                    moonColonyEntity.position = planet.position;
                    moonColonyEntity.isPlanet = false;
                    moonColonyEntity.cp = colonyEntity.cpm;
                    moonColonyEntity.cpm = colonyEntity.cp;
                    colonyEntities.add(moonColonyEntity);
                }

                colonyEntities.add(colonyEntity);
//                System.err.println(colonyEntity.getCordinate()+" "+colonyEntity.cp+" "+colonyEntity.cpm);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return colonyEntities;
    }

    public boolean upgrade(BuildRequirements requirements) {
        WebElement technologies = webDriver.findElement(By.id(TECHNOLOGIES));
        WebElement buildingElement = technologies.findElement(By.className(requirements.request.building.getName()));
        Long buildingLevel = getLevel(technologies, requirements.request.building.getName());
        if(buildingLevel >= requirements.request.level) {
            return true;
        }
        List<WebElement> upgrades = buildingElement.findElements(By.className("upgrade"));
        if(upgrades.isEmpty()) {
            return false;
        }
        upgrades.get(0).click();
        return true;
    }

    public Integer updateQueue(ColonyEntity colony, String queueType) {
        List<WebElement> elements = webDriver.findElements(By.id(queueType));
        if(elements.isEmpty()) {
            return null;
        }
        WebElement queueElement = elements.get(0);
        List<WebElement> dataDetails = queueElement.findElements(By.className("data"));
        if (dataDetails.isEmpty()) {
            queueManger.clean(colony, queueType);
        } else {
            String timeString = queueElement.findElement(By.className("timer")).getText();
            Integer second = DateUtil.parseCountDownToSec(timeString);
            queueManger.set(colony, queueType, LocalDateTime.now().plusSeconds(second));
            return second;
        }
        return null;
    }

    public void updateGalaxy(SystemView systemView) {
        List<TargetEntity> targets = TargetDAO.getInstance().find(systemView.galaxy, systemView.system);
        for(WebElement row : webDriver.findElements(By.className("galaxyRow"))) {
            // skip table headers
            if(row.findElements(By.className("cellPosition")).isEmpty()) {
                continue;
            }

            //skip lear rows
            final int position = Integer.parseInt(row.findElement(By.className("cellPosition")).getText());
            Optional<TargetEntity> targetFromDb = targets.stream()
                    .filter(t -> t.position.equals(position))
                    .findAny();
            WebElement cellPlayerName = row.findElement(By.className("cellPlayerName"));
            if(targetFromDb.isPresent() && cellPlayerName.getText().trim().isEmpty()) {
                instance.removePlanet(targetFromDb.get().toPlanet());
            }
            if(cellPlayerName.getText().trim().isEmpty()) {
                continue;
            }
            List<WebElement> targetElement = cellPlayerName.findElements(By.className("tooltipRel"));
            // me on player list
            if(targetElement.isEmpty()) {
                continue;
            }
            final WebElement playerElement =targetElement.get(0);
            final String playerName = playerElement.getText().trim();
            final String playerCode = playerElement.getAttribute("rel");
            List<WebElement> isStatus = cellPlayerName.findElements(By.tagName("pre"));
            String status = "";
            if(!isStatus.isEmpty()) {
                status = isStatus.get(0).getText();
            }
            final String alliance = row.findElement(By.className("cellAlliance")).getText();



            if(StringUtils.isEmpty(playerName) && targetFromDb.isPresent()) {
                instance.removePlanet(targetFromDb.get().toPlanet());
                continue;
            }
            if(StringUtils.isEmpty(playerName) && !targetFromDb.isPresent()) {
                continue;
            }
            // nothing changed, nothing to process
            if(targetFromDb.isPresent() && status.equals(targetFromDb.get().player.status)) {
                continue;
            }

            TargetEntity entity;
            if(targetFromDb.isPresent()) {
                entity = targetFromDb.get();
            } else {
                entity = new TargetEntity();
                entity.galaxy = systemView.galaxy;
                entity.system = systemView.system;
                entity.position = position;
            }
            PlayerEntity playerEntity = playerDAO.find(playerCode);
            playerEntity.name = playerName;
            playerEntity.alliance = alliance;
            playerEntity.status = status;
            playerEntity.type = setStatus(status);
            PlayerEntity savedPlayer = playerDAO.saveOrUpdate(playerEntity);

            entity.type = status;
            entity.player = savedPlayer;
            targetDAO.saveOrUpdate(entity);
        }

        galaxyDAO.update(systemView);
    }

    public static String setStatus(String status) {
        if (status.contains("A")) {
            return TargetEntity.ADMIN;
        } else if (status.contains("u")) {
            return TargetEntity.ABSENCE;
        } else if (status.contains("i") || status.contains("I")) {
            return TargetEntity.IN_ACTIVE;
        } else if (status.contains("s")) {
            return TargetEntity.WEAK;
        } else {
            return TargetEntity.NORMAL;
        }
    }
}
