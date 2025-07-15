package BE_Elixir.Elixir.domain.auth.service;

import BE_Elixir.Elixir.domain.achievement.service.MemberStatsService;
import BE_Elixir.Elixir.domain.auth.dto.AccessTokenDTO;
import BE_Elixir.Elixir.domain.auth.dto.SocialUserInfo;
import BE_Elixir.Elixir.domain.auth.dto.response.SocialLoginResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.request.LoginRequestDTO;
import BE_Elixir.Elixir.domain.challenge.event.events.LoginSuccessEvent;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.domain.member.service.MemberDetailsService;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.enums.LoginType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.redis.RedisAuthService;
import BE_Elixir.Elixir.global.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProvider jwtProvider;
    private final RedisAuthService redisAuthService;
    private final MemberDetailsService memberDetailsService;
    private final OauthClientFactory oauthClientFactory;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MemberStatsService memberStatsService;

    // 로그인 (jwt 발급 및 Redis 저장)
    public TokenResponseDTO signIn(LoginRequestDTO request) {
        try {
            // 일반 회원 검증
            LoginType loginType = memberRepository.findLoginTypeByEmail(request.getEmail())
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
            if (loginType != LoginType.LOCAL) {
                throw new CustomException(ErrorCode.EMAIL_REGISTERED_WITH_SOCIAL);
            }

            // email + password 기반 authentication 객체 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            // 실제 검증 authentication 메서드를 통해 요청된 member에 대한 검증 진행
            Authentication authentication =
                    authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // 인증 정보 기반 JWT 생성
            TokenResponseDTO tokenResponse = jwtProvider.generateToken(authentication);
            String refreshToken = tokenResponse.getRefreshToken();
            String email = authentication.getName();

            // Redis에 Refresh Token 저장
            redisAuthService.saveRefreshToken(email, refreshToken);
            log.info("Refresh Token Redis에 저장: email={}, token={}", email, refreshToken);

            // 챌린지 및 업적 관련
            // 로그인 성공 이벤트 발행
            eventPublisher.publishEvent(new LoginSuccessEvent(request.getEmail()));

            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
            Long memberId = member.getId();
            // 총 로그인 일수 증가
            memberStatsService.increaseStat(memberId, AchievementType.TOTAL_LOGIN_DAYS, 1);
            // 연속 로그인 일수 갱신
            memberStatsService.increaseStat(memberId, AchievementType.CONSECUTIVE_LOGIN_DAYS, 1);
            return tokenResponse;
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 비밀번호: {}", request.getEmail());
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    // 로그아웃 (토큰 블랙리스트 처리, Redis에서 Refresh Token 제거)
    public void logout(String email, String accessToken, String refreshToken) {
        // Access Token 검증 및 블랙리스트 처리
        if (jwtProvider.validateToken(accessToken)) {
            redisAuthService.addAccessTokenToBlacklist(accessToken);
            log.info("Access Token 블랙리스트 등록 완료");
        } else {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        // Refresh Token이 redis에 있는지 확인 및 제거
        if (refreshToken != null && redisAuthService.isRefreshTokenValid(email, refreshToken)) {
            redisAuthService.removeRefreshToken(email);
            log.info("Refresh Token 삭제 완료");
        } else {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    // Refresh Token을 이용해 새로운 Access Token, Refresh Token을 발급
    public AccessTokenDTO refreshAccessToken(String email, String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 회원 인증 정보 추출
        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, "", memberDetails.getAuthorities());

        return jwtProvider.generateAccessToken(authentication);
    }

    // 소셜 로그인
    public SocialLoginResponseDTO handleSocialLogin(LoginType loginType, String accessToken) {
        OauthClient client = oauthClientFactory.getClient(loginType);
        SocialUserInfo userInfo = client.getUserInfo(accessToken);

        Optional<Member> existing = memberRepository.findByEmail(userInfo.getEmail());

        if (existing.isPresent()) {
            // 이미 회원가입된 경우
            Member member = existing.get();

            // 로그인 타입 확인
            // 이미 일반 로그인 계정이 존재하는 경우
            if (member.getLoginType() == LoginType.LOCAL) {
                throw new CustomException(ErrorCode.EMAIL_REGISTERED_WITH_LOCAL);
            }
            // 가입된 타입과 로그인한 타입이 동일하지 않은 경우
            if (member.getLoginType() != client.getType()) {
                throw new CustomException(ErrorCode.EMAIL_REGISTERED_WITH_ANOTHER_SOCIAL);
            }

            // Spring Security Authentication 객체 생성
            MemberDetails memberDetails = memberDetailsService.loadUserByUsername(member.getEmail());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    memberDetails, "", memberDetails.getAuthorities()
            );

            // JWT 생성
            TokenResponseDTO tokenResponse = jwtProvider.generateToken(authentication);
            String refreshToken = tokenResponse.getRefreshToken();

            // Redis에 Refresh Token 저장
            redisAuthService.saveRefreshToken(member.getEmail(), refreshToken);
            log.info("[소셜 로그인] Refresh Token Redis에 저장: email={}, token={}", member.getEmail(), refreshToken);

            return SocialLoginResponseDTO.builder()
                    .isRegistered(true)
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(refreshToken)
                    .build();

        } else {
            return SocialLoginResponseDTO.builder()
                    .isRegistered(false)
                    .loginType(loginType)
                    .socialUserInfo(userInfo)
                    .build();
        }
    }

}