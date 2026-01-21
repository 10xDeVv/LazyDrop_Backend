package com.lazydrop.modules.session.core.controller;

import com.lazydrop.auth.IdentityResolver;
import com.lazydrop.common.exception.ResourceNotFoundException;
import com.lazydrop.modules.session.core.mapper.DropSessionMapper;
import com.lazydrop.security.UserPrincipal;
import com.lazydrop.modules.session.core.dto.DropSessionResponse;
import com.lazydrop.modules.session.core.model.DropSession;
import com.lazydrop.modules.session.core.service.DropSessionService;
import com.lazydrop.modules.user.model.User;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
public class DropSessionController{

    private final DropSessionService dropSessionService;
    private final IdentityResolver identityResolver;

    @PostMapping
    public ResponseEntity<DropSessionResponse> createDropSession(@AuthenticationPrincipal @Nullable UserPrincipal userPrincipal, HttpServletRequest req,
                                                                 HttpServletResponse response){
        User owner = identityResolver.resolve(userPrincipal, req, response);
        DropSession resp = dropSessionService.createDropSession(owner);
        return ResponseEntity.status(HttpStatus.CREATED).body(DropSessionMapper.toDropSessionResponse(resp));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<DropSessionResponse> getByCode(@PathVariable String code){
        DropSession session = dropSessionService.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        return ResponseEntity.ok(DropSessionMapper.toDropSessionResponse(session));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<DropSessionResponse> getBySessionId(@PathVariable UUID sessionId){
        DropSession session = dropSessionService.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        return ResponseEntity.ok(DropSessionMapper.toDropSessionResponse(session));
    }

    @GetMapping("/{sessionId}/qr")
    public ResponseEntity<Map<String,String>> getQrCode(@PathVariable UUID sessionId){
        String qrCode = dropSessionService.generateQrCode(sessionId);
        return ResponseEntity.ok(Map.of("qrCode", qrCode));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> closeSession(@PathVariable UUID sessionId, @AuthenticationPrincipal @Nullable UserPrincipal userPrincipal, HttpServletRequest req,
                                             HttpServletResponse response){
        User requester = identityResolver.resolve(userPrincipal, req, response);

        dropSessionService.closeSessionById(sessionId, requester);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, List<DropSessionResponse>>> getMyActiveSessions(
            @AuthenticationPrincipal @Nullable UserPrincipal userPrincipal,
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        User owner = identityResolver.resolve(userPrincipal, req, res);

        var sessions = dropSessionService.getActiveSessions(owner);

        var dtos = sessions.stream()
                .map(DropSessionMapper::toDropSessionResponse)
                .toList();

        return ResponseEntity.ok(Map.of("sessions", dtos));
    }

}
