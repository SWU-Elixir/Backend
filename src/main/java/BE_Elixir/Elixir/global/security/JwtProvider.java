package BE_Elixir.Elixir.global.security;

import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;


@Component
@Slf4j
public class JwtProvider {

    private final MemberRepository memberRepository;
    private final Key key;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidityInMs;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidityInMs;

    // application.properties에서 secret 값 가져와서 key에 저장
    public JwtProvider(@Value("${jwt.secret}") String secretKey, MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token 생성
    private String createAccessToken(String username, String authorities, long now) {
        Date accessTokenExpiration = new Date(now + accessTokenValidityInMs);
        return Jwts.builder()
                .subject(username)
                .claim("auth", authorities)
                .expiration(accessTokenExpiration)
                .signWith(key)
                .compact();
    }

    // Refresh Token 생성
    private String createRefreshToken(long now) {
        Date refreshTokenExpiration = new Date(now + refreshTokenValidityInMs);
        return Jwts.builder()
                .expiration(refreshTokenExpiration)
                .signWith(key)
                .compact();
    }

    // 인증 정보 기반 Access Token 발급 (토큰 재발급 시)
    public String generateAccessTokenToken(Authentication authentication) {
        String authorities = getAuthorities(authentication);
        long now = System.currentTimeMillis();
        return createAccessToken(authentication.getName(), authorities, now);
    }

    // 인증 정보 기반 Access Token + Refresh Token 발급 (로그인 시)
    public TokenResponseDTO generateToken(Authentication authentication) {
        String authorities = getAuthorities(authentication);
        long now = System.currentTimeMillis();

        String accessToken = createAccessToken(authentication.getName(), authorities, now);
        String refreshToken = createRefreshToken(now);

        return TokenResponseDTO.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Refresh Token을 이용해 새로운 Access Token, Refresh Token을 발급
    public TokenResponseDTO refreshAccessToken(String email, String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        Claims claims = parseClaims(refreshToken);
        String username = claims.getSubject();
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, "", null);

        String newAccessToken = generateAccessTokenToken(authentication);

        log.info("새로운 Access Token 발급: {}", newAccessToken);

        return TokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)  // 기존 거 그대로 반환
                .build();
    }

    // Authorities 변환
    private String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    // Jwt 복호화로 토큰에 들어있는 정보를 꺼내는 메소드
    public Authentication getAuthentication(String accessToken) {
        // Jwt 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            log.error("권한 정보가 없는 토큰입니다.");
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다. 이메일: " + email));

        MemberDetails principal = new MemberDetails(member);

        log.info("JWT 토큰에서 인증된 사용자: {}", email);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);

    }

    // 토큰 정보를 검증하는 메소드
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);

            log.info("JWT 토큰 검증 성공: {}", token);

            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 토큰: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 비어있습니다: {}", e.getMessage());
        } catch (Exception e) {
            log.error("알 수 없는 오류: {}", e.getMessage());
        }
        return false;
    }

    // claim 꺼내기
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // 토큰의 남은 유효 시간 계산 (밀리초 단위)
    public long getRemainingTime(String token) {
        Claims claims = parseClaims(token);
        Date expiration = claims.getExpiration();
        long currentTime = System.currentTimeMillis();

        return expiration.getTime() - currentTime;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
