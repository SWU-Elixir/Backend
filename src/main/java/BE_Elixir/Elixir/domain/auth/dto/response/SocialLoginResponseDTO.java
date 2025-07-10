package BE_Elixir.Elixir.domain.auth.dto.response;

import BE_Elixir.Elixir.domain.auth.dto.SocialUserInfo;
import BE_Elixir.Elixir.global.enums.LoginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class SocialLoginResponseDTO {
    private boolean isRegistered;

    private String accessToken;
    private String refreshToken;

    private LoginType loginType;
    private SocialUserInfo socialUserInfo;
}
