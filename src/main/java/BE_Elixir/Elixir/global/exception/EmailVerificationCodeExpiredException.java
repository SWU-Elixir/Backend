package BE_Elixir.Elixir.global.exception;

public class EmailVerificationCodeExpiredException extends RuntimeException{

    private final ErrorCode errorCode;

    public EmailVerificationCodeExpiredException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
