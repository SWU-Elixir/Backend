package BE_Elixir.Elixir.domain.challenge.dto.response;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
public class ChallengeResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String purpose;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String month;
    private int year;

    private String step1Goal1Type;
    private String step1Goal2Type;
    private String step2Goal1Type;
    private String step2Goal2Type;
    private String step3Goal1Type;
    private String step3Goal2Type;
    private String step4Goal1Type;
    private String step4Goal2Type;

    private String step1Goal1Desc;
    private String step1Goal2Desc;
    private String step2Goal1Desc;
    private String step2Goal2Desc;
    private String step3Goal1Desc;
    private String step3Goal2Desc;
    private String step4Goal1Desc;
    private String step4Goal2Desc;

    private String achievementName;
    private String achievementImageUrl;
    private String grayAchievementImageUrl;

    public ChallengeResponseDTO(Challenge challenge) {
        this.id = challenge.getId();
        this.name = challenge.getName();
        this.description = challenge.getDescription();
        this.purpose = challenge.getPurpose(); 
        this.startDate = challenge.getStartDate();
        this.endDate = challenge.getEndDate();
        this.month = challenge.getMonth();
        this.year = challenge.getYear();
        this.step1Goal1Type = challenge.getStep1Goal1Type();
        this.step1Goal2Type = challenge.getStep1Goal2Type();
        this.step2Goal1Type = challenge.getStep2Goal1Type();
        this.step2Goal2Type = challenge.getStep2Goal2Type();
        this.step3Goal1Type = challenge.getStep3Goal1Type();
        this.step3Goal2Type = challenge.getStep3Goal2Type();
        this.step4Goal1Type = challenge.getStep4Goal1Type();
        this.step4Goal2Type = challenge.getStep4Goal2Type();

        this.step1Goal1Desc = challenge.getStep1Goal1Desc();
        this.step1Goal2Desc = challenge.getStep1Goal2Desc();
        this.step2Goal1Desc = challenge.getStep2Goal1Desc();
        this.step2Goal2Desc = challenge.getStep2Goal2Desc();
        this.step3Goal1Desc = challenge.getStep3Goal1Desc();
        this.step3Goal2Desc = challenge.getStep3Goal2Desc();
        this.step4Goal1Desc = challenge.getStep4Goal1Desc();
        this.step4Goal2Desc = challenge.getStep4Goal2Desc();

        this.achievementName = challenge.getAchievementName();
        this.achievementImageUrl = challenge.getAchievementImageUrl();
        this.grayAchievementImageUrl = challenge.getGrayAchievementImageUrl();
    }
}
