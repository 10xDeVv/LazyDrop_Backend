package com.lazydrop.modules.session.core.model;

import com.lazydrop.common.exception.DropSessionExpiredException;
import com.lazydrop.modules.session.core.dto.DropSessionStatus;
import com.lazydrop.modules.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(
        name = "drop_session",
        indexes = {
                @Index(name = "idx_drop_session_code", columnList = "code"),
                @Index(name = "idx_drop_session_owner_status_expires", columnList = "owner_id,status,expires_at"),
                @Index(name = "idx_drop_session_status_expires", columnList = "status,expires_at"),
                @Index(name = "idx_drop_session_ended_at", columnList = "ended_at")
        }
)
public class DropSession {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    
    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DropSessionStatus status;

    @Enumerated(EnumType.STRING)
    private SessionEndReason endReason;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null){
            status = DropSessionStatus.OPEN;
        }
    }

    public boolean isUsable() {
        return status == DropSessionStatus.OPEN || status == DropSessionStatus.CONNECTED;
    }
    
    public void assertUsable() {
        if (!isUsable()) {
            throw new DropSessionExpiredException("DropSession is not active");
        }
    }

    public void end(SessionEndReason reason) {
        if (!isUsable()) return;
        this.endedAt = Instant.now();
        this.endReason = reason;

        this.status = (reason == SessionEndReason.EXPIRED)
                ? DropSessionStatus.EXPIRED
                : DropSessionStatus.CLOSED;
    }
}
