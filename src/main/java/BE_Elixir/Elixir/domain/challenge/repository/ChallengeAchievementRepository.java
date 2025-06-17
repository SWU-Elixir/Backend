package BE_Elixir.Elixir.domain.challenge.repository;

import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievementId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeAchievementRepository extends JpaRepository<ChallengeAchievement, ChallengeAchievementId> {
    // 챌린지에 대한 사용자의 달성 상태 정보 조회
    Optional<ChallengeAchievement> findByChallengeIdAndMemberId(Long challengeId, Long memberId);

    // 사용자의 챌린지 달성 정보 조회
    List<ChallengeAchievement> findByMemberId(Long memberId);

}
