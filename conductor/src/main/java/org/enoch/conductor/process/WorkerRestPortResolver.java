package org.enoch.conductor.process;

public final class WorkerRestPortResolver {
    private static final int FIRST_PORT = 20000;
    private static final int PORT_RANGE = 20000;

    private WorkerRestPortResolver() {
    }

    public static int resolve(String instanceId) {
        int hash = 0;
        for (char character : instanceId.toCharArray()) {
            hash = 31 * hash + character;
        }
        return FIRST_PORT + (hash & Integer.MAX_VALUE) % PORT_RANGE;
    }
}
