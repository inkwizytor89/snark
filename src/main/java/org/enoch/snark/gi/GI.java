package org.enoch.snark.gi;

import io.github.bonigarcia.wdm.WebDriverManager;
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
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.action.QueueManger;
import org.enoch.snark.instance.model.to.SystemView;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.si.module.building.BuildRequirements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.enoch.snark.instance.si.module.ConfigMap.WEBDRIVER_PATH;

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
    public static WebDriver webDriver;
    private final PlayerDAO playerDAO;
    private final Instance instance;
    private final GalaxyDAO galaxyDAO;
    private final TargetDAO targetDAO;
    private final QueueManger queueManger;

    private GI() {
        String pathToDriver = Instance.getMainConfigMap().getConfig(WEBDRIVER_PATH, "C:\\global\\selenium\\chromedriver.exe");
        if(!new File(pathToDriver).exists()) System.err.println("Missing file for driver "+pathToDriver);
        System.setProperty("webdriver.chrome.driver", pathToDriver);

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

    public static void reopenWebDriver() {
        if(webDriver != null) closeWebDriver();
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("start-maximized"); // open Browser in maximized mode
        options.addArguments("disable-infobars"); // disabling infobars
        options.addArguments("--disable-extensions"); // disabling extensions
        options.addArguments("--disable-gpu"); // applicable to Windows os only
        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        options.addArguments("--no-sandbox"); // Bypass OS security model
        options.addArguments("--disable-in-process-stack-traces");
        options.addArguments("--disable-logging");
        options.addArguments("--log-level=3");
        options.addArguments("--remote-allow-origins=*");

        webDriver = new ChromeDriver();
    }

    private static void closeWebDriver() {
        webDriver.quit();
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

//    public void doubleClickText(String text) {
//        try {
//            WebElement serverElement = findTextByXPath(text);
//            Actions actions = new Actions(webDriver);
//            actions.doubleClick(serverElement).perform();
//        } catch (NoSuchElementException e) {
//            System.err.println("Skip text '" + text + "' to click");
//        }
//    }

//    private WebElement findTextByXPath(String text) {
//        List<WebElement> elements = ((ChromeDriver) webDriver).findElementsByXPath("//*[contains(text(), '" + text + "')]");
//        int i = 1;
//        while(elements.isEmpty() && i <= 10) {
//            SleepUtil.pause(i);
//            i++;
//            elements = ((ChromeDriver) webDriver).findElementsByXPath("//*[contains(text(), '" + text + "')]");
//        }
//
//        if(elements.isEmpty()) {
//            System.err.println("No element '" + text + "' to click");
//            return ((ChromeDriver) webDriver).findElementsByXPath("//*[contains(text(), '" + text + "')]").get(0);
//        } else if(elements.size() > 1) {
//            System.err.println("Were many elements '" + text + "' to click");
//        }
//        return elements.get(0);
//    }

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
        List<WebElement> elements = webDriver.findElements(By.xpath(using));
        int i = 1;
        while(elements.isEmpty() && i <= 10) {
            SleepUtil.pause(i);
            i++;
            elements = webDriver.findElements(By.xpath(using));
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
        if(!webDriver.findElements(By.className("lifeformTech14101")).isEmpty()) {
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
        } else if(!webDriver.findElements(By.className("lifeformTech13101")).isEmpty()) {
            colony.lifeformTech13101 = getLevel(technologies, "lifeformTech13101");
            colony.lifeformTech13102 = getLevel(technologies, "lifeformTech13102");
            colony.lifeformTech13103 = getLevel(technologies, "lifeformTech13103");
            colony.lifeformTech13104 = getLevel(technologies, "lifeformTech13104");
            colony.lifeformTech13105 = getLevel(technologies, "lifeformTech13105");
            colony.lifeformTech13106 = getLevel(technologies, "lifeformTech13106");
            colony.lifeformTech13107 = getLevel(technologies, "lifeformTech13107");
            colony.lifeformTech13108 = getLevel(technologies, "lifeformTech13108");
            colony.lifeformTech13109 = getLevel(technologies, "lifeformTech13109");
            colony.lifeformTech13110 = getLevel(technologies, "lifeformTech13110");
            colony.lifeformTech13111 = getLevel(technologies, "lifeformTech13111");
            colony.lifeformTech13112 = getLevel(technologies, "lifeformTech13112");
            colony.save();
        } else if(!webDriver.findElements(By.className("lifeformTech12101")).isEmpty()) {
            colony.lifeformTech12101 = getLevel(technologies, "lifeformTech12101");
            colony.lifeformTech12102 = getLevel(technologies, "lifeformTech12102");
            colony.lifeformTech12103 = getLevel(technologies, "lifeformTech12103");
            colony.lifeformTech12104 = getLevel(technologies, "lifeformTech12104");
            colony.lifeformTech12105 = getLevel(technologies, "lifeformTech12105");
            colony.lifeformTech12106 = getLevel(technologies, "lifeformTech12106");
            colony.lifeformTech12107 = getLevel(technologies, "lifeformTech12107");
            colony.lifeformTech12108 = getLevel(technologies, "lifeformTech12108");
            colony.lifeformTech12109 = getLevel(technologies, "lifeformTech12109");
            colony.lifeformTech12110 = getLevel(technologies, "lifeformTech12110");
            colony.lifeformTech12111 = getLevel(technologies, "lifeformTech12111");
            colony.lifeformTech12112 = getLevel(technologies, "lifeformTech12112");
            colony.save();
        } else if(!webDriver.findElements(By.className("lifeformTech11101")).isEmpty()) {
            colony.lifeformTech11101 = getLevel(technologies, "lifeformTech11101");
            colony.lifeformTech11102 = getLevel(technologies, "lifeformTech11102");
            colony.lifeformTech11103 = getLevel(technologies, "lifeformTech11103");
            colony.lifeformTech11104 = getLevel(technologies, "lifeformTech11104");
            colony.lifeformTech11105 = getLevel(technologies, "lifeformTech11105");
            colony.lifeformTech11106 = getLevel(technologies, "lifeformTech11106");
            colony.lifeformTech11107 = getLevel(technologies, "lifeformTech11107");
            colony.lifeformTech11108 = getLevel(technologies, "lifeformTech11108");
            colony.lifeformTech11109 = getLevel(technologies, "lifeformTech11109");
            colony.lifeformTech11110 = getLevel(technologies, "lifeformTech11110");
            colony.lifeformTech11111 = getLevel(technologies, "lifeformTech11111");
            colony.lifeformTech11112 = getLevel(technologies, "lifeformTech11112");
            colony.save();
        }
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
        } catch (NumberFormatException e) {
            return Long.parseLong(element.findElement(By.className(name)).findElement(By.className("level")).getAttribute("data-value"));
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

    public boolean      upgradeBuilding(BuildRequirements requirements) {
        WebElement technologies = webDriver.findElement(By.id(TECHNOLOGIES));
        WebElement buildingElement = technologies.findElement(By.className(requirements.request.building.name()));
        Long buildingLevel = getLevel(technologies, requirements.request.building.name());
        if(buildingLevel >= requirements.request.level) {
            System.err.println(requirements + " already achieved");
            return true;
        }
        List<WebElement> upgrades = buildingElement.findElements(By.className("upgrade"));
        if(upgrades.isEmpty()) {
            return false;
        }
        upgrades.get(0).click();
        System.err.println(requirements + " upgrade");
        return true;
    }

    public Long updateQueue(ColonyEntity colony, String queueType) {
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
            Long second = DateUtil.parseCountDownToSec(timeString)+0L;
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
            final WebElement playerElement = targetElement.get(0);
            final String playerName = playerElement.getText().trim();
            final String playerCode = playerElement.getAttribute("rel").substring(6);
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
                entity.type = ColonyType.PLANET;
            }
            PlayerEntity playerEntity = playerDAO.find(playerCode);
            playerEntity.name = playerName;
            playerEntity.alliance = alliance;
            playerEntity.status = status;
            playerEntity.type = setStatus(status);
            PlayerEntity savedPlayer = playerDAO.saveOrUpdate(playerEntity);

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
