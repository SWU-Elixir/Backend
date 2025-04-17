package BE_Elixir.Elixir.domain.member.dto;

import BE_Elixir.Elixir.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequestDTO {

    private String email;
    private String password;
    private String nickname;
    private String profileUrl;
    private String gender;
    private Integer birthYear;

    private List<String> allergies;
    private List<String> mealStyles;
    private List<String> recipeStyles;
    private List<String> reasons;


    public Member toEntity(String encodedPassword, List<String> roles) {
        return Member.builder()
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .profileUrl(this.profileUrl)
                .gender(this.gender)
                .birthYear(this.birthYear)
                .roles(roles)
                .build();
    }
}