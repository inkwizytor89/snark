package org.enoch.snark.instance.si.module.define;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.enoch.common.config.ThreadDefinition;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.ConfigRepository;
import org.enoch.snark.db.repository.QueryService;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.hibernate.exception.SQLGrammarException;

import java.util.List;
import java.util.Set;

import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Slf4j
@RequiredArgsConstructor
public class DefineThread extends AbstractThread {

    public static final String threadType = DefineDefinition.THREAD_TYPE;
    public static final String NOT_NULL_STRATEGY = DefineDefinition.NOT_NULL_STRATEGY;

    private final QueryService queryService;
    private final CacheEntryRepository cacheEntryRepository;
    private final ConfigRepository configRepository;
    private DefineDefinition config;

    @Override
    protected void onStart() {
        super.onStart();
        config = new DefineDefinition();
        config.update(configRepository.findByThread_Module_NameAndThread_Name(
                threadEntity.module.name, "main"));
        config.update(threadEntity.configs);
    }

    @Override
    protected void onStep() {
        //todo on start write in debug all tables and fields
        //todo remove older als one week
        Set<String> ignoredKeys = Set.of(MODULE, NAME, TYPE, PAUSE, TIME, DEBUG, COMMAND_LIMIT, CONDITIONS, STRATEGY);
        threadEntity.configs.stream()
                .filter(threadConfig -> !ignoredKeys.contains(threadConfig.key))
                .forEach(threadConfig -> {
                    try {
                        String result = String.join(ARRAY_SEPARATOR, queryService.runString(threadConfig.value));
                        String strategy = config.getStrategy() == null
                                ? Strings.EMPTY
                                : config.getStrategy().toLowerCase();
                        if(StringUtils.isEmpty(result) && NOT_NULL_STRATEGY.equals(strategy)) {
                            log("CacheEntry key "+threadConfig.key+" and value "+result+" - skipping for not_null strategy");
                            return;
                        } else log("CacheEntry key "+threadConfig.key+" and value "+result);
                        String oldValue = cacheEntryRepository.getValue(threadConfig.key);
                        if(oldValue == null || !oldValue.equals(result)) {
                            log("set " + threadConfig.key + " = " + result + "(" + oldValue + ")");
                            cacheEntryRepository.setValue(threadConfig.key, result);
                        }
                    } catch (SQLGrammarException e) {
                        System.err.println(e.getMessage());
                        SleepUtil.secondsToSleep(30L);
                    }
                });
    }

    @Override
    public ThreadDefinition threadConfiguration() {
        return config.threadConfiguration();
    }
}
