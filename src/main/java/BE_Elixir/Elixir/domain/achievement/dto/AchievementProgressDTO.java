package BE_Elixir.Elixir.domain.achievement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AchievementProgressDTO {
    private Long id;
    private String name;
    private String description; // 설명 문구
    private int level;
    private int targetValue;
    private boolean achieved;
    private Integer currentProgress; // 달성했으면 null
    private LocalDateTime achievedAt; // 달성 안 했으면 null
}
