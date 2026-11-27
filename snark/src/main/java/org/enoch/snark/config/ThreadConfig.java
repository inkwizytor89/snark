package org.enoch.snark.config;

import org.enoch.common.config.ThreadDefinition;

public interface ThreadConfig {

    ThreadDefinition threadConfiguration(String thread, String module);
}
