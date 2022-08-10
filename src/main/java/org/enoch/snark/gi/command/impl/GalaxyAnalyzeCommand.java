package org.enoch.snark.gi.command.impl;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.GalaxyDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.dao.impl.GalaxyDAOImpl;
import org.enoch.snark.db.dao.impl.TargetDAOImpl;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.TargetEntity;
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

    private TargetDAO targetDAO;
    private GalaxyDAO galaxyDAO;
    private final GIUrlBuilder giUrlBuilder;
    private SystemView systemView;

    public GalaxyAnalyzeCommand(Instance instance, SystemView systemView) {
        super(instance, CommandType.INTERFACE_REQUIERED);
        targetDAO = instance.daoFactory.targetDAO;
        galaxyDAO = instance.daoFactory.galaxyDAO;
        giUrlBuilder = new GIUrlBuilder(instance);
        this.systemView = systemView;
        normalize(this.systemView);
    }

    private void normalize(SystemView systemView) {
        systemView.galaxy = Math.floorMod(systemView.galaxy, instance.universeEntity.galaxyMax);
    }

    @Override
    public boolean execute() {
        final Optional<GalaxyEntity> galaxyEntity = galaxyDAO.find(systemView);

        if(galaxyEntity.isPresent() && DateUtil.lessThanHours(22, galaxyEntity.get().updated)) {
            return true;
        }
        giUrlBuilder.openGalaxy(systemView.galaxy, systemView.system);
        List<TargetEntity> targets = instance.daoFactory.targetDAO.find(systemView.galaxy, systemView.system);
        for(WebElement row : webDriver.findElements(By.className("row"))) {
            final int position = Integer.parseInt(row.findElement(By.className("position")).getText());
            final String player = row.findElement(By.className("playername")).getText().trim();
            final String statusCode = row.findElement(By.className("status")).getText().trim();
            Optional<TargetEntity> targetFromDb = targets.stream().filter(t -> t.position.equals(position)).findAny();

            if(StringUtils.isEmpty(player) && targetFromDb.isPresent()) {
                instance.removePlanet(targetFromDb.get().toPlanet());
                continue;
            }
            if(StringUtils.isEmpty(player) && !targetFromDb.isPresent()) {
                continue;
            }
            String status = setStatus(statusCode);
            if(targetFromDb.isPresent() && status.equals(targetFromDb.get().type)) {
                continue;
            }
            TargetEntity entity;
            if(targetFromDb.isPresent()) {
                entity = targetFromDb.get();
            } else {
                entity = new TargetEntity();
                entity.universe = instance.universeEntity;
                entity.galaxy = systemView.galaxy;
                entity.system = systemView.system;
                entity.position = position;
            }
            entity.type = status;
            targetDAO.saveOrUpdate(entity);
        }

        galaxyDAO.update(systemView);
        return true;
    }

    private String setStatus(String status) {
        if (status.contains("A")) {
            return TargetEntity.ADMIN;
        } else if (status.contains("u")) {
            return TargetEntity.ABSENCE;
        } else if (status.contains("i") || status.contains("I")) {
            return TargetEntity.IN_ACTIVE;
        } else if (status.contains("s")) {
            return TargetEntity.WEAK;
        } else {
            return TargetEntity.NORMAL;
        }
    }
}
