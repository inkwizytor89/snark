package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FleetRepository extends JpaRepository<FleetEntity, Long> {
    int deleteBySource(ColonyEntity colony);

    List<FleetEntity> findByHash(String hash);

    Optional<FleetEntity> findFirstByHashOrderByUpdatedDesc(String hash);
}
