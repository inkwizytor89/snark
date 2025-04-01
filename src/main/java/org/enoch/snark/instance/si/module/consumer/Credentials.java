package org.enoch.snark.instance.si.module.consumer;

public interface Credentials {
    String login();
    String password();
    String token();
    String server();
    String hash();
}
