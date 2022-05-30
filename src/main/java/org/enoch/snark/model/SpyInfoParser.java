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
        planet.planet.metal = PlanetEntity.parseResource(resourcesParts[0]);
        planet.planet.crystal = PlanetEntity.parseResource(resourcesParts[1]);
        planet.planet.deuterium = PlanetEntity.parseResource(resourcesParts[2]);
        planet.planet.power = PlanetEntity.parseResource(resourcesParts[3]);
    }

    private void extractFleet() {//section_title
        if(!isSectionAvavible(Text.FLEET_TAG)) {
            planet.planet.lm = getMessageElementValue(Text.LM);
            planet.planet.cm = getMessageElementValue(Text.CM);
            planet.planet.kr = getMessageElementValue(Text.KR);
            planet.planet.ow = getMessageElementValue(Text.OW);
            planet.planet.pan = getMessageElementValue(Text.PAN);
            planet.planet.bom = getMessageElementValue(Text.BOM);
            planet.planet.ni = getMessageElementValue(Text.NI);
            planet.planet.gs = getMessageElementValue(Text.GS);
            planet.planet.mt = getMessageElementValue(Text.LT);
            planet.planet.dt = getMessageElementValue(Text.DT);
            planet.planet.kol = getMessageElementValue(Text.KOL);
            planet.planet.rec = getMessageElementValue(Text.REC);
            planet.planet.son = getMessageElementValue(Text.SON);
            planet.planet.sat = getMessageElementValue(Text.SAT);
        }
    }

    private void extractDefense() {
        if(!isSectionAvavible(Text.DEFENSE_TAG)) {
            planet.planet.wr = getMessageElementValue(Text.DEF_WR);
            planet.planet.ldl = getMessageElementValue(Text.DEF_LDL);
            planet.planet.cdl = getMessageElementValue(Text.DEF_CDL);
            planet.planet.dg = getMessageElementValue(Text.DEF_DG);
            planet.planet.dj = getMessageElementValue(Text.DEF_DJ);
            planet.planet.wp = getMessageElementValue(Text.DEF_WP);
            planet.planet.mpo = getMessageElementValue(Text.DEF_MPO);
            planet.planet.dpo = getMessageElementValue(Text.DEF_DPO);
            planet.planet.pr = getMessageElementValue(Text.DEF_PR);
            planet.planet.mr = getMessageElementValue(Text.DEF_MR);
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
