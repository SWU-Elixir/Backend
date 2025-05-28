package BE_Elixir.Elixir.domain.member.dto.response;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfileResponseDTO {

    private Long id;
    private String nickname;
    private String title;
    private String profileUrl;
    private int followerCount;
    private int followingCount;

}