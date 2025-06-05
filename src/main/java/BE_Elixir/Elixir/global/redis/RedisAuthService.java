package BE_Elixir.Elixir.global.redis;

import BE_Elixir.Elixir.global.security.JwtProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisAuthService {

    private final StringRedisTemplate redisTemplate;
    private final JwtProvider jwtProvider;

    public RedisAuthService(StringRedisTemplate redisTemplate, JwtProvider jwtProvider) {
        this.redisTemplate = redisTemplate;
        this.jwtProvider = jwtProvider;
    }

    // Redis에 RefreshToken 저장
    public void saveRefreshToken(String email, String refreshToken) {
        long expiration = 7 * 24 * 60 * 60; // 7일
        redisTemplate.opsForValue().set(email, refreshToken, expiration, TimeUnit.SECONDS);
    }

    // Redis에서 Refresh Token 삭제
    public void removeRefreshToken(String email) {
        redisTemplate.delete(email);
    }

    // Refresh Token이 Redis에 존재하는지 확인
    public boolean isRefreshTokenValid(String email, String refreshToken) {
        String savedToken = redisTemplate.opsForValue().get(email);
        return refreshToken.equals(savedToken);
    }

    // Redis에서 RefreshToken 가져오기 (email 기준)
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    // Access Token 블랙리스트에 추가
    public void addAccessTokenToBlacklist(String accessToken) {
        long remainingTime = jwtProvider.getRemainingTime(accessToken);
        // Access Token을 Redis에 저장, 만료 시간 설정
        redisTemplate.opsForValue().set(accessToken, "invalid", remainingTime, TimeUnit.MILLISECONDS);
    }

    // 블랙리스트에 있는 Access Token인지 확인
    public boolean isAccessTokenBlacklisted(String accessToken) {
        return redisTemplate.hasKey(accessToken);
    }

}