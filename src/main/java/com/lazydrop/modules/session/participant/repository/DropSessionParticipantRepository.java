package com.lazydrop.modules.session.participant.repository;

import com.lazydrop.modules.session.core.model.DropSession;
import com.lazydrop.modules.session.participant.model.DropSessionParticipant;
import com.lazydrop.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DropSessionParticipantRepository extends JpaRepository<DropSessionParticipant, UUID> {
    Optional<DropSessionParticipant> findByDropSessionAndUser(DropSession dropSession, User user);

    long countByDropSession(DropSession dropSession);

    List<DropSessionParticipant> findByDropSession(DropSession session);

    boolean existsByDropSessionAndUser(DropSession dropSession, User user);

    List<DropSessionParticipant> findByDisconnectedAtBefore(Instant cutoff);
}
