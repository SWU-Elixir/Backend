package BE_Elixir.Elixir.domain.member.controller.api;

import BE_Elixir.Elixir.domain.member.dto.request.MemberProfileRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SurveyRequestDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Tag(name = "MyPage API", description = "마이페이지 관련 API")
public interface MyPageApi {

    // 회원 정보 조회
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

    // 사용자 프로필 조회 (칭호, 닉네임, 프로필사진, 팔로워 수, 팔로잉 수)
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
    ResponseEntity<CommonResponse<MemberProfileResponseDTO>> getMemberProfile(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 다른 사용자 프로필 조회 (칭호, 닉네임, 프로필사진, 팔로워 수, 팔로잉 수)
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
    ResponseEntity<CommonResponse<MemberProfileResponseDTO>> getOtherMemberProfile(
            @PathVariable("memberId") Long memberId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 프로필 수정 시, 얻은 칭호 목록 조회
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

    // 로그인한 사용자 프로필 수정하기
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

    // 로그인한 사용자의 모든 챌린지 업적 조회
    @Operation(summary = "로그인한 사용자의 모든 챌린지 업적 조회하기", description = "로그인한 사용자의 모든 챌린지 업적을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인한 사용자의 모든 챌린지 업적 조회 성공",
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
    ResponseEntity<CommonResponse<List<MemberChallengeResponseDTO>>> getAllAchievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 로그인한 사용자가 달성한 최신 챌린지 업적 3개 조회하기
    @Operation(summary = "로그인한 사용자가 달성한 최신 챌린지 업적 3개 조회하기", description = "로그인한 사용자가 달성한 최신 챌린지 업적 3개를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인한 사용자가 달성한 최신 챌린지 업적 3개 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "로그인한 사용자가 달성한 최신 챌린지 업적 3개 성공",
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
    ResponseEntity<CommonResponse<List<MemberChallengeResponseDTO>>> getTop3Achievements(
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
    @Operation(summary = "다른 사용자의 모든 챌린지 업적 조회하기", description = "다른 사용자의 모든 챌린지 업적 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다른 사용자의 모든 챌린지 업적 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "다른 사용자의 모든 챌린지 업적 조회 성공",
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
    ResponseEntity<CommonResponse<List<MemberChallengeResponseDTO>>> getAllAchievementsByMemberId(
            @PathVariable Long memberId
    );

    // 다른 사용자가 달성한 최신 챌린지 업적 3개 조회하기
    @Operation(summary = "다른 사용자가 달성한 최신 챌린지 업적 3개 조회하기", description = "다른 사용자가 달성한 최신 챌린지 업적 3개 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다른 사용자가 달성한 최신 챌린지 업적 3개 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "다른 사용자가 달성한 최신 챌린지 업적 3개 조회 성공",
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
    ResponseEntity<CommonResponse<List<MemberChallengeResponseDTO>>> getTop3AchievementsByMemberId(
            @PathVariable Long memberId
    );


    // 로그인한 사용자의 모든 업적 조회
    @Operation(summary = "로그인한 사용자의 모든 업적 조회하기", description = "로그인한 사용자의 모든 업적을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인한 사용자의 모든 업적 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "로그인한 사용자의 모든 업적 조회 성공",
                                      "data": [
                                          {
                                            "achievementName": "꾸준함 입문자",
                                            "achievementImageUrl": "https://s3elixir.s3.ap-northeast-2.amazonaws.com/achievement/gray/TotalLogin1_g.png",
                                            "completed": false,
                                            "level": 1,
                                            "type": "TOTAL_LOGIN_DAYS",
                                            "code": "TOTAL_LOGIN_DAYS_LV1"
                                          },
                                          {
                                            "achievementName": "생활 루틴러",
                                            "achievementImageUrl": "https://s3elixir.s3.ap-northeast-2.amazonaws.com/achievement/gray/TotalLogin2_g.png",
                                            "completed": false,
                                            "level": 2,
                                            "type": "TOTAL_LOGIN_DAYS",
                                            "code": "TOTAL_LOGIN_DAYS_LV2"
                                          },
                                         {}...
                                       ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getAllMyStatsAchievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 로그인한 사용자가 달성한 최신 업적 3개 조회
    @Operation(summary = "로그인한 사용자가 달성한 최신 업적 3개 조회하기", description = "로그인한 사용자가 달성한 최신 업적 3개를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인한 사용자가 달성한 최신 업적 3개 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "로그인한 사용자가 달성한 최신 업적 3개 조회 성공",
                                      "data": [
                                        {
                                          "achievementName": "요리 탐험가",
                                          "achievementImageUrl": "https://s3elixir.s3.ap-northeast-2.amazonaws.com/achievement/color/Scrap1.png",
                                          "completed": true,
                                          "level": 1,
                                          "type": "TOTAL_SCRAPS",
                                          "code": "TOTAL_SCRAPS_LV1"
                                        }
                                      ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getMyTop3Achievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 다른 사용자의 모든 업적 조회
    @Operation(summary = "다른 사용자의 모든 업적 조회하기", description = "다른 사용자의 모든 업적을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다른 사용자의 모든 업적 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "다른 사용자의 모든 업적 조회 성공",
                                      "data": [
                                          {
                                            "achievementName": "꾸준함 입문자",
                                            "achievementImageUrl": "https://s3elixir.s3.ap-northeast-2.amazonaws.com/achievement/gray/TotalLogin1_g.png",
                                            "completed": false,
                                            "level": 1,
                                            "type": "TOTAL_LOGIN_DAYS",
                                            "code": "TOTAL_LOGIN_DAYS_LV1"
                                          },
                                          {}...
                                       ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getAllStatsAchievementsByMemberId(
            @PathVariable Long memberId
    );


    // 다른 사용자가 달성한 최신 업적 3개 조회
    @Operation(summary = "다른 사용자가 달성한 최신 업적 3개 조회하기", description = "다른 사용자가 달성한 최신 업적 3개를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다른 사용자가 달성한 최신 업적 3개 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "다른 사용자가 달성한 최신 업적 3개 조회 성공",
                                      "data": [
                                        {
                                          "achievementName": "요리 탐험가",
                                          "achievementImageUrl": "https://s3elixir.s3.ap-northeast-2.amazonaws.com/achievement/color/Scrap1.png",
                                          "completed": true,
                                          "level": 1,
                                          "type": "TOTAL_SCRAPS",
                                          "code": "TOTAL_SCRAPS_LV1"
                                        }
                                      ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getTop3StatsAchievementsByMemberId(
            @PathVariable Long memberId
    );
}