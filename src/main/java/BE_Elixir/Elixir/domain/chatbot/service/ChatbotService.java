package BE_Elixir.Elixir.domain.chatbot.service;

import BE_Elixir.Elixir.domain.chatbot.dto.ChatbotRequestDTO;
import BE_Elixir.Elixir.domain.chatbot.dto.ChatbotResponseDTO;
import BE_Elixir.Elixir.global.config.GptConfig;
import BE_Elixir.Elixir.global.redis.RedisChatbotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatbotService {

    private final RedisChatbotService redisChatbotService;
    private final PromptMessageFactory promptMessageFactory;
    private final GptConfig gptConfig;
    private final RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;
    private final ObjectMapper objectMapper = new ObjectMapper(); // 객체 생성

    public ChatbotResponseDTO chatbot(ChatbotRequestDTO dto) throws Exception {
        List<Map<String, String>> messages;
        String chatSessionId;

        try {
            if (dto.getChatSessionId() == null) {   // 최초 호출
                log.info("최초 호출");
                // 프롬프트 생성
                messages = promptMessageFactory.create(dto);

                // 세션 Id 생성 및 redis에 데이터 기본 형태 저장
                chatSessionId = redisChatbotService.saveInitialChatSession(dto.getType());

                if (messages == null || messages.isEmpty()) {
                    throw new IllegalStateException("초기 프롬프트 메시지를 생성하지 못했습니다.");
                }

                // redis에 system message 먼저 저장
                redisChatbotService.appendMessageToHistory(chatSessionId, messages.get(0));

                log.info("chatSessionId: {}", chatSessionId);
                log.info("request: " + messages.get(messages.size()-1).get("content"));

            } else { // 후속 요청
                log.info("후속 호출");
                if (dto.getMessage() == null || dto.getMessage().trim().isEmpty()) {
                    throw new IllegalArgumentException("후속 요청에는 message 값이 필요합니다.");
                }

                // redis에서 sessionId로 history 조회
                messages = redisChatbotService.getHistory(dto.getChatSessionId());
                chatSessionId = dto.getChatSessionId();

                // history로 가져 온 messages에 사용자의 질문 추가
                messages.add(Map.of("role", "user", "content", dto.getMessage()));

                log.info("chatSessionId: {}", chatSessionId);
                log.info("request: " + messages.get(messages.size()-1).get("content"));
            }

            // gpt api 호출
            Map<String, String> response = callGpt(messages);

            // gpt 응답 파싱 후 클리닝 적용
            String cleanedContent = response.get("content").replace("**", "");
            response.put("content", cleanedContent); // 수정된 content로 덮어쓰기

            // redis에 대화 기록
            redisChatbotService.appendMessageToHistory(chatSessionId, messages.get(messages.size()-1));
            redisChatbotService.appendMessageToHistory(chatSessionId, response);

            log.info("response: " + response.get("content"));

            return ChatbotResponseDTO.builder()
                    .chatSessionId(chatSessionId)
                    .message(cleanedContent)
                    .build();

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("잘못된 입력 또는 상태 오류: {}", e.getMessage());
            throw e;
        } catch (JsonProcessingException e) {
            log.error("GPT 응답 파싱 중 오류 발생", e);
            throw new RuntimeException("GPT 응답 파싱 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("챗봇 처리 중 예외 발생", e);
            throw new RuntimeException("챗봇 처리 중 오류가 발생했습니다.", e);
        }
    }

    // gpt 호출
    private Map<String, String> callGpt(List<Map<String, String>> messages) throws JsonProcessingException {
        Map<String, Object> requestBody = Map.of(
                "model", gptConfig.getModel(),
                "messages", messages
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, httpHeaders);
        String API_URL = "https://api.openai.com/v1/chat/completions";

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            log.error("GPT API 호출 실패", e);
            throw new RuntimeException("GPT API 호출에 실패했습니다.", e);
        }

        // GPT 응답에서 content만 파싱
        JsonNode jsonNode = objectMapper.readTree(response.getBody());  //JsonProcessingException
        if (!jsonNode.has("choices") || !jsonNode.get("choices").isArray() || jsonNode.get("choices").isEmpty()) {
            throw new IllegalStateException("GPT 응답에 유효한 메시지가 없습니다.");
        }

        JsonNode messageNode = jsonNode.get("choices").get(0).get("message");
        if (messageNode == null || !messageNode.has("content")) {
            throw new IllegalStateException("GPT 응답 메시지 파싱 실패: content 없음");
        }

        // Map으로 변환
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("role", messageNode.get("role").asText());
        messageMap.put("content", messageNode.get("content").asText());

        return messageMap;
    }

    // 챗봇 세션 삭제
    public void deleteChatSession(String chatSessionId) {
        redisChatbotService.deleteChatSession(chatSessionId);
    }
}