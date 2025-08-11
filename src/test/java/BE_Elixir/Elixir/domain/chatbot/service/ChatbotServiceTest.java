package BE_Elixir.Elixir.domain.chatbot.service;

import BE_Elixir.Elixir.domain.chatbot.dto.ChatbotRequestDTO;
import BE_Elixir.Elixir.domain.chatbot.dto.ChatbotResponseDTO;
import BE_Elixir.Elixir.global.config.GptConfig;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.redis.RedisChatbotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatbotService 단위 테스트")
class ChatbotServiceTest {

    @InjectMocks
    private ChatbotService chatbotService;

    @Mock private RedisChatbotService redisChatbotService;
    @Mock private PromptMessageFactory promptMessageFactory;
    @Mock private GptConfig gptConfig;
    @Mock private RestTemplate restTemplate;
    @Mock private HttpHeaders httpHeaders;

    private ChatbotRequestDTO initialRequestDto;
    private ChatbotRequestDTO followUpRequestDto;
    private List<Map<String, String>> mockMessages;
    private String mockChatSessionId;
    private ResponseEntity<String> mockGptResponse;

    @BeforeEach
    void setUp() {
        // 실제 HttpHeaders 객체 생성 및 설정
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        httpHeaders.setBearerAuth("test-token"); // 필요시 추가

        // ChatbotService에 HttpHeaders 수동 주입 (리플렉션 사용)
        try {
            java.lang.reflect.Field headersField = ChatbotService.class.getDeclaredField("httpHeaders");
            headersField.setAccessible(true);
            headersField.set(chatbotService, httpHeaders);
        } catch (Exception e) {
            throw new RuntimeException("HttpHeaders 주입 실패", e);
        }

        // 초기 요청 DTO
        initialRequestDto = ChatbotRequestDTO.builder()
                .type("FREETALK")
                .message("안녕하세요")
                .chatSessionId(null)
                .build();

        // 후속 요청 DTO
        mockChatSessionId = "test-session-id";
        followUpRequestDto = ChatbotRequestDTO.builder()
                .type("FREETALK")
                .message("후속 질문입니다")
                .chatSessionId(mockChatSessionId)
                .build();

        // Mock 메시지 리스트
        mockMessages = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "안녕하세요");

        mockMessages.add(systemMessage);
        mockMessages.add(userMessage);

        // Mock GPT 응답
        String mockGptResponseBody = """
            {
                "choices": [{
                    "message": {
                        "role": "assistant",
                        "content": "안녕하세요! 도움이 필요하시면 언제든 말씀해주세요."
                    }
                }]
            }
            """;
        mockGptResponse = new ResponseEntity<>(mockGptResponseBody, HttpStatus.OK);
    }

    @Test
    @DisplayName("성공: 최초 호출 시 정상 처리")
    void chatbot_InitialCall_Success() throws JsonProcessingException {
        // Given
        when(promptMessageFactory.create(initialRequestDto)).thenReturn(mockMessages);
        when(redisChatbotService.saveInitialChatSession("FREETALK")).thenReturn(mockChatSessionId);
        when(gptConfig.getModel()).thenReturn("gpt-4o-mini");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockGptResponse);

        // When
        ChatbotResponseDTO result = chatbotService.chatbot(initialRequestDto);

        // Then
        assertThat(result.getChatSessionId()).isEqualTo(mockChatSessionId);
        assertThat(result.getMessage()).isEqualTo("안녕하세요! 도움이 필요하시면 언제든 말씀해주세요.");

        verify(promptMessageFactory).create(initialRequestDto);
        verify(redisChatbotService).saveInitialChatSession("FREETALK");
