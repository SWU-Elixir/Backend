package BE_Elixir.Elixir.global.redis;
import BE_Elixir.Elixir.domain.recommendation.dto.RecommendationResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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

    // 추천 레시피 캐시 저장 메서드
    public void cacheRecommendations(String cacheKey, List<RecommendationResponseDTO> recommendations, Duration ttl) {
        if (recommendations == null || recommendations.isEmpty()) {
            redisTemplate.delete(cacheKey);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(recommendations);
            redisTemplate.opsForValue().set(cacheKey, json, ttl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 추천 레시피 캐시 조회 메서드
    public List<RecommendationResponseDTO> getCachedRecommendations(String cacheKey) {
        String json = redisTemplate.opsForValue().get(cacheKey);
        if (json == null) return null;

        try {
            return objectMapper.readValue(json, new TypeReference<List<RecommendationResponseDTO>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}