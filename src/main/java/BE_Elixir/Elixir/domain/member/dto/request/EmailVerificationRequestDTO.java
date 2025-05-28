package BE_Elixir.Elixir.domain.member.dto.request;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationRequestDTO {

    private String email;

}
