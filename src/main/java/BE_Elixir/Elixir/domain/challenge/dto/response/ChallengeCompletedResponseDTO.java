package BE_Elixir.Elixir.domain.challenge.dto.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class ChallengeCompletedResponseDTO {

    private String achievementName; // 업적 명
    private String message; // 메시지
    private String achievementImageUrl; // 업적 이미지
    private boolean challengeCompleted; // 챌린지 최종 달성 여부
}
