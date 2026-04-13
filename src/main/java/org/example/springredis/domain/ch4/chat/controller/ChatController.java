package org.example.springredis.domain.ch4.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.chat.entity.Channel;
import org.example.springredis.domain.ch4.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ch4/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/channels")
    public ResponseEntity<List<Channel>> getAllChannels() {
        return ResponseEntity.ok(chatService.getAllChannels());
    }

    @PostMapping("/channels")
    public ResponseEntity<Channel> createChannel(@RequestBody CreateChannelRequest request) {
        return ResponseEntity.ok(chatService.createChannel(request.name()));
    }

    @PostMapping("/{userId}/join/{channelId}")
    public ResponseEntity<Void> joinChannel(@PathVariable Long userId, @PathVariable Long channelId) {
        chatService.joinChannel(userId, channelId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/receive/{channelId}")
    public ResponseEntity<Map<String, Object>> receiveMessage(@PathVariable Long userId, @PathVariable Long channelId) {
        Long count = chatService.receiveMessage(userId, channelId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{userId}/read/{channelId}")
    public ResponseEntity<Void> readMessages(@PathVariable Long userId, @PathVariable Long channelId) {
        chatService.readMessages(userId, channelId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/unread")
    public ResponseEntity<Map<Object, Object>> getUnreadCounts(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getUnreadCounts(userId));
    }

    @GetMapping("/{userId}/unread/{channelId}")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable Long userId,
                                                               @PathVariable Long channelId) {
        return ResponseEntity.ok(Map.of("unreadCount", chatService.getUnreadCount(userId, channelId)));
    }

    record CreateChannelRequest(String name) {}
}
