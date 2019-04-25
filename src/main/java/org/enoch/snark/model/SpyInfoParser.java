package org.enoch.snark.model;

import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.gi.text.Marker;
import org.enoch.snark.gi.text.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Optional;
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
        planet.metal = PlanetEntity.parseResource(resourcesParts[0]);
        planet.crystal = PlanetEntity.parseResource(resourcesParts[1]);
        planet.deuterium = PlanetEntity.parseResource(resourcesParts[2]);
        planet.power = PlanetEntity.parseResource(resourcesParts[3]);
    }

    private void extractFleet() {//section_title
        Optional<Element> oFleet = document.getElementsByClass(Marker.SECTION_TITLE).stream()
                .filter(element -> element.text().contains(Text.FLEET_TAG))
                .findFirst();
        if(oFleet.isPresent()) {
            String fleetText = oFleet.get().text().replace(Text.FLEET_TAG, Text.EMPTY).trim();
            if(fleetText.isEmpty()) {
                planet.fleetSum = 0L;
            }
        }
    }

    private void extractDefense() {
        planet.defWr = getMessageElementValue(Text.DEF_WR);
        planet.defLdl = getMessageElementValue(Text.DEF_LDL);
        planet.defCdl = getMessageElementValue(Text.DEF_CDL);
        planet.defDg = getMessageElementValue(Text.DEF_DG);
        planet.defDj = getMessageElementValue(Text.DEF_DJ);
        planet.defWp = getMessageElementValue(Text.DEF_WP);
        planet.defMpo = getMessageElementValue(Text.DEF_MPO);
        planet.defDpo = getMessageElementValue(Text.DEF_DPO);
        planet.defPr = getMessageElementValue(Text.DEF_PR);
        planet.defMr = getMessageElementValue(Text.DEF_MR);
    }

    private int getMessageElementValue(String elementTag) {
        String textContent = document.text();
        Pattern pattern = Pattern.compile(elementTag +"\\s([0-9]+)");
        Matcher matcher = pattern.matcher(textContent);
        if(matcher.find()) {
            String group = matcher.group(1);
            return Integer.parseInt(group);
        }
        return 0;
    }

    public TargetEntity extractPlanetEntity() {
        return planet;
    }
}
