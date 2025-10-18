package org.enoch.snark.instance.si.module.consumer;

import org.enoch.snark.common.time.Duration;

public interface Credentials {
    String login();
    String password();
    String token();
    String server();
    String hash();
    Duration restartDuration();
}
