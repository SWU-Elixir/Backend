package BE_Elixir.Elixir.domain.member.service;

import BE_Elixir.Elixir.domain.auth.dto.request.TokenRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.SignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.exception.OccupiedException;
import BE_Elixir.Elixir.global.redis.RedisService;
import BE_Elixir.Elixir.global.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    // 이메일 중복 체크
    public boolean isEmailDuplicated(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 회원가입 (USER 권한을 추가하여 데이터 추가)
    public Member signUp(SignUpRequestDTO request) {
        List<String> roles = new ArrayList<>();
        roles.add("USER");

        try {
            Member member = request.toEntity(
                    passwordEncoder.encode(request.getPassword()), roles
            );

            member.setRoles(roles);

            return memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().toUpperCase().contains("EMAIL_UNIQUE")) {
                throw new OccupiedException(ErrorCode.EXISTS_MEMBER);
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("회원가입 중 오류가 발생했습니다.");
        }
    }


    // 회원 탈퇴
    public void withdraw(String email, String accessToken, String refreshToken) {
        // Access Token 검증 및 블랙리스트 처리
        if (jwtProvider.validateToken(accessToken)) {
            redisService.addAccessTokenToBlacklist(accessToken);
            log.info("Access Token 블랙리스트 처리");
        } else {
            throw new RuntimeException("유효하지 않거나 만료된 Access Token");
        }

        // Refresh Token이 redis에 있는지 확인 및 제거
        if (refreshToken != null && redisService.isRefreshTokenValid(email, refreshToken)) {
            // redis에서 제거
            redisService.removeRefreshToken(email);
            log.info("Refresh Token 무효화");
        } else {
            throw new RuntimeException("유효하지 않거나 만료된 Refresh Token");
        }

        // 회원 정보 및 삭제
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. email: " + email));
        memberRepository.delete(member);
    }

}