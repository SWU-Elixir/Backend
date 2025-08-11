package BE_Elixir.Elixir.domain.member.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import BE_Elixir.Elixir.domain.member.dto.request.MemberProfileRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SurveyRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.response.MemberResponseDTO;
import BE_Elixir.Elixir.domain.member.dto.response.SurveyResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
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

import java.io.IOException;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @InjectMocks
    private MyPageService myPageService;

    @Mock private MemberRepository memberRepository;
    @Mock private S3Service s3Service;
    @Mock private MultipartFile multipartFile;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
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
        @DisplayName("실패: 회원이 존재하지 않음")
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
}