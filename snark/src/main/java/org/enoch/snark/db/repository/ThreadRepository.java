package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadRepository extends JpaRepository<ThreadEntity, Long> {

    Optional<ThreadEntity> findByModuleAndName(ModuleEntity module, String name);

    List<ThreadEntity> findByModule(ModuleEntity module);

    @EntityGraph(attributePaths = {"module", "configs"})
    @Query("select distinct t from ThreadEntity t")
    List<ThreadEntity> findAllWithModule();

    @Query("select t from ThreadEntity t where t.module.name = ?1 and t.name = ?2")
    Optional<ThreadEntity> find(String moduleName, String threadName);
}
