package org.enoch.snark.instance.si;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.config.ConfigTerm;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RemotePropertiesMap {
    private  List<ConfigTerm> fileConfigs = new ArrayList<>();
    private  List<ConfigTerm> remoteConfigs = new ArrayList<>();

    public void addRemoteConfig(ConfigTerm config) {
        remoteConfigs.stream()
                .filter(c -> c.getKey().equals(config.getKey()) && c.getModule().equals(config.getModule()) && c.getThread().equals(config.getThread()))
                .findFirst()
                .ifPresent(remoteConfigs::remove);
        remoteConfigs.add(config);
    }

    public void resetRemoteConfig() {
        remoteConfigs = new ArrayList<>();
    }

    public String showConfig(String phrase) {
        List<ConfigTerm> configs = new ArrayList<>();
        final String key = phrase.trim().toLowerCase();
        if("remote".equals(key)) configs.addAll(remoteConfigs);
        else if("file".equals(key)) configs.addAll(fileConfigs);
        else if("all".equals(key) || StringUtils.isEmpty(key)) {
            configs.addAll(remoteConfigs);
            configs.addAll(fileConfigs);
        } else {
            fileConfigs.stream()
                    .filter(config -> config.toString().contains(key))
                    .forEach(configs::add);
            remoteConfigs.stream()
                    .filter(config -> config.toString().contains(key))
                    .forEach(configs::add);
        }
        configs.removeIf(x -> x.getKey().equals("password"));
        return configs.stream().map(ConfigTerm::toString).collect(Collectors.joining("\n"));
    }
}
