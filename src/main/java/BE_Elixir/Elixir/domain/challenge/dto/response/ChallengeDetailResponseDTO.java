package BE_Elixir.Elixir.domain.challenge.dto.response;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ChallengeDetailResponseDTO {
    private final String name;
    private final String period;
    private final String description;
    private final String purpose;

    private final String step1Goal1Desc;
    private final String step1Goal2Desc;
    private final String step2Goal1Desc;
    private final String step2Goal2Desc;
    private final String step3Goal1Desc;
    private final String step3Goal2Desc;
    private final String step4Goal1Desc;
    private final String step4Goal2Desc;

    private final String achievementName;
    private final List<String> ingredients;

    public ChallengeDetailResponseDTO(Challenge challenge, List<String> ingredients) {
        this.name = challenge.getName();

        int year = challenge.getYear();
        int month = challenge.getMonth();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        this.period = String.format("%d월 %d일 ~ %d월 %d일",
                startDate.getMonthValue(), startDate.getDayOfMonth(),
                endDate.getMonthValue(), endDate.getDayOfMonth());

        this.description = challenge.getDescription();
        this.purpose = challenge.getPurpose();

        this.step1Goal1Desc = challenge.getStep1Goal1Desc();
        this.step1Goal2Desc = challenge.getStep1Goal2Desc();
        this.step2Goal1Desc = challenge.getStep2Goal1Desc();
        this.step2Goal2Desc = challenge.getStep2Goal2Desc();
        this.step3Goal1Desc = challenge.getStep3Goal1Desc();
        this.step3Goal2Desc = challenge.getStep3Goal2Desc();
        this.step4Goal1Desc = challenge.getStep4Goal1Desc();
        this.step4Goal2Desc = challenge.getStep4Goal2Desc();

        this.achievementName = challenge.getAchievementName();
        this.ingredients = ingredients;
    }
}
