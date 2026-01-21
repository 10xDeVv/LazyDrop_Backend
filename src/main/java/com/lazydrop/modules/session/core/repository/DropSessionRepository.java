package com.lazydrop.modules.session.core.repository;

import com.lazydrop.modules.session.core.model.DropSession;
import com.lazydrop.modules.session.core.dto.DropSessionStatus;
import com.lazydrop.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DropSessionRepository extends JpaRepository<DropSession, UUID> {

    Optional<DropSession> findByCode(String code);

    long countByOwnerAndStatusInAndExpiresAtAfter(User owner, Collection<DropSessionStatus> statuses, Instant expiresAtAfter);

    List<DropSession> findByStatusInAndExpiresAtBefore(List<DropSessionStatus> open, Instant now);

    List<DropSession> findByOwnerAndStatusInAndExpiresAtAfter(
            User owner,
            List<DropSessionStatus> statuses,
            Instant now
    );

}
