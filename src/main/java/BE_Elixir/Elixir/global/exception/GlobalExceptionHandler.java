package BE_Elixir.Elixir.global.exception;

import BE_Elixir.Elixir.global.response.CommonResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@RestControllerAdvice
@Order(value = Integer.MAX_VALUE)
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e, HttpServletRequest request) throws Exception {

        // swagger 요청 중 발생한 에러에 대해 springdoc에서 알아서 처리하도록 넘긴다
        String uri = request.getRequestURI();
        if (uri.contains("/api-docs") || uri.contains("/swagger") || uri.contains("/swagger-ui")) {
            log.warn("[Swagger 예외 무시] {} - {}", uri, e.getMessage());
            throw e; // 예외를 던져 SpringDoc가 직접 처리하게 함
        }

        log.error("서버 내부 오류 발생: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "INTERNAL_SERVER_ERROR",
                        "서버 내부 오류가 발생했습니다."
                ));
    }
}
