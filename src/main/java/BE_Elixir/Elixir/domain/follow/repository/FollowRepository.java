package BE_Elixir.Elixir.domain.follow.repository;

import BE_Elixir.Elixir.domain.follow.entity.Follow;
import BE_Elixir.Elixir.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 팔로워, 팔로잉 여부
    boolean existsByFollowerAndFollowing(Member follower, Member following);

    // 팔로워, 팔로잉 정보로 제거
    void deleteByFollowerAndFollowing(Member follower, Member following);

    // 회원을 팔로우하는 목록, 즉 팔로워 목록 찾기
    List<Follow> findByFollower(Member follower);

    // 회원 팔로잉 목록 찾기
    List<Follow> findByFollowing(Member following);

}
