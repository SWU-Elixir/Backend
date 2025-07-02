package BE_Elixir.Elixir.domain.challenge.event.events;

import lombok.Getter;

@Getter
// 로그인할 때 발생하는 이벤트
public class LoginSuccessEvent {
    private final String email;

    public LoginSuccessEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
