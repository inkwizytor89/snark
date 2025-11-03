package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.instance.model.to.SystemView;
import org.enoch.snark.instance.model.uc.SystemUC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface GalaxyRepository extends JpaRepository<GalaxyEntity, Long> {

    List<GalaxyEntity> findByUpdatedIsNull();

    Optional<GalaxyEntity> findByGalaxyAndSystem(Integer galaxy, Integer system);

    List<GalaxyEntity> findByUpdatedBeforeOrderByUpdatedAsc(LocalDateTime date);

    @Transactional
    default void persistGalaxyMap(int galaxy, int systemCount) {
        List<GalaxyEntity> toPersist = new ArrayList<>();
            for (int j = 1; j <= systemCount; j++) {
                GalaxyEntity galaxyEntity = new GalaxyEntity();
                galaxyEntity.galaxy = galaxy;
                galaxyEntity.system = j;
                galaxyEntity.updated = null;
                toPersist.add(galaxyEntity);
            }
        this.saveAllAndFlush(toPersist);
    }

    default List<GalaxyEntity> findRange(Integer galaxy, int from, int to, int systemMax, boolean wrap) {
        if(wrap) {
            from = SystemUC.systemNormalize(from, systemMax);
            to = SystemUC.systemNormalize(to, systemMax);
        }
        if (from <= to) {
            return findInRange(galaxy, from, to);
        } else if(wrap){
            return findOutRange(galaxy, from, to, systemMax);
        }
        throw new IllegalStateException("Unknown system range from="+from+" to="+to);
    }

    // bez zawijania
    @Query("""
        FROM GalaxyEntity g
        WHERE g.galaxy = :galaxy
          AND g.system BETWEEN :from AND :to
        ORDER BY g.system
        """)
    List<GalaxyEntity> findInRange(@Param("galaxy") Integer galaxy,
                                           @Param("from") Integer from,
                                           @Param("to") Integer to);

    // z zawijaniem — czyli dwa zakresy: [from..maxSystem] ∪ [1..to]
    @Query("""
        FROM GalaxyEntity g
        WHERE g.galaxy = :galaxy
          AND (g.system BETWEEN :from AND :maxSystem OR g.system BETWEEN 1 AND :to)
        ORDER BY g.system
        """)
    List<GalaxyEntity> findOutRange(@Param("galaxy") Integer galaxy,
                                        @Param("from") Integer from,
                                        @Param("to") Integer to,
                                        @Param("maxSystem") Integer maxSystem);
}
