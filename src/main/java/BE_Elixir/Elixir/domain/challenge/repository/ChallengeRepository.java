package BE_Elixir.Elixir.domain.challenge.repository;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // 연도 별 챌린지 조회
    List<Challenge> findByYear(int year);

    // 연도&달로 현재 챌린지 조회
    Optional<Challenge> findByYearAndMonth(int year, int month);

    // 현재 진행 중인 챌린지를 찾는 메서드
    @Query("SELECT c FROM Challenge c " +
            "JOIN ChallengeAchievement ca ON ca.challengeId = c.id " +
            "WHERE ca.memberId = :memberId " +
            "AND c.startDate <= CURRENT_TIMESTAMP " +
            "AND c.endDate >= CURRENT_TIMESTAMP")
    Optional<Challenge> findCurrentByMemberId(@Param("memberId") Long memberId);
}
