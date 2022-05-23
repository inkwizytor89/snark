package org.enoch.snark.db.dao.impl;

import org.enoch.snark.db.dao.ErrorDAO;
import org.enoch.snark.db.entity.ErrorEntity;
import org.enoch.snark.db.entity.UniverseEntity;
import org.enoch.snark.exception.DatabseError;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ErrorDAOImpl extends AbstractDAOImpl<ErrorEntity> implements ErrorDAO {

    public ErrorDAOImpl(UniverseEntity universeEntity) {
        super(universeEntity);
    }

    @Override
    protected Class<ErrorEntity> getEntitylass() {
        return ErrorEntity.class;
    }

    @Override
    public void save(DatabseError error) {
        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.page = error.getPage();
        errorEntity.action = error.getAction();
        errorEntity.message = error.getMessage();
        errorEntity.value = error.getValue();
        Arrays.stream(error.getStackTrace())
                .map(StackTraceElement::toString)
                .collect( Collectors.joining("\n"));
        saveOrUpdate(errorEntity);
    }
}
