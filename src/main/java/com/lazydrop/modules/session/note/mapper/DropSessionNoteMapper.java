package com.lazydrop.modules.session.note.mapper;

import com.lazydrop.modules.session.note.dto.SessionNoteDto;
import com.lazydrop.modules.session.note.model.DropSessionNote;

public class DropSessionNoteMapper {

    public static SessionNoteDto toSessionNoteDto(DropSessionNote note) {
        return new SessionNoteDto(
                note.getId().toString(),
                note.getSender().getId().toString(),
                note.getContent(),
                note.getCreatedAt()
        );
    }
}
