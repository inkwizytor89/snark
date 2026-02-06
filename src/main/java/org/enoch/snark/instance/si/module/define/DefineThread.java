package org.enoch.snark.instance.si.module.define;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.QueryService;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.hibernate.exception.SQLGrammarException;

import java.util.List;

import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Slf4j
@RequiredArgsConstructor
public class DefineThread extends AbstractThread {

    public static final String threadType = "define";
    public static final String NOT_NULL_STRATEGY = "not_null";

    private final QueryService queryService;
    private final CacheEntryRepository cacheEntryRepository;

    @Override
    protected void onStep() {
        //todo on start write in debug all tables and fields
        //todo remove older als one week
        map.entrySet().stream()
                .filter(entry -> !List.of(MODULE, NAME, TYPE, PAUSE, TIME, DEBUG, COMMAND_LIMIT, CONDITIONS, STRATEGY).contains(entry.getKey()))
                .forEach(entry -> {
                    try {
                        String result = String.join(ARRAY_SEPARATOR, queryService.runString(entry.getValue()));
                        if(StringUtils.isEmpty(result) && NOT_NULL_STRATEGY.equals(map().getConfig(STRATEGY, Strings.EMPTY).toLowerCase())) {
                            log("CacheEntry key "+entry.getKey()+" and value "+result+" - skipping for not_null strategy");
                            return;
                        } else log("CacheEntry key "+entry.getKey()+" and value "+result);
                        String oldValue = cacheEntryRepository.getValue(entry.getKey());
                        if(oldValue == null || !oldValue.equals(result)) {
                            log("set " + entry.getKey() + " = " + result + "(" + oldValue + ")");
                            cacheEntryRepository.setValue(entry.getKey(), result);
                        }
                    } catch (SQLGrammarException e) {
                        System.err.println(e.getMessage());
                        SleepUtil.secondsToSleep(30L);
                    }
                });
    }
}
