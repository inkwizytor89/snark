package org.enoch.snark.db.dao;

import org.enoch.snark.db.entity.ErrorEntity;
import org.enoch.snark.exception.DatabseError;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ErrorDAO extends AbstractDAO<ErrorEntity> {

    private static ErrorDAO INSTANCE;

    private ErrorDAO() {
        super();
    }

    public static ErrorDAO getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ErrorDAO();
        }
        return INSTANCE;
    }

    @Override
    protected Class<ErrorEntity> getEntityClass() {
        return ErrorEntity.class;
    }

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
