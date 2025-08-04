package BE_Elixir.Elixir.domain.recipe.service;

import BE_Elixir.Elixir.domain.achievement.service.MemberStatsService;
import BE_Elixir.Elixir.domain.challenge.event.events.RecipeEvent;
import BE_Elixir.Elixir.domain.follow.repository.FollowRepository;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.MaterialDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeHomeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeSummaryResponse;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.Difficulty;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.redis.RedisRecipeService;
import BE_Elixir.Elixir.global.s3.S3Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeService 테스트")
class RecipeServiceTest {
    @InjectMocks
    private RecipeService recipeService;

    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeEventRepository recipeEventRepository;
    @Mock private IngredientRepository ingredientRepository;
    @Mock private FollowRepository followRepository;
    @Mock private S3Service s3Service;
    @Mock private RedisRecipeService redisRecipeService;
    @Mock private MemberStatsService memberStatsService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private Member member;
    private RecipeRequestDTO requestDTO;
    private MultipartFile mockMainImage;
    private List<MultipartFile> mockStepImages;
    private Recipe recipe;


    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testUser")
                .build();

        requestDTO = createSampleRecipeRequestDTO();
    }

    private RecipeRequestDTO createSampleRecipeRequestDTO() {
        RecipeRequestDTO dto = new RecipeRequestDTO();
        dto.setTitle("제목");
        dto.setDescription("설명");
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
    @DisplayName("레시피 등록 테스트")
    class createRecipe {

        @Test
        @DisplayName("성공: 대표 이미지와 단계 이미지 포함")
        void should_CreateRecipeSuccessfully_WithImages() throws IOException {
            // given
            MockMultipartFile mainImage = new MockMultipartFile("image", "main.jpg", "image/jpeg", "main".getBytes());
            MockMultipartFile stepImage = new MockMultipartFile("step", "step1.jpg", "image/jpeg", "step".getBytes());

            given(s3Service.upload(mainImage, "recipe/main")).willReturn("main-url");
            given(s3Service.upload(stepImage, "recipe/steps")).willReturn("step-url");

            Ingredient ingredient = mock(Ingredient.class);
            given(ingredient.getName()).willReturn("감자");
            given(ingredientRepository.findById(10L)).willReturn(Optional.of(ingredient));

            given(recipeRepository.save(any(Recipe.class))).willAnswer(invocation -> {
                Recipe r = invocation.getArgument(0);
                if (r.getId() == null) {
                    r.setId(100L);
                }
                return r;
            });
            given(followRepository.existsByFollowerAndFollowing(any(), any())).willReturn(false);
            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndLikeFlagTrue(anyLong(), anyLong()))
                    .willReturn(false);
            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(anyLong(), anyLong()))
                    .willReturn(false);

            // when
            RecipeDetailResponseDTO response = recipeService.createRecipe(requestDTO, mainImage, List.of(stepImage), member);

            // then
            assertThat(response).isNotNull();
            verify(s3Service).upload(mainImage, "recipe/main");
            verify(s3Service).upload(stepImage, "recipe/steps");
            verify(ingredientRepository).findById(10L);
            verify(recipeRepository).save(any());
            verify(memberStatsService).increaseStat(eq(member.getId()), eq(AchievementType.TOTAL_RECIPE_LOGS), eq(1));
            verify(eventPublisher).publishEvent(any(RecipeEvent.class));
        }

        @Test
        @DisplayName("성공: 모든 이미지 없이 레시피 등록")
        void should_CreateRecipeSuccessfully_WithoutAnyImages() throws IOException {
            // given
            given(ingredientRepository.findById(10L)).willReturn(Optional.of(mock(Ingredient.class)));
            given(recipeRepository.save(any(Recipe.class))).willAnswer(invocation -> {
                Recipe savedRecipe = invocation.getArgument(0);
                savedRecipe.setId(100L);
                return savedRecipe;
            });
            given(followRepository.existsByFollowerAndFollowing(any(), any())).willReturn(false);
            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndLikeFlagTrue(anyLong(), anyLong())).willReturn(false);
            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(anyLong(), anyLong())).willReturn(false);

            // when
            RecipeDetailResponseDTO response = recipeService.createRecipe(requestDTO, null, null, member);

            // then
            assertThat(response).isNotNull();
            verify(recipeRepository).save(any());
            verify(s3Service, never()).upload(any(), any());  // 이미지 업로드 호출 안됨
        }

        @Test
        @DisplayName("예외: 대표 이미지 S3 업로드 실패")
        void should_ThrowException_When_MainImageUploadFails() throws IOException {
            // given
            MockMultipartFile mainImage = new MockMultipartFile("image", "main.jpg", "image/jpeg", "main".getBytes());
            given(s3Service.upload(mainImage, "recipe/main")).willThrow(new IOException());

            // when & then
            assertThatThrownBy(() -> recipeService.createRecipe(requestDTO, mainImage, null, member))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.S3_UPLOAD_ERROR);

            verify(s3Service).upload(mainImage, "recipe/main");
            verify(ingredientRepository, never()).findById(any());
            verify(recipeRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외: 단계 별 이미지 S3 업로드 실패")
        void should_ThrowException_When_StepImageUploadFails() throws IOException {
            // given
            MockMultipartFile stepImage = new MockMultipartFile("step", "step1.jpg", "image/jpeg", "step".getBytes());

            given(s3Service.upload(stepImage, "recipe/steps")).willThrow(new IOException());

            // when & then
            assertThatThrownBy(() -> recipeService.createRecipe(requestDTO, null, List.of(stepImage), member))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.S3_UPLOAD_ERROR);

            verify(s3Service).upload(stepImage, "recipe/steps");
            verify(recipeRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외: ingredientTagIds 에 해당하는 재료가 없음")
        void should_ThrowException_When_IngredientNotFound() {
            // given
            given(ingredientRepository.findById(10L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recipeService.createRecipe(requestDTO, null, null, member))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INGREDIENT_NOT_FOUND);

            verify(ingredientRepository).findById(10L);
            verify(recipeRepository, never()).save(any());
        }
    }

    @Test
    void getRecipe() {
    }

    @Test
    void getRecipeDetail() {
    }

    @Test
    void getRecipeList() {
    }

    @Test
    void getRecipeListByCategory() {
    }

    @Test
    void searchRecipe() {
    }

    @Test
    void getPopularSearchKeywords() {
    }

    @Test
    void saveSearchKeyword() {
    }

    @Test
    void updateRecipe() {
    }

    @Test
    void deleteRecipe() {
    }

    @Test
    void getMyRecipes() {
    }
}