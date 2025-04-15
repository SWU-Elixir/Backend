package BE_Elixir.Elixir.domain.member.controller.api;

import BE_Elixir.Elixir.domain.auth.dto.request.TokenRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.SignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "Member API", description = "회원 관련 API")
public interface MemberApi {

    @Operation(
            summary = "이메일 중복 여부 조회",
            description = "기존 가입 회원과 이메일이 중복되는지 조회합니다."
    )
    // 반환 상태 코드 및 의미
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "중복 여부 조회 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class)))
    })
    ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email);

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(schema = @Schema(type = "string", example = "회원가입 성공 - member_id = 1"))),
            @ApiResponse(responseCode = "400", description = "회원가입 실패",
                    content = @Content(schema = @Schema(type = "string", example = "회원가입 실패: 이메일이 이미 존재합니다.")))
    })
    ResponseEntity<?> signUp(@RequestBody SignUpRequestDTO request);

    @Operation(summary = "회원탈퇴",
            description = "회원 정보를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원탈퇴 성공",
                    content = @Content(schema = @Schema(type = "string", example = "회원탈퇴 성공"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(type = "string", example = "요청이 잘못되었습니다."))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(type = "string", example = "회원탈퇴 중 오류가 발생했습니다.")))
    })
    ResponseEntity<?> withdrawal(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );
}