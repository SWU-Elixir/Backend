package BE_Elixir.Elixir.domain.member.dto;

import BE_Elixir.Elixir.domain.member.entity.Member;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequestDTO {

    private String email;
    private String password;
    private String nickname;
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
                .gender(this.gender)
                .birthYear(this.birthYear)
                .roles(roles)
                .build();
    }
}