package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.dao.PlanetDAO;
import org.enoch.snark.db.dao.impl.GalaxyDAOImpl;
import org.enoch.snark.db.dao.impl.PlanetDAOImpl;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.gi.command.CommandType;
import org.enoch.snark.gi.command.GICommand;
import org.enoch.snark.gi.macro.GIUrlBuilder;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.SystemView;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

public class GalaxyAnalyzeCommand extends GICommand {

    private PlanetDAO planetDAO = new PlanetDAOImpl(instance.universeEntity);
    private GalaxyDAO galaxyDAO = new GalaxyDAOImpl(instance.universeEntity);
    private final GIUrlBuilder giUrlBuilder;
    private SystemView systemView;

    public GalaxyAnalyzeCommand(Instance instance, SystemView systemView) {
        super(instance, CommandType.INTERFACE_REQUIERED);
        giUrlBuilder = new GIUrlBuilder(instance);
        this.systemView = systemView;
        normalize(this.systemView);
    }

    private void normalize(SystemView systemView) {
        systemView.galaxy = Math.floorMod(systemView.galaxy, instance.universeEntity.getGalaxyMax());
    }

    @Override
    public boolean execute() {
        final Optional<GalaxyEntity> galaxyEntity = galaxyDAO.find(systemView);
        if(galaxyEntity.isPresent()) {
            System.err.println(galaxyEntity.get().getUpdated());
        } else {
            System.err.println("pierwsza aktualizacja "+galaxyEntity);
        }
        if(galaxyEntity.isPresent() && DateUtil.lessThan20H(galaxyEntity.get().getUpdated())) {
            return true;
        }

        giUrlBuilder.openGalaxy(systemView.galaxy, systemView.system);
        final List<WebElement> rows = webDriver.findElements(By.className("row"));
        for(WebElement row : rows) {
            final int position = Integer.parseInt(row.findElement(By.className("position")).getText());
            final String status = row.findElement(By.className("status")).getText();
            System.err.println(position+": "+status);
            if(isAvailableFarm(status)) {
                final Optional<PlanetEntity> planetEntity = planetDAO.find(systemView.galaxy, systemView.system, position);
                if(!planetEntity.isPresent()) {
                    PlanetEntity entity = new PlanetEntity();
                    entity.setUniverse(instance.universeEntity);
                    entity.setGalaxy(systemView.galaxy);
                    entity.setSystem(systemView.system);
                    entity.setPosition(position);
                    entity.setType(PlanetEntity.IN_ACTIVE);
                    planetDAO.saveOrUpdatePlanet(entity);
                }
            }
        }

        galaxyDAO.update(systemView);
        return true;
    }

    private boolean isAvailableFarm(String status) {

        return !status.contains("u") && (status.contains("i") || status.contains("I"));
    }
}
