package BE_Elixir.Elixir.domain.challenge.entity;

import BE_Elixir.Elixir.domain.challenge.dto.request.ChallengeRequestDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 챌린지 명

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description; // 챌린지 설명

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String purpose; // 챌린지 목적


    // private LocalDateTime startDate; // 시작 일시
    // private LocalDateTime endDate; // 끝나는 일시
    private int month; // 월
    private int year; // 년도

    // 유형과 설명은 문자열로 저장
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

    private String achievementName; // 업적 명
    private String achievementImageUrl; // 업적 이미지
    private String grayAchievementImageUrl; // 업적 달성 안 한 이미지


}
