package BE_Elixir.Elixir.global.exception;

import BE_Elixir.Elixir.global.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class RestApiExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(CommonResponse.error(
                        errorCode.getStatus(),
                        errorCode.name(),
                        errorCode.getMessage()
                ));
    }

    // 그외 시스템 에러는 이 아래에 각자 구현
    // 파일 용량 초과 예외
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonResponse<?>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(ErrorCode.FILE_SIZE_EXCEEDED.getStatus())
                .body(CommonResponse.error(
                        ErrorCode.FILE_SIZE_EXCEEDED.getStatus(),
                        ErrorCode.FILE_SIZE_EXCEEDED.name(),
                        ErrorCode.FILE_SIZE_EXCEEDED.getMessage()
                ));
    }

    //
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonResponse<?>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(ErrorCode.FILE_SIZE_EXCEEDED.getStatus())
                .body(CommonResponse.error(
                        ErrorCode.FILE_SIZE_EXCEEDED.getStatus(),
                        ErrorCode.FILE_SIZE_EXCEEDED.name(),
                        ErrorCode.FILE_SIZE_EXCEEDED.getMessage()
                ));
    }
}
