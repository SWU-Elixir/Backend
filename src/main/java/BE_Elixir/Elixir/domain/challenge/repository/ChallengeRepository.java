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

}
