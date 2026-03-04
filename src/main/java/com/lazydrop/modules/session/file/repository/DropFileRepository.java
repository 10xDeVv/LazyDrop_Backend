package com.lazydrop.modules.session.file.repository;

import com.lazydrop.modules.session.file.model.DropFile;
import com.lazydrop.modules.session.core.model.DropSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DropFileRepository extends JpaRepository <DropFile, UUID>{
    long countByDropSession(DropSession dropSession);
    List<DropFile> findByDropSession(DropSession dropSession);
    @NonNull Optional<DropFile> findById(@NonNull UUID id);
    void deleteByDropSession(DropSession dropSession);

    @Override
    void delete(@NonNull DropFile entity);
}
