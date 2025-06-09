package BE_Elixir.Elixir.domain.member.dto.request;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePasswordRequestDTO {

    private String email;
    private String newPassword;

}
