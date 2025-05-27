package BE_Elixir.Elixir.domain.member.dto.request;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfileRequestDTO {

    private String title;
    private String nickname;
    private String gender;
    private Integer birthYear;

}
