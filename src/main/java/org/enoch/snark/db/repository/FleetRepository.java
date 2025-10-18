package org.enoch.snark.db.repository;

import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.JPAUtility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_BACK;
import static org.enoch.snark.action.command.FollowingAction.DELAY_TO_FLEET_THERE;

@Repository
public interface FleetRepository extends JpaRepository<FleetEntity, Long> {
    int deleteBySource(ColonyEntity colony);

    List<FleetEntity> findByHash(String hash);

    Optional<FleetEntity> findFirstByHashOrderByUpdatedDesc(String hash);

    default boolean isBlockedWithExpiredTime(String hash, String expiry) {
        Optional<FleetEntity> lastSend = findFirstByHashOrderByUpdatedDesc(hash);
        if(lastSend.isEmpty()) return false;

        else if (DELAY_TO_FLEET_THERE.equals(expiry)) return LocalDateTime.now().isBefore(lastSend.get().visited);
        else if (DELAY_TO_FLEET_BACK.equals(expiry)) return LocalDateTime.now().isBefore(lastSend.get().back);
        else return LocalDateTime.now().isBefore(lastSend.get().updated.plusSeconds(new Duration(expiry).getSeconds()));
    }
}
