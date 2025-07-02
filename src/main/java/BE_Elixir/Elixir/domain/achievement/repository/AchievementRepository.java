package BE_Elixir.Elixir.domain.achievement.repository;

import BE_Elixir.Elixir.domain.achievement.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
}
