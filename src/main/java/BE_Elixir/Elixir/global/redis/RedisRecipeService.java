package BE_Elixir.Elixir.global.redis;
import BE_Elixir.Elixir.domain.recommendation.dto.RecommendationResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisRecipeService {
    private final StringRedisTemplate redisTemplate;
    private static final String POPULAR_SEARCH_KEY = "recipe:popularSearch";
    private static final String RECOMMEND_KEY_PREFIX = "recommendations:user:";
    private final ObjectMapper objectMapper;


    // 검색어 횟수 증가
    public void incrementKeyword(String keyword) {
        redisTemplate.opsForZSet().incrementScore(POPULAR_SEARCH_KEY, keyword, 1);
    }

    // 인기 검색어 조회 (Top 10)
    public List<String> getTopKeywords(int count) {
        return redisTemplate.opsForZSet()
                .reverseRange(POPULAR_SEARCH_KEY, 0, count - 1)
                .stream()
                .toList();
    }

    // 추천 레시피 캐시 저장
    public void cacheRecommendations(Long userId, List<RecommendationResponseDTO> recommendations, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(recommendations); // JSON 문자열로 변환
            redisTemplate.opsForValue().set(RECOMMEND_KEY_PREFIX + userId, json, ttl); // 캐싱
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 추천 레시피 캐시 조회
    public List<RecommendationResponseDTO> getCachedRecommendations(Long userId) {
        String json = redisTemplate.opsForValue().get(RECOMMEND_KEY_PREFIX + userId);

        if (json == null) return null;

        try {
            return objectMapper.readValue(json, new TypeReference<>() {}); // 다시 객체 리스트로 변환
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}