package org.enoch.snark.gi;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.exception.GIException;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Utils;
import org.enoch.snark.instance.commander.QueueManger;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.model.Planet;
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
import java.util.concurrent.TimeUnit;

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
    private QueueManger queueManger;

    private GI() {
        webDriver = new ChromeDriver();
        queueManger = QueueManger.getInstance();
    }

    public static GI getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new GI();
        }
        return INSTANCE;
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
            sleep(TimeUnit.MILLISECONDS, 200 * i);
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
            sleep(TimeUnit.MILLISECONDS, 200 * i);
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
            sleep(TimeUnit.MILLISECONDS, 200 * i);
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

    public List<EventFleet> readEventFleet() {
        List<EventFleet> eventFleets = new ArrayList<>();
        try {
            List<WebElement> eventHeader = webDriver.findElements(By.id("eventHeader"));
            if(!eventHeader.isEmpty()) {
                if(eventHeader.get(0).isDisplayed())
                    webDriver.findElement(By.className("event_list")).click();
            }
            Utils.sleep();
            webDriver.findElement(By.className("event_list")).click();
            Utils.sleep();
            List<WebElement> tableRows = new WebDriverWait(webDriver, 5)
                            .until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(webDriver.findElement(By.id("eventContent")), By.tagName(TR_TAG)));

            for (WebElement webElement : tableRows) {
                EventFleet eventFleet = new EventFleet();
                WebElement countDown = webElement.findElement(By.className("countDown"));
                List<WebElement> hostile = countDown.findElements(By.className("hostile"));
                eventFleet.isForeign = !hostile.isEmpty();
                eventFleet.countDown =  countDown.getText();
                eventFleet.arrivalTime =  webElement.findElement(By.className("arrivalTime")).getText();
                eventFleet.arrivalTime =  webElement.findElement(By.className("arrivalTime")).getText();
                eventFleet.missionFleet =  webElement.findElement(By.className("missionFleet")).findElement(By.className("tooltipHTML")).getAttribute(TITLE_ATTRIBUTE);
                eventFleet.originFleet =  webElement.findElement(By.className("originFleet")).getText();
                eventFleet.coordsOrigin =  webElement.findElement(By.className("coordsOrigin")).getText();
                eventFleet.detailsFleet =  webElement.findElement(By.className("detailsFleet")).getText();
                eventFleet.iconMovement =  "?";
                eventFleet.destFleet =  webElement.findElement(By.className("destFleet")).getText();
                eventFleet.destCoords =  webElement.findElement(By.className("destCoords")).getText();
                eventFleet.sendProbe =  webElement.findElement(By.className("sendProbe")).getText();
                List<WebElement> player = webElement.findElements(By.className("sendMail"));
                if(player.size() > 1) {
                    eventFleet.sendMail =  player.get(1).getAttribute(HREF_ATTRIBUTE);
                } else {
                    eventFleet.sendMail = "";
                }

                eventFleets.add(eventFleet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eventFleets;
    }

    public void sleep(TimeUnit timeUnit, int i) {
        try {
            timeUnit.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void updateColony(ColonyEntity colony) {
        new GIUrlBuilder().open(PAGE_RESOURCES, colony);
        new GIUrlBuilder().open(PAGE_FACILITIES, colony);
        if(isLifeformAvailable()) {
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
        if (webDriver.findElements(By.id("warning")).size() > 0) {
            return;
        }
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
        colony.save();

    }

    public void updateLifeform(ColonyEntity colony) {
        List<WebElement> raceToChoose = webDriver.findElements(By.className("lfsettingsContent"));
        if(!raceToChoose.isEmpty()) {
            return;
        }
        WebElement technologies = webDriver.findElement(By.id(TECHNOLOGIES));
        colony.lifeformTech14101 = getLevel(technologies,"lifeformTech14101");
        colony.lifeformTech14102 = getLevel(technologies,"lifeformTech14102");
        colony.lifeformTech14103 = getLevel(technologies,"lifeformTech14103");
        colony.lifeformTech14104 = getLevel(technologies,"lifeformTech14104");
        colony.lifeformTech14105 = getLevel(technologies,"lifeformTech14105");
        colony.lifeformTech14106 = getLevel(technologies,"lifeformTech14106");
        colony.lifeformTech14107 = getLevel(technologies,"lifeformTech14107");
        colony.lifeformTech14108 = getLevel(technologies,"lifeformTech14108");
        colony.lifeformTech14109 = getLevel(technologies,"lifeformTech14109");
        colony.lifeformTech14110 = getLevel(technologies,"lifeformTech14110");
        colony.lifeformTech14111 = getLevel(technologies,"lifeformTech14111");
        colony.lifeformTech14112 = getLevel(technologies,"lifeformTech14112");
        colony.save();
    }

    public boolean isLifeformAvailable() {
        return !webDriver.findElements(By.id("lifeform")).isEmpty();
    }

    public void updateFacilities(ColonyEntity colony) {
        WebElement technologies = webDriver.findElement(By.id(TECHNOLOGIES));
        colony.roboticsFactory = getLevel(technologies,"roboticsFactory");
        colony.shipyard = getLevel(technologies,"shipyard");
        colony.researchLaboratory = getLevel(technologies,"researchLaboratory");
        colony.allianceDepot = getLevel(technologies,"allianceDepot");
        colony.missileSilo = getLevel(technologies,"missileSilo");
        colony.naniteFactory = getLevel(technologies,"naniteFactory");
        colony.terraformer = getLevel(technologies,"terraformer");
        colony.repairDock = getLevel(technologies,"repairDock");
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
        return Long.parseLong(element.findElement(By.className(name)).findElement(By.className("level")).getText());
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
        List<WebElement> coloniesWebElements = new WebDriverWait(webDriver, 5)
                .until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(webDriver.findElement(By.id("planetList")), By.tagName(DIV_TAG)));
        for(WebElement colonyWebElement : coloniesWebElements) {
            try {
                ColonyEntity colonyEntity = new ColonyEntity();

                colonyEntity.cp = Integer.parseInt(colonyWebElement.getAttribute(ID_ATTRIBUTE).split("-")[1]);
                Planet planet = new Planet(colonyWebElement.findElement(By.className("planet-koords ")).getText());
                colonyEntity.galaxy = planet.galaxy;
                colonyEntity.system = planet.system;
                colonyEntity.position = planet.position;

                List<WebElement> moons = colonyWebElement.findElements(By.className("moonlink"));
                if(!moons.isEmpty()) {
                    String link = moons.get(0).getAttribute(HREF_ATTRIBUTE);
                    int i = link.indexOf("cp=");
                    colonyEntity.cpm = Integer.parseInt(link.substring(i+3));
                }

                colonyEntities.add(colonyEntity);
                System.err.println(colonyEntity.getCordinate()+" "+colonyEntity.cp+" "+colonyEntity.cpm);
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
            Long second = DateUtil.parseCountDownToSec(timeString);
            queueManger.set(colony, queueType, LocalDateTime.now().plusSeconds(second));
            return second;
        }
        return null;
    }
}
