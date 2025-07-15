package BE_Elixir.Elixir.domain.member.dto.response;

import BE_Elixir.Elixir.domain.achievement.entity.Achievement;
import BE_Elixir.Elixir.domain.achievement.entity.MemberAchievement;
import BE_Elixir.Elixir.global.enums.AchievementType;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class MemberAchievementResponseDTO {
    private String achievementName;
    private String description; // 설명 문구
    private String achievementImageUrl;
    private boolean completed;
    private int level;
    private AchievementType type;
    private String code;

    public static MemberAchievementResponseDTO from(MemberAchievement ma) {
        boolean isCompleted = ma.isCompleted();
        return new MemberAchievementResponseDTO(
                ma.getAchievement().getAchievementName(),
                ma.getAchievement().getDescription(),
                isCompleted ? ma.getAchievement().getAchievementImageUrl()
                        : ma.getAchievement().getGrayAchievementImageUrl(),
                isCompleted,
                ma.getAchievement().getLevel(),
                ma.getAchievement().getType(),
                ma.getAchievement().getCode()
        );
    }

    public static MemberAchievementResponseDTO from(Achievement achievement, MemberAchievement ma) {
        String imageUrl = ma.isCompleted()
                ? achievement.getAchievementImageUrl()
                : achievement.getGrayAchievementImageUrl();

        return MemberAchievementResponseDTO.builder()
                .achievementName(achievement.getAchievementName())
                .description(achievement.getDescription())
                .achievementImageUrl(imageUrl)
                .completed(ma.isCompleted())
                .level(achievement.getLevel())
                .type(achievement.getType())
                .code(achievement.getCode())
                .build();
    }

}
