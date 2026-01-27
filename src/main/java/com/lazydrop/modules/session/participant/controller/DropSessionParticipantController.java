package com.lazydrop.modules.session.participant.controller;

import com.lazydrop.auth.IdentityResolver;
import com.lazydrop.common.exception.DropSessionExpiredException;
import com.lazydrop.common.exception.ResourceNotFoundException;
import com.lazydrop.modules.session.participant.dto.ParticipantSettingsRequest;
import com.lazydrop.modules.session.participant.dto.ParticipantSettingsResponse;
import com.lazydrop.modules.session.participant.mapper.DropSessionParticipantMapper;
import com.lazydrop.security.UserPrincipal;
import com.lazydrop.modules.session.participant.dto.ParticipantDto;
import com.lazydrop.modules.session.core.model.DropSession;
import com.lazydrop.modules.session.participant.service.DropSessionParticipantService;
import com.lazydrop.modules.session.core.service.DropSessionService;
import com.lazydrop.modules.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions/{sessionId}/participants")
public class DropSessionParticipantController{

    private final DropSessionParticipantService participantService;
    private final DropSessionService dropSessionService;
    private final IdentityResolver identityResolver;

    @PostMapping
    public ResponseEntity<ParticipantDto> joinSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest req,
            HttpServletResponse response){
        DropSession session = dropSessionService.findById(sessionId)
                .orElseThrow(() -> new DropSessionExpiredException("DropSession not found"));

        User user = identityResolver.resolve(principal, req, response);
        var participant = participantService.joinSession(session, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(DropSessionParticipantMapper.toParticipantDto(participant));
    }

    @DeleteMapping
    public ResponseEntity<Void> leaveSession(@PathVariable UUID sessionId,
                                             @AuthenticationPrincipal UserPrincipal principal,
                                             HttpServletRequest req,
                                             HttpServletResponse response){
        DropSession session = dropSessionService.findById(sessionId)
                .orElseThrow(() -> new DropSessionExpiredException("DropSession not found"));

        User user = identityResolver.resolve(principal, req, response);
        participantService.leave(session, user);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ParticipantDto>> getParticipants(@PathVariable UUID sessionId){
        DropSession session = dropSessionService.findById(sessionId)
                .orElseThrow(() -> new DropSessionExpiredException("DropSession not found"));

        List<ParticipantDto> participants = participantService.getParticipants(session)
                .stream()
                .map(DropSessionParticipantMapper::toParticipantDto)
                .toList();

        return ResponseEntity.ok(participants);
    }
    
    @GetMapping("/me/settings")
    public ResponseEntity<ParticipantSettingsResponse> getMySettings(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest req,
            HttpServletResponse response
    ){
        DropSession session = dropSessionService.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("DropSession not found"));

        User user = identityResolver.resolve(principal, req, response);
        ParticipantSettingsResponse resp = participantService.getMySettings(session, user);

        return ResponseEntity.ok(resp);
    }
    

    @PatchMapping("/me/settings")
    public ResponseEntity<ParticipantSettingsResponse> updateMySettings(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid ParticipantSettingsRequest request,
            HttpServletRequest req,
            HttpServletResponse response
    ){
        DropSession session = dropSessionService.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("DropSession not found"));

        User user = identityResolver.resolve(principal, req, response);
        ParticipantSettingsResponse resp = participantService.updateMySettings(session, user, request);

        return ResponseEntity.ok(resp);
    }
}
