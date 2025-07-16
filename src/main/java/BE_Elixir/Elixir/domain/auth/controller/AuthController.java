package BE_Elixir.Elixir.domain.auth.controller;

import BE_Elixir.Elixir.domain.auth.controller.api.AuthApi;
import BE_Elixir.Elixir.domain.auth.dto.AccessTokenDTO;
import BE_Elixir.Elixir.domain.auth.dto.request.SocialLoginRequestDTO;
import BE_Elixir.Elixir.domain.auth.dto.response.SocialLoginResponseDTO;
import BE_Elixir.Elixir.domain.auth.service.AuthService;
import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.request.LoginRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.enums.LoginType;
import BE_Elixir.Elixir.global.redis.RedisAuthService;
import BE_Elixir.Elixir.global.response.CommonResponse;
import BE_Elixir.Elixir.global.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final RedisAuthService redisAuthService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<TokenResponseDTO>> login(
            @RequestBody LoginRequestDTO request
    ) {
        log.info("로그인 요청 - email: {}", request.getEmail());

        TokenResponseDTO token = authService.signIn(request);
        log.info("로그인 성공 - email: {}", request.getEmail());
        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "로그인 성공", token));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<?>> logout(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    ) {
        String email = memberDetails.getUsername();
        log.info("로그아웃 요청 - email: {}", email);

        String accessToken = jwtProvider.resolveToken(request);
        String refreshToken = redisAuthService.getRefreshToken(email);

        authService.logout(email, accessToken, refreshToken);
        log.info("로그아웃 성공 - email: {}", email);

        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),"로그아웃 성공"));
    }

    // Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<AccessTokenDTO>> refresh (
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    ) {
        String email = memberDetails.getUsername();
        log.info("Access Token 재발급 요청 - email: {}", email);

        String refreshToken = redisAuthService.getRefreshToken(email);
        AccessTokenDTO token = authService.refreshAccessToken(email, refreshToken);

        log.info("Access Token 재발급 성공 - email: {}", email);
        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "Access Token 재발급 성공", token));
    }

    // sns 소셜 로그인
    @PostMapping(value="/oauth/{loginType}")
    public ResponseEntity<CommonResponse<SocialLoginResponseDTO>> socialLogin(
            @PathVariable(name="loginType") LoginType loginType,
            @RequestBody SocialLoginRequestDTO request
    ) {
        log.info("소셜 로그인 요청 - type: {}", loginType);
        SocialLoginResponseDTO response = authService.handleSocialLogin(loginType, request.getAccessToken());

        log.info("소셜 로그인 성공");
        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "소셜 로그인 성공", response));
    }

}