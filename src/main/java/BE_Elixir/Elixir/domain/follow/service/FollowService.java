package BE_Elixir.Elixir.domain.follow.service;

import BE_Elixir.Elixir.domain.achievement.service.MemberStatsService;
import BE_Elixir.Elixir.domain.follow.entity.Follow;
import BE_Elixir.Elixir.domain.follow.repository.FollowRepository;
import BE_Elixir.Elixir.domain.member.dto.response.MemberSummaryDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
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
    private final MemberStatsService memberStatsService;

    // 팔로우하기
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new CustomException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }

        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);
        if (exists) {
            throw new CustomException(ErrorCode.ALREADY_FOLLOWING);
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
        // 팔로워 수 증가 (팔로우 당하는 사람 기준)
        memberStatsService.increaseStat(followingId, AchievementType.TOTAL_FOLLOWERS, 1);
    }

    // 팔로우 취소하기
    public void unfollow(Long followerId, Long followingId) {
        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 팔로우 관계가 없는 경우 예외처리
        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);
        if (!exists) {
            throw new CustomException(ErrorCode.FOLLOW_RELATION_NOT_FOUND);
        }

        followRepository.deleteByFollowerAndFollowing(follower, following);
        // 팔로워 수 감소
        memberStatsService.increaseStat(followingId, AchievementType.TOTAL_FOLLOWERS, -1);
    }

    // 팔로워 목록 조회하기 (그 회원을 팔로우하는 목록)
    public List<MemberSummaryDTO> getFollowers(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return followRepository.findByFollowing(member).stream()
                .map(Follow::getFollower)
                .map(m -> MemberSummaryDTO.builder()
                        .id(m.getId())
                        .nickname(m.getNickname())
                        .profileUrl(m.getProfileUrl())
                        .title(m.getTitle())
                        .build()
                )
                .toList();
    }

    // 팔로잉 목록 조회하기 (그 회원이 팔로우하는 목록)
    public List<MemberSummaryDTO> getFollowings(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return followRepository.findByFollower(member).stream()
                .map(Follow::getFollowing)
                .map(m -> MemberSummaryDTO.builder()
                        .id(m.getId())
                        .nickname(m.getNickname())
                        .profileUrl(m.getProfileUrl())
                        .title(m.getTitle())
                        .build()
                )
                .toList();
    }
}