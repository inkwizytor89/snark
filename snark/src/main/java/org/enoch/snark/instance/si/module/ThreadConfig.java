package org.enoch.snark.instance.si.module;

import org.enoch.common.config.ThreadDefinition;

public interface ThreadConfig {

    ThreadDefinition threadConfiguration(String thread, String module);
}
