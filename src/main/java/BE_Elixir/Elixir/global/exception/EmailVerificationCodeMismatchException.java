package BE_Elixir.Elixir.global.exception;

public class EmailVerificationCodeMismatchException extends RuntimeException{

    private final ErrorCode errorCode;

    public EmailVerificationCodeMismatchException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
