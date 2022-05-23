package org.enoch.snark.exception;

public abstract class MainException extends RuntimeException {
    protected String page;
    protected String request; //no fleet
    protected String type; // exception
    protected String action; // finding xpath
    protected String value; // //"+tag+"[@" + attribute + "='"+value+"']

    protected MainException(String message) {
        super(message);
    }
}
