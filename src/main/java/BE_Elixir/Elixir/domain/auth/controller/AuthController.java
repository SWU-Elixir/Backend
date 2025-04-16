package BE_Elixir.Elixir.domain.auth.controller;

import BE_Elixir.Elixir.domain.auth.controller.api.AuthApi;
import BE_Elixir.Elixir.domain.auth.service.AuthService;
import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.auth.dto.request.LoginRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.redis.RedisService;
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
    private final RedisService redisService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<TokenResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        log.info("로그인 요청 - email: {}", request.getEmail());

        try {
            TokenResponseDTO token = authService.signIn(request);
            log.info("로그인 성공 - email: {}", request.getEmail());
            return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "로그인 성공", token));

        } catch (Exception e) {
            log.warn("로그인 실패 - email: {}, message: {}", request.getEmail(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.error(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.toString(),
                            "로그인 실패 - " + e.getMessage()));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<?>> logout(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    ) {
        String email = memberDetails.getUsername();
        log.info("로그아웃 요청 - email: {}", email);

        try {
            String accessToken = jwtProvider.resolveToken(request);
            String refreshToken = redisService.getRefreshToken(email);

            authService.logout(email, accessToken, refreshToken);
            log.info("로그아웃 성공 - email: {}", email);

            return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),"로그아웃 성공", null));
        } catch (Exception e) {
            log.warn("로그아웃 실패 - email: {}, message: {}", email, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponse.error(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.toString(),
                            "로그아웃 실패 - " + e.getMessage()));
        }
    }

    // Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<TokenResponseDTO>> refresh (
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    ) {
        String email = memberDetails.getUsername();
        log.info("Access Token 재발급 요청 - email: {}", email);

        try {
            String refreshToken = redisService.getRefreshToken(email);
            TokenResponseDTO token = jwtProvider.refreshAccessToken(email, refreshToken);

            log.info("Access Token 재발급 성공 - email: {}", email);
            return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "Access Token 재발급 성공", token));
        } catch (Exception e) {
            log.warn("Access Token 재발급 실패 - email: {}, message: {}", email, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.error(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.toString(),
                            "Access Token 재발급 실패 - " + e.getMessage()));
        }
    }
}