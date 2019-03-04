package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.model.SystemView;

import java.util.List;
import java.util.Optional;

public interface GalaxyDAO {
    void saveOrUpdate(GalaxyEntity galaxyEntity);
    void update(SystemView systemView);
    Optional<GalaxyEntity> find(SystemView systemView);
    List<GalaxyEntity> findLatestGalaxyToView();

}
