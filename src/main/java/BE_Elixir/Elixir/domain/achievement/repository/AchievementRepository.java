package BE_Elixir.Elixir.domain.achievement.repository;

import BE_Elixir.Elixir.domain.achievement.entity.Achievement;
import BE_Elixir.Elixir.global.enums.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    // 업적 코드로 업적을 조회
    Optional<Achievement> findByCode(String code);
}
