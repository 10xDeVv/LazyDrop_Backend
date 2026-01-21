package com.lazydrop.modules.session.file.model;

import com.lazydrop.modules.session.participant.model.DropSessionParticipant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "drop_file_download",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_download_participant_file",
                columnNames = {"participant_id", "file_id"}
        ),
        indexes = {
                @Index(name = "idx_download_participant", columnList = "participant_id"),
                @Index(name = "idx_download_file", columnList = "file_id")
        }
)
public class DropFileDownload {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="file_id", nullable=false)
    private DropFile file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="participant_id", nullable=false)
    private DropSessionParticipant participant;

    @Column(name="downloaded_at", nullable = false, updatable = false)
    private Instant downloadedAt;

    @PrePersist
    void onCreate() {
        if (this.downloadedAt == null) {
            this.downloadedAt = Instant.now();
        }
    }
}
