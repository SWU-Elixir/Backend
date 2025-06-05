package BE_Elixir.Elixir.domain.member.controller.api;

import BE_Elixir.Elixir.domain.member.dto.request.*;
import BE_Elixir.Elixir.domain.member.dto.response.*;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeImageResponseDTO;
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

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "회원가입 성공 - memberId: 1",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "회원가입 실패",
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


    @Operation(summary = "회원 정보 조회",
            description = "회원의 기본적인 정보(id, 이메일, 닉네임, 젠더, 생년)를 조회합니다",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "회원 정보 조회 성공",
                                      "data": {
                                        "id": 1,
                                        "email": "example@naver.com",
                                        "nickname": "example",
                                        "gender": "female",
                                        "birthYear": 2002,
                                        "profileUrl": "https://s3elixir.s3..."
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "회원 정보 조회 실패",
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
    ResponseEntity<CommonResponse<MemberResponseDTO>> getMemberInfo(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );

    @Operation(summary = "로그인한 사용자의 칭호 목록 조회",
            description = "프로필 수정 시, 로그인한 사용자의 칭호 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "칭호 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "칭호 목록 조회 성공",
                                      "data": {
                                        "memberId": 1,
                                        "titles": [ ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "칭호 목록 조회 실패",
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
    ResponseEntity<CommonResponse<MemberTitlesResponseDTO>> getTitles(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    @Operation(summary = "로그인한 사용자 프로필 수정",
            description = "로그인한 사용자의 프로필을 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 프로필 수정 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "사용자 프로필 수정 성공",
                                      "data": {
                                        "memberId": 1,
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "사용자 프로필 수정 실패",
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
    ResponseEntity<CommonResponse<MemberResponseDTO>> updateMemberProfile(
            @RequestPart(value = "dto", required = false) MemberProfileRequestDTO dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    @Operation(summary = "로그인한 사용자의 프로필 조회",
            description = "로그인한 사용자의 프로필 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인한 사용자의 프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "로그인한 사용자의 프로필 조회 성공",
                                      "data": {
                                          "id": 2,
                                          "nickname": "A",
                                          "title": null,
                                          "profileUrl": "https://s3elixir.s3.ap-northeast-2.amazonaws.com/member/XXX.jpg",
                                          "followerCount": 1,
                                          "followingCount": 2
                                        }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "로그인한 사용자의 프로필 조회 실패",
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
    public ResponseEntity<CommonResponse<MemberProfileResponseDTO>> getMemberProfile(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    @Operation(summary = "특정 사용자의 프로필 조회",
            description = "특정 사용자의 프로필 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "특정 사용자의 프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "특정 사용자의 프로필 조회 성공 - id: 1",
                                      "data": {
                                        "memberId": 1,
                                        "titles": [ ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "특정 사용자의 프로필 조회 실패",
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
    public ResponseEntity<CommonResponse<MemberProfileResponseDTO>> getOtherMemberProfile(
            @PathVariable("memberId") Long memberId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 로그인한 사용자가 작성한 레시피 조회하기
    @Operation(summary = "로그인한 사용자가 작성한 레시피 조회하기", description = "로그인한 사용자가 작성한 레시피를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내가 작성한 레시피 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "내 레시피 조회 성공",
                                      "data": [
                                        {
                                          "recipeId": 49,
                                          "imageUrl": "https://image.com"
                                        }
                                      ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getMyRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 로그인한 사용자가 스크랩한 레시피 조회하기
    @Operation(summary = "로그인한 사용자가 스크랩한 레시피 조회하기", description = "로그인한 사용자가 스크랩한 레시피를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내가 스크랩한 레시피 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "내가 스크랩한 레시피 조회 성공",
                                      "data": [
                                        {
                                          "recipeId": 1,
                                          "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00028_1.png"
                                        },
                                        {
                                          "recipeId": 2,
                                          "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00029_1.png"
                                        }
                                      ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getMyScrapRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails
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


    // 로그인한 사용자의 모든 챌린지 업적 정보 조회
    @Operation(summary = "로그인한 사용자의 모든 챌린지 업적 정보 조회하기", description = "로그인한 사용자의 모든 챌린지 업적 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인한 사용자의 모든 챌린지 업적 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                       "status": 200,
                                       "code": "200 OK",
                                       "message": "모든 챌린지 업적 조회 성공",
                                       "data": [
                                         {
                                           "year": 2025,
                                           "month": 2,
                                           "achievementName": "비타민 수호자",
                                           "achievementImageUrl": "https://example.com/images/color_2.png",
                                           "challengeCompleted": true
                                         },
                                         {
                                           "year": 2025,
                                           "month": 3,
                                           "achievementName": "비타민 수호자",
                                           "achievementImageUrl": "https://example.com/images/color_3.png",
                                           "challengeCompleted": true
                                         },
                                         {
                                           "year": 2025,
                                           "month": 4,
                                           "achievementName": "비타민 수호자",
                                           "achievementImageUrl": "https://example.com/images/color_4.png",
                                           "challengeCompleted": true
                                         },
                                         {
                                           "year": 2025,
                                           "month": 5,
                                           "achievementName": "비타민 수호자",
                                           "achievementImageUrl": "https://example.com/images/gray_5.png",
                                           "challengeCompleted": false
                                         },
                                         {
                                           "year": 2025,
                                           "month": 6,
                                           "achievementName": "비타민 수호자",
                                           "achievementImageUrl": "https://example.com/images/gray_6.png",
                                           "challengeCompleted": false
                                         }
                                       ]
                                     }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getAllAchievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    );


    // 로그인한 사용자의 달성한 업적 최신 3개 조회하기
    @Operation(summary = "로그인한 사용자의 달성한 업적 최신 3개 조회하기", description = "로그인한 사용자의 달성한 업적 최신 3개를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인한 사용자의 달성한 업적 최신 3개 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "최근 업적 3개 조회 성공",
                                      "data": [
                                        {
                                          "year": 2025,
                                          "month": 4,
                                          "achievementName": "비타민 수호자",
                                          "achievementImageUrl": "https://example.com/images/color_4.png",
                                          "challengeCompleted": true
                                        },
                                        {
                                          "year": 2025,
                                          "month": 3,
                                          "achievementName": "비타민 수호자",
                                          "achievementImageUrl": "https://example.com/images/color_3.png",
                                          "challengeCompleted": true
                                        },
                                        {
                                          "year": 2025,
                                          "month": 2,
                                          "achievementName": "비타민 수호자",
                                          "achievementImageUrl": "https://example.com/images/color_2.png",
                                          "challengeCompleted": true
                                        }
                                      ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getTop3Achievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 로그인한 사용자의 설문조사 결과 조회하기
    @Operation(summary = "로그인한 사용자의 설문조사 결과 조회하기",
            description = "로그인한 사용자의 설문조사 결과를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인한 사용자의 설문조사 결과 조회하기 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "설문조사 결과 조회 성공, 회원 ID: 1",
                                      "data": {
                                            "memberId": 1,
                                            "allergies": ["알류", "호두"],
                                            "mealStyles": ["고기 위주", "혼합식"],
                                            "recipeStyles": ["한식", "양식", "디저트"],
                                            "reasons": ["혈당 조절"]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "로그인한 사용자의 설문조사 결과 조회하기 실패",
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
    ResponseEntity<CommonResponse<SurveyResponseDTO>> getSurvey(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 로그인한 사용자의 설문조사 결과 수정하기
    @Operation(summary = "로그인한 사용자의 설문조사 결과 수정하기",
            description = "로그인한 사용자의 설문조사 결과를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인한 사용자의 설문조사 결과 수정하기 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "설문조사 결과 수정 성공, 회원 ID: 1",
                                      "data": {
                                            "memberId": 1,
                                            "allergies": ["알류", "호두"],
                                            "mealStyles": ["고기 위주", "혼합식"],
                                            "recipeStyles": ["한식", "양식", "디저트"],
                                            "reasons": ["혈당 조절"]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "로그인한 사용자의 설문조사 결과 수정하기 실패",
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
    ResponseEntity<CommonResponse<SurveyResponseDTO>> updateSurvey(
            @RequestBody SurveyRequestDTO dto,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 다른 사용자가 업로드한 모든 레시피 조회하기
    @Operation(summary = "다른 사용자가 업로드한 모든 레시피 조회하기", description = "다른 사용자가 업로드한 모든 레시피 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다른 사용자가 업로드한 모든 레시피 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "다른 사용자 레시피 조회 성공",
                                      "data": [
                                        {
                                          "recipeId": 1,
                                          "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00028_1.png"
                                        },
                                        {
                                          "recipeId": 2,
                                          "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00029_1.png"
                                        },
                                        .
                                        .
                                        .
                                        {
                                          "recipeId": 9,
                                          "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00089_1.png"
                                        }
                                      ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getUserRecipes(
            @PathVariable("memberId") Long memberId
    );


    // 다른 사용자의 모든 챌린지 업적 정보 조회하기
    @Operation(summary = "다른 사용자의 모든 챌린지 업적 정보 조회하기", description = "다른 사용자의 모든 챌린지 업적 정보 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다른 사용자의 모든 챌린지 업적 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "다른 사용자의 모든 업적 조회 성공",
                                      "data": [
                                        {
                                          "year": 2025,
                                          "month": 2,
                                          "achievementName": "비타민 수호자",
                                          "achievementImageUrl": "https://example.com/images/gray_2.png",
                                          "challengeCompleted": false
                                        },
                                        .
                                        .
                                        .
                                        {
                                          "year": 2025,
                                          "month": 6,
                                          "achievementName": "비타민 수호자",
                                          "achievementImageUrl": "https://example.com/images/gray_6.png",
                                          "challengeCompleted": false
                                        }
                                      ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getAllAchievementsByMemberId(
            @PathVariable Long memberId
    );


    // 다른 사용자의 최근 3개 업적 조회하기
    @Operation(summary = "다른 사용자의 최근 3개 업적 조회하기", description = "다른 사용자의 최근 3개 업적 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다른 사용자의 최근 3개 업적 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "다른 사용자의 최근 업적 3개 조회 성공",
                                      "data": [
                                        {
                                          "year": 2025,
                                          "month": 4,
                                          "achievementName": "비타민 수호자",
                                          "achievementImageUrl": "https://example.com/images/color_4.png",
                                          "challengeCompleted": true
                                        },
                                        {
                                          "year": 2025,
                                          "month": 3,
                                          "achievementName": "비타민 수호자",
                                          "achievementImageUrl": "https://example.com/images/color_3.png",
                                          "challengeCompleted": true
                                        },
                                        {
                                          "year": 2025,
                                          "month": 2,
                                          "achievementName": "비타민 수호자",
                                          "achievementImageUrl": "https://example.com/images/color_2.png",
                                          "challengeCompleted": true
                                        }
                                      ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getTop3AchievementsByMemberId(
            @PathVariable Long memberId
    );
}