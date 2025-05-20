package BE_Elixir.Elixir.domain.chatbot.controller.api;

import BE_Elixir.Elixir.domain.chatbot.dto.ChatbotRequestDTO;
import BE_Elixir.Elixir.domain.chatbot.dto.ChatbotResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "Chatbot API", description = "챗봇 관련 API")
public interface ChatbotApi {

    @Operation(summary = "챗봇에 메세지 전송 및 응답 메시지 받아오기",
            description = "서버에 사용자의 요청을 보내고, 응답을 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "챗봇 응답 받기 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "챗봇 응답 받기 성공 - 회원 ID: 1",
                                      "data": {
                                        "type": "DIET_FEEDBACK",
                                        "targetId": 1
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "챗봇 응답 받기 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "code": "400 INTERNAL_SERVER_ERROR",
                                      "message": "챗봇 응답 받기 실패: 오류 메시지",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<ChatbotResponseDTO>> chatbot(
            @RequestBody ChatbotRequestDTO dto,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    @Operation(summary = "챗봇 세션 삭제하기",
            description = "챗봇 세션을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "챗봇 세션 삭제 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "챗봇 세션 삭제 성공 - 회원 ID: 1, 세션 ID: ...",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "챗봇 세션 삭제 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "code": "400 INTERNAL_SERVER_ERROR",
                                      "message": "챗봇 세션 삭제 실패: 오류 메시지",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> deleteChatSession(
            @PathVariable("chatSessionId") String chatSessionId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );
}