package BE_Elixir.Elixir.domain.challenge.dto.response;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import lombok.Getter;

@Getter
public class ChallengeListResponseDTO {
    private final Long id;
    private final String name;
    private int month; // 월
    private int year; // 년도

    private String achievementName; // 업적 명

    public ChallengeListResponseDTO(Challenge challenge) {
        this.id = challenge.getId();
        this.name = challenge.getName();
        this.month = challenge.getMonth();
        this.year = challenge.getYear();
        this.achievementName = challenge.getAchievementName();
    }
}
