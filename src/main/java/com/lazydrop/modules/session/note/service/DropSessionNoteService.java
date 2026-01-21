package com.lazydrop.modules.session.note.service;

import com.lazydrop.common.exception.BadRequestException;
import com.lazydrop.common.exception.ForbiddenOperationException;
import com.lazydrop.common.exception.PlanLimitExceededException;
import com.lazydrop.common.exception.ResourceNotFoundException;
import com.lazydrop.modules.session.core.event.DropSessionEndedEvent;
import com.lazydrop.modules.session.core.model.DropSession;
import com.lazydrop.modules.session.core.service.DropSessionService;
import com.lazydrop.modules.session.note.dto.CreateSessionNoteRequest;
import com.lazydrop.modules.session.note.model.DropSessionNote;
import com.lazydrop.modules.session.note.repository.DropSessionNoteRepository;
import com.lazydrop.modules.session.participant.model.DropSessionParticipant;
import com.lazydrop.modules.session.participant.service.DropSessionParticipantService;
import com.lazydrop.modules.subscription.model.PlanLimits;
import com.lazydrop.modules.subscription.service.SubscriptionService;
import com.lazydrop.modules.user.model.User;
import com.lazydrop.modules.websocket.MessageType;
import com.lazydrop.modules.websocket.WebSocketNotifier;
import com.lazydrop.modules.websocket.payload.SessionNoteCreatedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DropSessionNoteService {

    private final DropSessionNoteRepository noteRepository;
    private final DropSessionService dropSessionService;
    private final SubscriptionService subscriptionService;
    private final DropSessionParticipantService dropSessionParticipantService;
    private final WebSocketNotifier webSocketNotifier;


    @Transactional
    public DropSessionNote createUserNote(UUID sessionId, User user, CreateSessionNoteRequest request){
        if (user.isGuest()) {
            throw new ForbiddenOperationException("Create an account to use Session Notes.");
        }

        DropSession session = dropSessionService.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.assertUsable();

        DropSessionParticipant participant = requireParticipant(session, user);

        String content = enforceNotesLimits(session, request);

        DropSessionNote note = DropSessionNote.builder()
                .session(session)
                .sender(participant)
                .content(content)
                .createdAt(Instant.now())
                .build();

        note = noteRepository.save(note);

        log.info("Created note {} for session {}", note.getId(), sessionId);

        publishNoteCreated(sessionId, participant.getId(), note, request.clientNoteId());

        log.info("Published note {} for session {}", note.getId(), sessionId);

        return note;
    }

    @Transactional(readOnly = true)
    public List<DropSessionNote> getRecentNotes(UUID sessionId, User user, int limit){
        if (user.isGuest()) {
            throw new ForbiddenOperationException("Create an account to use Session Notes.");
        }

        DropSession session = dropSessionService.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.assertUsable();

        assertParticipant(session, user);

        int safeLimit = Math.min(Math.max(limit, 1), 100);

        List<DropSessionNote> notes = noteRepository.findBySession_IdOrderByCreatedAtDesc(sessionId, PageRequest.of(0, safeLimit));

        Collections.reverse(notes);
        return notes;
    }

    @Async
    @EventListener
    @Transactional
    public void handleSessionEnded(DropSessionEndedEvent event){
        UUID sessionId = event.getSessionId();
        long deleted = noteRepository.deleteBySession_Id(sessionId);
        log.info("Deleted {} notes for session {}", deleted, sessionId);
    }

    private void publishNoteCreated(UUID sessionId, UUID participantId, DropSessionNote note, String clientNoteId){
        SessionNoteCreatedPayload payload = new SessionNoteCreatedPayload(
            note.getId().toString(),
                participantId.toString(),
                note.getContent(),
                note.getCreatedAt(),
                clientNoteId
        );

        webSocketNotifier.sendEventAfterCommit(sessionId.toString(), MessageType.SESSION_NOTE_CREATED, payload);
    }

    private String enforceNotesLimits(DropSession session, CreateSessionNoteRequest request){
        PlanLimits limits = subscriptionService.getLimitsForUser(session.getOwner());

        if (limits.maxNotesPerSession() <= 0) {
            throw new ForbiddenOperationException("Session Notes are not available for this plan.");
        }

        long noteCount = noteRepository.countBySession_Id(session.getId());
        if (noteCount >= limits.maxNotesPerSession()) {
            throw new PlanLimitExceededException("Session notes limit reached for this session");
        }

        return sanitize(request.content(), limits.maxNoteLength());
    }

    private void assertParticipant(DropSession session, User user){
        if (!dropSessionParticipantService.existsByDropSessionAndUser(session, user)) {
            throw new ForbiddenOperationException("You must join the session before accessing this feature");
        }
    }

    private DropSessionParticipant requireParticipant(
            DropSession session,
            User user
    ) {
        return dropSessionParticipantService
                .findByDropSessionAndUser(session, user)
                .orElseThrow(() ->
                        new ForbiddenOperationException(
                                "You must join the session before accessing this feature"
                        )
                );
    }

    private static String sanitize(String raw, int maxLen) {
        if (raw == null) throw new BadRequestException("content is required");
        String trimmed = raw.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) throw new BadRequestException("content cannot be blank");

        if (maxLen > 0 && trimmed.length() > maxLen) {
            return trimmed.substring(0, maxLen);
        }
        return trimmed;
    }
}
