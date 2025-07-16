package BE_Elixir.Elixir.domain.auth.service;

import BE_Elixir.Elixir.domain.auth.dto.social.SocialUserInfo;
import BE_Elixir.Elixir.global.enums.LoginType;

public interface OauthClient {
    SocialUserInfo getUserInfo(String accessToken);
    LoginType getType();
}
