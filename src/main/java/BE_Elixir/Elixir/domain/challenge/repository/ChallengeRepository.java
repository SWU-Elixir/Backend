package BE_Elixir.Elixir.domain.challenge.repository;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // 연도 별 챌린지 조회
    List<Challenge> findByYear(int year);

}
