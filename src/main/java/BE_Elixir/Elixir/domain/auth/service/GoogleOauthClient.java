package BE_Elixir.Elixir.domain.auth.service;

import BE_Elixir.Elixir.domain.auth.dto.social.GoogleUserInfoResponse;
import BE_Elixir.Elixir.domain.auth.dto.social.SocialUserInfo;
import BE_Elixir.Elixir.global.enums.LoginType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleOauthClient implements OauthClient {
    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        String uri = "https://www.googleapis.com/oauth2/v2/userinfo";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfoResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                requestEntity,
                GoogleUserInfoResponse.class
        );

        GoogleUserInfoResponse body = response.getBody();

        if (body.getEmail() == null) {
            throw new CustomException(ErrorCode.SOCIAL_USER_INFO_FETCH_FAILED);
        }

        return new SocialUserInfo(
                body.getEmail(),
                body.getName(),
                null, // gender (구글 기본 userinfo에 없음)
                null, // birthYear (구글 기본 userinfo에 없음)
                body.getPicture()
        );
    }

    @Override
    public LoginType getType() {
        return LoginType.GOOGLE;
    }
}