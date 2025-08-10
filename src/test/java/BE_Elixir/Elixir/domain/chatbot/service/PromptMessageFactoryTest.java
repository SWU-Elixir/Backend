package BE_Elixir.Elixir.domain.chatbot.service;

import BE_Elixir.Elixir.domain.chatbot.dto.ChatbotRequestDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogResponseDTO;
import BE_Elixir.Elixir.domain.dietLog.service.DietLogService;
import BE_Elixir.Elixir.domain.ingredient.dto.ChallengeIngredientDTO;
import BE_Elixir.Elixir.domain.ingredient.service.IngredientService;
import BE_Elixir.Elixir.domain.recipe.dto.MaterialDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.service.RecipeService;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromptMessageFactory 단위 테스트")
class PromptMessageFactoryTest {

    @InjectMocks
    private PromptMessageFactory promptMessageFactory;

    @Mock private DietLogService dietLogService;
    @Mock private RecipeService recipeService;
    @Mock private IngredientService ingredientService;

    @Test
    @DisplayName("성공: DIET_FEEDBACK 타입으로 메시지를 생성한다")
    void createDietFeedbackMessage() {
        // given
        Long targetId = 1L;
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("DIET_FEEDBACK")
                .targetId(targetId)
                .build();

        DietLogResponseDTO dietLogResponse = DietLogResponseDTO.builder()
                .name("아침 식단")
                .type("아침")
                .ingredientTagId(List.of(1L, 2L))
                .build();

        given(dietLogService.getDietLog(targetId)).willReturn(dietLogResponse);
        given(ingredientService.getIngredientNamesByIds(any()))
                .willReturn(List.of("시금치", "계란"));

        // when
        List<Map<String, String>> result = promptMessageFactory.create(dto);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0))
                .containsEntry("role", "system")
                .containsKey("content");
        assertThat(result.get(1))
                .containsEntry("role", "user")
                .containsKey("content");

        String userContent = result.get(1).get("content");
        assertThat(userContent)
                .contains("아침 식단")
                .contains("아침")
                .contains("시금치, 계란");
    }

    @Test
    @DisplayName("성공: RECIPE_FEEDBACK 타입으로 메시지를 생성한다")
    void createRecipeFeedbackMessage() {
        // given
        Long targetId = 1L;
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("RECIPE_FEEDBACK")
                .targetId(targetId)
                .build();

        // Mock 객체를 이용한 방식
        RecipeResponseDTO recipeResponse = mock(RecipeResponseDTO.class);
        when(recipeResponse.getTitle()).thenReturn("시금치 오믈렛");
        when(recipeResponse.getDescription()).thenReturn("건강한 저속노화 레시피");
        when(recipeResponse.getCategorySlowAging()).thenReturn(CategorySlowAging.valueOf("혈당조절"));
        when(recipeResponse.getCategoryType()).thenReturn(CategoryType.valueOf("양식"));
        when(recipeResponse.getIngredients()).thenReturn(List.of(
                new MaterialDTO("시금치", "100", "g"),
                new MaterialDTO("계란", "2", "개")
        ));
        when(recipeResponse.getSeasonings()).thenReturn(List.of(
                new MaterialDTO("소금", "1", "T")
        ));
        when(recipeResponse.getStepDescriptions()).thenReturn(List.of("시금치를 씻는다", "계란을 푼다", "오믈렛을 만든다"));
        when(recipeResponse.getAllergies()).thenReturn(List.of("계란"));
        when(recipeResponse.getTips()).thenReturn("중불에서 조리하세요");

        given(recipeService.getRecipe(targetId)).willReturn(recipeResponse);

        // when
        List<Map<String, String>> result = promptMessageFactory.create(dto);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0))
                .containsEntry("role", "system")
                .containsKey("content");
        assertThat(result.get(1))
                .containsEntry("role", "user")
                .containsKey("content");

        String userContent = result.get(1).get("content");
        assertThat(userContent)
                .contains("시금치 오믈렛")
                .contains("건강한 저속노화 레시피")
                .contains("혈당조절")
                .contains("양식")
                .contains("시금치 100g, 계란 2개")
                .contains("소금 1T");
    }

    @Test
    @DisplayName("성공: RECOMMEND 타입으로 메시지를 생성한다 - 챌린지 재료 포함")
    void createRecommendMessageWithChallengeIngredients() {
        // given
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("RECOMMEND")
                .durationDays(3)
                .includeChallengeIngredients(true)
                .additionalConditions("저염식")
                .build();

        given(ingredientService.getChallengeIngredients())
                .willReturn(List.of(
                        ChallengeIngredientDTO.builder().name("시금치").build(),
                        ChallengeIngredientDTO.builder().name("브로콜리").build()
                ));

        // when
        List<Map<String, String>> result = promptMessageFactory.create(dto);

        // then
        assertThat(result).hasSize(2);
        String userContent = result.get(1).get("content");
        assertThat(userContent)
                .contains("기간: 3일")
                .contains("포함할 식재료: 시금치, 브로콜리")
                .contains("추가 조건: 저염식");
    }

    @Test
    @DisplayName("성공: RECOMMEND 타입으로 메시지를 생성한다 - 챌린지 재료 미포함")
    void createRecommendMessageWithoutChallengeIngredients() {
        // given
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("RECOMMEND")
                .durationDays(5)
                .includeChallengeIngredients(false)
                .additionalConditions("고단백")
                .build();

        // when
        List<Map<String, String>> result = promptMessageFactory.create(dto);

        // then
        assertThat(result).hasSize(2);
        String userContent = result.get(1).get("content");
        assertThat(userContent)
                .contains("기간: 5일")
                .contains("추가 조건: 고단백")
                .doesNotContain("포함할 식재료:");
    }

    @Test
    @DisplayName("성공: FREETALK 타입으로 메시지를 생성한다")
    void createFreetalkMessage() {
        // given
        String message = "저속노화 식단에 대해 알려주세요";
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("FREETALK")
                .message(message)
                .build();

        // when
        List<Map<String, String>> result = promptMessageFactory.create(dto);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0))
                .containsEntry("role", "system")
                .containsKey("content");
        assertThat(result.get(1))
                .containsEntry("role", "user")
                .containsEntry("content", message);
    }

    @Test
    @DisplayName("실패: DTO가 null인 경우 예외가 발생한다")
    void throwExceptionWhenDtoIsNull() {
        // when & then
        assertThatThrownBy(() -> promptMessageFactory.create(null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("실패: 타입이 null인 경우 예외가 발생한다")
    void throwExceptionWhenTypeIsNull() {
        // given
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type(null)
                .build();

        // when & then
        assertThatThrownBy(() -> promptMessageFactory.create(dto))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("실패: 정의되지 않은 타입인 경우 예외가 발생한다")
    void throwExceptionWhenTypeIsUndefined() {
        // given
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("INVALID_TYPE")
                .build();

        // when & then
        assertThatThrownBy(() -> promptMessageFactory.create(dto))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("실패: DIET_FEEDBACK 타입에 targetId가 없으면 예외가 발생한다")
    void throwExceptionWhenDietFeedbackMissingTargetId() {
        // given
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("DIET_FEEDBACK")
                .targetId(null)
                .build();

        // when & then
        assertThatThrownBy(() -> promptMessageFactory.create(dto))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("실패: RECIPE_FEEDBACK 타입에 targetId가 없으면 예외가 발생한다")
    void throwExceptionWhenRecipeFeedbackMissingTargetId() {
        // given
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("RECIPE_FEEDBACK")
                .targetId(null)
                .build();

        // when & then
        assertThatThrownBy(() -> promptMessageFactory.create(dto))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("실패: RECOMMEND 타입에 durationDays가 없으면 예외가 발생한다")
    void throwExceptionWhenRecommendMissingDurationDays() {
        // given
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("RECOMMEND")
                .durationDays(null)
                .includeChallengeIngredients(true)
                .additionalConditions("저염식")
                .build();

        // when & then
        assertThatThrownBy(() -> promptMessageFactory.create(dto))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("실패: RECOMMEND 타입에 includeChallengeIngredients가 없으면 예외가 발생한다")
    void throwExceptionWhenRecommendMissingIncludeChallengeIngredients() {
        // given
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("RECOMMEND")
                .durationDays(3)
                .includeChallengeIngredients(null)
                .additionalConditions("저염식")
                .build();

        // when & then
        assertThatThrownBy(() -> promptMessageFactory.create(dto))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("실패: RECOMMEND 타입에 additionalConditions가 없으면 예외가 발생한다")
    void throwExceptionWhenRecommendMissingAdditionalConditions() {
        // given
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("RECOMMEND")
                .durationDays(3)
                .includeChallengeIngredients(true)
                .additionalConditions(null)
                .build();

        // when & then
        assertThatThrownBy(() -> promptMessageFactory.create(dto))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("실패: FREETALK 타입에 message가 없으면 예외가 발생한다")
    void throwExceptionWhenFreetalkMissingMessage() {
        // given
        ChatbotRequestDTO dto = ChatbotRequestDTO.builder()
                .type("FREETALK")
                .message(null)
                .build();

        // when & then
        assertThatThrownBy(() -> promptMessageFactory.create(dto))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }
}