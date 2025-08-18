package BE_Elixir.Elixir.domain.member.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import BE_Elixir.Elixir.domain.achievement.entity.Achievement;
import BE_Elixir.Elixir.domain.achievement.entity.MemberAchievement;
import BE_Elixir.Elixir.domain.achievement.repository.AchievementRepository;
import BE_Elixir.Elixir.domain.achievement.repository.MemberAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import BE_Elixir.Elixir.domain.member.dto.request.MemberProfileRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SurveyRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.response.MemberResponseDTO;
import BE_Elixir.Elixir.domain.member.dto.response.SurveyResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.enums.LoginType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @InjectMocks
    private MyPageService myPageService;

    @Mock private MemberRepository memberRepository;
    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeEventRepository recipeEventRepository;
    @Mock private ChallengeRepository challengeRepository;
    @Mock private ChallengeAchievementRepository challengeAchievementRepository;
    @Mock private AchievementRepository achievementRepository;
    @Mock private MemberAchievementRepository memberAchievementRepository;
    @Mock private S3Service s3Service;
    @Mock private MultipartFile multipartFile;

    private Member member;
    private Challenge challenge;
    private ChallengeAchievement challengeAchievement;
    private Achievement achievement;
    private MemberAchievement memberAchievement;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded-password")
                .nickname("test")
                .title("초보자")
                .gender("FEMALE")
                .birthYear(2000)
                .profileUrl("http://example.com/profile.jpg")
                .loginType(LoginType.LOCAL)
                .roles(Collections.singletonList("USER"))
                .allergy_알류(true)
                .allergy_굴(true)
                .mealStyle_채소위주(true)
                .recipeStyle_한식(true)
                .recipeStyle_중식(true)
                .reason_항산화강화(true)
                .reason_혈당조절(true)
                .build();

        // 기본 Challenge 데이터
        challenge = new Challenge();
        challenge.setId(100L);
        challenge.setYear(2025);
        challenge.setMonth(8);
        challenge.setAchievementName("8월 챌린지");
        challenge.setAchievementImageUrl("color.jpg");
        challenge.setGrayAchievementImageUrl("gray.jpg");

        // 기본 ChallengeAchievement (미달성)
        challengeAchievement = new ChallengeAchievement();
        challengeAchievement.setChallengeId(challenge.getId());
        challengeAchievement.setMemberId(member.getId());
        challengeAchievement.setStep4Goal1Achieved(false);
        challengeAchievement.setStep4Goal2Achieved(false);

        // 일반 업적
        achievement = new Achievement();
        achievement.setId(200L);
        achievement.setAchievementName("첫 업적");
        achievement.setAchievementImageUrl("achieve.jpg");

        // 일반 업적 달성 정보 (기본: 미달성 상태)
        memberAchievement = new MemberAchievement();
        memberAchievement.setAchievement(achievement);
        memberAchievement.setMember(member);
        memberAchievement.setCurrentProgress(0);
        memberAchievement.setCompleted(false);
    }

    @Nested
    @DisplayName("프로필 수정 테스트")
    class updateProfile {
        @Test
        @DisplayName("성공: 모든 필드 수정")
        void updateMemberProfile_Success_AllFields() throws IOException {
            // given
            Long memberId = 1L;
            String newImageUrl = "http://s3.com/new_image.jpg";

            MemberProfileRequestDTO dto = MemberProfileRequestDTO.builder()
                    .nickname("newNickname")
                    .title("전문가")
                    .gender("MALE")
                    .birthYear(1995)
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(multipartFile.isEmpty()).willReturn(false);
            given(s3Service.upload(multipartFile, "member")).willReturn(newImageUrl);
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            MemberResponseDTO result = myPageService.updateMemberProfile(memberId, dto, multipartFile);

            // then
            assertNotNull(result);
            assertEquals(memberId, result.getId());
            assertEquals("newNickname", result.getNickname());
            assertEquals("전문가", result.getTitle());
            assertEquals("MALE", result.getGender());
            assertEquals(1995, result.getBirthYear());
            assertEquals(newImageUrl, result.getProfileUrl());

            verify(s3Service).deleteS3("http://example.com/profile.jpg", "member");
            verify(s3Service).upload(multipartFile, "member");
            verify(memberRepository).save(member);
        }

        @Test
        @DisplayName("성공: 닉네임만 수정")
        void updateMemberProfile_Success_OnlyNickname() {
            // given
            Long memberId = 1L;

            MemberProfileRequestDTO dto = MemberProfileRequestDTO.builder()
                    .nickname("onlyNickname")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            MemberResponseDTO result = myPageService.updateMemberProfile(memberId, dto, null);

            // then
            assertNotNull(result);
            assertEquals("onlyNickname", result.getNickname());
            assertEquals("초보자", result.getTitle()); // 기존 값 유지
            assertEquals("http://example.com/profile.jpg", result.getProfileUrl()); // 기존 값 유지

            verify(memberRepository).save(member);
            verifyNoInteractions(s3Service);
        }

        @Test
        @DisplayName("성공: 이미지만 수정")
        void updateMemberProfile_Success_OnlyImage() throws IOException {
            // given
            Long memberId = 1L;
            String newImageUrl = "http://s3.com/new_image.jpg";

            MemberProfileRequestDTO dto = MemberProfileRequestDTO.builder().build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(multipartFile.isEmpty()).willReturn(false);
            given(s3Service.upload(multipartFile, "member")).willReturn(newImageUrl);
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            MemberResponseDTO result = myPageService.updateMemberProfile(memberId, dto, multipartFile);

            // then
            assertNotNull(result);
            assertEquals(newImageUrl, result.getProfileUrl());
            assertEquals("test", result.getNickname()); // 기존 값 유지

            verify(s3Service).deleteS3("http://example.com/profile.jpg", "member");
            verify(s3Service).upload(multipartFile, "member");
            verify(memberRepository).save(member);
        }

        @Test
        @DisplayName("성공: 기존 프로필 이미지가 없는 경우")
        void updateMemberProfile_Success_NoExistingProfileImage() throws IOException {
            // given
            Long memberId = 1L;
            member.setProfileUrl(null); // 기존 프로필 이미지 없음
            String newImageUrl = "http://s3.com/new_image.jpg";

            MemberProfileRequestDTO dto = MemberProfileRequestDTO.builder()
                    .nickname("newNickname")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(multipartFile.isEmpty()).willReturn(false);
            given(s3Service.upload(multipartFile, "member")).willReturn(newImageUrl);
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            MemberResponseDTO result = myPageService.updateMemberProfile(memberId, dto, multipartFile);

            // then
            assertNotNull(result);
            assertEquals(newImageUrl, result.getProfileUrl());

            verify(s3Service, never()).deleteS3(anyString(), anyString()); // 삭제 호출 안됨
            verify(s3Service).upload(multipartFile, "member");
            verify(memberRepository).save(member);
        }

        @Test
        @DisplayName("실패: 회원이 존재하지 않음")
        void updateMemberProfile_Fail_MemberNotFound() {
            // given
            Long memberId = 999L;
            MemberProfileRequestDTO dto = MemberProfileRequestDTO.builder()
                    .nickname("newNickname")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> myPageService.updateMemberProfile(memberId, dto, null));

            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
            verify(memberRepository, never()).save(any());
            verifyNoInteractions(s3Service);
        }

        @Test
        @DisplayName("실패: S3 업로드 실패")
        void updateMemberProfile_Fail_S3UploadError() throws IOException {
            // given
            Long memberId = 1L;

            MemberProfileRequestDTO dto = MemberProfileRequestDTO.builder()
                    .nickname("newNickname")
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(multipartFile.isEmpty()).willReturn(false);
            given(s3Service.upload(multipartFile, "member")).willThrow(new IOException("S3 업로드 실패"));

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> myPageService.updateMemberProfile(memberId, dto, multipartFile));

            assertEquals(ErrorCode.S3_UPLOAD_ERROR, exception.getErrorCode());
            verify(s3Service).deleteS3("http://example.com/profile.jpg", "member");
            verify(memberRepository, never()).save(any());
        }
    }


    @Nested
    @DisplayName("설문조사 수정 테스트")
    class LocalSignUp {

        @Test
        @DisplayName("성공: 모든 필드 수정")
        void updateSurvey_Success_AllFields() {
            // given
            Long memberId = 1L;

            SurveyRequestDTO dto = SurveyRequestDTO.builder()
                    .allergies(List.of("유제품", "돼지고기"))
                    .mealStyles(List.of("고기위주", "혼합식"))
                    .recipeStyles(List.of("디저트"))
                    .reasons(List.of("염증감소", "혈당조절"))
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            SurveyResponseDTO result = myPageService.updateSurvey(memberId, dto);

            // then
            assertNotNull(result);
            assertEquals(memberId, result.getMemberId());
            assertEquals(Set.of("유제품", "돼지고기"), new HashSet<>(result.getAllergies()));
            assertEquals(Set.of("고기위주", "혼합식"), new HashSet<>(result.getMealStyles()));
            assertEquals(Set.of("디저트"), new HashSet<>(result.getRecipeStyles()));
            assertEquals(Set.of("염증감소", "혈당조절"), new HashSet<>(result.getReasons()));

            verify(memberRepository).save(member);
        }

        @Test
        @DisplayName("성공: 알레르기만 수정")
        void updateSurvey_Success_OnlyAllergies() {
            // given
            Long memberId = 1L;

            SurveyRequestDTO dto = SurveyRequestDTO.builder()
                    .allergies(List.of("유제품"))
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            SurveyResponseDTO result = myPageService.updateSurvey(memberId, dto);

            // then
            assertNotNull(result);
            assertEquals(Set.of("유제품"), new HashSet<>(result.getAllergies()));

            verify(memberRepository).save(member);
        }

        @Test
        @DisplayName("성공: 식사 스타일만 수정")
        void updateSurvey_Success_IndividualFields() {
            // given
            Long memberId = 1L;

            // 식사 스타일만 수정
            SurveyRequestDTO mealStyleDto = SurveyRequestDTO.builder()
                    .mealStyles(Arrays.asList("고기위주", "혼합식"))
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            SurveyResponseDTO result = myPageService.updateSurvey(memberId, mealStyleDto);

            // then
            assertNotNull(result);
            assertEquals(Set.of("고기위주", "혼합식"), new HashSet<>(result.getMealStyles()));

            verify(memberRepository).save(member);
        }

        @Test
        @DisplayName("예외: 회원이 존재하지 않음")
        void updateSurvey_Fail_MemberNotFound() {
            // given
            Long memberId = 999L;
            SurveyRequestDTO dto = SurveyRequestDTO.builder()
                    .allergies(List.of("닭고기"))
                    .build();

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> myPageService.updateSurvey(memberId, dto));

            assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
            verify(memberRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("내가 작성한 레시피 조회 테스트")
    class GetMyRecipes {

        @Test
        @DisplayName("성공: 로그인한 사용자의 모든 레시피 조회")
        void success() {
            // given
            Recipe recipe = new Recipe();
            recipe.setId(1L);
            recipe.setImageUrl("image.jpg");

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(recipeRepository.findAllByMember(member)).willReturn(List.of(recipe));

            // when
            var result = myPageService.getMyRecipes(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getImageUrl()).isEqualTo("image.jpg");
        }

        @Test
        @DisplayName("예외: 회원을 찾을 수 없음")
        void fail_memberNotFound() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> myPageService.getMyRecipes(1L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("나의 레시피 스크랩 테스트")
    class GetMyScrapRecipes {

        @Test
        @DisplayName("성공: 스크랩한 레시피 목록 반환")
        void success() {
            // given
            Recipe recipe = new Recipe();
            recipe.setId(1L);
            recipe.setImageUrl("scrap.jpg");

            RecipeEvent event = new RecipeEvent();
            event.setRecipe(recipe);

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(recipeEventRepository.findByMemberAndScrapFlagTrue(member))
                    .willReturn(List.of(event));

            // when
            var result = myPageService.getMyScrapRecipes(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getImageUrl()).isEqualTo("scrap.jpg");
        }

        @Test
        @DisplayName("예외: 회원 없음")
        void fail_memberNotFound() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> myPageService.getMyScrapRecipes(1L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("다른 사용자가 작성한 레시피 조회 테스트")
    class GetUserRecipes {

        @Test
        @DisplayName("성공: 다른 사용자의 레시피 최대 9개 반환")
        void success() {
            // given
            List<Recipe> recipes = IntStream.range(0, 12)
                    .mapToObj(i -> {
                        Recipe r = new Recipe();
                        r.setId((long) i);
                        r.setImageUrl("img" + i);
                        return r;
                    })
                    .toList();

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(recipeRepository.findAllByMember(member)).willReturn(recipes);

            // when
            var result = myPageService.getUserRecipes(1L);

            // then
            assertThat(result).hasSize(9);
        }

        @Test
        @DisplayName("예외: 회원 없음")
        void fail_memberNotFound() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> myPageService.getUserRecipes(1L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("챌린지 업적 조회 테스트")
    class ChallengeAchievementTests {

        @Test
        @DisplayName("성공: 모든 챌린지 업적 조회")
        void getAllChallengeAchievements_success() {
            // given
            Long memberId = 1L;
            // 달성 상태로 변경
            challengeAchievement.setStep4Goal1Achieved(true);
            challengeAchievement.setStep4Goal2Achieved(true);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(challengeRepository.findAllOrderedByYearAndMonth()).willReturn(List.of(challenge));
            given(challengeAchievementRepository.findByMemberId(1L))
                    .willReturn(List.of(challengeAchievement));

            // when
            var result = myPageService.getAllChallengeAchievements(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isChallengeCompleted()).isTrue();
            assertThat(result.get(0).getAchievementImageUrl()).isEqualTo("color.jpg");
        }

        @Test
        @DisplayName("성공: 최신 3개 챌린지 업적 조회")
        void getTop3ChallengeAchievements_success() {
            // given
            Long memberId = 1L;
            challengeAchievement.setStep4Goal1Achieved(true);
            challengeAchievement.setStep4Goal2Achieved(true);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(challengeAchievementRepository.findByMemberId(1L))
                    .willReturn(List.of(challengeAchievement));
            given(challengeRepository.findAllById(anySet()))
                    .willReturn(List.of(challenge));

            // when
            var result = myPageService.getTop3ChallengeAchievements(1L);


            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAchievementName()).isEqualTo("8월 챌린지");
            assertThat(result.get(0).getAchievementImageUrl()).isEqualTo("color.jpg");
        }

        @Test
        @DisplayName("예외: 회원 없음")
        void fail_memberNotFound() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.empty());


            // when & then
            assertThatThrownBy(() -> myPageService.getAllChallengeAchievements(1L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("일반 업적 조회 테스트")
    class StatsAchievementTests {

        @Test
        @DisplayName("성공: 모든 일반 업적 조회")
        void getAllMyStatsAchievements_success() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(achievementRepository.findAll()).willReturn(List.of(achievement));
            given(memberAchievementRepository.findAllByMember(member)).willReturn(List.of());

            // when
            var result = myPageService.getAllMyStatsAchievements(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAchievementName()).isEqualTo("첫 업적");
        }

        @Test
        @DisplayName("성공: 최신 3개 일반 업적 조회")
        void getTop3StatsAchievements_success() {
            // given
            memberAchievement.setCompleted(true);
            memberAchievement.setCompletedAt(LocalDateTime.now());

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(memberAchievementRepository
                    .findTop3ByMemberAndCompletedTrueOrderByCompletedAtDescUpdatedAtDesc(member))
                    .willReturn(List.of(memberAchievement));

            // when
            var result = myPageService.getTop3StatsAchievements(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAchievementName()).isEqualTo("첫 업적");
        }
    }

    @Nested
    @DisplayName("통합 업적 조회 테스트")
    class AllAchievementsTests {

        @Test
        @DisplayName("성공: 최신 3개 통합 업적 조회")
        void getTop3AllAchievements_success() {
            // given
            // 일반 업적 달성
            memberAchievement.setCompleted(true);
            memberAchievement.setCompletedAt(LocalDateTime.now().minusDays(1));

            // 챌린지 업적 달성
            challengeAchievement.setStep4Goal1Achieved(true);
            challengeAchievement.setStep4Goal2Achieved(true);

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(memberAchievementRepository
                    .findTop3ByMemberAndCompletedTrueOrderByCompletedAtDescUpdatedAtDesc(member))
                    .willReturn(List.of(memberAchievement));
            given(challengeAchievementRepository.findByMemberId(1L))
                    .willReturn(List.of(challengeAchievement));
            given(challengeRepository.findAllById(any()))
                    .willReturn(List.of(challenge));

            // when
            var result = myPageService.getTop3AllAchievements(1L);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).isCompleted()).isTrue();
        }
    }
}