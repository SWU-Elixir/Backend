package BE_Elixir.Elixir.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;

// jwt 인증을 위한 커스텀 필터
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtProvider jwtProvider;
    private final String[] whitelist;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // 허용 경로는 필터 건너뛰기
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, requestURI)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // 1. Request Header 에서 JWT 토큰(Access Token) 추출
        String token = resolveToken((HttpServletRequest) request);

        if (token == null) {
            log.warn("Authorization 헤더에서 JWT 토큰을 찾을 수 없습니다.");
        } else if (!jwtProvider.validateToken(token)) {
            log.warn("유효하지 않거나 만료된 JWT 토큰이 요청에 포함되어 있습니다.");
        }

        // 2. validationToken()으로 토큰 유효성 검사
        if (token != null && jwtProvider.validateToken(token)) {
            try {
                // 토큰이 유효할 경우 토큰에서 Authentication 객체를 갖고 와서 SecurityContext에 저장
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JWT 토큰 인증 성공: {}", token);
            } catch (Exception e) {
                // 인증 실패시 예외 처리
                log.error("JWT 인증 실패", e);
                SecurityContextHolder.clearContext();

            }
        } else {
            log.warn("유효하지 않거나 만료된 토큰입니다.");
        }

        chain.doFilter(request, response);
    }

    // Request Header 에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
