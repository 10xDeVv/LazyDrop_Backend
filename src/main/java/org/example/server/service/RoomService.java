package org.example.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.CreateRoomResponse;
import org.example.server.dto.JoinRoomResponse;
import org.example.server.models.*;
import org.example.server.utility.CodeUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final SupabaseStorageService supabaseStorageService;
    private final QRCodeService qrCodeService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CodeUtility codeUtility;

    @Value("${app.room.expiry-minutes}")
    private int expiryMinutes;

    private final Map<String, RoomSession> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> codeToRoomMap = new ConcurrentHashMap<>();

    public CreateRoomResponse createRoom(){
        String code = codeUtility.newNumericCode(6);
        String secret = codeUtility.newSecret(16);
        String roomId = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plusMinutes(expiryMinutes);

        RoomSession room = RoomSession.builder()
                .id(roomId)
                .code(code)
                .secret(secret)
                .createdAt(now)
                .expiresAt(expiryTime)
                .connected(false)
                .autoDownload(false)
                .files(new ArrayList<>())
                .build();

        rooms.put(roomId, room);
        codeToRoomMap.put(code, roomId);

        String qrData = String.format("https://lazydrop.app/join?code=%s&secret=%s", code.replace("-", ""), secret);
        String qrCodeBase64 = qrCodeService.generateQRCode(qrData, 300, 300);

        log.info("Created ephemeral room: {} (code: {}) - expires in {} minutes",
                roomId, code, expiryMinutes);

        return CreateRoomResponse.builder()
                .roomId(roomId)
                .code(code)
                .qrCodeData(qrCodeBase64)
                .expiresIn(room.getRemainingSeconds())
                .build();
    }

    public Optional<RoomSession> getRoomByCode(String code){
        String normalized = code.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        String roomId = codeToRoomMap.get(normalized);
        if(roomId != null){
            return getRoom(roomId);
        }
        return Optional.empty();
    }

    public Optional<RoomSession> getRoom(String roomId){
        RoomSession room = rooms.get(roomId);
        if(room != null && !room.isExpired()) {
            return Optional.of(room);
        }else {
            cleanUpRoom(roomId);
        }
        return Optional.empty();
    }

    public JoinRoomResponse joinRoom(String code, String senderSessionId){
        Optional<RoomSession> roomOpt = getRoomByCode(code);

        if (roomOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired room code");
        }

        RoomSession room = roomOpt.get();

        if (room.isConnected()) {
            throw new IllegalArgumentException("Room already has a sender connected");
        }

        room.setConnected(true);
        room.setSenderSessionId(senderSessionId);

        notifyRoomJoined(room.getId());

        notifyPeerJoined(room.getId());

        log.info("Sender joined room: {}", room.getId());


        return JoinRoomResponse.builder()
                .roomId(room.getId())
                .success(true)
                .message("Successfully joined room")
                .build();
    }

    public void addFileToRoom(String roomId, FileMetadata file){
        RoomSession room = rooms.get(roomId);
        if (room != null && !room.isExpired()){
            room.addFile(file);
            log.info("File added to room {}: {}", room.getId(), file.getName());
        }
    }

    public void markFileAsDownloaded(String roomId, String fileId){
        RoomSession room = rooms.get(roomId);
        if (room != null){
            room.getFiles().stream()
                    .filter(f -> f.getId().equals(fileId))
                    .findFirst()
                    .ifPresent(file -> {
                        file.setDownloaded(true);
                        log.info("File downloaded: {}", file.getName());
                    });
        }
    }

    @Scheduled(
            initialDelayString = "PT10S",
            fixedRateString = "#{${app.room.cleanup-interval-seconds} * 1000}")
    public void cleanUpExpiredRooms(){
        LocalDateTime now = LocalDateTime.now();
        List<String> expiredRoomIds = new ArrayList<>();

        rooms.forEach((roomId, room) -> {
            if (room.isExpired()){
                expiredRoomIds.add(roomId);
            }
        });

        expiredRoomIds.forEach(this::cleanUpRoom);

        if (!expiredRoomIds.isEmpty()){
            log.info("Cleaned up {} expired rooms", expiredRoomIds.size());
        }
    }


    private void cleanUpRoom(String roomId){
        RoomSession room = rooms.remove(roomId);
        if(room != null){
            codeToRoomMap.remove(room.getCode());

            room.getFiles().forEach(file -> {
                try{
                    supabaseStorageService.deleteFile(file.getStoragePath());
                    log.info("Deleted file from storage: {}", file.getName());
                } catch (Exception e) {
                    log.error("Failed to delete file: {}", file.getStoragePath(), e);
                }
            });
            notifyRoomExpired(roomId);
            log.info("Room {} cleaned up - all files deleted (ephemeral)", roomId);
        }
    }

    private void notifyRoomJoined(String roomId){
        WebSocketMessage message = WebSocketMessage.builder()
                .type(MessageType.ROOM_JOINED)
                .roomId(roomId)
                .payload(null)
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }

    private void notifyRoomExpired(String roomId){
        WebSocketMessage message = WebSocketMessage.builder()
                .type(MessageType.ROOM_EXPIRED)
                .roomId(roomId)
                .payload(null)
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }

    private void notifyPeerJoined(String roomId){
        WebSocketMessage message = WebSocketMessage.builder()
                .type(MessageType.PEER_JOINED)
                .roomId(roomId)
                .payload(null)
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
        log.info("Sent PEER_JOINED notification for room: {}", roomId);
    }
}
