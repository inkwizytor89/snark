package org.enoch.snark.db.repository;

import org.enoch.snark.db.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    Optional<Object> findByMessageId(Long messageId);
}
