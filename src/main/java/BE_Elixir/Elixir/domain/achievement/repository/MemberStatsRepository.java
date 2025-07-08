package BE_Elixir.Elixir.domain.achievement.repository;

import BE_Elixir.Elixir.domain.achievement.entity.MemberStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberStatsRepository extends JpaRepository<MemberStats, Long> {
}
