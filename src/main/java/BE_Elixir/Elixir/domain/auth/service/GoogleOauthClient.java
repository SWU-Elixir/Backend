package BE_Elixir.Elixir.domain.auth.service;

import BE_Elixir.Elixir.domain.auth.dto.GoogleUserInfoResponse;
import BE_Elixir.Elixir.domain.auth.dto.SocialUserInfo;
import BE_Elixir.Elixir.global.enums.LoginType;
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

        try {
            ResponseEntity<GoogleUserInfoResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestEntity,
                    GoogleUserInfoResponse.class
            );

            GoogleUserInfoResponse body = response.getBody();
            if (body.getEmail() == null) {
                throw new IllegalArgumentException("구글 사용자 정보 조회 실패");
            }

            return new SocialUserInfo(
                    body.getEmail(),
                    body.getName(),
                    null, // gender (구글 기본 userinfo에 없음)
                    null, // birthYear (구글 기본 userinfo에 없음)
                    body.getPicture()
            );
        } catch (Exception e) {
            throw new RuntimeException("구글 사용자 정보 조회 중 오류 발생", e);
        }
    }

    @Override
    public LoginType getType() {
        return LoginType.GOOGLE;
    }
}