package com.lazydrop.modules.session.file.model;

import com.lazydrop.modules.session.core.model.DropSession;
import com.lazydrop.modules.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        indexes = {
                @Index(name = "idx_drop_file_session_created", columnList = "drop_session_id,created_at")
        }
)
public class DropFile {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drop_session_id", nullable = false)
    private DropSession dropSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader", nullable = false)
    private User uploader;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
