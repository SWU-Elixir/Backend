package BE_Elixir.Elixir.domain.achievement.service;

import BE_Elixir.Elixir.domain.achievement.entity.MemberStats;
import BE_Elixir.Elixir.domain.achievement.repository.MemberStatsRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberStatsService 테스트")
class MemberStatsServiceTest {
    @InjectMocks
    private MemberStatsService memberStatsService;

    @Mock private MemberStatsRepository memberStatsRepository;
    @Mock private AchievementService achievementService;

    private MemberStats stats;

    private final Long memberId = 1L;

    @BeforeEach
    void setUp() {
        stats = new MemberStats();
        stats.setMemberId(memberId);
        stats.setTotalLoginDays(3);
        stats.setConsecutiveLoginDays(3);
        stats.setTotalDietLogs(2);
        stats.setTotalRecipeLogs(4);
        stats.setTotalScraps(1);
        stats.setTotalFollowers(0);
        stats.setLastLoginDate(LocalDate.now().minusDays(1));
    }

    @Nested
    @DisplayName("increaseStat 성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("TOTAL_LOGIN_DAYS - 오늘 최초 로그인 시 하루 증가")
        void increaseTotalLoginDays() {
            stats.setLastLoginDate(LocalDate.now().minusDays(1));
            when(memberStatsRepository.findById(memberId)).thenReturn(Optional.of(stats));

            memberStatsService.increaseStat(memberId, AchievementType.TOTAL_LOGIN_DAYS, 1);

            assertThat(stats.getTotalLoginDays()).isEqualTo(4);
            verify(memberStatsRepository).save(stats);
        }

        @Test
        @DisplayName("CONSECUTIVE_LOGIN_DAYS - 어제 로그인한 경우 연속 로그인 증가")
        void increaseConsecutiveLoginDays() {
            stats.setLastLoginDate(LocalDate.now().minusDays(1));
            when(memberStatsRepository.findById(memberId)).thenReturn(Optional.of(stats));

            memberStatsService.increaseStat(memberId, AchievementType.CONSECUTIVE_LOGIN_DAYS, 1);

            assertThat(stats.getConsecutiveLoginDays()).isEqualTo(4);
            verify(memberStatsRepository).save(stats);
        }

        @Test
        @DisplayName("TOTAL_DIET_LOGS - 식단 로그 누적")
        void increaseTotalDietLogs() {
            when(memberStatsRepository.findById(memberId)).thenReturn(Optional.of(stats));

            memberStatsService.increaseStat(memberId, AchievementType.TOTAL_DIET_LOGS, 3);

            assertThat(stats.getTotalDietLogs()).isEqualTo(5);
            verify(memberStatsRepository).save(stats);
        }

        @Test
        @DisplayName("TOTAL_RECIPE_LOGS - 레시피 로그 누적")
        void increaseTotalRecipeLogs() {
            when(memberStatsRepository.findById(memberId)).thenReturn(Optional.of(stats));

            memberStatsService.increaseStat(memberId, AchievementType.TOTAL_RECIPE_LOGS, 2);

            assertThat(stats.getTotalRecipeLogs()).isEqualTo(6);
            verify(memberStatsRepository).save(stats);
        }

        @Test
        @DisplayName("TOTAL_SCRAPS - 스크랩 누적")
        void increaseTotalScraps() {
            when(memberStatsRepository.findById(memberId)).thenReturn(Optional.of(stats));

            memberStatsService.increaseStat(memberId, AchievementType.TOTAL_SCRAPS, 5);

            assertThat(stats.getTotalScraps()).isEqualTo(6);
            verify(memberStatsRepository).save(stats);
        }

        @Test
        @DisplayName("TOTAL_FOLLOWERS - 팔로워 수 누적")
        void increaseTotalFollowers() {
            when(memberStatsRepository.findById(memberId)).thenReturn(Optional.of(stats));

            memberStatsService.increaseStat(memberId, AchievementType.TOTAL_FOLLOWERS, 10);

            assertThat(stats.getTotalFollowers()).isEqualTo(10);
            verify(memberStatsRepository).save(stats);
        }
    }

    @Nested
    @DisplayName("increaseStat 예외 케이스")
    class ExceptionCases {

        @Test
        @DisplayName("기존 데이터 없으면 createDefaultStats로 생성 후 저장")
        void createDefaultStatsAndIncrease() {
            when(memberStatsRepository.findById(memberId)).thenReturn(Optional.empty());
            ArgumentCaptor<MemberStats> captor = ArgumentCaptor.forClass(MemberStats.class);

            memberStatsService.increaseStat(memberId, AchievementType.TOTAL_SCRAPS, 2);

            verify(memberStatsRepository).save(captor.capture());
            MemberStats savedStats = captor.getValue();

            assertThat(savedStats.getMemberId()).isEqualTo(memberId);
            assertThat(savedStats.getTotalScraps()).isEqualTo(2);
        }

        @Test
        @DisplayName("CONSECUTIVE_LOGIN_DAYS - 이미 오늘 로그인한 경우 증가 안 함")
        void consecutiveLogin_noIncrease_ifAlreadyLoggedInToday() {
            stats.setLastLoginDate(LocalDate.now()); // 오늘 로그인
            int before = stats.getConsecutiveLoginDays();
            when(memberStatsRepository.findById(memberId)).thenReturn(Optional.of(stats));

            memberStatsService.increaseStat(memberId, AchievementType.CONSECUTIVE_LOGIN_DAYS, 1);

            assertThat(stats.getConsecutiveLoginDays()).isEqualTo(before); // 변화 없음
            verify(memberStatsRepository).save(stats);
        }

        @Test
        @DisplayName("CONSECUTIVE_LOGIN_DAYS - 2일 이상 미접속 시 0으로 초기화")
        void consecutiveLogin_reset_ifMissedTwoDays() {
            stats.setLastLoginDate(LocalDate.now().minusDays(3)); // 3일 미접속
            stats.setConsecutiveLoginDays(5);
            when(memberStatsRepository.findById(memberId)).thenReturn(Optional.of(stats));

            memberStatsService.increaseStat(memberId, AchievementType.CONSECUTIVE_LOGIN_DAYS, 0);

            assertThat(stats.getConsecutiveLoginDays()).isEqualTo(1); // 초기화
            verify(memberStatsRepository).save(stats);
        }
    }
}
