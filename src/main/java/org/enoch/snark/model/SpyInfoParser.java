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
        planet.metal = PlanetEntity.parseResource(resourcesParts[0]);
        planet.crystal = PlanetEntity.parseResource(resourcesParts[1]);
        planet.deuterium = PlanetEntity.parseResource(resourcesParts[2]);
        planet.power = PlanetEntity.parseResource(resourcesParts[3]);
    }

    private void extractFleet() {//section_title
        if(!isSectionAvavible(Text.FLEET_TAG)) {
            planet.lm = getMessageElementValue(Text.LM);
            planet.cm = getMessageElementValue(Text.CM);
            planet.kr = getMessageElementValue(Text.KR);
            planet.ow = getMessageElementValue(Text.OW);
            planet.pan = getMessageElementValue(Text.PAN);
            planet.bom = getMessageElementValue(Text.BOM);
            planet.ni = getMessageElementValue(Text.NI);
            planet.gs = getMessageElementValue(Text.GS);
            planet.mt = getMessageElementValue(Text.LT);
            planet.dt = getMessageElementValue(Text.DT);
            planet.kol = getMessageElementValue(Text.KOL);
            planet.rec = getMessageElementValue(Text.REC);
            planet.son = getMessageElementValue(Text.SON);
            planet.sat = getMessageElementValue(Text.SAT);
        }
    }

    private void extractDefense() {
        if(!isSectionAvavible(Text.DEFENSE_TAG)) {
            planet.wr = getMessageElementValue(Text.DEF_WR);
            planet.ldl = getMessageElementValue(Text.DEF_LDL);
            planet.cdl = getMessageElementValue(Text.DEF_CDL);
            planet.dg = getMessageElementValue(Text.DEF_DG);
            planet.dj = getMessageElementValue(Text.DEF_DJ);
            planet.wp = getMessageElementValue(Text.DEF_WP);
            planet.mpo = getMessageElementValue(Text.DEF_MPO);
            planet.dpo = getMessageElementValue(Text.DEF_DPO);
            planet.pr = getMessageElementValue(Text.DEF_PR);
            planet.mr = getMessageElementValue(Text.DEF_MR);
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
