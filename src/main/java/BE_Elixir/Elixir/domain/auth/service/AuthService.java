package BE_Elixir.Elixir.domain.auth.service;

import BE_Elixir.Elixir.domain.auth.dto.AccessTokenDTO;
import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.request.LoginRequestDTO;
import BE_Elixir.Elixir.domain.challenge.service.ChallengeAchievementService;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.service.MemberDetailsService;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.redis.RedisAuthService;
import BE_Elixir.Elixir.global.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProvider jwtProvider;
    private final RedisAuthService redisAuthService;
    private final MemberDetailsService memberDetailsService;
    private final ChallengeAchievementService challengeAchievementService;

    // 로그인 (jwt 발급 및 Redis 저장)
    public TokenResponseDTO signIn(LoginRequestDTO request) {
        try {
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

            String memberEmail = request.getEmail();

            // 자동 참여 메서드 호출
            challengeAchievementService.challengeParticipation(memberEmail);

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
}