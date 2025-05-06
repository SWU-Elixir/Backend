package BE_Elixir.Elixir.domain.challenge.entity;

import java.io.Serializable;


public class ChallengeAchievementId implements Serializable {
    private Long memberId;
    private Long challengeId;
    // equals, hashCode 필수
}