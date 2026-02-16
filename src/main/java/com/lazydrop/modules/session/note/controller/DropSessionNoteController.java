package com.lazydrop.modules.session.note.controller;

import com.lazydrop.auth.IdentityResolver;
import com.lazydrop.modules.session.note.dto.CreateSessionNoteRequest;
import com.lazydrop.modules.session.note.dto.SessionNoteDto;
import com.lazydrop.modules.session.note.mapper.DropSessionNoteMapper;
import com.lazydrop.modules.session.note.service.DropSessionNoteService;
import com.lazydrop.modules.user.model.User;
import com.lazydrop.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/sessions/{sessionId}/notes")
public class DropSessionNoteController {
    private final DropSessionNoteService noteService;
    private final IdentityResolver identityResolver;

    @PostMapping
    public ResponseEntity<SessionNoteDto> createNote(
            @PathVariable UUID sessionId,
            @Valid @RequestBody CreateSessionNoteRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest req,
            HttpServletResponse response
    ){
        User user = identityResolver.resolve(userPrincipal, req, response);
        var note = noteService.createUserNote(sessionId, user, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(DropSessionNoteMapper.toSessionNoteDto(note));
    }

    @GetMapping
    public ResponseEntity<List<SessionNoteDto>> getRecent(
            @PathVariable UUID sessionId,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest req,
            HttpServletResponse res
    ){
        User user = identityResolver.resolve(userPrincipal, req, res);
        List<SessionNoteDto> notes = noteService.getRecentNotes(sessionId, user, limit)
                .stream()
                .map(DropSessionNoteMapper::toSessionNoteDto)
                .toList();

        return ResponseEntity.ok(notes);
    }
}
