package BE_Elixir.Elixir.global.redis;

import BE_Elixir.Elixir.global.redis.dto.EmailVerificationDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisEmailVerificationService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "mail-verification:";

    // 인증 정보 저장
    public void saveVerificationCode(String email, String code, Instant mailSendTime) throws JsonProcessingException {
        String key = KEY_PREFIX + email;
        EmailVerificationDTO dto = new EmailVerificationDTO(email, code, mailSendTime);

        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(dto));
    }

    // 인증 정보 조회
    public EmailVerificationDTO getVerification(String email) throws JsonProcessingException {
        String key = KEY_PREFIX + email;
        String value = redisTemplate.opsForValue().get(key);

        return objectMapper.readValue(value, EmailVerificationDTO.class);
    }

    // 인증 정보 삭제
    public void deleteVerification(String email) {
        String key = KEY_PREFIX + email;
        redisTemplate.delete(key);
    }

}
