package BE_Elixir.Elixir.domain.member.dto.response;


import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberAchievementResponseDTO {
    private int year;          // 챌린지 연도
    private int month;         // 챌린지 월
    private String achievementName; // 업적 명
    private String achievementImageUrl; // 업적 이미지
    private boolean challengeCompleted; // 챌린지 최종 달성 여부
}
