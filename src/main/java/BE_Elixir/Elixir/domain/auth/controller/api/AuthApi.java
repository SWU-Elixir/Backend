package BE_Elixir.Elixir.domain.auth.controller.api;

import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.request.LoginRequestDTO;
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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "Auth API", description = "회원 인증 관련 API")
public interface AuthApi {

    @Operation(summary = "로그인", description = "로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "grantType": "bearer",
                                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                      "refreshToken": "dGhpc2lzYXJlZnJlc2h0b2tlbg=="
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "로그인 실패",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "로그인 실패: 이메일 또는 비밀번호가 일치하지 않습니다.")))
    })
    ResponseEntity<CommonResponse<TokenResponseDTO>> login(@RequestBody LoginRequestDTO request);

    @Operation(summary = "로그아웃",
            description = "로그아웃합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "로그아웃 성공"))),
            @ApiResponse(responseCode = "400", description = "로그아웃 실패",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "로그아웃 실패: 이미 만료된 토큰입니다.")))
    })
    ResponseEntity<CommonResponse<?>> logout(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );

    @Operation(summary = "토큰 재발급",
            description = "Access Token, Refresh Token을 재발급합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "grantType": "bearer",
                                      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                      "refreshToken": "dGhpc2lzYXJlZnJlc2h0b2tlbg=="
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Access Token 재발급 실패",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Access Token 재발급 실패: 유효하지 않은 Refresh Token입니다.")))
    })
    ResponseEntity<CommonResponse<TokenResponseDTO>> refresh (
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );
}