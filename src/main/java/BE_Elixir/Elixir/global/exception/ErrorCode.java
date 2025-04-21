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
    UNAUTHORIZED_OPERATION(HttpStatus.FORBIDDEN.value(), "수정 권한이 없습니다.");

    private final int status;
    private final String message;
}
