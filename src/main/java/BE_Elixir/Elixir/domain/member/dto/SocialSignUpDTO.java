package BE_Elixir.Elixir.domain.member.dto;

import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class SocialSignUpDTO {
    private Member member;
    private TokenResponseDTO tokenResponseDTO;
}