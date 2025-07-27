package BE_Elixir.Elixir.domain.member.controller.api;

import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.member.dto.request.*;
import BE_Elixir.Elixir.domain.member.dto.response.*;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeImageResponseDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Tag(name = "Member API", description = "회원 관련 API")
public interface MemberApi {

    @Operation(
            summary = "이메일 중복 여부 조회",
            description = "기존 가입 회원과 이메일이 중복되는지 조회합니다."
    )
    // 반환 상태 코드 및 의미
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "중복 여부 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "이메일 중복 체크 성공",
                                      "data": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "중복 여부 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "code": "400 INTERNAL_SERVER_ERROR",
                                      "message": "서버 내부 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<Boolean>> checkEmailDuplicate(@RequestParam String email);

    @Operation(summary = "일반 회원용 회원가입", description = "새로운 일반 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "일반 회원용 회원가입 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "일반 회원용 회원가입 성공 - memberId: 1",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "일반 회원용 회원가입 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 409,
                                      "code": "409 CONFLICT",
                                      "message": "이미 존재하는 회원입니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> signUp(
            @RequestPart("dto") SignUpRequestDTO dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    );

    @Operation(summary = "소셜 회원용 회원가입", description = "새로운 소셜 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "소셜 회원용 회원가입 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "소셜 회원용 회원가입 성공 - memberId: 1",
                                      "data": {
                                          "grantType": "Bearer",
                                          "accessToken": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtb29sMDIwODAzQGdtYWlsLmNvbSIsImF1dGgiOiJVU0VSIiwiZXhwIjoxNzUzNTk4NDQxfQ.YW8gIVXLWMFEm_ywGtDI4TTSJTHOpNVOxKG2wLeBBC2fCW_ZaG1rF7e_9Xps9gnp",
                                          "refreshToken": "eyJhbGciOiJIUzM4NCJ9.eyJleHAiOjE3NTQ4MDQ0NDF9.Ew5909EU638VuUe3gzNDUv50r162BMrZuDlJD43brRr8dSIclat71BOtPRzZZX9Q"
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "소셜 회원용 회원가입 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "code": "500 INTERNAL_SERVER_ERROR",
                                      "message": "서버 내부 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<TokenResponseDTO>> socialSignUp(
            @PathVariable(name="loginType") LoginType loginType,
            @RequestPart("dto") SocialSignUpRequestDTO dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    );

    @Operation(summary = "이메일 인증 요청", description = "이메일 인증을 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "이메일 인증 요청 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "이메일 인증 요청 성공 - email: A@example.com",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "이메일 인증 요청 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "code": "404 NOT_FOUND",
                                      "message": "해당 회원을 찾을 수 없습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> sendVerificationCode(
            @RequestBody EmailVerificationRequestDTO dto
    );

    @Operation(summary = "이메일 인증 검증 요청", description = "이메일 인증번호와 검증을 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "이메일 인증 검증 요청 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "이메일 인증 검증 요청 성공 - email: A@example.com",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "이메일 인증 검증 요청 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "code": "400 BAD_REQUEST",
                                      "message": "이메일 인증번호가 일치하지 않습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> verifyCode(
            @RequestBody EmailVerificationCheckRequestDTO dto
    );

    @Operation(summary = "비밀번호 업데이트", description = "새로운 비밀번호로 업데이트합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "비밀번호 업데이트 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "비밀번호 업데이트 요청 성공 - email: A@example.com",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "비밀번호 업데이트 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "code": "404 NOT_FOUND",
                                      "message": "해당 회원을 찾을 수 없습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> updatePassword(
            @RequestBody UpdatePasswordRequestDTO dto
    );

    @Operation(summary = "회원탈퇴",
            description = "회원 정보를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원탈퇴 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "회원탈퇴 성공 - email: example@naver.com",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "회원탈퇴 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "code": "404 NOT_FOUND",
                                      "message": "해당 회원을 찾을 수 없습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> withdrawal(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );

    // 팔로우 하기
    @Operation(summary = "팔로우 하기",
            description = "현재 사용자가 다른 사용자를 팔로우합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팔로우 하기 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "팔로우 하기 성공",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "팔로우 하기 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "code": "500 INTERNAL_SERVER_ERROR",
                                      "message": "서버 내부 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> follow(
            @PathVariable("targetMemberId") Long followingId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 언팔로우 하기
    @Operation(summary = "언팔로우 하기",
            description = "현재 사용자가 다른 사용자를 언팔로우합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "언팔로우 하기 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "언팔로우 하기 성공",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "언팔로우 하기 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "code": "500 INTERNAL_SERVER_ERROR",
                                      "message": "서버 내부 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> unfollow(
            @PathVariable("targetMemberId") Long followingId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // (현재 사용자의) 팔로잉 목록 조회하기 (사용자가 팔로우하는 목록)
    @Operation(summary = "현재 사용자의 팔로잉 목록 조회하기",
            description = "현재 사용자가 팔로우하는 회원 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "현재 사용자의 팔로잉 목록 조회하기 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "현재 사용자의 팔로잉 목록 조회하기 성공",
                                      "data": [
                                          {
                                            "id": 1,
                                            "nickname": "example",
                                            "profileUrl": "https://s3elixir.s3...",
                                            "title": "봄동마스터"
                                          }, ...
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "현재 사용자의 팔로잉 목록 조회하기 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "code": "500 INTERNAL_SERVER_ERROR",
                                      "message": "서버 내부 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberSummaryDTO>>> getFollowing(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // (현재 사용자의) 팔로워 목록 조회하기 (사용자를 팔로잉하는 목록)
    @Operation(summary = "현재 사용자의 팔로워 목록 조회하기",
            description = "현재 사용자를 팔로우하는 회원 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "현재 사용자의 팔로워 목록 조회하기 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "현재 사용자의 팔로워 목록 조회하기 성공",
                                      "data": [
                                          {
                                            "id": 1,
                                            "nickname": "example",
                                            "profileUrl": "https://s3elixir.s3...",
                                            "title": "봄동마스터"
                                          }, ...
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "현재 사용자의 팔로워 목록 조회하기 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "code": "500 INTERNAL_SERVER_ERROR",
                                      "message": "서버 내부 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberSummaryDTO>>> getFollower(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // (특정 사용자의) 팔로잉 목록 조회하기 (사용자가 팔로우하는 목록)
    @Operation(summary = "특정 사용자의 팔로잉 목록 조회하기",
            description = "특정 사용자가 팔로우하는 회원 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "특정 사용자의 팔로잉 목록 조회하기 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "특정 사용자의 팔로잉 목록 조회하기 성공",
                                      "data": [
                                          {
                                            "id": 1,
                                            "nickname": "example",
                                            "profileUrl": "https://s3elixir.s3...",
                                            "title": "봄동마스터"
                                          }, ...
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "특정 사용자의 팔로잉 목록 조회하기 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "code": "500 INTERNAL_SERVER_ERROR",
                                      "message": "서버 내부 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberSummaryDTO>>> getFollowingByMemberId(
            @PathVariable("targetMemberId") Long targetMemberId
    );

    // (특정 사용자의) 팔로우 목록 조회하기 (사용자를 팔로잉하는 목록)
    @Operation(summary = "특정 사용자의 팔로워 목록 조회하기",
            description = "특정 사용자를 팔로우하는 회원 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "특정 사용자의 팔로워 목록 조회하기 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "특정 사용자의 팔로워 목록 조회하기 성공",
                                      "data": [
                                          {
                                            "id": 1,
                                            "nickname": "example",
                                            "profileUrl": "https://s3elixir.s3...",
                                            "title": "봄동마스터"
                                          }, ...
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "특정 사용자의 팔로워 목록 조회하기 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "code": "500 INTERNAL_SERVER_ERROR",
                                      "message": "서버 내부 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberSummaryDTO>>> getFollowerByMemberId(
            @PathVariable("targetMemberId") Long targetMemberId
    );


}