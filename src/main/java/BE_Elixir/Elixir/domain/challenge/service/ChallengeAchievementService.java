package BE_Elixir.Elixir.domain.challenge.service;

import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeProgressResponseDTO;
import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChallengeAchievementService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeAchievementRepository challengeAchievementRepository;

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
                .orElseThrow(() -> new IllegalStateException("챌린지 ID로 챌린지를 찾을 수 없습니다."));

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
                .orElseThrow(() -> new IllegalArgumentException("이번 달의 챌린지를 찾을 수 없습니다."));

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

}
