package BE_Elixir.Elixir.domain.challenge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@Entity
@IdClass(ChallengeAchievementId.class)
public class ChallengeAchievement {

    @Id
    private Long memberId;

    @Id
    private Long challengeId;

    // 챌린지 달성 여부
    private boolean step1Goal1Achieved;
    private boolean step1Goal2Achieved;
    private boolean step2Goal1Achieved;
    private boolean step2Goal2Achieved;
    private boolean step3Goal1Achieved;
    private boolean step3Goal2Achieved;
    private boolean step4Goal1Achieved;
    private boolean step4Goal2Achieved;

    // 챌린지 활성 여부
    private boolean step1Goal1Active;
    private boolean step1Goal2Active;
    private boolean step2Goal1Active;
    private boolean step2Goal2Active;
    private boolean step3Goal1Active;
    private boolean step3Goal2Active;
    private boolean step4Goal1Active;
    private boolean step4Goal2Active;

    // 챌린지 최종 달성 여부
    private boolean challengeCompleted;

    // 챌린지 최종 달성 일시
    private LocalDateTime challengeCompletedAt;

    // 챌린지 활성화된 시점 (기록 유효성 판단 기준)
    private LocalDateTime openedAt;


    // 캡슐화(챌린지 최종 달성 여부)
    public boolean isAllGoalsAchieved() {
        return Stream.of(
                step1Goal1Achieved, step1Goal2Achieved,
                step2Goal1Achieved, step2Goal2Achieved,
                step3Goal1Achieved, step3Goal2Achieved,
                step4Goal1Achieved, step4Goal2Achieved
        ).allMatch(Boolean::booleanValue);
    }

}
