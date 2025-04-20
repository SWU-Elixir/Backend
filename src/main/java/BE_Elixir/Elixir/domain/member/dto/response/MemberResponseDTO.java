package BE_Elixir.Elixir.domain.member.dto.response;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDTO {

    private Long id;
    private String email;
    private String nickname;
    private String gender;
    private Integer birthYear;
    private String profileUrl;

}
