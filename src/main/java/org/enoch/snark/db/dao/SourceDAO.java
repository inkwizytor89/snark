package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.SourceEntity;

import java.util.List;

public interface SourceDAO {
    void saveOrUpdate(SourceEntity sourceEntity);
    List<SourceEntity> fetchAll();
}
