package BE_Elixir.Elixir.domain.achievement.controller.api;

import BE_Elixir.Elixir.domain.achievement.dto.AllAchievementStatusResponseDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeListResponseDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

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
                                        "achievementMap": {}
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
