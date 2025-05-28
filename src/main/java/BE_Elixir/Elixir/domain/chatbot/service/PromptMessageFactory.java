package BE_Elixir.Elixir.domain.chatbot.service;

import BE_Elixir.Elixir.domain.chatbot.dto.ChatbotRequestDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogResponseDTO;
import BE_Elixir.Elixir.domain.dietLog.service.DietLogService;
import BE_Elixir.Elixir.domain.ingredient.dto.ChallengeIngredientDTO;
import BE_Elixir.Elixir.domain.ingredient.service.IngredientService;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class PromptMessageFactory {

    private final DietLogService dietLogService;
    private final RecipeService recipeService;
    private final IngredientService ingredientService;

    // 최초 요청 시
    public List<Map<String, String>> create(ChatbotRequestDTO dto) {
        if (dto == null || dto.getType() == null) {
            throw new IllegalArgumentException("요청 정보 또는 type이 null입니다.");
        }

        return switch (dto.getType()) {
            case "DIET_FEEDBACK" -> {
                if (dto.getTargetId() == null) {
                    throw new IllegalArgumentException("DIET_FEEDBACK 요청에는 targetId가 필요합니다.");
                }
                yield promptDietFeedback(dto.getTargetId());
            }
            case "RECIPE_FEEDBACK" -> {
                if (dto.getTargetId() == null) {
                    throw new IllegalArgumentException("RECIPE_FEEDBACK 요청에는 targetId가 필요합니다.");
                }
                yield promptRecipeFeedback(dto.getTargetId());
            }
            case "RECOMMEND" -> {
                if (dto.getDurationDays() == null) {
                    throw new IllegalArgumentException("RECOMMEND 요청에는 durationDays가 필요합니다.");
                }
                if (dto.getIncludeChallengeIngredients() == null) {
                    throw new IllegalArgumentException("RECOMMEND 요청에는 includeChallengeIngredients가 필요합니다.");
                }
                if (dto.getAdditionalConditions() == null) {
                    throw new IllegalArgumentException("RECOMMEND 요청에는 additionalConditions가 필요합니다.");
                }
                yield promptRecommend(dto.getDurationDays(), dto.getIncludeChallengeIngredients(), dto.getAdditionalConditions());
            }
            case "FREETALK" -> {
                if (dto.getMessage() == null) {
                    throw new IllegalArgumentException("FREETALK 요청에는 message가 필요합니다.");
                }
                yield promptFreetalk(dto.getMessage());
            }
            default -> throw new IllegalArgumentException("지원하지 않는 type입니다. type: " + dto.getType());
        };
    }


    // 프롬프트 생성 - 식단 피드백
    private List<Map<String, String>> promptDietFeedback(Long dietLogId) {
        DietLogResponseDTO dietLog = dietLogService.getDietLog(dietLogId);
        String system = "당신은 저속노화 식단에 대한 전문 지식을 갖춘 AI입니다. 저속노화 식단을 실천하려는 사용자의 식단을 평가하고, 간결하고 정확한 피드백과 실용적인 조언을 제공합니다. 절대로 마크다운 문법을 사용하지 마십시오. 리스트는 사용해도 됩니다. 반드시 일반 텍스트 형식으로만 출력하십시오.";

        String message = String.format(
                """
                식단명: %s
                타입: %s
                대표 식재료: %s
                위 정보를 기반으로 식단을 전문적으로 평가하고, 추가적인 조언도 덧붙여주세요. 입력한 값을 다시 출력하지는 말고 피드백만 말해주세요.
                """,
                dietLog.getName(),
                dietLog.getType(),
                String.join(", ", ingredientService.getIngredientNamesByIds(dietLog.getIngredientTagId()))
        );

        return createMessage(system, message);
    }

    // 프롬프트 생성 - 레시피 피드백
    private List<Map<String, String>> promptRecipeFeedback(Long recipeId) {
        RecipeResponseDTO recipe = recipeService.getRecipe(recipeId);

        String system = "당신은 저속노화 식단에 대한 전문 지식을 갖춘 AI입니다. 저속노화 식단을 실천하려는 사용자의 레시피를 평가하고, 간결하고 정확한 피드백과 실용적인 조언을 제공합니다. 절대로 마크다운 문법을 사용하지 마십시오. 리스트는 사용해도 됩니다. 반드시 일반 텍스트 형식으로만 출력하십시오.";

        String message = String.format(
                """
                레시피명: %s
                레시피 설명: %s
                목적: %s
                타입: %s
                식재료: %s
                양념: %s
                조리 과정 설명: %s
                알러지: %s
                팁: %s
                위 정보를 기반으로 레시피를 전문적으로 평가하고, 추가적인 조언도 덧붙여주세요. 저속노화 식단에 적합한지, 목적에 맞는 레시피인지, 틀린 것은 없는지에 대해 중점적으로 피드백 해주세요. 입력한 값을 다시 출력하지는 말고 피드백만 말해주세요.
                """,
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getCategorySlowAging(),
                recipe.getCategoryType(),
                formatMap(recipe.getIngredients()),
                formatMap(recipe.getSeasoning()),
                String.join(", ", recipe.getStepDescriptions()),
                String.join(", ", recipe.getAllergies()),
                recipe.getTips()
        );

        return createMessage(system, message);
    }

    // 프롬프트 생성 - 식단 추천
    private List<Map<String, String>> promptRecommend(Integer durationDays, Boolean includeChallengeIngredients, String additionalConditions) {
        String system = "당신은 저속노화 식단에 대한 전문 지식을 갖춘 AI입니다. 저속노화 식단을 실천하려는 사용자에게 조건에 맞는 식단을 추천하고, 간결하고 정확한 식단 구성과 실용적인 조언을 제공합니다. 절대로 마크다운 문법을 사용하지 마십시오. 리스트는 사용해도 됩니다. 반드시 일반 텍스트 형식으로만 출력하십시오.";

        String message;
        if (includeChallengeIngredients) {
            String ingredients = ingredientService.getChallengeIngredients().stream()
                    .map(ChallengeIngredientDTO::getName)
                    .collect(Collectors.joining(", "));

            message = String.format(
                    """
                    기간: %d일
                    포함할 식재료: %s
                    추가 조건: %s
                    위 정보를 기반으로 하루 세 끼의 식단을 구성하세요. 포함할 식재료는 모든 식사에 포함되지 않아도 괜찮습니다. 아래 예시 형식처럼 작성해주세요.
                    예시 형식:
                    3일 동안의 저속노화 식단을 아래와 같이 제안합니다.
    
                    1일차:
                    - 아침: 시금치 오믈렛 (계란 2개, 시금치, 아몬드 슬라이스)
                    - 점심: 봄동 샐러드 (봄동, 훈제 닭가슴살, 아몬드, 발사믹 드레싱)
                    - 저녁: 시금치와 딸기를 곁들인 연어 구이 (구운 연어, 시금치, 신선한 딸기)
    
                    2일차:
                    ...
                    식단을 어떻게 구성했는지에 대한 설명
    
                    이 형식을 그대로 따라 작성해주세요.
                    """,
                    durationDays, ingredients, additionalConditions
            );
        } else {
            message = String.format(
                    """
                    기간: %d일
                    추가 조건: %s
                    위 정보를 기반으로 하루 세 끼의 식단을 구성하세요. 포함할 식재료는 모든 식사에 포함되지 않아도 괜찮습니다. 아래 예시 형식처럼 작성해주세요.
                    예시 형식:
                    3일 동안의 저속노화 식단을 아래와 같이 제안합니다.
    
                    1일차:
                    - 아침: 시금치 오믈렛 (계란 2개, 시금치, 아몬드 슬라이스)
                    - 점심: 봄동 샐러드 (봄동, 훈제 닭가슴살, 아몬드, 발사믹 드레싱)
                    - 저녁: 시금치와 딸기를 곁들인 연어 구이 (구운 연어, 시금치, 신선한 딸기)
    
                    2일차:
                    ...
                    식단을 어떻게 구성했는지에 대한 설명
    
                    이 형식을 그대로 따라 작성해주세요.
                    """,
                    durationDays, additionalConditions
            );
        }

        return createMessage(system, message);
    }

    // 프롬프트 생성 - 자유롭게 대화
    private List<Map<String, String>> promptFreetalk(String message) {
        String system = "당신은 저속노화 식단에 대한 전문 지식을 갖춘 AI입니다. 저속노화 식단을 실천하려는 사용자에게 간결하고 정확한 정보과 실용적인 조언을 제공합니다. 절대로 마크다운 문법을 사용하지 마십시오. 반드시 일반 텍스트 형식으로만 출력하십시오. 또한 질문에 간결히 대답하세요.";

        return createMessage(system, message);
    }

    // 레시피 식재료, 양념 구조를 문자열로 변환 (Map<String, String> -> ' '과 ', '로 join하기 ex. 소금 1T, 설탕 2T)
    private String formatMap(Map<String, String> map) {
        return map.entrySet().stream()
                .map(e -> e.getKey() + " " + e.getValue())
                .collect(Collectors.joining(", "));
    }

    // message 생성
    private List<Map<String, String>> createMessage(String system, String message) {
        return List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", message)
        );
    }
}