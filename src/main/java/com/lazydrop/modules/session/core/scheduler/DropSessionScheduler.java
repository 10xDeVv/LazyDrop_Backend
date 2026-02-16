package com.lazydrop.modules.session.core.scheduler;

import com.lazydrop.modules.session.participant.service.DropSessionParticipantService;
import com.lazydrop.modules.session.core.service.DropSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class DropSessionScheduler {

    private final DropSessionService dropSessionService;
    private final DropSessionParticipantService participantService;

    /**
     * Cleanup expired sessions every minute.
     * - Marks sessions as expired
     * - Fires DropSessionExpiredEvent
     * - Cleans up session files via DropFileService listener
     */
    @Scheduled(fixedRate = 60000)
    public void expireSessions() {
        log.info("Running scheduled cleanup: expireSessions");
        dropSessionService.cleanUpExpiredSession();
    }

    /**
     * Cleanup disconnected participants every 5 minutes.
     * - Removes participants with null websocketConnectionId
     * - Only removes participants disconnected longer than maxDisconnectDuration
     */
    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void cleanupDisconnectedParticipants() {
        Duration maxDisconnectDuration = Duration.ofMinutes(5);
        log.info("Running scheduled cleanup: cleanupDisconnectedParticipants (maxDisconnectDuration = {} min)",
                maxDisconnectDuration.toMinutes());
        participantService.cleanupDisconnected(maxDisconnectDuration);
    }
}
