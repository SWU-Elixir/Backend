package BE_Elixir.Elixir.global.enums;

public enum LoginType {
    LOCAL,
    GOOGLE,
    KAKAO,
    NAVER;

    public boolean isSocial() {
        return this != LOCAL;
    }
}