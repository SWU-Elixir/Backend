package BE_Elixir.Elixir.domain.achievement.entity;

import BE_Elixir.Elixir.global.enums.AchievementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementType type;   // 업적 종류

    @Column(nullable = false)
    private int level;      // 업적의 레벨(초급, 중급, 고급)

    @Column(unique = true, nullable = false)
    private String code; // 예: "TOTAL_LOGIN_DAYS_LV1"

    @Column(nullable = false)
    private int targetValue;    // 업적 달성 목표 수치(7, 30, 100 등)

    private String description; // 설명 문구
    private String achievementName; // 업적 명
    private String achievementImageUrl; // 업적 이미지
    private String grayAchievementImageUrl; // 업적 달성 안 한 이미지

}