//        verify(redisChatbotService).appendMessageToHistory(eq(mockChatSessionId), any(Map.class));
        verify(redisChatbotService, times(3))
                .appendMessageToHistory(eq("test-session-id"), anyMap());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("성공: 후속 호출 시 정상 처리")
    void chatbot_FollowUpCall_Success() throws JsonProcessingException {
        // Given
        List<Map<String, String>> historyMessages = new ArrayList<>();
        Map<String, String> systemMessage = Map.of("role", "system", "content", "You are a helpful assistant.");
        Map<String, String> previousUserMessage = Map.of("role", "user", "content", "이전 질문");
        Map<String, String> previousAssistantMessage = Map.of("role", "assistant", "content", "이전 답변");

        historyMessages.add(systemMessage);
        historyMessages.add(previousUserMessage);
        historyMessages.add(previousAssistantMessage);

        when(redisChatbotService.getHistory(mockChatSessionId)).thenReturn(historyMessages);
        when(gptConfig.getModel()).thenReturn("gpt-4o-mini");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockGptResponse);

        // When
        ChatbotResponseDTO result = chatbotService.chatbot(followUpRequestDto);

        // Then
        assertThat(result.getChatSessionId()).isEqualTo(mockChatSessionId);
        assertThat(result.getMessage()).isEqualTo("안녕하세요! 도움이 필요하시면 언제든 말씀해주세요.");

        verify(redisChatbotService).getHistory(mockChatSessionId);
        verify(redisChatbotService, times(2)).appendMessageToHistory(eq(mockChatSessionId), any(Map.class));
    }

    @Test
    @DisplayName("성공: 챗봇 세션 삭제")
    void deleteChatSession_Success() {
        // Given
        String sessionId = "test-session-id";

        // When
        chatbotService.deleteChatSession(sessionId);

        // Then
        verify(redisChatbotService).deleteChatSession(sessionId);
    }

    @Test
    @DisplayName("실패: PromptMessageFactory에서 null 반환")
    void chatbot_PromptMessageFactory_ReturnsNull_ThrowsException() throws JsonProcessingException {
        // Given
        when(promptMessageFactory.create(initialRequestDto)).thenReturn(null);
        when(redisChatbotService.saveInitialChatSession("FREETALK")).thenReturn(mockChatSessionId);

        // When & Then
        assertThatThrownBy(() -> chatbotService.chatbot(initialRequestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHATBOT_INITIAL_PROMPT_MISSING);

        verify(promptMessageFactory).create(initialRequestDto);
        verify(redisChatbotService).saveInitialChatSession("FREETALK");
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("실패: PromptMessageFactory에서 빈 리스트 반환")
    void chatbot_PromptMessageFactory_ReturnsEmpty_ThrowsException() throws JsonProcessingException {
        // Given
        when(promptMessageFactory.create(initialRequestDto)).thenReturn(new ArrayList<>());
        when(redisChatbotService.saveInitialChatSession("FREETALK")).thenReturn(mockChatSessionId);

        // When & Then
        assertThatThrownBy(() -> chatbotService.chatbot(initialRequestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHATBOT_INITIAL_PROMPT_MISSING);

        verify(promptMessageFactory).create(initialRequestDto);
        verify(redisChatbotService).saveInitialChatSession("FREETALK");
    }

    @Test
    @DisplayName("실패: 후속 호출인데 message가 null")
    void chatbot_FollowUpCall_MessageNull_ThrowsException() throws JsonProcessingException {
        // Given
        ChatbotRequestDTO invalidDto = ChatbotRequestDTO.builder()
                .type("FREETALK")
                .message(null)
                .chatSessionId(mockChatSessionId)
                .build();

        // When & Then
        assertThatThrownBy(() -> chatbotService.chatbot(invalidDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);

        verify(redisChatbotService, never()).getHistory(anyString());
    }

    @Test
    @DisplayName("실패: 후속 호출인데 message가 빈 문자열")
    void chatbot_FollowUpCall_MessageEmpty_ThrowsException() {
        // Given
        ChatbotRequestDTO invalidDto = ChatbotRequestDTO.builder()
                .type("FREETALK")
                .message("   ")
                .chatSessionId(mockChatSessionId)
                .build();

        // When & Then
        assertThatThrownBy(() -> chatbotService.chatbot(invalidDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("실패: GPT API 호출 자체가 실패")
    void chatbot_GptApiCall_Fails_ThrowsException() throws JsonProcessingException {
        // Given
        when(promptMessageFactory.create(initialRequestDto)).thenReturn(mockMessages);
        when(redisChatbotService.saveInitialChatSession("FREETALK")).thenReturn(mockChatSessionId);
        when(gptConfig.getModel()).thenReturn("gpt-4o-mini");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("API 호출 실패"));

        // When & Then
        assertThatThrownBy(() -> chatbotService.chatbot(initialRequestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTERNAL_SERVER_ERROR);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("실패: GPT 응답의 JSON 파싱 중 오류 발생")
    void chatbot_JsonParsing_Fails_ThrowsException() throws JsonProcessingException {
        // Given
        String invalidJsonResponse = "{ invalid json }";
        ResponseEntity<String> invalidResponse = new ResponseEntity<>(invalidJsonResponse, HttpStatus.OK);

        when(promptMessageFactory.create(initialRequestDto)).thenReturn(mockMessages);
        when(redisChatbotService.saveInitialChatSession("FREETALK")).thenReturn(mockChatSessionId);
        when(gptConfig.getModel()).thenReturn("gpt-4o-mini");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(invalidResponse);

        // When & Then
        assertThatThrownBy(() -> chatbotService.chatbot(initialRequestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.JSON_PROCESSING_ERROR);
    }

    @Test
    @DisplayName("실패: GPT 응답에 message content가 없는 경우")
    void chatbot_GptResponse_NoContent_ThrowsException() throws JsonProcessingException {
        // Given
        String responseWithoutContent = """
            {
                "choices": [{
                    "message": {
                        "role": "assistant"
                    }
                }]
            }
            """;
        ResponseEntity<String> invalidResponse = new ResponseEntity<>(responseWithoutContent, HttpStatus.OK);

        when(promptMessageFactory.create(initialRequestDto)).thenReturn(mockMessages);
        when(redisChatbotService.saveInitialChatSession("FREETALK")).thenReturn(mockChatSessionId);
        when(gptConfig.getModel()).thenReturn("gpt-4o-mini");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(invalidResponse);

        // When & Then
        assertThatThrownBy(() -> chatbotService.chatbot(initialRequestDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTERNAL_SERVER_ERROR);
    }


}