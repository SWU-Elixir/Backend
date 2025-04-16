package BE_Elixir.Elixir.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class AccessTokenDTO {
    private String accessToken;
}