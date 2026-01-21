package com.lazydrop.modules.user.model;

import com.lazydrop.modules.subscription.model.Subscription;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "supabase_user_id", nullable = true, unique = true)
    private UUID supabaseUserId;

    @Column(nullable = false)
    private String email;

    @Column(name = "guest")
    private boolean guest;

    @Column(name = "guest_id", unique = true)
    private String guestId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy="user", cascade=ALL, orphanRemoval=true)
    private Subscription subscription;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}