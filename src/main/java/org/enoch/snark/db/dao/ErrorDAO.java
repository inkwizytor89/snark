package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.ErrorEntity;
import org.enoch.snark.exception.DatabseError;

public interface ErrorDAO extends AbstractDAO<ErrorEntity> {

    void save(DatabseError error);

}
