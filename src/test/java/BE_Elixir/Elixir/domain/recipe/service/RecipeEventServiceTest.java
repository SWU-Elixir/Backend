package BE_Elixir.Elixir.domain.recipe.service;

import BE_Elixir.Elixir.domain.achievement.service.MemberStatsService;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.MaterialDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeCommentCreateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeCommentResponseDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.Difficulty;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeEventService 테스트")
class RecipeEventServiceTest {
    @InjectMocks
    RecipeEventService recipeEventService;

    @Mock RecipeRepository recipeRepository;
    @Mock RecipeEventRepository recipeEventRepository;
    @Mock MemberStatsService memberStatsService;

    private RecipeRequestDTO requestDTO;
    private Recipe recipe;
    private Member member;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testUser")
                .build();

        pageable = PageRequest.of(0, 10);

        requestDTO = createSampleRecipeRequestDTO();

        recipe = Recipe.from(requestDTO, member);
        Ingredient ingredient = new Ingredient();
        ingredient.setId(10L);
        ingredient.setName("감자");

        RecipeIngredient recipeIngredient = new RecipeIngredient(recipe, ingredient);
        recipe.setIngredientTags(List.of(recipeIngredient));
    }

    private RecipeRequestDTO createSampleRecipeRequestDTO() {
        RecipeRequestDTO dto = new RecipeRequestDTO();
        dto.setTitle("감자조림");
        dto.setDescription("감자조림에 대한 설명");
        dto.setCategoryType(CategoryType.한식);
        dto.setCategorySlowAging(CategorySlowAging.항산화강화);
        dto.setDifficulty(Difficulty.보통);
        dto.setTimeHours(1);
        dto.setTimeMinutes(20);
        dto.setIngredientTagIds(List.of(10L));

        MaterialDTO ingredient = new MaterialDTO("감자", "2", "개");
        MaterialDTO seasoning1 = new MaterialDTO("간장", "2", "큰술");
        MaterialDTO seasoning2 = new MaterialDTO("설탕", "1", "큰술");
        dto.setIngredients(List.of(ingredient));
        dto.setSeasonings(List.of(seasoning1, seasoning2));

        dto.setStepDescriptions(List.of("1단계"));
        dto.setTips("팁");
        dto.setAllergies(List.of("우유", "밀"));

        return dto;
    }


    @Nested
    @DisplayName("댓글 등록")
    class AddCommentTests {

        @Test
        @DisplayName("성공: 댓글 등록")
        void addComment_Success() {
            RecipeCommentCreateRequestDTO requestDTO = new RecipeCommentCreateRequestDTO(1L, "내용");
            given(recipeRepository.findById(1L)).willReturn(Optional.of(recipe));

            RecipeEvent savedEvent = RecipeEvent.createRecipeComment(recipe, requestDTO, member);
            given(recipeEventRepository.save(any())).willReturn(savedEvent);

            RecipeCommentResponseDTO result = recipeEventService.addComment(requestDTO, member);

            assertEquals("내용", result.getContent());
            verify(recipeRepository).findById(1L);
            verify(recipeEventRepository).save(any());
        }

        @Test
        @DisplayName("예외: 존재하지 않는 레시피에 댓글 등록 시도")
        void addComment_Failure_RecipeNotFound() {
            RecipeCommentCreateRequestDTO requestDTO = new RecipeCommentCreateRequestDTO(1L, "내용");
            given(recipeRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> recipeEventService.addComment(requestDTO, member))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.RECIPE_NOT_FOUND.getMessage());

            verify(recipeEventRepository, never()).save(any());
        }
    }

}