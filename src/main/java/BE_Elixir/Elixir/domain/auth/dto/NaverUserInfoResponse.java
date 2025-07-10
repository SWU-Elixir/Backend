package BE_Elixir.Elixir.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfoResponse {
    private NaverResponse response;

    @Getter
    @NoArgsConstructor
    public static class NaverResponse {
        private String id;
        private String email;
        private String name;
        private String nickname;
        private String profile_image;
        private String gender;
        private String birthyear;
    }
}