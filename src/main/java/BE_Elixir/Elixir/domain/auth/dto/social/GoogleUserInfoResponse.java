package BE_Elixir.Elixir.domain.auth.dto.social;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleUserInfoResponse {
    private String id;
    private String email;
    private String verified_email;
    private String name;
    private String given_name;
    private String family_name;
    private String picture;
    private String locale;
}