package BE_Elixir.Elixir.domain.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenRequestDTO {
    private String accessToken;
    private String refreshToken;
}
