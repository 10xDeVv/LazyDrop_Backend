package com.lazydrop.modules.session.file.repository;

import com.lazydrop.modules.session.core.model.DropSession;
import com.lazydrop.modules.session.file.model.DropFile;
import com.lazydrop.modules.session.file.model.DropFileDownload;
import com.lazydrop.modules.session.participant.model.DropSessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface DropFileDownloadRepository extends JpaRepository<DropFileDownload, UUID> {
    boolean existsByParticipantAndFile(DropSessionParticipant participant, DropFile file);

    @Query("""
  select d.file.id from DropFileDownload d
  where d.participant = :participant and d.file.dropSession = :session
""")
    Set<UUID> findDownloadedFileIds(@Param("participant") DropSessionParticipant participant,
                                    @Param("session") DropSession session);

}
