package BE_Elixir.Elixir.domain.achievement.service;

import BE_Elixir.Elixir.domain.achievement.dto.AchievementCategoryDTO;
import BE_Elixir.Elixir.domain.achievement.dto.AllAchievementStatusResponseDTO;
import BE_Elixir.Elixir.domain.achievement.entity.Achievement;
import BE_Elixir.Elixir.domain.achievement.entity.MemberAchievement;
import BE_Elixir.Elixir.domain.achievement.entity.MemberStats;
import BE_Elixir.Elixir.domain.achievement.repository.AchievementRepository;
import BE_Elixir.Elixir.domain.achievement.repository.MemberAchievementRepository;
import BE_Elixir.Elixir.domain.achievement.repository.MemberStatsRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AchievementService 테스트")
class AchievementServiceTest {
    @InjectMocks
    private AchievementService service;

    @Mock private MemberAchievementRepository memberAchievementRepository;
    @Mock private AchievementRepository achievementRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private MemberStatsRepository memberStatsRepository;

    private Member member;
    private MemberDetails memberDetails;
    private Achievement achievement;
    private MemberAchievement memberAchievement;
    private MemberStats memberStats;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("tester")
                .build();

        memberDetails = new MemberDetails(member);

        achievement = new Achievement();
        achievement.setId(100L);
        achievement.setType(AchievementType.TOTAL_LOGIN_DAYS);
        achievement.setAchievementName("첫 로그인 업적");
        achievement.setDescription("로그인을 처음 완료했습니다.");
        achievement.setLevel(1);
        achievement.setTargetValue(1);

        memberAchievement = new MemberAchievement();
        memberAchievement.setMember(member);
        memberAchievement.setAchievement(achievement);
        memberAchievement.setCurrentProgress(1);
        memberAchievement.setCompleted(true);
        memberAchievement.setCompletedAt(LocalDateTime.now());

        memberStats = new MemberStats();
        memberStats.setMemberId(member.getId());
        memberStats.setTotalLoginDays(1);
        memberStats.setConsecutiveLoginDays(1);
        memberStats.setTotalDietLogs(0);
        memberStats.setTotalRecipeLogs(0);
        memberStats.setTotalScraps(0);
        memberStats.setTotalFollowers(0);
    }


    @Nested
    @DisplayName("업적 목록 조회 테스트")
    class GetMyAchievements {

        @Test
        @DisplayName("성공: 회원 업적 목록 조회")
        void getMyAchievements_success() {
            // given
            when(memberRepository.findByEmail(memberDetails.getUsername()))
                    .thenReturn(Optional.of(member));
            when(memberStatsRepository.findById(member.getId()))
                    .thenReturn(Optional.of(memberStats));
            when(memberAchievementRepository.findAllByMember(member))
                    .thenReturn(List.of(memberAchievement));
            when(achievementRepository.findAll())
                    .thenReturn(List.of(achievement));

            // when
            AllAchievementStatusResponseDTO result = service.getMyAchievements(memberDetails);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAchievementMap()).containsKey(AchievementType.TOTAL_LOGIN_DAYS);

            AchievementCategoryDTO categoryDTO = result.getAchievementMap().get(AchievementType.TOTAL_LOGIN_DAYS);
            assertThat(categoryDTO).isNotNull();
            assertThat(categoryDTO.getCurrentValue()).isEqualTo(1);
            assertThat(categoryDTO.getAchievements()).hasSize(1);
            assertThat(categoryDTO.getAchievements().get(0).getName())
                    .isEqualTo("첫 로그인 업적");

            verify(memberRepository).findByEmail(anyString());
        }

        @Test
        @DisplayName("예외: 회원이 존재하지 않을 경우")
        void getMyAchievements_memberNotFound() {
            // given
            when(memberRepository.findByEmail(anyString()))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.getMyAchievements(memberDetails))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("업적 달성 동기화 테스트")
    class SyncUserAchievements {

        @Test
        @DisplayName("성공: 기존 업적의 진행도 갱신")
        void syncUserAchievements_updateProgress() {
            // given
            when(memberRepository.findById(member.getId()))
                    .thenReturn(Optional.of(member));
            when(achievementRepository.findAllByType(AchievementType.TOTAL_LOGIN_DAYS))
                    .thenReturn(List.of(achievement));
            when(memberAchievementRepository.findAllByMember(member))
                    .thenReturn(List.of(memberAchievement));

            // when
            service.syncUserAchievements(member.getId(), AchievementType.TOTAL_LOGIN_DAYS, 5);

            // then
            assertThat(memberAchievement.getCurrentProgress()).isEqualTo(5);
            assertThat(memberAchievement.isCompleted()).isTrue();
            verify(memberAchievementRepository).save(any(MemberAchievement.class));
        }

        @Test
        @DisplayName("예외: 회원이 존재하지 않을 경우")
        void syncUserAchievements_memberNotFound() {
            // given
            when(memberRepository.findById(anyLong()))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.syncUserAchievements(999L, AchievementType.TOTAL_LOGIN_DAYS, 5))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }
}