package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.model.SystemView;

import java.util.List;
import java.util.Optional;

public interface GalaxyDAO extends AbstractDAO<GalaxyEntity> {
    void update(SystemView systemView);
    Optional<GalaxyEntity> find(SystemView systemView);
    List<GalaxyEntity> findLatestGalaxyToView();

}
