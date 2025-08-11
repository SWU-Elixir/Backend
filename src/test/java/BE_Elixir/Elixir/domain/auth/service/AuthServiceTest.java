package BE_Elixir.Elixir.domain.auth.service;

import BE_Elixir.Elixir.domain.auth.dto.AccessTokenDTO;
import BE_Elixir.Elixir.domain.auth.dto.social.SocialUserInfo;
import BE_Elixir.Elixir.domain.auth.dto.response.SocialLoginResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.request.LoginRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.domain.member.service.MemberDetailsService;
import BE_Elixir.Elixir.global.enums.LoginType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.redis.RedisAuthService;
import BE_Elixir.Elixir.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private AuthenticationManagerBuilder authenticationManagerBuilder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtProvider jwtProvider;
    @Mock private RedisAuthService redisAuthService;
    @Mock private MemberDetailsService memberDetailsService;
    @Mock private OauthClientFactory oauthClientFactory;
    @Mock private MemberRepository memberRepository;
    @Mock private OauthClient oauthClient;

    private LoginRequestDTO loginRequest;
    private TokenResponseDTO tokenResponse;
    private Authentication authentication;
    private MemberDetails memberDetails;
    private Member member;
    private SocialUserInfo socialUserInfo;

    @BeforeEach
    void setUp() {
        // 공통 테스트 데이터 초기화
        loginRequest = new LoginRequestDTO("test@test.com", "password");
        tokenResponse = TokenResponseDTO.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .password("encoded-password")
                .loginType(LoginType.LOCAL)
                .roles(Collections.singletonList("USER"))
                .build();

        memberDetails = MemberDetails.builder()
                .member(member)
                .build();

        authentication = new UsernamePasswordAuthenticationToken(
                "test@test.com", "password", memberDetails.getAuthorities());

        socialUserInfo = SocialUserInfo.builder()
                .email("social@test.com")
                .nickname("Social User")
                .build();
    }

    @Nested
    @DisplayName("일반 로그인 테스트")
    class SignInTest {

        @Test
        @DisplayName("성공: 토큰 발급")
        void signIn_Success() {
            // given
            when(memberRepository.findLoginTypeByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.of(LoginType.LOCAL));
            when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtProvider.generateToken(authentication)).thenReturn(tokenResponse);

            // when
            TokenResponseDTO result = authService.signIn(loginRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");

            verify(redisAuthService).saveRefreshToken("test@test.com", "refresh-token");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자")
        void signIn_Fail_MemberNotFound() {
            // given
            when(memberRepository.findLoginTypeByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.signIn(loginRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 비밀번호 틀린 경우")
        void signIn_Fail_InvalidCredentials() {
            // given
            when(memberRepository.findLoginTypeByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.of(LoginType.LOCAL));
            when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // when & then
            assertThatThrownBy(() -> authService.signIn(loginRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("실패: 소셜 계정으로 로그인 시도")
        void signIn_Fail_SocialAccount() {
            // given
            when(memberRepository.findLoginTypeByEmail(loginRequest.getEmail()))
                    .thenReturn(Optional.of(LoginType.GOOGLE));

            // when & then
            assertThatThrownBy(() -> authService.signIn(loginRequest))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_REGISTERED_WITH_SOCIAL);
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class LogoutTest {

        @Test
        @DisplayName("성공: JWT 블랙리스트 처리 및 삭제")
        void logout_Success() {
            // given
            String email = "test@test.com";
            String accessToken = "access-token";
            String refreshToken = "refresh-token";

            when(jwtProvider.validateToken(accessToken)).thenReturn(true);
            when(redisAuthService.isRefreshTokenValid(email, refreshToken)).thenReturn(true);

            // when
            authService.logout(email, accessToken, refreshToken);

            // then
            verify(redisAuthService).addAccessTokenToBlacklist(accessToken);
            verify(redisAuthService).removeRefreshToken(email);
        }

        @Test
        @DisplayName("실패: Access Token 검증 실패")
        void logout_Fail_InvalidAccessToken() {
            // given
            String email = "test@test.com";
            String accessToken = "invalid-access-token";
            String refreshToken = "refresh-token";

            when(jwtProvider.validateToken(accessToken)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.logout(email, accessToken, refreshToken))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ACCESS_TOKEN);
        }

        @Test
        @DisplayName("실패: Refresh Token 불일치")
        void logout_Fail_InvalidRefreshToken() {
            // given
            String email = "test@test.com";
            String accessToken = "access-token";
            String refreshToken = "invalid-refresh-token";

            when(jwtProvider.validateToken(accessToken)).thenReturn(true);
            when(redisAuthService.isRefreshTokenValid(email, refreshToken)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.logout(email, accessToken, refreshToken))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    @Nested
    @DisplayName("AccessToken 재발급 테스트")
    class RefreshAccessTokenTest {

        @Test
        @DisplayName("성공: Refresh Token 검증 후 새 AccessToken 발급")
        void refreshAccessToken_Success() {
            // given
            String email = "test@test.com";
            String refreshToken = "refresh-token";
            AccessTokenDTO expectedToken = AccessTokenDTO.builder()
                    .accessToken("new-access-token")
                    .build();

            when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
            when(memberDetailsService.loadUserByUsername(email)).thenReturn(memberDetails);
            when(jwtProvider.generateAccessToken(any(Authentication.class))).thenReturn(expectedToken);

            // when
            AccessTokenDTO result = authService.refreshAccessToken(email, refreshToken);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        }

        @Test
        @DisplayName("실패: refresh token 검증 실패")
        void refreshAccessToken_Fail_InvalidRefreshToken() {
            // given
            String email = "test@test.com";
            String refreshToken = "invalid-refresh-token";

            when(jwtProvider.validateToken(refreshToken)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refreshAccessToken(email, refreshToken))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    @Nested
    @DisplayName("소셜 로그인 테스트")
    class HandleSocialLoginTest {

        @Test
        @DisplayName("성공: 신규 회원")
        void handleSocialLogin_Success_NewMember() {
            // given
            when(oauthClientFactory.getClient(LoginType.GOOGLE)).thenReturn(oauthClient);
            when(oauthClient.getUserInfo("social-access-token")).thenReturn(socialUserInfo);
            when(memberRepository.findByEmail(socialUserInfo.getEmail()))
                    .thenReturn(Optional.empty());

            // when
            SocialLoginResponseDTO result = authService.handleSocialLogin(
                    LoginType.GOOGLE, "social-access-token");

            // then
            assertThat(result).isNotNull();
            assertThat(result.isRegistered()).isFalse();
            assertThat(result.getLoginType()).isEqualTo(LoginType.GOOGLE);
            assertThat(result.getSocialUserInfo()).isEqualTo(socialUserInfo);
        }

        @Test
        @DisplayName("성공: 기존 소셜 회원")
        void handleSocialLogin_Success_ExistingSocialMember() {
            // given
            Member socialMember = Member.builder()
                    .id(2L)
                    .email(socialUserInfo.getEmail())
                    .loginType(LoginType.GOOGLE)
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();

            when(oauthClientFactory.getClient(LoginType.GOOGLE)).thenReturn(oauthClient);
            when(oauthClient.getUserInfo("social-access-token")).thenReturn(socialUserInfo);
            when(oauthClient.getType()).thenReturn(LoginType.GOOGLE);
            when(memberRepository.findByEmail(socialUserInfo.getEmail()))
                    .thenReturn(Optional.of(socialMember));
            when(memberDetailsService.loadUserByUsername(socialMember.getEmail()))
                    .thenReturn(MemberDetails.builder().member(socialMember).build());
            when(jwtProvider.generateToken(any(Authentication.class))).thenReturn(tokenResponse);

            // when
            SocialLoginResponseDTO result = authService.handleSocialLogin(
                    LoginType.GOOGLE, "social-access-token");

            // then
            assertThat(result).isNotNull();
            assertThat(result.isRegistered()).isTrue();
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");

            verify(redisAuthService).saveRefreshToken(socialMember.getEmail(), "refresh-token");
        }

        @Test
        @DisplayName("실패: 일반 회원 계정이 이미 존재")
        void handleSocialLogin_Fail_LocalAccountExists() {
            // given
            Member localMember = Member.builder()
                    .id(3L)
                    .email(socialUserInfo.getEmail())
                    .loginType(LoginType.LOCAL)
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();

            when(oauthClientFactory.getClient(LoginType.GOOGLE)).thenReturn(oauthClient);
            when(oauthClient.getUserInfo("social-access-token")).thenReturn(socialUserInfo);
            when(memberRepository.findByEmail(socialUserInfo.getEmail()))
                    .thenReturn(Optional.of(localMember));

            // when & then
            assertThatThrownBy(() -> authService.handleSocialLogin(
                    LoginType.GOOGLE, "social-access-token"))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_REGISTERED_WITH_LOCAL);
        }

        @Test
        @DisplayName("실패: 이미 다른 소셜 계정으로 가입된 경우")
        void handleSocialLogin_Fail_DifferentSocialAccount() {
            // given
            Member kakaoMember = Member.builder()
                    .id(4L)
                    .email(socialUserInfo.getEmail())
                    .loginType(LoginType.KAKAO)
                    .roles(Collections.singletonList("ROLE_USER"))
                    .build();

            when(oauthClientFactory.getClient(LoginType.GOOGLE)).thenReturn(oauthClient);
            when(oauthClient.getUserInfo("social-access-token")).thenReturn(socialUserInfo);
            when(oauthClient.getType()).thenReturn(LoginType.GOOGLE);
            when(memberRepository.findByEmail(socialUserInfo.getEmail()))
                    .thenReturn(Optional.of(kakaoMember));

            // when & then
            assertThatThrownBy(() -> authService.handleSocialLogin(
                    LoginType.GOOGLE, "social-access-token"))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_REGISTERED_WITH_ANOTHER_SOCIAL);
        }
    }
}