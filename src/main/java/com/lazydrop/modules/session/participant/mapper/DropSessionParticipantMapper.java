package com.lazydrop.modules.session.participant.mapper;

import com.lazydrop.modules.session.participant.dto.ParticipantDto;
import com.lazydrop.modules.session.participant.model.DropSessionParticipant;
import com.lazydrop.modules.user.model.User;

public class DropSessionParticipantMapper {
    public static ParticipantDto toParticipantDto(DropSessionParticipant participant) {
        User u = participant.getUser();

        String name;
        if (u.isGuest()) {
            // show something stable-ish but not personal
            String tail = (u.getGuestId() != null && u.getGuestId().length() >= 4)
                    ? u.getGuestId().substring(u.getGuestId().length() - 4).toUpperCase()
                    : "GUEST";
            name = "Guest " + tail;
        } else {
            // email prefix or whatever you want
            String email = u.getEmail();
            name = (email != null && email.contains("@")) ? email.substring(0, email.indexOf("@")) : "Member";
        }

        return ParticipantDto.builder()
                .id(participant.getId().toString())
                .role(participant.getRole().toString())
                .userId(u.getId().toString())
                .autoDownload(participant.isAutoDownload())
                .displayName(name)
                .guest(u.isGuest())
                .build();
    }
}
