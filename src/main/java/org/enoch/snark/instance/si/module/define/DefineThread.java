package org.enoch.snark.instance.si.module.define;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enoch.snark.db.repository.CacheEntryRepository;
import org.enoch.snark.db.repository.QueryService;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.util.List;

import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Slf4j
@RequiredArgsConstructor
public class DefineThread extends AbstractThread {

    public static final String threadType = "define";

    private final QueryService queryService;
    private final CacheEntryRepository cacheEntryRepository;

    @Override
    protected void onStep() {
        //todo on start write in debug all tables and fields
        //todo remove older als one week
        map.entrySet().stream()
                .filter(entry -> !List.of(NAME, TYPE, PAUSE, TIME).contains(entry.getKey()))
                .forEach(entry -> {
                    String result = String.join(ARRAY_SEPARATOR, queryService.runQuery(entry.getValue()));
                    log("ADD CacheEntry key "+entry.getKey()+" and value "+result);
                    cacheEntryRepository.setValue(entry.getKey(), result);
                });
    }
}
