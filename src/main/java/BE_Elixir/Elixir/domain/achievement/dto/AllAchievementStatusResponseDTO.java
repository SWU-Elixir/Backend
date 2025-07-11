package BE_Elixir.Elixir.domain.achievement.dto;

import BE_Elixir.Elixir.global.enums.AchievementType;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllAchievementStatusResponseDTO {
    // 사용자의 모든 업적 상태 목록
    private Map<AchievementType, AchievementCategoryDTO> achievementMap;
}
