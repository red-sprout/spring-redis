package org.example.springredis.domain.ch4.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.springredis.domain.ch4.chat.entity.Channel;
import org.example.springredis.domain.ch4.chat.repository.ChannelRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String USER_KEY_PREFIX = "user:";
    private static final String CHANNEL_FIELD_PREFIX = "channel:";

    private final ChannelRepository channelRepository;
    // 읽지 않은 메시지 수는 정수 카운터 → String 직렬화 사용 (JSON 직렬화 시 HINCRBY 불가)
    private final StringRedisTemplate stringRedisTemplate;

    public Channel createChannel(String name) {
        return channelRepository.save(Channel.builder().name(name).build());
    }

    // HSETNX user:{userId} channel:{channelId} 0
    public void joinChannel(Long userId, Long channelId) {
        stringRedisTemplate.opsForHash().putIfAbsent(
                USER_KEY_PREFIX + userId,
                CHANNEL_FIELD_PREFIX + channelId,
                "0"
        );
    }

    // HINCRBY user:{userId} channel:{channelId} 1
    public Long receiveMessage(Long userId, Long channelId) {
        return stringRedisTemplate.opsForHash().increment(
                USER_KEY_PREFIX + userId,
                CHANNEL_FIELD_PREFIX + channelId,
                1
        );
    }

    // HSET user:{userId} channel:{channelId} 0
    public void readMessages(Long userId, Long channelId) {
        stringRedisTemplate.opsForHash().put(
                USER_KEY_PREFIX + userId,
                CHANNEL_FIELD_PREFIX + channelId,
                "0"
        );
    }

    // HGETALL user:{userId}
    public Map<Object, Object> getUnreadCounts(Long userId) {
        return stringRedisTemplate.opsForHash().entries(USER_KEY_PREFIX + userId);
    }

    // HGET user:{userId} channel:{channelId}
    public Object getUnreadCount(Long userId, Long channelId) {
        return stringRedisTemplate.opsForHash().get(
                USER_KEY_PREFIX + userId,
                CHANNEL_FIELD_PREFIX + channelId
        );
    }

    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }
}
