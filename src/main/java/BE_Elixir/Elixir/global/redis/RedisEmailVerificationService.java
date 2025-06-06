package BE_Elixir.Elixir.global.redis;

import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
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
    public void saveVerificationCode(String email, String code, Instant mailSendTime) {
        String key = KEY_PREFIX + email;
        EmailVerificationDTO dto = new EmailVerificationDTO(email, code, mailSendTime);

        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR);
        }
    }

    // 인증 정보 조회
    public EmailVerificationDTO getVerification(String email){
        String key = KEY_PREFIX + email;
        String value = redisTemplate.opsForValue().get(key);

        try {
            return objectMapper.readValue(value, EmailVerificationDTO.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR);
        }
    }

    // 인증 정보 삭제
    public void deleteVerification(String email) {
        String key = KEY_PREFIX + email;
        redisTemplate.delete(key);
    }

}
