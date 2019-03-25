package org.enoch.snark.model;

import org.enoch.snark.db.entity.PlanetEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
        planet = new PlanetEntity();
        Elements title = document.getElementsByClass("msg_title new blue_txt");

    }

    public PlanetEntity createPlanet() {
        return planet;
    }
}
