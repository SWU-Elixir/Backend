package BE_Elixir.Elixir.domain.auth.service;

import BE_Elixir.Elixir.domain.auth.dto.social.KakaoUserInfoResponse;
import BE_Elixir.Elixir.domain.auth.dto.social.SocialUserInfo;
import BE_Elixir.Elixir.global.enums.LoginType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KakaoOauthClient implements OauthClient {
    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        String uri = "https://kapi.kakao.com/v2/user/me";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                requestEntity,
                KakaoUserInfoResponse.class
        );

        KakaoUserInfoResponse body = response.getBody();

        if (body.getKakao_account() == null || body.getKakao_account().getEmail() == null) {
            throw new CustomException(ErrorCode.SOCIAL_USER_INFO_FETCH_FAILED);
        }

        String email = body.getKakao_account().getEmail();
        String nickname = null;
        String profileImageUrl = null;

        KakaoUserInfoResponse.KakaoAccount.Profile profile = body.getKakao_account().getProfile();
        if (profile != null) {
            nickname = profile.getNickname();
            profileImageUrl = profile.getProfile_image_url();
        }

        return new SocialUserInfo(
                email,
                nickname,
                null, // gender
                null, // birthYear
                profileImageUrl
        );
    }

    @Override
    public LoginType getType() {
        return LoginType.KAKAO;
    }
}