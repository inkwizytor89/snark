package org.enoch.snark.instance.model.exception;

public class NotEnoughResources extends RuntimeException {

    public NotEnoughResources() {
        super();
    }
    public NotEnoughResources(String message) {
        super(message);
    }
}
