package BE_Elixir.Elixir.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class SocialUserInfo {

    private String email;
    private String nickname;
    private String gender;
    private Integer birthYear;

    private String profileImage; // ??

}
