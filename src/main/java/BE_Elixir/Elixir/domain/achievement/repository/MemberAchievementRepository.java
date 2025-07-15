package BE_Elixir.Elixir.domain.achievement.repository;

import BE_Elixir.Elixir.domain.achievement.entity.MemberAchievement;
import BE_Elixir.Elixir.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberAchievementRepository extends JpaRepository<MemberAchievement, Long> {
    List<MemberAchievement> findAllByMember(Member member);

    // id 기반 완료된 업적 조회
    List<MemberAchievement> findByMemberIdAndCompleted(Long memberId, Boolean completed);

    List<MemberAchievement> findTop3ByMemberAndCompletedTrueOrderByCompletedAtDescUpdatedAtDesc(Member member);
}