package org.enoch.snark.instance;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.enoch.snark.instance.PropertyNames.*;

public class AppProperties {

    public String server;
    public String url;
    public String username;
    public String password;
    public String mode;
    public String config;
    public String pathToChromeWebdriver;
    //    private final int expeditionDt;
    //    private final String sleep;
    //    private final int galaxyNumber;
    //    private final int defenseModule;
    //    public final int farmModule;
    //    private final int aggressionModule;
    //    private final int expeditionModule;
    //    private final int researchModule;
    //    private final int buldingModule;
    //    private final int spaceScanModule;
    //    private final int samplingModule;
    //    private List<SourcePlanet> expeditionPlanets = new ArrayList<>();
    //    private List<SourcePlanet> fleetSave = new ArrayList<>();
//    public Integer fleetNumber;
//    public List<SourcePlanet> sourcePlanets = new ArrayList<>();


    public AppProperties(String pathToProperties) throws IOException {
        Properties properties = new java.util.Properties();
        FileInputStream fileInputStream = new FileInputStream(pathToProperties);
        properties.load(fileInputStream);

        url = properties.getProperty(URL);
        server = properties.getProperty(SERVER);
        username = properties.getProperty(LOGIN);
        password = properties.getProperty(PASSWORD);

        mode = properties.getProperty(MODE);
        config = properties.getProperty(CONFIG);

        pathToChromeWebdriver = properties.getProperty(WEBDRIVER_CHROME_DRIVER);
        System.setProperty(WEBDRIVER_CHROME_DRIVER, pathToChromeWebdriver);

//
//        fleetNumber = Integer.parseInt(properties.getProperty(FLEET_NUMBER));
//        String[] planets = properties.getProperty(PLANET_IDS).split(";");
//        for(String planetCode : planets) sourcePlanets.add(new SourcePlanet(planetCode));
//        String[] expeditionPlanetsTab = properties.getProperty(PLANET_EXPEDITION).split(";");
//        for(String planetCode : expeditionPlanetsTab) expeditionPlanets.add(new SourcePlanet(planetCode));
//        expeditionDt = Integer.parseInt(properties.getProperty(EXPEDITION_DT));
//        String[] fleetSaveTab = properties.getProperty(FLEET_SAVE).split(";");
//        for(String planetCode : fleetSaveTab) fleetSave.add(new SourcePlanet(planetCode));
//        sleep = properties.getProperty(SLEEP);
//        galaxyNumber = Integer.parseInt(properties.getProperty(GALAXY_NUMBER));
//
//        defenseModule = Integer.parseInt(properties.getProperty(DEFENSE_MODULE));
//        farmModule = Integer.parseInt(properties.getProperty(FARM_MODULE));
//        aggressionModule = Integer.parseInt(properties.getProperty(AGGRESSION_MODULE));
//        expeditionModule = Integer.parseInt(properties.getProperty(EXPEDITION_MODULE));
//        researchModule = Integer.parseInt(properties.getProperty(RESEARCH_MODULE));
//        buldingModule = Integer.parseInt(properties.getProperty(BULDING_MODULE));
//        spaceScanModule = Integer.parseInt(properties.getProperty(SCAN_SPACE_MODULE));
//        samplingModule = Integer.parseInt(properties.getProperty(SAMPLING_MODULE));


        fileInputStream.close();
    }
}
