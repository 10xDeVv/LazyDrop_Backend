package org.example.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.CreateRoomResponse;
import org.example.server.dto.JoinRoomRequest;
import org.example.server.dto.JoinRoomResponse;
import org.example.server.models.RoomSession;
import org.example.server.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create")
    public ResponseEntity<CreateRoomResponse> createRoom(){
        try{
            CreateRoomResponse response = roomService.createRoom();
            return ResponseEntity.ok(response);
        }catch (Exception e){
            log.error("Failed to create room", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/join")
    public ResponseEntity<JoinRoomResponse> joinRoom(
            @Valid @RequestBody JoinRoomRequest request,
            @RequestHeader("X-Session-Id") String senderSessionId
    ){
        try{
            String normalized = request.getCode().replaceAll("[^A-Za-z0-9]", "")
                    .toUpperCase();

            JoinRoomResponse response = roomService.joinRoom(normalized, senderSessionId);
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest()
                    .body(JoinRoomResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }catch (Exception e){
            log.error("Failed to join room", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomSession> getRoom(@PathVariable String roomId){
        return roomService.getRoom(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
