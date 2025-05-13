package BE_Elixir.Elixir.domain.member.dto.response;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSummaryDTO {

    private Long id;
    private String nickname;
    private String profileUrl;
    private String title;

}
