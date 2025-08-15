package BE_Elixir.Elixir.domain.recommendation.service;

import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.MaterialDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.domain.recommendation.dto.RecommendationResponseDTO;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.Difficulty;
import BE_Elixir.Elixir.global.redis.RedisRecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService 테스트")
class RecommendationServiceTest {
    @InjectMocks
    RecommendationService recommendationService;

    @Mock RecipeRepository recipeRepository;
    @Mock RedisRecipeService redisRecipeService;
    @Mock RecipeEventRepository recipeEventRepository;
    @Mock IngredientRepository ingredientRepository;

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

        member.setMealStyle_채소위주(true);
        member.setAllergy_밀(true);
        member.setRecipeStyle_한식(true);
        member.setReason_항산화강화(true);

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
    @DisplayName("사용자 맞춤형 레시피 추천 테스트")
    class GetRecommendationsForUserTests {

        @Test
        @DisplayName("성공: 캐시에서 추천 결과가 존재하면 캐시 반환")
        void returnsCachedRecommendations() {
            // given
            List<RecommendationResponseDTO> cached = List.of(
                    new RecommendationResponseDTO(new Recipe(), false)
            );
            given(redisRecipeService.getCachedRecommendations(String.valueOf(member.getId()))).willReturn(cached);

            // when
            List<RecommendationResponseDTO> result = recommendationService.getRecommendationsForUser(member);

            // then
            assertThat(result).isEqualTo(cached);
            then(redisRecipeService).should(times(1)).getCachedRecommendations(String.valueOf(member.getId()));
            then(recipeRepository).should(never()).findAll();
        }

        @Test
        @DisplayName("성공: 캐시 없고, 필터링 후 추천 결과 반환 및 캐싱")
        void returnsFilteredRecommendationsAndCaches() {
            // given
            given(redisRecipeService.getCachedRecommendations(String.valueOf(member.getId()))).willReturn(null);

            Recipe recipe1 = Mockito.mock(Recipe.class);
            given(recipe1.getId()).willReturn(10L);
            given(recipe1.getAllergyList()).willReturn(List.of("우유"));
            given(recipe1.getIngredientTags()).willReturn(List.of());
            given(recipe1.getCategoryType()).willReturn(CategoryType.한식);
            given(recipe1.getCategorySlowAging()).willReturn(CategorySlowAging.항산화강화);

            List<Recipe> allRecipes = List.of(recipe1);
            given(recipeRepository.findAll()).willReturn(allRecipes);

            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(10L, member.getId())).willReturn(true);

            // when
            List<RecommendationResponseDTO> result = recommendationService.getRecommendationsForUser(member);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getScrappedByCurrentUser()).isTrue();

            then(redisRecipeService).should().cacheRecommendations(eq(String.valueOf(member.getId())), anyList(), eq(Duration.ofHours(1)));
        }

        @Test
        @DisplayName("성공: 필터링된 결과 없으면 전체 중 랜덤 3개 추천")
        void returnsRandomWhenNoFiltered() {
            // given
            given(redisRecipeService.getCachedRecommendations(String.valueOf(member.getId()))).willReturn(null);

            Recipe recipe1 = new Recipe();
            recipe1.setCategoryType(CategoryType.한식);
            recipe1.setCategorySlowAging(CategorySlowAging.항산화강화);
            recipe1.setIngredientTags(new ArrayList<>());

            Recipe recipe2 = new Recipe();
            recipe2.setCategoryType(CategoryType.중식);
            recipe2.setCategorySlowAging(CategorySlowAging.항산화강화);
            recipe2.setIngredientTags(new ArrayList<>());

            Recipe recipe3 = new Recipe();
            recipe3.setCategoryType(CategoryType.디저트);
            recipe3.setCategorySlowAging(CategorySlowAging.혈당조절);
            recipe3.setIngredientTags(new ArrayList<>());

            Recipe recipe4 = new Recipe();
            recipe4.setCategoryType(CategoryType.양식);
            recipe4.setCategorySlowAging(CategorySlowAging.혈당조절);
            recipe4.setIngredientTags(new ArrayList<>());
            List<Recipe> allRecipes = List.of(recipe1, recipe2, recipe3, recipe4);

            given(recipeRepository.findAll()).willReturn(allRecipes);

            // when
            List<RecommendationResponseDTO> result = recommendationService.getRecommendationsForUser(member);

            // then
            assertThat(result).hasSizeLessThanOrEqualTo(3);
            then(redisRecipeService).should().cacheRecommendations(eq(String.valueOf(member.getId())), anyList(), eq(Duration.ofHours(1)));
        }

        @Test
        @DisplayName("예외: 캐시 조회 후 결과가 null 이거나 비어있으면 빈 리스트 반환")
        void getRecommendedKeywords_ReturnsEmptyWhenCacheEmpty() {
            // given
            given(redisRecipeService.getCachedRecommendations(String.valueOf(member.getId()))).willReturn(null);

            // when
            List<String> keywords = recommendationService.getRecommendedKeywords(member);

            // then
            assertThat(keywords).isEmpty();
        }
    }


    @Nested
    @DisplayName("추천 검색어 조회 테스트")
    class GetRecommendedKeywordsTests {

        @Test
        @DisplayName("성공: 캐시된 추천 결과가 있고, 키워드 및 식재료 이름 포함 반환")
        void returnsKeywordsFromCachedRecommendations() {
            // given
            RecommendationResponseDTO dto = Mockito.mock(RecommendationResponseDTO.class);
            given(dto.getTitle()).willReturn("감자조림은 맛있다");
            given(dto.getIngredientTagIds()).willReturn(List.of(1L, 2L));

            given(redisRecipeService.getCachedRecommendations(String.valueOf(member.getId())))
                    .willReturn(List.of(dto));

            Ingredient ing1 = new Ingredient();
            ing1.setName("감자");
            Ingredient ing2 = new Ingredient();
            ing2.setName("고추");

            given(ingredientRepository.findByIdIn(List.of(1L, 2L))).willReturn(List.of(ing1, ing2));

            // when
            List<String> keywords = recommendationService.getRecommendedKeywords(member);

            // then
            assertThat(keywords).contains("감자조림", "맛있다", "감자", "고추");
            assertThat(keywords.size()).isLessThanOrEqualTo(5);
        }
    }

}