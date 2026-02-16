package com.lazydrop.modules.session.file.mapper;

import com.lazydrop.modules.session.file.dto.DropFileDto;
import com.lazydrop.modules.session.file.model.DropFile;

public class DropFileMapper {

    public static DropFileDto toDropFileDto(DropFile file) {
        return DropFileDto.builder()
                .id(file.getId().toString())
                .createdAt(file.getCreatedAt())
                .sizeBytes(file.getSizeBytes())
                .originalName(file.getOriginalName())
                .build();
    }
}
