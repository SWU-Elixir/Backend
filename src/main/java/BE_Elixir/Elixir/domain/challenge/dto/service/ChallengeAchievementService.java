package BE_Elixir.Elixir.domain.challenge.dto.service;

import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeCompletedResponseDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeProgressResponseDTO;
import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievementId;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChallengeAchievementService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeAchievementRepository challengeAchievementRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void save(ChallengeAchievement achievement) {
        challengeAchievementRepository.save(achievement); // DB 반영
    }

    // 로그인한 사용자의 현재 진행 상황 조회
    public ChallengeProgressResponseDTO getProgress(
            Long memberId
    ) {
        ChallengeAchievement achievement = createIfNotExists(memberId);

        Challenge challenge = challengeRepository.findById(achievement.getChallengeId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

        return ChallengeProgressResponseDTO.from(challenge, achievement);
    }

    // 주어진 memberId에 대해 해당하는 챌린지 달성 정보를 생성하거나, 기존 정보를 반환
    public ChallengeAchievement createIfNotExists(Long memberId) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // 현재 연도와 월에 해당하는 챌린지 조회
        Challenge challenge = challengeRepository
                .findByYearAndMonth(year, month)
                .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

        // 해당 챌린지와 회원 ID에 대한 챌린지 달성 정보 조회
        return challengeAchievementRepository
                .findByChallengeIdAndMemberId(challenge.getId(), memberId)
                .orElseGet(() -> {
                    // 챌린지 달성 정보가 없으면 새로 생성
                    ChallengeAchievement achievement = new ChallengeAchievement();
                    achievement.setMemberId(memberId);
                    achievement.setChallengeId(challenge.getId());
                    achievement.setOpenedAt(LocalDateTime.now());

                    // 기본적으로 1단계 목표 활성화 설정 (다른 단계는 비활성화)
                    achievement.setStep1Goal1Active(true);
                    achievement.setStep1Goal2Active(true);

                    return challengeAchievementRepository.save(achievement);
                });
    }

    // 챌린지 최종 완료 여부 조회
    @Transactional
    public ChallengeCompletedResponseDTO getChallengeCompletion(Long memberId) {

        ChallengeAchievement achievement = createIfNotExists(memberId);

        Challenge challenge = challengeRepository.findById(achievement.getChallengeId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));


        // 세부 목표 8개 달성 여부
        boolean allAchieved = achievement.isAllGoalsAchieved();

        if (allAchieved && !achievement.isChallengeCompleted()) {
            achievement.setChallengeCompleted(true);
            achievement.setChallengeCompletedAt(LocalDateTime.now());
            challengeAchievementRepository.save(achievement);
        }

        if (achievement.isChallengeCompleted()) {
            return new ChallengeCompletedResponseDTO(
                    challenge.getAchievementName(),
                    "챌린지를 완료했습니다!",
                    challenge.getAchievementImageUrl(),
                    true
            );
        } else {
            return new ChallengeCompletedResponseDTO(
                    challenge.getAchievementName(),
                    "아직 챌린지를 달성하지 못했습니다.",
                    null,
                    false
            );
        }
    }

    // 로그인한 사용자의 이전 챌린지 진행 상황 조회
    public ChallengeProgressResponseDTO getBeforeProgress(Long memberId, Long challengeId) {

        Challenge challenge = challengeRepository
                .findById(challengeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

        Optional<ChallengeAchievement> optionalAchievement =
                challengeAchievementRepository.findByChallengeIdAndMemberId(challengeId, memberId);

        return optionalAchievement
                .map(achievement -> ChallengeProgressResponseDTO.from(challenge, achievement))
                .orElseGet(() -> ChallengeProgressResponseDTO.empty(challenge));
    }

    // 챌린지 자동 참여
    @Transactional
    public void challengeParticipation(String memberEmail) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // 회원 정보 조회 (email -> memberId)
        Long memberId = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND))
                .getId();

        Optional<Challenge> optionalChallenge = challengeRepository.findByYearAndMonth(year, month);

        if (optionalChallenge.isEmpty()) {
            log.info("이번 달 챌린지가 없어 자동 참여를 스킵합니다. memberEmail={}", memberEmail);
            return;
        }

        Challenge challenge = optionalChallenge.get();
        ChallengeAchievementId id = new ChallengeAchievementId(memberId, challenge.getId());

        boolean exists = challengeAchievementRepository.existsById(id);

        if (!exists) {
            ChallengeAchievement achievement = new ChallengeAchievement();
            achievement.setMemberId(memberId);
            achievement.setChallengeId(challenge.getId());
            achievement.setStep1Goal1Active(true);
            achievement.setStep1Goal2Active(true);
            achievement.setStep2Goal1Active(false);
            achievement.setStep2Goal2Active(false);
            achievement.setStep3Goal1Active(false);
            achievement.setStep3Goal2Active(false);
            achievement.setStep4Goal1Active(false);
            achievement.setStep4Goal2Active(false);
            achievement.setOpenedAt(LocalDateTime.now());

            challengeAchievementRepository.save(achievement);
        }
    }
}
