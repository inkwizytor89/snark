package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    Optional<PlayerEntity> findFirstByCode(String code);

    default PlayerEntity mainPlayer() {
        Optional<PlayerEntity> byCode = this.findFirstByCode("");
        if(byCode.isPresent()) return byCode.get();

        PlayerEntity player = new PlayerEntity();
        player.name = "";
        player.code = "";
        return this.save(player);
    }
}
