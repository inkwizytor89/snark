package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FleetRepository extends JpaRepository<FleetEntity, Long> {
    int deleteBySource(ColonyEntity colony);
}
