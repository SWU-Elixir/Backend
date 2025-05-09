package BE_Elixir.Elixir.domain.challenge.service;

import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeProgressResponseDTO;
import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        // 현재 진행 중인 챌린지 조회
        Challenge challenge = challengeRepository.findCurrentByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 챌린지가 없습니다."));

        // 해당 챌린지의 진행 상황 조회
        ChallengeAchievement achievement = challengeAchievementRepository.findByChallengeIdAndMemberId(challenge.getId(), memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 챌린지 진행 정보가 없습니다."));

        return ChallengeProgressResponseDTO.from(challenge, achievement);
    }
}
