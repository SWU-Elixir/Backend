package BE_Elixir.Elixir.domain.auth.service;

import BE_Elixir.Elixir.domain.auth.dto.social.NaverUserInfoResponse;
import BE_Elixir.Elixir.domain.auth.dto.social.SocialUserInfo;
import BE_Elixir.Elixir.global.enums.LoginType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NaverOauthClient implements OauthClient {
    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        String uri = "https://openapi.naver.com/v1/nid/me";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<NaverUserInfoResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                requestEntity,
                NaverUserInfoResponse.class
        );

        NaverUserInfoResponse body = response.getBody();
        NaverUserInfoResponse.NaverResponse naver = body.getResponse();

        if (naver.getEmail() == null) {
            throw new CustomException(ErrorCode.SOCIAL_USER_INFO_FETCH_FAILED);
        }

        String birthYearStr = naver.getBirthyear();
        Integer birthYear = null;

        if (birthYearStr != null && !birthYearStr.isBlank()) {
            try {
                birthYear = Integer.parseInt(birthYearStr);
            } catch (NumberFormatException e) {
                // 잘못된 형식이면 null 처리
            }
        }

        return new SocialUserInfo(
                naver.getEmail(),
                naver.getNickname(),
                naver.getGender(),
                birthYear,
                naver.getProfile_image()
        );
    }

    @Override
    public LoginType getType() {
        return LoginType.NAVER;
    }
}