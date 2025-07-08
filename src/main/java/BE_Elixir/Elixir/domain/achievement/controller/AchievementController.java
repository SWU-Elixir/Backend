package BE_Elixir.Elixir.domain.achievement.controller;

import BE_Elixir.Elixir.domain.achievement.controller.api.AchievementApi;
import BE_Elixir.Elixir.domain.achievement.dto.AllAchievementStatusResponseDTO;
import BE_Elixir.Elixir.domain.achievement.service.AchievementService;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeProgressResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/achievement")
@RequiredArgsConstructor
public class AchievementController implements AchievementApi {
    private final AchievementService achievementService;

    // 업적 통합 조회
    @GetMapping("/progress")
    public ResponseEntity<CommonResponse<AllAchievementStatusResponseDTO>> getMyAchievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        AllAchievementStatusResponseDTO dto = achievementService.getMyAchievements(memberDetails);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                memberDetails.getUsername() + " 사용자의 현재 진행 상태 및 달성 여부", dto));
    }
}