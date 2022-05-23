package org.enoch.snark.exception;

public class GIException extends MainException implements DatabseError {

    public static final String NOT_FOUND = "not found element";
    public static final String TOO_MANY = "too many elements";

    public GIException(String action, String value, String message) {
        super(message);
        this.action = action;
        this.value = value;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getPage() {
        return page;
    }
}
