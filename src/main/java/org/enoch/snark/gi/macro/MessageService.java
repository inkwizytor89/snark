package org.enoch.snark.gi.macro;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SpyInfo;
import org.enoch.snark.model.WarInfo;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MessageService {
    private static final Logger log = Logger.getLogger( MessageService.class.getName() );
    private Instance instance;

    private Map<String, SpyInfo> spyMesseges = new HashMap<>();

    public MessageService(Instance instance) {
        this.instance = instance;
    }

    private void loadSpyFile(File file) {
        final SpyInfo info = new SpyInfo(file);
        if(info.planet != null) spyMesseges.put(info.messageId, info);
    }

    public void storeSpyMessage(List<String> links) {
        for(String link : links) {
            storeSpyMessage(link);
        }
        new GIUrlBuilder(instance).openOverview();
    }

    private void storeSpyMessage(String link) {
        String messageId = getMessageIdFromLink(link);
        if(spyMesseges.containsKey(messageId)) {
            return;
        }
        File file = storeAsFile(spyDir, messageId, link);
        File finalFile = new File(spyDir.getAbsolutePath()+"/" +
                new SpyInfo(file).planet.toFileName() + "#" +messageId);
        final boolean renameCorrect = file.renameTo(finalFile);
        if(!renameCorrect) log.log(Level.SEVERE, "Cann not store report "+finalFile.getAbsolutePath());
        loadSpyFile(finalFile);
    }

    private String getMessageIdFromLink(String link) {
        Pattern pattern = Pattern.compile("messageId=(.*?)&tabid");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        return StringUtils.EMPTY;
    }

    private File storeAsFile(File dir, String messageId, String link) {
        final File newFile = new File(dir, messageId);
        try {
            final WebDriver webDriver = null;//instance.session.getWebDriver();
            webDriver.get(link);
            String text = messageId +"\n"+webDriver.getPageSource();

            newFile.createNewFile();
            try(PrintWriter out = new PrintWriter(newFile)){
                out.println(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newFile;
    }

    private void loadWarMesseges() {
    }

    public SpyInfo getLastSpyInfo(Planet planet) {
        List<SpyInfo> requieredSpyInfo = new ArrayList<>();
        for (SpyInfo info : spyMesseges.values()) {
            if(info.planet.equals(planet) ) {
                requieredSpyInfo.add(info);
            }
        }
        return returnNewest(requieredSpyInfo);
    }

    private SpyInfo returnNewest(List<SpyInfo> infos) {
        if(infos.size() < 1) return null;
        SpyInfo newestInfo = infos.get(0);
        for (SpyInfo info : infos) {
            if(info.date.isAfter(newestInfo.date)) {
                newestInfo = info;
            }
        }
        return newestInfo;
    }


}