package BE_Elixir.Elixir.domain.member.dto.response;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberTitlesResponseDTO {

    private Long memberId;
    private List<String> titles;

}