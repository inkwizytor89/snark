package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.ConfigEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigEntity, Long> {

    Optional<ConfigEntity> findByThreadAndKey(
            ThreadEntity thread, String key);

    List<ConfigEntity> findByThread_Module_NameAndThread_Name(String moduleName, String threadName);
}