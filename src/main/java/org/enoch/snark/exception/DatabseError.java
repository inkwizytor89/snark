package org.enoch.snark.exception;

public interface DatabseError {
    String getMessage();

    StackTraceElement[] getStackTrace();

    String getAction();
    String getValue();
    String getPage();
}
