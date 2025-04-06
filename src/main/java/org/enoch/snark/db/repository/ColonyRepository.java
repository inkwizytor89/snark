package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.ColonyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColonyRepository extends JpaRepository<ColonyEntity, Long> {

    Optional<ColonyEntity> findByCp(Integer cp);
}
