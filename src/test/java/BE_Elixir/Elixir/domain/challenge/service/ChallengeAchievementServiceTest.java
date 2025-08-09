package BE_Elixir.Elixir.domain.challenge.service;

import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeCompletedResponseDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeProgressResponseDTO;
import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievementId;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeAchievementService 테스트")
class ChallengeAchievementServiceTest {
    @InjectMocks
    private ChallengeAchievementService service;

    @Mock private ChallengeRepository challengeRepository;
    @Mock private ChallengeAchievementRepository challengeAchievementRepository;
    @Mock private MemberRepository memberRepository;

    private Challenge challenge;
    private ChallengeAchievement achievement;
    private Member member;
    private Long challengeId;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testUser")
                .build();

        challengeId = 100L;

        challenge = new Challenge();
        challenge.setId(challengeId);
        challenge.setName("건강왕");
        challenge.setYear(2025);
        challenge.setMonth(8);
        challenge.setAchievementName("건강왕");
        challenge.setAchievementImageUrl("achievement.png");

        achievement = new ChallengeAchievement();
        achievement.setMemberId(member.getId());
        achievement.setChallengeId(challengeId);

        // 모든 목표 달성
        achievement.setStep1Goal1Achieved(true);
        achievement.setStep1Goal2Achieved(true);
        achievement.setStep2Goal1Achieved(true);
        achievement.setStep2Goal2Achieved(true);
        achievement.setStep3Goal1Achieved(true);
        achievement.setStep3Goal2Achieved(true);
        achievement.setStep4Goal1Achieved(true);
        achievement.setStep4Goal2Achieved(true);
    }

    @Nested
    @DisplayName("save 메서드")
    class SaveTests {
        @Test
        @DisplayName("성공: 챌린지 달성 정보 저장")
        void save_success() {
            // given
            when(challengeAchievementRepository.save(any())).thenReturn(achievement);

            // when
            service.save(achievement);

            // then
            verify(challengeAchievementRepository, times(1)).save(achievement);
        }
    }

    @Nested
    @DisplayName("getProgress 메서드")
    class GetProgressTests {
        @Test
        @DisplayName("성공: 진행 상황 조회")
        void getProgress_success() {
            // given
            when(challengeRepository.findByYearAndMonth(anyInt(), anyInt())).thenReturn(Optional.of(challenge));
            when(challengeAchievementRepository.findByChallengeIdAndMemberId(challengeId, member.getId()))
                    .thenReturn(Optional.of(achievement));
            when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

            // when
            ChallengeProgressResponseDTO result = service.getProgress(member.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("건강왕");
        }

        @Test
        @DisplayName("예외: 해당 월 챌린지가 없으면 CustomException 발생")
        void getProgress_fail_noChallenge() {
            // given
            when(challengeRepository.findByYearAndMonth(anyInt(), anyInt()))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(CustomException.class, () -> service.getProgress(member.getId()));
        }
    }

    @Nested
    @DisplayName("createIfNotExists 메서드")
    class CreateIfNotExistsTests {
        @Test
        @DisplayName("성공: 기존 데이터 반환")
        void createIfNotExists_existing() {
            // given
            when(challengeRepository.findByYearAndMonth(anyInt(), anyInt()))
                    .thenReturn(Optional.of(challenge));
            when(challengeAchievementRepository.findByChallengeIdAndMemberId(challengeId, member.getId()))
                    .thenReturn(Optional.of(achievement));

            // when
            ChallengeAchievement result = service.createIfNotExists(member.getId());

            // then
            assertThat(result).isSameAs(achievement);
        }

        @Test
        @DisplayName("성공: 기존 데이터 없으면 새로 생성")
        void createIfNotExists_new() {
            // given
            when(challengeRepository.findByYearAndMonth(anyInt(), anyInt()))
                    .thenReturn(Optional.of(challenge));
            when(challengeAchievementRepository.findByChallengeIdAndMemberId(challengeId, member.getId()))
                    .thenReturn(Optional.empty());
            when(challengeAchievementRepository.save(any())).thenReturn(achievement);

            // when
            ChallengeAchievement result = service.createIfNotExists(member.getId());

            // then
            assertThat(result).isSameAs(achievement);
            verify(challengeAchievementRepository).save(any());
        }

        @Test
        @DisplayName("예외: 해당 월 챌린지가 없으면 CustomException 발생")
        void createIfNotExists_fail_noChallenge() {
            // given
            when(challengeRepository.findByYearAndMonth(anyInt(), anyInt()))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(CustomException.class, () -> service.createIfNotExists(member.getId()));
        }
    }

    @Nested
    @DisplayName("getChallengeCompletion 메서드")
    class GetChallengeCompletionTests {
        @Test
        @DisplayName("성공: 모든 목표 달성 후 완료 상태 반환")
        void getChallengeCompletion_completed() {
            // given
            achievement.setStep4Goal1Achieved(true);
            achievement.setStep4Goal2Achieved(true);
            when(challengeRepository.findByYearAndMonth(anyInt(), anyInt())).thenReturn(Optional.of(challenge));
            when(challengeAchievementRepository.findByChallengeIdAndMemberId(challengeId, member.getId()))
                    .thenReturn(Optional.of(achievement));
            when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
            when(challengeAchievementRepository.save(any())).thenReturn(achievement);

            // when
            ChallengeCompletedResponseDTO result = service.getChallengeCompletion(member.getId());

            // then
            assertThat(result.isChallengeCompleted()).isTrue();
        }

        @Test
        @DisplayName("예외: 해당 월 챌린지가 없으면 CustomException 발생")
        void getChallengeCompletion_fail_noChallenge() {
            // given
            when(challengeRepository.findByYearAndMonth(anyInt(), anyInt()))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(CustomException.class, () -> service.getChallengeCompletion(member.getId()));
        }
    }

    @Nested
    @DisplayName("getBeforeProgress 메서드")
    class GetBeforeProgressTests {
        @Test
        @DisplayName("성공: 이전 진행 상황 조회 (기록 있음)")
        void getBeforeProgress_withRecord() {
            // given
            when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
            when(challengeAchievementRepository.findByChallengeIdAndMemberId(challengeId, member.getId()))
                    .thenReturn(Optional.of(achievement));

            // when
            ChallengeProgressResponseDTO result = service.getBeforeProgress(member.getId(), challengeId);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("성공: 이전 진행 상황 조회 (기록 없음)")
        void getBeforeProgress_noRecord() {
            // given
            when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
            when(challengeAchievementRepository.findByChallengeIdAndMemberId(challengeId, member.getId()))
                    .thenReturn(Optional.empty());

            // when
            ChallengeProgressResponseDTO result = service.getBeforeProgress(member.getId(), challengeId);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("예외: 해당 챌린지가 없으면 CustomException 발생")
        void getBeforeProgress_fail_noChallenge() {
            // given
            when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(CustomException.class, () -> service.getBeforeProgress(member.getId(), challengeId));
        }
    }

    @Nested
    @DisplayName("challengeParticipation 메서드")
    class ChallengeParticipationTests {
        @Test
        @DisplayName("성공: 기존 기록이 없으면 새로 저장")
        void challengeParticipation_new() {
            // given
            when(memberRepository.findByEmail(anyString()))
                    .thenReturn(Optional.of(member));
            when(challengeRepository.findByYearAndMonth(anyInt(), anyInt())).thenReturn(Optional.of(challenge));
            when(challengeAchievementRepository.existsById(any(ChallengeAchievementId.class))).thenReturn(false);

            // when
            service.challengeParticipation("test@test.com");

            // then
            verify(challengeAchievementRepository).save(any(ChallengeAchievement.class));
        }

        @Test
        @DisplayName("성공: 이번 달 챌린지가 없으면 저장하지 않음")
        void challengeParticipation_noChallengeThisMonth() {
            // given
            when(memberRepository.findByEmail(anyString()))
                    .thenReturn(Optional.of(member));
            when(challengeRepository.findByYearAndMonth(anyInt(), anyInt())).thenReturn(Optional.empty());

            // when
            service.challengeParticipation("test@test.com");

            // then
            verify(challengeAchievementRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외: 회원이 없으면 CustomException 발생")
        void challengeParticipation_fail_noMember() {
            // given
            when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // when & then
            assertThrows(CustomException.class, () -> service.challengeParticipation("test@test.com"));
        }
    }
}