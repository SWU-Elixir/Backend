package BE_Elixir.Elixir.global.exception;

public class OccupiedException extends RuntimeException {

    private final ErrorCode errorCode;

    public OccupiedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
