package BE_Elixir.Elixir.domain.achievement.service;


import BE_Elixir.Elixir.domain.achievement.entity.MemberStats;
import BE_Elixir.Elixir.domain.achievement.repository.MemberStatsRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MemberStatsService {
    private final MemberStatsRepository memberStatsRepository;
    private final AchievementService achievementService;

    // 업적 유형에 따른 Stats 증가
    @Transactional
    public void increaseStat(Long memberId, AchievementType type, int amount) {
        MemberStats stats = memberStatsRepository.findById(memberId)
                .orElseGet(() -> createDefaultStats(memberId));
        LocalDate today = LocalDate.now();
        switch (type) {
            case TOTAL_LOGIN_DAYS -> {
                if (!today.equals(stats.getLastLoginDate())) {
                    stats.setTotalLoginDays(stats.getTotalLoginDays() + 1);
                }
            }
            case CONSECUTIVE_LOGIN_DAYS -> updateConsecutiveLogin(stats);
            case TOTAL_DIET_LOGS -> stats.setTotalDietLogs(stats.getTotalDietLogs() + amount);
            case TOTAL_RECIPE_LOGS -> stats.setTotalRecipeLogs(stats.getTotalRecipeLogs() + amount);
            case TOTAL_SCRAPS -> stats.setTotalScraps(stats.getTotalScraps() + amount);
            case TOTAL_FOLLOWERS -> stats.setTotalFollowers(stats.getTotalFollowers() + amount);
        }

        memberStatsRepository.save(stats);

        // 업적 진행도 동기화
        achievementService.syncUserAchievements(
                memberId,
                type,
                getCurrentValueByType(stats, type)
        );
    }

    // 연속 로그인 일수
    private void updateConsecutiveLogin(MemberStats stats) {
        LocalDate today = LocalDate.now();
        LocalDate lastLoginDate = stats.getLastLoginDate();

        if (lastLoginDate == null || lastLoginDate.isBefore(today.minusDays(1))) {
            // 마지막 로그인일이 2일 이상 전이면 연속 로그인 리셋
            stats.setConsecutiveLoginDays(1);
        } else if (lastLoginDate.equals(today.minusDays(1))) {
            // 어제 접속, 연속 로그인 성공
            stats.setConsecutiveLoginDays(stats.getConsecutiveLoginDays() + 1);
        } else if (lastLoginDate.equals(today)) {
            // 이미 오늘 접속, 중복 증가 방지
            return;
        }

        // 마지막 로그인일 갱신
        stats.setLastLoginDate(today);
    }


    // 통계 default
    private MemberStats createDefaultStats(Long memberId) {
        MemberStats stats = new MemberStats();
        stats.setMemberId(memberId);
        stats.setTotalLoginDays(0);
        stats.setConsecutiveLoginDays(0);
        stats.setTotalDietLogs(0);
        stats.setTotalRecipeLogs(0);
        stats.setTotalScraps(0);
        stats.setTotalFollowers(0);
        return stats;
    }


    // 현재 값 반환
    private int getCurrentValueByType(MemberStats stats, AchievementType type) {
        return switch (type) {
            case TOTAL_LOGIN_DAYS -> stats.getTotalLoginDays();
            case CONSECUTIVE_LOGIN_DAYS -> stats.getConsecutiveLoginDays();
            case TOTAL_DIET_LOGS -> stats.getTotalDietLogs();
            case TOTAL_RECIPE_LOGS -> stats.getTotalRecipeLogs();
            case TOTAL_SCRAPS -> stats.getTotalScraps();
            case TOTAL_FOLLOWERS -> stats.getTotalFollowers();
        };
    }
}
