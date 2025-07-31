package BE_Elixir.Elixir.domain.member.service;

import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.member.dto.SocialSignUpDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SocialSignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.LoginType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.email.EmailService;
import BE_Elixir.Elixir.global.redis.RedisEmailVerificationService;
import BE_Elixir.Elixir.global.redis.RedisAuthService;
import BE_Elixir.Elixir.global.redis.dto.EmailVerificationDTO;
import BE_Elixir.Elixir.global.s3.S3Service;
import BE_Elixir.Elixir.global.security.JwtProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 테스트")
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock private MemberRepository memberRepository;
    @Mock private MemberDetailsService memberDetailsService;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private RedisAuthService redisAuthService;
    @Mock private RedisEmailVerificationService redisMailVerificationService;
    @Mock private S3Service s3Service;
    @Mock private EmailService mailService;
    @Mock private MultipartFile profileImage;

    private Member member;
    private SignUpRequestDTO signUpRequestDTO;
    private SocialSignUpRequestDTO socialSignUpRequestDTO;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("test@test.com")
                .password("encoded-password")
                .nickname("test")
                .profileUrl("http://example.com/profile.jpg")
                .loginType(LoginType.LOCAL)
                .roles(Collections.singletonList("USER"))
                .build();

        signUpRequestDTO = SignUpRequestDTO.builder()
                .email("test@test.com")
                .password("password123")
                .nickname("test")
                .allergies(List.of("호두", "알류"))
                .mealStyles(List.of("채소위주", "혼합식"))
                .recipeStyles(List.of("한식", "양식"))
                .reasons(List.of("항산화강화", "혈당조절"))
                .build();

        socialSignUpRequestDTO = SocialSignUpRequestDTO.builder()
                .email("social@test.com")
                .nickname("socialUser")
                .profileImageUrl("http://social.com/profile.jpg")
                .allergies(List.of("알류"))
                .mealStyles(List.of("채소위주"))
                .recipeStyles(List.of("한식"))
                .reasons(List.of("항산화강화"))
                .build();
    }

    @Nested
    @DisplayName("일반 회원 회원가입")
    class LocalSignUp {

        @Test
        @DisplayName("성공: 프로필 이미지 있음")
        void should_SignUpSuccessfully_When_ValidRequestWithImage() throws IOException {
            // given
            given(passwordEncoder.encode(anyString())).willReturn("encoded-password");
            given(profileImage.isEmpty()).willReturn(false);
            given(s3Service.upload(profileImage, "member")).willReturn("http://s3.com/image.jpg");
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            Member result = memberService.localSignUp(signUpRequestDTO, profileImage);

            // then
            assertThat(result).isNotNull();
            verify(passwordEncoder).encode(signUpRequestDTO.getPassword());
            verify(s3Service).upload(profileImage, "member");
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("성공: 프로필 이미지 없음")
        void should_SignUpSuccessfully_When_ValidRequestWithoutImage() throws IOException {
            // given
            given(passwordEncoder.encode(anyString())).willReturn("encoded-password");
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            Member result = memberService.localSignUp(signUpRequestDTO, null);

            // then
            assertThat(result).isNotNull();
            verify(passwordEncoder).encode(signUpRequestDTO.getPassword());
            verify(s3Service, never()).upload(any(), anyString());
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("실패: 이메일 중복")
        void should_ThrowException_When_EmailDuplicated() {
            // given
            given(passwordEncoder.encode(anyString())).willReturn("encoded-password");
            given(memberRepository.save(any(Member.class)))
                    .willThrow(new DataIntegrityViolationException("EMAIL_UNIQUE constraint violation"));

            // when & then
            assertThatThrownBy(() -> memberService.localSignUp(signUpRequestDTO, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXISTS_MEMBER);
        }

        @Test
        @DisplayName("실패: S3 업로드 실패")
        void should_ThrowException_When_S3UploadFails() throws IOException {
            // given
            given(passwordEncoder.encode(anyString())).willReturn("encoded-password");
            given(profileImage.isEmpty()).willReturn(false);
            given(s3Service.upload(profileImage, "member")).willThrow(new IOException());

            // when & then
            assertThatThrownBy(() -> memberService.localSignUp(signUpRequestDTO, profileImage))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.S3_UPLOAD_ERROR);
        }
    }

    @Nested
    @DisplayName("소셜 회원 회원가입 테스트")
    class SocialSignUp {

        @Test
        @DisplayName("성공: 업로드 이미지 있음")
        void should_SocialSignUpSuccessfully_When_ValidRequestWithUploadImage() throws IOException {
            // given
            LoginType loginType = LoginType.GOOGLE;
            given(profileImage.isEmpty()).willReturn(false);
            given(s3Service.upload(profileImage, "member")).willReturn("http://s3.com/image.jpg");
            given(memberRepository.save(any(Member.class))).willReturn(member);

            MemberDetails memberDetails = mock(MemberDetails.class);
            given(memberDetailsService.loadUserByUsername(anyString())).willReturn(memberDetails);

            TokenResponseDTO tokenResponse = TokenResponseDTO.builder()
                    .accessToken("accessToken")
                    .refreshToken("refreshToken")
                    .build();
            given(jwtProvider.generateToken(any(Authentication.class))).willReturn(tokenResponse);

            // when
            SocialSignUpDTO result = memberService.socialSignUp(loginType, socialSignUpRequestDTO, profileImage);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMember()).isNotNull();
            assertThat(result.getTokenResponseDTO()).isNotNull();
            verify(s3Service).upload(profileImage, "member");
            verify(redisAuthService).saveRefreshToken(anyString(), anyString());
        }

        @Test
        @DisplayName("성공: 소셜 프로필 이미지 URL 사용")
        void should_SocialSignUpSuccessfully_When_ValidRequestWithSocialImageUrl() throws IOException {
            // given
            LoginType loginType = LoginType.GOOGLE;
            given(memberRepository.save(any(Member.class))).willReturn(member);

            MemberDetails memberDetails = mock(MemberDetails.class);
            given(memberDetailsService.loadUserByUsername(anyString())).willReturn(memberDetails);

            TokenResponseDTO tokenResponse = TokenResponseDTO.builder()
                    .accessToken("accessToken")
                    .refreshToken("refreshToken")
                    .build();
            given(jwtProvider.generateToken(any(Authentication.class))).willReturn(tokenResponse);

            // when
            SocialSignUpDTO result = memberService.socialSignUp(loginType, socialSignUpRequestDTO, null);

            // then
            assertThat(result).isNotNull();
            verify(s3Service, never()).upload(any(), anyString());
            verify(redisAuthService).saveRefreshToken(anyString(), anyString());
        }

        @Test
        @DisplayName("실패: 로그인 타입이 LOCAL")
        void should_ThrowException_When_LoginTypeIsLocal() {
            // given
            LoginType loginType = LoginType.LOCAL;

            // when & then
            assertThatThrownBy(() -> memberService.socialSignUp(loginType, socialSignUpRequestDTO, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_TYPE_MISMATCH);
        }

        @Test
        @DisplayName("실패: 이메일 중복")
        void should_ThrowException_When_SocialEmailDuplicated() {
            // given
            LoginType loginType = LoginType.GOOGLE;
            given(memberRepository.save(any(Member.class)))
                    .willThrow(new DataIntegrityViolationException("EMAIL_UNIQUE constraint violation"));

            // when & then
            assertThatThrownBy(() -> memberService.socialSignUp(loginType, socialSignUpRequestDTO, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXISTS_MEMBER);
        }

        @Test
        @DisplayName("실패: S3 업로드 실패")
        void should_ThrowException_When_SocialS3UploadFails() throws IOException {
            // given
            LoginType loginType = LoginType.GOOGLE;
            given(profileImage.isEmpty()).willReturn(false);
            given(s3Service.upload(profileImage, "member")).willThrow(new IOException());

            // when & then
            assertThatThrownBy(() -> memberService.socialSignUp(loginType, socialSignUpRequestDTO, profileImage))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.S3_UPLOAD_ERROR);
        }
    }

    @Nested
    @DisplayName("이메일 인증번호 요청 테스트")
    class SendVerificationCode {

        @Test
        @DisplayName("성공: 인증 코드 전송 및 redis 저장")
        void should_SendVerificationCodeSuccessfully_When_ValidLocalMember() {
            // given
            String email = "test@test.com";
            String verificationCode = "123456";
            Instant sendTime = Instant.now();

            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
            given(mailService.sendMail(email)).willReturn(verificationCode);
            given(mailService.getMailSendTime()).willReturn(sendTime);

            // when
            memberService.sendVerificationCode(email);

            // then
            verify(memberRepository).findByEmail(email);
            verify(mailService).sendMail(email);
            verify(mailService).setMailSendTime(any(Instant.class));
            verify(redisMailVerificationService).saveVerificationCode(email, verificationCode, sendTime);
        }

        @Test
        @DisplayName("실패: 회원 존재하지 않음")
        void should_ThrowException_When_MemberNotFound() {
            // given
            String email = "nonexistent@test.com";
            given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.sendVerificationCode(email))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 소셜 계정")
        void should_ThrowException_When_SocialAccount() {
            // given
            String email = "social@test.com";
            Member socialMember = Member.builder()
                    .email(email)
                    .loginType(LoginType.GOOGLE)
                    .build();
            given(memberRepository.findByEmail(email)).willReturn(Optional.of(socialMember));

            // when & then
            assertThatThrownBy(() -> memberService.sendVerificationCode(email))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_REGISTERED_WITH_SOCIAL);
        }
    }

    @Nested
    @DisplayName("이메일 인증번호 검증 테스트")
    class VerifyCode {

        @Test
        @DisplayName("성공")
        void should_VerifyCodeSuccessfully_When_ValidCode() {
            // given
            String email = "test@test.com";
            String code = "123456";
            Instant sendTime = Instant.now().minus(Duration.ofMinutes(1));

            EmailVerificationDTO dto = EmailVerificationDTO.builder()
                    .code(code)
                    .emailSendTime(sendTime)
                    .build();

            given(redisMailVerificationService.getVerification(email)).willReturn(dto);
            given(mailService.getValidityDuration()).willReturn(Duration.ofMinutes(5));

            // when
            boolean result = memberService.verifyCode(email, code);

            // then
            assertThat(result).isTrue();
            verify(redisMailVerificationService).deleteVerification(email);
        }

        @Test
        @DisplayName("실패: 코드 불일치")
        void should_ThrowException_When_CodeMismatch() {
            // given
            String email = "test@test.com";
            String inputCode = "123456";
            String actualCode = "654321";
            Instant sendTime = Instant.now().minus(Duration.ofMinutes(1));

            EmailVerificationDTO dto = EmailVerificationDTO.builder()
                    .code(actualCode)
                    .emailSendTime(sendTime)
                    .build();

            given(redisMailVerificationService.getVerification(email)).willReturn(dto);
            given(mailService.getValidityDuration()).willReturn(Duration.ofMinutes(5));

            // when & then
            assertThatThrownBy(() -> memberService.verifyCode(email, inputCode))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        @Test
        @DisplayName("실패: 유효기간 초과")
        void should_ThrowException_When_CodeExpired() {
            // given
            String email = "test@test.com";
            String code = "123456";
            Instant sendTime = Instant.now().minus(Duration.ofMinutes(10)); // 10분 전

            EmailVerificationDTO dto = EmailVerificationDTO.builder()
                    .code(code)
                    .emailSendTime(sendTime)
                    .build();

            given(redisMailVerificationService.getVerification(email)).willReturn(dto);
            given(mailService.getValidityDuration()).willReturn(Duration.ofMinutes(5)); // 5분 유효

            // when & then
            assertThatThrownBy(() -> memberService.verifyCode(email, code))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }
    }

    @Nested
    @DisplayName("비밀번호 수정 테스트")
    class UpdatePassword {

        @Test
        @DisplayName("성공")
        void should_UpdatePasswordSuccessfully_When_ValidMember() {
            // given
            String email = "test@test.com";
            String newPassword = "newPassword123";
            String encodedPassword = "encodedNewPassword";

            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
            given(passwordEncoder.encode(newPassword)).willReturn(encodedPassword);
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            memberService.updatePassword(email, newPassword);

            // then
            verify(memberRepository).findByEmail(email);
            verify(passwordEncoder).encode(newPassword);
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("실패: 회원 존재하지 않음")
        void should_ThrowException_When_MemberNotFoundForPasswordUpdate() {
            // given
            String email = "nonexistent@test.com";
            String newPassword = "newPassword123";

            given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.updatePassword(email, newPassword))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("회원탈퇴 테스트")
    class Withdraw {

        @Test
        @DisplayName("성공")
        void should_WithdrawSuccessfully_When_ValidTokens() {
            // given
            String email = "test@test.com";
            String accessToken = "validAccessToken";
            String refreshToken = "validRefreshToken";

            given(jwtProvider.validateToken(accessToken)).willReturn(true);
            given(redisAuthService.isRefreshTokenValid(email, refreshToken)).willReturn(true);
            given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

            // when
            memberService.withdraw(email, accessToken, refreshToken);

            // then
            verify(redisAuthService).addAccessTokenToBlacklist(accessToken);
            verify(redisAuthService).removeRefreshToken(email);
            verify(s3Service).deleteS3(member.getProfileUrl(), "member");
            verify(memberRepository).delete(member);
        }

        @Test
        @DisplayName("실패: Access Token 검증 실패")
        void should_ThrowException_When_InvalidAccessToken() {
            // given
            String email = "test@test.com";
            String accessToken = "invalidAccessToken";
            String refreshToken = "validRefreshToken";

            given(jwtProvider.validateToken(accessToken)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> memberService.withdraw(email, accessToken, refreshToken))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACCESS_TOKEN);
        }

        @Test
        @DisplayName("실패: Refresh Token 유효하지 않음")
        void should_ThrowException_When_InvalidRefreshToken() {
            // given
            String email = "test@test.com";
            String accessToken = "validAccessToken";
            String refreshToken = "invalidRefreshToken";

            given(jwtProvider.validateToken(accessToken)).willReturn(true);
            given(redisAuthService.isRefreshTokenValid(email, refreshToken)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> memberService.withdraw(email, accessToken, refreshToken))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);

            verify(redisAuthService).addAccessTokenToBlacklist(accessToken);
        }
    }
}