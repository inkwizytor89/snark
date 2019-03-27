package org.enoch.snark.model;

import org.enoch.snark.db.entity.PlanetEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.time.format.DateTimeFormatter;

public class SpyInfoParser {
    private String content;
    private final Document document;
    private PlanetEntity planet;

    public SpyInfoParser(String messageContent) {
        content = messageContent;
        document = Jsoup.parse(messageContent);
        extractPlanet();
    }

    private void extractPlanet() {
        String title = document.getElementsByClass("msg_title new blue_txt").text();
        planet = new PlanetEntity(extractCoordinateFromTitle(title));
        extractResource();
    }

    private void extractResource() {

        String[] resourcesParts = document.getElementsByAttributeValue("data-type", "resources").text().split("\\s+");
        planet.metal = Long.parseLong(resourcesParts[0].replace(".",""));
        planet.crystal = Long.parseLong(resourcesParts[1].replace(".",""));
        planet.deuterium = Long.parseLong(resourcesParts[2].replace(".",""));
        planet.power = Long.parseLong(resourcesParts[3].replace(".",""));
    }

    private String extractCoordinateFromTitle(String input) {
        final String[] inputParts = input.split("\\s+");
        return inputParts[inputParts.length-1];
    }

    public PlanetEntity extractPlanetEntity() {
        return planet;
    }
}
