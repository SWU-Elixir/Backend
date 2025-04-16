package BE_Elixir.Elixir.domain.auth.service;

import BE_Elixir.Elixir.domain.auth.dto.AccessTokenDTO;
import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.request.LoginRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.service.MemberDetailsService;
import BE_Elixir.Elixir.global.redis.RedisService;
import BE_Elixir.Elixir.global.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final MemberDetailsService memberDetailsService;

    // 로그인 (jwt 발급 및 Redis 저장)
    public TokenResponseDTO signIn(LoginRequestDTO request) {
        log.info("login");

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
            redisService.saveRefreshToken(email, refreshToken);
            log.info("Refresh Token Redis에 저장: email={}, token={}", email, refreshToken);

            return tokenResponse;
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 비밀번호: {}", request.getEmail());
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");

        } catch (Exception e) {
            log.error("로그인 중 예외 발생 - email: {}, message: {}", request.getEmail(), e.getMessage(), e);
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    // 로그아웃 (토큰 블랙리스트 처리, Redis에서 Refresh Token 제거)
    public void logout(String email, String accessToken, String refreshToken) {
        // Access Token 검증 및 블랙리스트 처리
        if (jwtProvider.validateToken(accessToken)) {
            redisService.addAccessTokenToBlacklist(accessToken);
            log.info("Access Token 블랙리스트 등록 완료");
        } else {
            throw new RuntimeException("유효하지 않거나 만료된 Access Token");
        }

        // Refresh Token이 redis에 있는지 확인 및 제거
        if (refreshToken != null && redisService.isRefreshTokenValid(email, refreshToken)) {
            redisService.removeRefreshToken(email);
            log.info("Refresh Token 삭제 완료");
        } else {
            throw new RuntimeException("유효하지 않거나 만료된 Refresh Token");
        }
    }

    // Refresh Token을 이용해 새로운 Access Token, Refresh Token을 발급
    public AccessTokenDTO refreshAccessToken(String email, String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // 회원 인증 정보 츄츌
        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, "", memberDetails.getAuthorities());

        return jwtProvider.generateAccessToken(authentication);
    }
}
