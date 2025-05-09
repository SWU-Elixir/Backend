package BE_Elixir.Elixir.domain.challenge.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class ChallengeAchievementId implements Serializable {
    private Long memberId;
    private Long challengeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeAchievementId that = (ChallengeAchievementId) o;
        return Objects.equals(memberId, that.memberId) &&
                Objects.equals(challengeId, that.challengeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, challengeId);
    }
}