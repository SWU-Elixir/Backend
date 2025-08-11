package BE_Elixir.Elixir.domain.follow.service;

import BE_Elixir.Elixir.domain.achievement.service.MemberStatsService;
import BE_Elixir.Elixir.domain.follow.entity.Follow;
import BE_Elixir.Elixir.domain.follow.repository.FollowRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService 단위 테스트")
class FollowServiceTest {

    @InjectMocks
    private FollowService followService;

    @Mock private FollowRepository followRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private MemberStatsService memberStatsService;

    private Member follower;
    private Member following;

    @BeforeEach
    void setUp() {
        follower = Member.builder()
                .id(1L)
                .email("follower@example.com")
                .nickname("follower")
                .profileUrl("http://example.com/follower.jpg")
                .title("요리 초보")
                .build();

        following = Member.builder()
                .id(2L)
                .email("following@example.com")
                .nickname("following")
                .profileUrl("http://example.com/following.jpg")
                .title("요리 고수")
                .build();
    }

    @Nested
    @DisplayName("팔로우하기 테스트")
    class Follow {

        @Test
        @DisplayName("성공")
        void should_FollowSuccessfully_When_ValidRequest() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;

            given(memberRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId)).willReturn(Optional.of(following));
            given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(false);
            given(followRepository.save(any(BE_Elixir.Elixir.domain.follow.entity.Follow.class)))
                    .willReturn(BE_Elixir.Elixir.domain.follow.entity.Follow.builder()
                            .follower(follower)
                            .following(following)
                            .build());

            // when
            followService.follow(followerId, followingId);

            // then
            verify(memberRepository).findById(followerId);
            verify(memberRepository).findById(followingId);
            verify(followRepository).existsByFollowerAndFollowing(follower, following);
            verify(followRepository).save(any(BE_Elixir.Elixir.domain.follow.entity.Follow.class));
            verify(memberStatsService).increaseStat(followingId, AchievementType.TOTAL_FOLLOWERS, 1);
        }

        @Test
        @DisplayName("실패: 자기 자신을 팔로우하려는 경우")
        void should_ThrowException_When_SelfFollow() {
            // given
            Long memberId = 1L;

            // when & then
            assertThatThrownBy(() -> followService.follow(memberId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SELF_FOLLOW_NOT_ALLOWED);

            verify(memberRepository, never()).findById(any());
            verify(followRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 팔로워 회원 존재하지 않음")
        void should_ThrowException_When_FollowerNotFound() {
            // given
            Long followerId = 999L;
            Long followingId = 2L;

            given(memberRepository.findById(followerId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.follow(followerId, followingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            verify(memberRepository).findById(followerId);
            verify(memberRepository, never()).findById(followingId);
            verify(followRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 팔로잉 대상 회원 존재하지 않음")
        void should_ThrowException_When_FollowingNotFound() {
            // given
            Long followerId = 1L;
            Long followingId = 999L;

            given(memberRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.follow(followerId, followingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            verify(memberRepository).findById(followerId);
            verify(memberRepository).findById(followingId);
            verify(followRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 이미 팔로우 관계 존재")
        void should_ThrowException_When_AlreadyFollowing() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;

            given(memberRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId)).willReturn(Optional.of(following));
            given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> followService.follow(followerId, followingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_FOLLOWING);

            verify(memberRepository).findById(followerId);
            verify(memberRepository).findById(followingId);
            verify(followRepository).existsByFollowerAndFollowing(follower, following);
            verify(followRepository, never()).save(any());
            verify(memberStatsService, never()).increaseStat(any(), any(), anyInt());
        }
    }

    @Nested
    @DisplayName("팔로우 취소하기 테스트")
    class Unfollow {

        @Test
        @DisplayName("성공")
        void should_UnfollowSuccessfully_When_ValidRequest() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;

            given(memberRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId)).willReturn(Optional.of(following));
            given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(true);

            // when
            followService.unfollow(followerId, followingId);

            // then
            verify(memberRepository).findById(followerId);
            verify(memberRepository).findById(followingId);
            verify(followRepository).existsByFollowerAndFollowing(follower, following);
            verify(followRepository).deleteByFollowerAndFollowing(follower, following);
            verify(memberStatsService).increaseStat(followingId, AchievementType.TOTAL_FOLLOWERS, -1);
        }

        @Test
        @DisplayName("실패: 팔로워 회원 존재하지 않음")
        void should_ThrowException_When_FollowerNotFoundOnUnfollow() {
            // given
            Long followerId = 999L;
            Long followingId = 2L;

            given(memberRepository.findById(followerId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.unfollow(followerId, followingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            verify(memberRepository).findById(followerId);
            verify(memberRepository, never()).findById(followingId);
            verify(followRepository, never()).deleteByFollowerAndFollowing(any(), any());
        }

        @Test
        @DisplayName("실패: 팔로잉 대상 회원 존재하지 않음")
        void should_ThrowException_When_FollowingNotFoundOnUnfollow() {
            // given
            Long followerId = 1L;
            Long followingId = 999L;

            given(memberRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> followService.unfollow(followerId, followingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            verify(memberRepository).findById(followerId);
            verify(memberRepository).findById(followingId);
            verify(followRepository, never()).deleteByFollowerAndFollowing(any(), any());
        }

        @Test
        @DisplayName("실패: 팔로우 관계 존재하지 않음")
        void should_ThrowException_When_FollowRelationNotFound() {
            // given
            Long followerId = 1L;
            Long followingId = 2L;

            given(memberRepository.findById(followerId)).willReturn(Optional.of(follower));
            given(memberRepository.findById(followingId)).willReturn(Optional.of(following));
            given(followRepository.existsByFollowerAndFollowing(follower, following)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> followService.unfollow(followerId, followingId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FOLLOW_RELATION_NOT_FOUND);

            verify(memberRepository).findById(followerId);
            verify(memberRepository).findById(followingId);
            verify(followRepository).existsByFollowerAndFollowing(follower, following);
            verify(followRepository, never()).deleteByFollowerAndFollowing(any(), any());
            verify(memberStatsService, never()).increaseStat(any(), any(), anyInt());
        }
    }
}