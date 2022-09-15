package org.enoch.snark.model;

import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.text.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpyInfoParser {
    private String content;
    private final Document document;
    private TargetEntity planet;

    public SpyInfoParser(String messageContent) {
        content = messageContent;
        document = Jsoup.parse(messageContent);
        extractPlanet();
    }

    private void extractPlanet() {
        String title = document.getElementsByClass("msg_title new blue_txt").text();
        planet = new TargetEntity(extractCoordinateFromTitle(title));
        extractResource();
        extractFleet();
        extractDefense();
        planet.calculateDefenseAndShips();
    }

    private String extractCoordinateFromTitle(String input) {
        final String[] inputParts = input.split("\\s+");
        return inputParts[inputParts.length-1];
    }

    private void extractResource() {
        String[] resourcesParts = document.getElementsByAttributeValue("data-type", "resources").text().split("\\s+");
        try {
            planet.metal = PlanetEntity.parseResource(resourcesParts[0]);
            planet.crystal = PlanetEntity.parseResource(resourcesParts[1]);
            planet.deuterium = PlanetEntity.parseResource(resourcesParts[2]);
            planet.energy = PlanetEntity.parseResource(resourcesParts[3]);
        } catch (NumberFormatException e) {
            System.err.println("format problem");
        }
    }

    private void extractFleet() {//section_title
        if(!isSectionAvavible(Text.FLEET_TAG)) {
            planet.fighterLight = getMessageElementValue(Text.LM);
            planet.fighterHeavy = getMessageElementValue(Text.CM);
            planet.cruiser = getMessageElementValue(Text.KR);
            planet.battleship = getMessageElementValue(Text.OW);
            planet.interceptor = getMessageElementValue(Text.PAN);
            planet.bomber = getMessageElementValue(Text.BOM);
            planet.destroyer = getMessageElementValue(Text.NI);
            planet.deathstar = getMessageElementValue(Text.GS);
            planet.transporterSmall = getMessageElementValue(Text.LT);
            planet.transporterLarge = getMessageElementValue(Text.DT);
            planet.colonyShip = getMessageElementValue(Text.KOL);
            planet.recycler = getMessageElementValue(Text.REC);
            planet.espionageProbe = getMessageElementValue(Text.SON);
            planet.sat = getMessageElementValue(Text.SAT);
        }
    }

    private void extractDefense() {
        if(!isSectionAvavible(Text.DEFENSE_TAG)) {
            planet.rocketLauncher = getMessageElementValue(Text.DEF_WR);
            planet.laserCannonLight = getMessageElementValue(Text.DEF_LDL);
            planet.laserCannonHeavy = getMessageElementValue(Text.DEF_CDL);
            planet.gaussCannon = getMessageElementValue(Text.DEF_DG);
            planet.ionCannon = getMessageElementValue(Text.DEF_DJ);
            planet.plasmaCannon = getMessageElementValue(Text.DEF_WP);
            planet.shieldDomeSmall = getMessageElementValue(Text.DEF_MPO);
            planet.shieldDomeLarge = getMessageElementValue(Text.DEF_DPO);
            planet.missileInterceptor = getMessageElementValue(Text.DEF_PR);
            planet.missileInterplanetary = getMessageElementValue(Text.DEF_MR);
        }
    }

    private boolean isSectionAvavible(String section) {
        String textContent = document.text();
        Pattern pattern = Pattern.compile(section +"\\s"+Text.UNKNOWN_DATA);
        Matcher matcher = pattern.matcher(textContent);
        return matcher.find();
    }

    private Long getMessageElementValue(String elementTag) {
        String textContent = document.text();
        Pattern pattern = Pattern.compile(elementTag +"\\s([0-9]+)");
        Matcher matcher = pattern.matcher(textContent);
        if(matcher.find()) {
            String group = matcher.group(1);
            return Long.parseLong(group);
        }
        return 0L;
    }

    public TargetEntity extractPlanetEntity() {
        return planet;
    }
}
