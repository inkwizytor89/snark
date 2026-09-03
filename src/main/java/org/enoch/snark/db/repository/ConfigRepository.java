package org.enoch.snark.db.repository;

import org.enoch.snark.config.ConfigTerm;
import org.enoch.snark.db.entity.ConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ConfigRepository extends JpaRepository<ConfigEntity, Long> {

    Optional<ConfigEntity> findByModuleAndThreadAndKey(
            String module,
            String thread,
            String key
    );
    List<ConfigEntity> findByModule(String module);

    List<ConfigEntity> findByModuleAndThread(String module, String thread);

    default void saveAll(List<ConfigTerm> configList) {
        List<ConfigEntity> entityList = configList.stream().map(ConfigEntity::new).collect(Collectors.toUnmodifiableList());
        entityList.forEach(configEntity -> findByModuleAndThreadAndKey(
                configEntity.getModule(),
                configEntity.getThread(),
                configEntity.getKey()
        ).ifPresentOrElse((config ) -> {
            config.setValue(configEntity.getValue());
        }, () -> {
            configEntity.setValue(configEntity.getValue());
            save(configEntity);
        }));
    }
}