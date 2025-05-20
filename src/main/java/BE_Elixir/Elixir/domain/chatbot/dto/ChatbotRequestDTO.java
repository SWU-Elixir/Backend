package BE_Elixir.Elixir.domain.chatbot.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotRequestDTO {

    String chatSessionId;   // 챗봇 세션 ID
    String type;            // 시나리오 유형 (DIET_FEEDBACK, RECIPE_FEEDBACK, RECOMMEND, FREETALK)
    Long targetId;          // 식단, 레시피 피드백의 경우 해당하는 데이터의 id
    Integer durationDays;       // 식단 추천 시, 기간
    Boolean includeChallengeIngredients;    // 식단 추천 시, 챌린지 식재료 포함 여부
    String additionalConditions;    // 식단 추천 시, 추가 조건
    String message;  // 사용자 입력 메시지

}