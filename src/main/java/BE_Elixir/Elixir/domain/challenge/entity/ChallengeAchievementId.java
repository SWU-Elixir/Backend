package BE_Elixir.Elixir.domain.challenge.entity;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChallengeAchievementId implements Serializable {
    private Long memberId;
    private Long challengeId;
}