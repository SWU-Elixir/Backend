package BE_Elixir.Elixir.domain.member.dto.response;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
public class MemberRecentAchievementDTO {
    private String achievementName; // 업적 명
    private String achievementImageUrl; // 업적 이미지
    private boolean completed;
    private LocalDateTime completedAt;
}
