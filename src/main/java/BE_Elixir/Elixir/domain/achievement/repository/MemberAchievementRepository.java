package BE_Elixir.Elixir.domain.achievement.repository;

import BE_Elixir.Elixir.domain.achievement.entity.MemberAchievement;
import BE_Elixir.Elixir.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberAchievementRepository extends JpaRepository<MemberAchievement, Long> {
    List<MemberAchievement> findAllByMember(Member member);
}