package BE_Elixir.Elixir.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {


    EXISTS_MEMBER(HttpStatus.CONFLICT.value(), "이미 존재하는 회원입니다."),
    RECIPE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "레시피가 존재하지 않습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "댓글이 존재하지 않습니다."),
    UNAUTHORIZED_OPERATION(HttpStatus.FORBIDDEN.value(), "수정 권한이 없습니다."),
    INVALID_OPERATION(HttpStatus.FORBIDDEN.value(), "잘못된 요청입니다."), // 통합이벤트
    ALREADY_SCRAPPED(HttpStatus.FORBIDDEN.value(), "이미 스크랩한 레시피입니다."),
    SCRAP_NOT_FOUND(HttpStatus.FORBIDDEN.value(), "스크랩한 레시피가 없습니다.");

    private final int status;
    private final String message;
}

