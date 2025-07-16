package BE_Elixir.Elixir.domain.member.dto.request;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.global.enums.LoginType;
import lombok.*;
import lombok.extern.java.Log;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialSignUpRequestDTO {

    private String email;
    private String nickname;
    private String gender;
    private Integer birthYear;
    private String profileImageUrl; // google은 사용자 정보 조회 시 프로필 url을 줌

    private List<String> allergies;
    private List<String> mealStyles;
    private List<String> recipeStyles;
    private List<String> reasons;

    public Member toEntity(List<String> roles, LoginType loginType) {
        return Member.builder()
                .email(this.email)
                .nickname(this.nickname)
                .gender(this.gender)
                .birthYear(this.birthYear)
                .roles(roles)
                .loginType(loginType)
                .build();
    }
}