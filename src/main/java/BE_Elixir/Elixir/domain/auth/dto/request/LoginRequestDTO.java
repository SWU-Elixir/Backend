package BE_Elixir.Elixir.domain.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginRequestDTO {
    private String email;
    private String password;
}