package BE_Elixir.Elixir.domain.chatbot.controller;


import BE_Elixir.Elixir.domain.chatbot.controller.api.ChatbotApi;
import BE_Elixir.Elixir.domain.chatbot.dto.ChatbotRequestDTO;
import BE_Elixir.Elixir.domain.chatbot.dto.ChatbotResponseDTO;
import BE_Elixir.Elixir.domain.chatbot.service.ChatbotService;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatbotController implements ChatbotApi {

    private final ChatbotService chatbotService;


    // 챗봇 메시지 전공 및 응답
    @PostMapping()
    public ResponseEntity<CommonResponse<ChatbotResponseDTO>> chatbot(
            @RequestBody ChatbotRequestDTO dto,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("챗봇 응답 받기 요청");
        Long memberId = memberDetails.getId();

        try {
            ChatbotResponseDTO responseDTO = chatbotService.chatbot(dto);
            log.info("챗봇 응답 받기 성공 - 회원 ID: {}", memberId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                            "챗봇 응답 받기 성공 - 회원 ID:" + memberId, responseDTO));

        } catch (Exception e) {
            log.error("챗봇 응답 받기 실패 - 회원 ID: {}, 메시지: {}", memberId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "챗봇 응답 받기 실패: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{chatSessionId}")
    public ResponseEntity<CommonResponse<?>> deleteChatSession(
            @PathVariable("chatSessionId") String chatSessionId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("챗봇 세션 삭제 요청");
        Long memberId = memberDetails.getId();

        try {
            chatbotService.deleteChatSession(chatSessionId);
            log.info("챗봇 세션 삭제 성공 - 회원 ID: {}, 세션 ID: {}", memberId, chatSessionId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                            "챗봇 세션 삭제 성공 - 회원 ID:" + memberId));

        } catch (Exception e) {
            log.error("응답 실패 - 회원 ID: {}, , 세션 ID: {}, 메시지: {}", memberId, chatSessionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "챗봇 세션 삭제 실패: " + e.getMessage()));
        }
    }
}