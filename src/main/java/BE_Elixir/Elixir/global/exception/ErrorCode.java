package BE_Elixir.Elixir.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    EXISTS_MEMBER(HttpStatus.CONFLICT.value(), "이미 존재하는 회원입니다.");

    private final int status;
    private final String message;
}