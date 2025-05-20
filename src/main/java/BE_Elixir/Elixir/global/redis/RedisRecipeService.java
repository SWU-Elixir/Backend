package BE_Elixir.Elixir.global.redis;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisRecipeService {
    private final StringRedisTemplate redisTemplate;
    private static final String POPULAR_SEARCH_KEY = "recipe:popularSearch";

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
}