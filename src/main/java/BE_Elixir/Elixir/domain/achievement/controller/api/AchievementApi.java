package BE_Elixir.Elixir.domain.achievement.controller.api;

import BE_Elixir.Elixir.domain.achievement.dto.AllAchievementStatusResponseDTO;
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

@Tag(name = "Achievement API", description = "사용자 행동 기반 업적 달성 API")
public interface AchievementApi {
    // 업적 통합 조회
    @Operation(summary = "사용자의 업적 통합 조회", description = "사용자의 업적을 통합 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업적 통합 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "mj@example.com 사용자의 현재 진행 상태 및 달성 여부",
                                      "data": {
                                        "achievementMap": {
                                          "TOTAL_LOGIN_DAYS": {
                                            "currentValue": 0,
                                            "achievements": [
                                              {
                                                "id": 1,
                                                "name": "꾸준함 입문자",
                                                "level": 1,
                                                "targetValue": 7,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 2,
                                                "name": "생활 루틴러",
                                                "level": 2,
                                                "targetValue": 30,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 3,
                                                "name": "엘릭서 수호자",
                                                "level": 3,
                                                "targetValue": 100,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              }
                                            ]
                                          },
                                          "TOTAL_FOLLOWERS": {
                                            "currentValue": 0,
                                            "achievements": [
                                              {
                                                "id": 16,
                                                "name": "식단 친구",
                                                "level": 1,
                                                "targetValue": 3,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 17,
                                                "name": "엘릭서 인기인",
                                                "level": 2,
                                                "targetValue": 10,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 18,
                                                "name": "저속노화 리더",
                                                "level": 3,
                                                "targetValue": 50,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              }
                                            ]
                                          },
                                          "TOTAL_RECIPE_LOGS": {
                                            "currentValue": 0,
                                            "achievements": [
                                              {
                                                "id": 10,
                                                "name": "초보 요리사",
                                                "level": 1,
                                                "targetValue": 1,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 11,
                                                "name": "맛 연구소",
                                                "level": 2,
                                                "targetValue": 5,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 12,
                                                "name": "요리 창조자",
                                                "level": 3,
                                                "targetValue": 20,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              }
                                            ]
                                          },
                                          "TOTAL_SCRAPS": {
                                            "currentValue": 0,
                                            "achievements": [
                                              {
                                                "id": 13,
                                                "name": "요리 탐험가",
                                                "level": 1,
                                                "targetValue": 3,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 14,
                                                "name": "레시피 컬렉터",
                                                "level": 2,
                                                "targetValue": 10,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 15,
                                                "name": "엘릭서 아카이브",
                                                "level": 3,
                                                "targetValue": 30,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              }
                                            ]
                                          },
                                          "TOTAL_DIET_LOGS": {
                                            "currentValue": 0,
                                            "achievements": [
                                              {
                                                "id": 7,
                                                "name": "식단 파수꾼",
                                                "level": 1,
                                                "targetValue": 10,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 8,
                                                "name": "밥상 전문자",
                                                "level": 2,
                                                "targetValue": 30,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 9,
                                                "name": "쩝쩝박사",
                                                "level": 3,
                                                "targetValue": 100,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              }
                                            ]
                                          },
                                          "CONSECUTIVE_LOGIN_DAYS": {
                                            "currentValue": 0,
                                            "achievements": [
                                              {
                                                "id": 4,
                                                "name": "새싹신입",
                                                "level": 1,
                                                "targetValue": 3,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 5,
                                                "name": "의지력 수호자",
                                                "level": 2,
                                                "targetValue": 7,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              },
                                              {
                                                "id": 6,
                                                "name": "앱 중독자",
                                                "level": 3,
                                                "targetValue": 30,
                                                "achieved": false,
                                                "currentProgress": 0,
                                                "achievedAt": null
                                              }
                                            ]
                                          }
                                        }
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "업적 통합 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<AllAchievementStatusResponseDTO>> getMyAchievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    );
}
