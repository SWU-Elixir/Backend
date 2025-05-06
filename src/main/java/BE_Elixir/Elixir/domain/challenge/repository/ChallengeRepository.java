package BE_Elixir.Elixir.domain.challenge.repository;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {


}
