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

    public ChallengeAchievement createIfNotExists(Long memberId) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        Challenge challenge = challengeRepository
                .findByYearAndMonth(year, month)
                .orElseThrow(() -> new IllegalArgumentException("이번 달의 챌린지를 찾을 수 없습니다."));

        return challengeAchievementRepository
                .findByChallengeIdAndMemberId(challenge.getId(), memberId)
                .orElseGet(() -> {
                    ChallengeAchievement achievement = new ChallengeAchievement();
                    achievement.setMemberId(memberId);
                    achievement.setChallengeId(challenge.getId());
                    achievement.setOpenedAt(LocalDateTime.now());

                    // step1 활성화만 true, 나머지는 false (시작 시 기본값)
                    achievement.setStep1Goal1Active(true);
                    achievement.setStep1Goal2Active(true);

                    return challengeAchievementRepository.save(achievement);
                });
    }

}
