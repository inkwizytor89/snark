package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.UniverseEntity;

import java.util.List;

public interface UniverseDAO {

    List<UniverseEntity> fetchAllUniverses();

    String getMode(final Long id);
}
