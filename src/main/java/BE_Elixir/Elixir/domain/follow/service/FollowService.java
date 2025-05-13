package BE_Elixir.Elixir.domain.follow.service;

import BE_Elixir.Elixir.domain.follow.entity.Follow;
import BE_Elixir.Elixir.domain.follow.repository.FollowRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    // 팔로우하기
    private void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }

        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new NoSuchElementException("팔로워 회원이 존재하지 않습니다."));
        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new NoSuchElementException("팔로우 대상 회원이 존재하지 않습니다."));

        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);
        if (exists) {
            throw new IllegalArgumentException("이미 팔로우한 회원입니다.");
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
    }

    // 팔로우 취소하기
    private void unfollow(Long followerId, Long followingId) {
        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new NoSuchElementException("팔로워 회원이 존재하지 않습니다."));
        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new NoSuchElementException("팔로우 대상 회원이 존재하지 않습니다."));

        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    // 팔로워 목록 조회하기 (그 회원을 팔로우하는 목록)
    private List<Member> getFollowers(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(("회원이 존재하지 않습니다.")));

        return followRepository.findByFollower(member).stream()
                .map(Follow::getFollowing)
                .toList();
    }

    // 팔로잉 목록 조회하기 (그 회원이 팔로우하는 목록)
    private List<Member> getFollowings(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(("회원이 존재하지 않습니다.")));

        return followRepository.findByFollowing(member).stream()
                .map(Follow::getFollowing)
                .toList();

    }

}