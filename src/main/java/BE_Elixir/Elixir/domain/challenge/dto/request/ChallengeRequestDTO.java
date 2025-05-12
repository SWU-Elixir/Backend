package BE_Elixir.Elixir.domain.challenge.dto.request;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
public class ChallengeRequestDTO {

    private String name;
    private String description;
    private String purpose;
    //private LocalDateTime startDate;
    //private LocalDateTime endDate;
    private int month;
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

    // ChallengeRequestDTO에서 Challenge 엔티티로 변환
    public static Challenge from(ChallengeRequestDTO dto) {
        Challenge challenge = new Challenge();

        challenge.setName(dto.name);
        challenge.setDescription(dto.description);
        challenge.setPurpose(dto.purpose);
        //challenge.setStartDate(dto.startDate);
        //challenge.setEndDate(dto.endDate);
        challenge.setMonth(dto.month);
        challenge.setYear(dto.year);

        challenge.setStep1Goal1Type(dto.step1Goal1Type);
        challenge.setStep1Goal2Type(dto.step1Goal2Type);
        challenge.setStep2Goal1Type(dto.step2Goal1Type);
        challenge.setStep2Goal2Type(dto.step2Goal2Type);
        challenge.setStep3Goal1Type(dto.step3Goal1Type);
        challenge.setStep3Goal2Type(dto.step3Goal2Type);
        challenge.setStep4Goal1Type(dto.step4Goal1Type);
        challenge.setStep4Goal2Type(dto.step4Goal2Type);

        challenge.setStep1Goal1Desc(dto.step1Goal1Desc);
        challenge.setStep1Goal2Desc(dto.step1Goal2Desc);
        challenge.setStep2Goal1Desc(dto.step2Goal1Desc);
        challenge.setStep2Goal2Desc(dto.step2Goal2Desc);
        challenge.setStep3Goal1Desc(dto.step3Goal1Desc);
        challenge.setStep3Goal2Desc(dto.step3Goal2Desc);
        challenge.setStep4Goal1Desc(dto.step4Goal1Desc);
        challenge.setStep4Goal2Desc(dto.step4Goal2Desc);

        challenge.setAchievementName(dto.achievementName);

        return challenge;
    }

}
