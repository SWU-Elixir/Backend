package BE_Elixir.Elixir.global.security;

import BE_Elixir.Elixir.global.response.CommonResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        // 허용 경로는 필터 건너뛰기
        if (isWhitelisted(requestURI)) {
            log.info("Whitelist 패턴에 일치: URI = {}", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // Request Header 에서 JWT 토큰(Access Token) 추출
        String token = resolveToken((HttpServletRequest) request);

        // JWT 토큰 검증 및 예외 처리
        if (token == null) {
            log.warn("Authorization 헤더에서 JWT 토큰을 찾을 수 없습니다.");
            sendErrorResponse(httpResponse, 401, "UNAUTHORIZED", "Authorization 헤더에서 JWT 토큰을 찾을 수 없습니다.");
            return;
        }

        if (!jwtProvider.validateToken(token)) {
            log.warn("유효하지 않거나 만료된 JWT 토큰이 요청에 포함되어 있습니다.");
            sendErrorResponse(httpResponse, 401, "UNAUTHORIZED", "유효하지 않거나 만료된 JWT 토큰이 요청에 포함되어 있습니다.");
            return;
        }

        // 토큰이 유효할 경우 토큰에서 Authentication 객체를 갖고 와 SecurityContext 에 저장
        try {
            Authentication authentication = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("JWT 토큰 인증 성공: {}", token);
        } catch (Exception e) {
            // 인증 실패시 예외 처리
            log.error("JWT 인증 실패", e);
            SecurityContextHolder.clearContext();
            sendErrorResponse(httpResponse, 401, "UNAUTHORIZED", "JWT 인증 중 오류가 발생했습니다.");
            return;
        }

        chain.doFilter(request, response);
    }

    // 허용된 경로인지 확인
    private boolean isWhitelisted(String requestURI) {
        for (String pattern : whitelist) {
            if (pathMatcher.match(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }

    // Request Header 에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 응답 생성 메서드
    private void sendErrorResponse(HttpServletResponse response, int status, String code, String message) throws IOException {
        CommonResponse<?> errorResponse = CommonResponse.error(status, code, message);

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}
