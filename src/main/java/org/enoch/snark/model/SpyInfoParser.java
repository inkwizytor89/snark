package org.enoch.snark.model;

import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Optional;

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
        Optional<Element> oFleet = document.getElementsByClass("section_title").stream()
                .filter(element -> element.text().contains("Floty"))
                .findFirst();
        if(oFleet.isPresent()) {
            String fleetText = oFleet.get().text().replace("Floty", "").trim();
            if(fleetText.isEmpty()) {
                planet.fleetSum = 0L;
            }
        }
    }

    private void extractDefense() {
        Optional<Element> oDefense = document.getElementsByClass("section_title").stream()
                .filter(element -> element.text().contains("Obrona"))
                .findFirst();
        if(oDefense.isPresent()) {
            String defenseText = oDefense.get().text().replace("Obrona", "").trim();
            if(defenseText.isEmpty()) {
                planet.defenseSum = 0L;
            }
        }
    }

    public TargetEntity extractPlanetEntity() {
        return planet;
    }
}
