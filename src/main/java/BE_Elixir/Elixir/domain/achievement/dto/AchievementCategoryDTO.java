package BE_Elixir.Elixir.domain.achievement.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AchievementCategoryDTO {
    private int currentValue;
    private List<AchievementProgressDTO> achievements;
}
