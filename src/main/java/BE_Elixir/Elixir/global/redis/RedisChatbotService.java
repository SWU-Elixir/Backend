package BE_Elixir.Elixir.global.redis;

import BE_Elixir.Elixir.global.redis.dto.ChatSessionDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisChatbotService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHAT_SESSION_KEY_PREFIX = "chatbot:content";
    private static final int MAX_HISTORY_SIZE = 11;

    // 최초 세션 값 생성
    public String saveInitialChatSession(String type) throws JsonProcessingException {
        String chatSessionId = generateSessionId(); // 세션 ID 생성
        String key = CHAT_SESSION_KEY_PREFIX + chatSessionId;

        ChatSessionDTO sessionDTO = new ChatSessionDTO(type, List.of());

        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(sessionDTO));
        log.info("새로운 챗봇 세션 생성. 세션 ID: {}\n{}", chatSessionId, sessionDTO);
        return chatSessionId;
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    // history 조회
    public List<Map<String, String>> getHistory(String chatSessionId) throws JsonProcessingException {
        String key = CHAT_SESSION_KEY_PREFIX + chatSessionId;
        String value = redisTemplate.opsForValue().get(key);

        ChatSessionDTO sessionDTO = objectMapper.readValue(value, ChatSessionDTO.class);
        log.info("{}\n{}", chatSessionId, sessionDTO);
        return sessionDTO.getHistory();
    }

    // Redis 값 출력 (디버깅용)
    public void printChatSessionValue(String chatSessionId) {
        String key = CHAT_SESSION_KEY_PREFIX + chatSessionId;
        String value = redisTemplate.opsForValue().get(key);

        log.info("Redis 세션 값 (chatSessionId={}):\n{}", chatSessionId, value);
    }

    // 세션 history에 메시지 추가 및 리스트 개수 유지
    public void appendMessageToHistory(String chatSessionId, Map<String, String> message) throws JsonProcessingException {
        String key = CHAT_SESSION_KEY_PREFIX + chatSessionId;
        String value = redisTemplate.opsForValue().get(key);

        ChatSessionDTO sessionDTO = objectMapper.readValue(value, ChatSessionDTO.class);
        List<Map<String, String>> history = sessionDTO.getHistory();

        if (history.size() > MAX_HISTORY_SIZE) {
            if (sessionDTO.getType().equals("FREETALK"))
                history.remove(0);
            else {
                history.remove(3);
            }
        }

        history.add(message);

        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(sessionDTO));
    }

    // 세션 삭제
    public void deleteChatSession(String chatSessionId) {
        String key = CHAT_SESSION_KEY_PREFIX + chatSessionId;
        redisTemplate.delete(key);
    }
}
