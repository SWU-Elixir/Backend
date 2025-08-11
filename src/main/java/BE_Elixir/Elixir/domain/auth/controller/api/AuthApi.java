package BE_Elixir.Elixir.domain.auth.controller.api;

import BE_Elixir.Elixir.domain.auth.dto.AccessTokenDTO;
import BE_Elixir.Elixir.domain.auth.dto.request.SocialLoginRequestDTO;
import BE_Elixir.Elixir.domain.auth.dto.response.SocialLoginResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.request.LoginRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.enums.LoginType;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "Auth API", description = "회원 인증 관련 API")
public interface AuthApi {

    @Operation(summary = "일반 로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일반 로그인 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "일반 로그인 성공",
                                      "data": {
                                        "grantType": "bearer",
                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "refreshToken": "dGhpc2lzYXJlZnJlc2h0b2tlbg=="
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "일반 로그인 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 401,
                                      "code": "401 UNAUTHORIZED",
                                      "message": "회원 정보가 일치하지 않습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<TokenResponseDTO>> login(@RequestBody LoginRequestDTO request);

    @Operation(summary = "로그아웃",
            description = "현재 로그인된 회원을 로그아웃합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "로그아웃 성공",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "로그아웃 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 401,
                                      "code": "401 BAD_REQUEST",
                                      "message": "Access Token이 유효하지 않습니다.",
                                      "data": null
                                    }
                                    """))),
    })
    ResponseEntity<CommonResponse<?>> logout(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );

    @Operation(summary = "토큰 재발급",
            description = "유효한 Refresh Token을 이용해 Access Token을 재발급합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "Access Token 재발급 성공",
                                      "data": {
                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Access Token 재발급 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 401,
                                      "code": "401 UNAUTHORIZED",
                                      "message": "Token 발급 중 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<AccessTokenDTO>> refresh (
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );

    @Operation(summary = "소셜 로그인", description = "구글, 네이버, 카카오를 이용해 소셜 로그인을 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "소셜 로그인 성공(이미 가입한 회원의 경우)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "소셜 로그인 성공",
                                      "data": {
                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "refreshToken": "dGhpc2lzYXJlZnJlc2h0b2tlbg==",
                                        "loginType": "NAVER",
                                        "registered": false,
                                        "socialUserInfo": null
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "200", description = "소셜 로그인 성공(아직 가입하지 않은 회원의 경우)",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "소셜 로그인 성공",
                                      "data": {
                                        "accessToken": null,
                                        "refreshToken": null,
                                        "loginType": "NAVER",
                                        "socialUserInfo": {
                                          "email": "test@naver.com",
                                          "nickname": "홍길동",
                                          "gender": "F",
                                          "birthYear": 2002,
                                          "profileImage": "https://...jpg"
                                        },
                                        "registered": false
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "소셜 로그인 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 409,
                                      "code": "409 CONFLICT",
                                      "message": "이미 가입된 이메일입니다. 일반 로그인을 사용해주세요.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<SocialLoginResponseDTO>> socialLogin(
            @PathVariable(name="loginType") LoginType loginType,
            @RequestBody SocialLoginRequestDTO request
    );
}