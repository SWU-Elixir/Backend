package BE_Elixir.Elixir.global.response;

import lombok.Getter;

@Getter
public class CommonResponse<T> {

    private int status;
    private String code;
    private String message;
    private T data;

    // 생성자
    private CommonResponse(int status, String code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 성공 응답
    public static <T> CommonResponse<T> success(int status, String code, String message, T data) {
        return new CommonResponse<>(status, code, message, data);
    }
    // 성공 응답 (data 없는 경우)
    public static <T> CommonResponse<T> success(int status, String code, String message) {
        return new CommonResponse<>(status, code, message, null);
    }

    // 실패 응답
    public static <T> CommonResponse<T> error(int status, String code, String message) {
        return new CommonResponse<>(status, code, message, null);
    }

}
