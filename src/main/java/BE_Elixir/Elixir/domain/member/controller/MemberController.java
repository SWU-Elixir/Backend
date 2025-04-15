package BE_Elixir.Elixir.domain.member.controller;

import BE_Elixir.Elixir.domain.auth.dto.request.TokenRequestDTO;
import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.auth.service.AuthService;
import BE_Elixir.Elixir.domain.member.controller.api.MemberApi;
import BE_Elixir.Elixir.domain.member.dto.SignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.service.MemberService;
import BE_Elixir.Elixir.global.redis.RedisService;
import BE_Elixir.Elixir.global.security.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    // 이메일 중복 체크
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        log.info("이메일 중복 체크 요청: {}", email);

        boolean isDuplicate = memberService.isEmailDuplicated(email);
        return ResponseEntity.ok(isDuplicate);
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequestDTO request) {
        log.info("회원가입 요청 - 이메일: {}", request.getEmail());

        try {
            Member member = memberService.signUp(request);
            log.info("회원가입 성공 - 회원 ID: {}", member.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("회원가입 성공 - member_id = " + member.getId());

        } catch (Exception e) {
            log.error("회원가입 실패 - 이메일: {}, 메시지: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("회원가입 실패: " + e.getMessage());
        }
    }

    // 회원탈퇴
    @DeleteMapping("/withdrawal")
    public ResponseEntity<?> withdrawal(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    ) {
        String email = memberDetails.getUsername();
        log.info("회원탈퇴 요청 - 이메일: {}", email);

        try {
            String accessToken = jwtProvider.resolveToken(request);
            String refreshToken = redisService.getRefreshToken(memberDetails.getUsername());

            memberService.withdraw(email, accessToken, refreshToken);
            log.info("회원탈퇴 성공 - 이메일: {}", email);

            return ResponseEntity.ok("회원탈퇴 성공");
        } catch (IllegalArgumentException e) {
            log.warn("회원탈퇴 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("회원탈퇴 실패 - 시스템 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원탈퇴 중 오류가 발생했습니다.");
        }
    }


}
