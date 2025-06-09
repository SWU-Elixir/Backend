package BE_Elixir.Elixir.domain.challenge.dto.response;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeProgressResponseDTO {
    private Long challengeId;
    private String name;
    private int year;
    private int month;

    // 1단계
    private boolean step1Goal1Achieved;
    private boolean step1Goal2Achieved;

    // 2단계
    private boolean step2Goal1Active;
    private boolean step2Goal2Active;
    private boolean step2Goal1Achieved;
    private boolean step2Goal2Achieved;

    // 3단계
    private boolean step3Goal1Active;
    private boolean step3Goal2Active;
    private boolean step3Goal1Achieved;
    private boolean step3Goal2Achieved;
    
    // 4단계
    private boolean step4Goal1Active;
    private boolean step4Goal2Active;
    private boolean step4Goal1Achieved;
    private boolean step4Goal2Achieved;
    
    // 최종
    private boolean challengeCompleted;

    public static ChallengeProgressResponseDTO from(Challenge challenge, ChallengeAchievement achievement) {
        boolean step1Goal1 = achievement.isStep1Goal1Achieved();
        boolean step1Goal2 = achievement.isStep1Goal2Achieved();

        boolean step2Active = step1Goal1 && step1Goal2;
        boolean step2Goal1 = achievement.isStep2Goal1Achieved();
        boolean step2Goal2 = achievement.isStep2Goal2Achieved();

        boolean step3Active = step2Goal1 && step2Goal2;
        boolean step3Goal1 = achievement.isStep3Goal1Achieved();
        boolean step3Goal2 = achievement.isStep3Goal2Achieved();

        boolean step4Active = step3Goal1 && step3Goal2;
        boolean step4Goal1 = achievement.isStep4Goal1Achieved();
        boolean step4Goal2 = achievement.isStep4Goal2Achieved();

        boolean completed = step4Goal1 && step4Goal2;

        return ChallengeProgressResponseDTO.builder()
                .challengeId(challenge.getId())
                .name(challenge.getName())
                .year(challenge.getYear())
                .month(challenge.getMonth())

                .step1Goal1Achieved(step1Goal1)
                .step1Goal2Achieved(step1Goal2)

                .step2Goal1Active(step2Active)
                .step2Goal2Active(step2Active)
                .step2Goal1Achieved(step2Goal1)
                .step2Goal2Achieved(step2Goal2)

                .step3Goal1Active(step3Active)
                .step3Goal2Active(step3Active)
                .step3Goal1Achieved(step3Goal1)
                .step3Goal2Achieved(step3Goal2)

                .step4Goal1Active(step4Active)
                .step4Goal2Active(step4Active)
                .step4Goal1Achieved(step4Goal1)
                .step4Goal2Achieved(step4Goal2)

                .challengeCompleted(completed)
                .build();
    }

    public static ChallengeProgressResponseDTO empty(Challenge challenge) {
        return ChallengeProgressResponseDTO.builder()
                .challengeId(challenge.getId())
                .name(challenge.getName())
                .year(challenge.getYear())
                .month(challenge.getMonth())

                // 1단계
                .step1Goal1Achieved(false)
                .step1Goal2Achieved(false)

                // 2단계
                .step2Goal1Active(false)
                .step2Goal2Active(false)
                .step2Goal1Achieved(false)
                .step2Goal2Achieved(false)

                // 3단계
                .step3Goal1Active(false)
                .step3Goal2Active(false)
                .step3Goal1Achieved(false)
                .step3Goal2Achieved(false)

                // 4단계
                .step4Goal1Active(false)
                .step4Goal2Active(false)
                .step4Goal1Achieved(false)
                .step4Goal2Achieved(false)

                // 최종 완료 여부
                .challengeCompleted(false)
                .build();
    }

}
