package BE_Elixir.Elixir.domain.recipe.service;

import BE_Elixir.Elixir.domain.achievement.service.MemberStatsService;
import BE_Elixir.Elixir.domain.challenge.event.events.RecipeEvent;
import BE_Elixir.Elixir.domain.follow.repository.FollowRepository;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.domain.recipe.dto.MaterialDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeHomeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeResponseDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeService 테스트")
class RecipeServiceTest {
    @InjectMocks
    private RecipeService recipeService;

    @Mock private RecipeRepository recipeRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private RecipeEventRepository recipeEventRepository;
    @Mock private IngredientRepository ingredientRepository;
    @Mock private FollowRepository followRepository;
    @Mock private S3Service s3Service;
    @Mock private RedisRecipeService redisRecipeService;
    @Mock private MemberStatsService memberStatsService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private RecipeRequestDTO requestDTO;
    private MultipartFile mockMainImage;
    private List<MultipartFile> mockStepImages;
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

    @Nested
    @DisplayName("레시피 조회 관련 테스트")
    class RecipeQueryTests {

        @Test
        @DisplayName("레시피 단건 조회")
        void getRecipe() {
            // given
            Long recipeId = 1L;
            Recipe recipe = Recipe.from(createSampleRecipeRequestDTO(), member);
            recipe.setId(recipeId);
            recipe.setIngredientTags(new ArrayList<>());

            given(recipeRepository.findById(recipeId)).willReturn(Optional.of(recipe));

            // when
            RecipeResponseDTO result = recipeService.getRecipe(recipeId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("감자조림");
            verify(recipeRepository).findById(recipeId);
        }

        @Test
        @DisplayName("레시피 상세 조회")
        void getRecipeDetail() {
            // given
            Long recipeId = 1L;
            Recipe recipe = Recipe.from(createSampleRecipeRequestDTO(), member);
            recipe.setId(recipeId);
            recipe.setIngredientTags(new ArrayList<>());

            given(recipeRepository.findWithAllById(recipeId)).willReturn(Optional.of(recipe));
            given(followRepository.existsByFollowerAndFollowing(member, recipe.getMember())).willReturn(false);
            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndLikeFlagTrue(recipeId, member.getId())).willReturn(false);
            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(recipeId, member.getId())).willReturn(false);

            // when
            RecipeDetailResponseDTO result = recipeService.getRecipeDetail(recipeId, member);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("감자조림");
            verify(recipeRepository).findWithAllById(recipeId);
            verify(followRepository).existsByFollowerAndFollowing(member, recipe.getMember());
            verify(recipeEventRepository).existsByRecipeIdAndMemberIdAndLikeFlagTrue(recipeId, member.getId());
            verify(recipeEventRepository).existsByRecipeIdAndMemberIdAndScrapFlagTrue(recipeId, member.getId());
        }

        @Test
        @DisplayName("전체 레시피 조회")
        void getRecipeList() {
            // given
            Recipe recipe1 = Recipe.from(createSampleRecipeRequestDTO(), member);
            recipe1.setId(1L);
            recipe1.setIngredientTags(new ArrayList<>());

            Recipe recipe2 = Recipe.from(createSampleRecipeRequestDTO(), member);
            recipe2.setId(2L);
            recipe2.setTitle("비빔밥");
            recipe2.setIngredientTags(new ArrayList<>());

            List<Recipe> recipes = List.of(recipe1, recipe2);

            given(recipeRepository.findAll(pageable)).willReturn(new PageImpl<>(recipes));
            given(recipeEventRepository.findLikedRecipeIdsByMemberId(member.getId())).willReturn(List.of());
            given(recipeEventRepository.findScrappedRecipeIdsByMemberId(member.getId())).willReturn(List.of());

            // when
            Page<RecipeHomeResponseDTO> result = recipeService.getRecipeList(pageable, member);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("감자조림");
            verify(recipeRepository).findAll(pageable);
            verify(recipeEventRepository).findLikedRecipeIdsByMemberId(member.getId());
            verify(recipeEventRepository).findScrappedRecipeIdsByMemberId(member.getId());
        }

        @Test
        @DisplayName("카테고리별 레시피 목록 조회")
        void getRecipeListByCategory() {
            // given
            CategoryType categoryType = CategoryType.한식;
            CategorySlowAging categorySlowAging = CategorySlowAging.항산화강화;

            Long recipeId = 1L;
            Recipe recipe = Recipe.from(createSampleRecipeRequestDTO(), member);
            recipe.setId(recipeId);
            recipe.setIngredientTags(new ArrayList<>());

            List<Recipe> recipeList = List.of(recipe);
            Page<Recipe> recipePage = new PageImpl<>(recipeList);

            given(recipeRepository.findByCategoryTypeAndCategorySlowAging(categoryType, categorySlowAging, pageable))
                    .willReturn(recipePage);
            given(recipeEventRepository.findLikedRecipeIdsByMemberId(member.getId()))
                    .willReturn(List.of(1L));
            given(recipeEventRepository.findScrappedRecipeIdsByMemberId(member.getId()))
                    .willReturn(List.of());

            // when
            Page<RecipeHomeResponseDTO> result = recipeService.getRecipeListByCategory(categoryType, categorySlowAging, pageable, member);

            // then
            assertThat(result.getContent()).hasSize(1);
            RecipeHomeResponseDTO dto = result.getContent().get(0);
            assertThat(dto.getTitle()).isEqualTo("감자조림");
            assertThat(dto.getLikedByCurrentUser()).isTrue();
            assertThat(dto.getScrappedByCurrentUser()).isFalse();

            verify(recipeRepository).findByCategoryTypeAndCategorySlowAging(categoryType, categorySlowAging, pageable);
            verify(recipeEventRepository).findLikedRecipeIdsByMemberId(member.getId());
            verify(recipeEventRepository).findScrappedRecipeIdsByMemberId(member.getId());
        }

        @Test
        @DisplayName("로그인한 사용자가 작성한 레시피 10개 조회")
        void getMyRecipes() {
            // given
            Long recipeId = 1L;
            Recipe recipe = Recipe.from(createSampleRecipeRequestDTO(), member);
            recipe.setId(recipeId);
            recipe.setIngredientTags(new ArrayList<>());

            given(recipeRepository.findTopRecipesByUserId(any(Long.class), anyInt()))
                    .willReturn(List.of(recipe));

            // when
            List<RecipeSummaryResponse> result = recipeService.getMyRecipes(member, 10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("감자조림");
            verify(recipeRepository).findTopRecipesByUserId(any(Long.class), anyInt());
        }

        @Test
        @DisplayName("레시피 검색 결과 조회")
        void searchRecipe() {
            // given
            String keyword = "감자";
            CategoryType categoryType = null;
            CategorySlowAging categorySlowAging = null;
            Long recipeId = 1L;
            Recipe recipe = Recipe.from(createSampleRecipeRequestDTO(), member);
            recipe.setId(recipeId);
            recipe.setIngredientTags(new ArrayList<>());

            List<Recipe> recipeList = List.of(recipe);
            Page<Recipe> recipePage = new PageImpl<>(recipeList, pageable, recipeList.size());

            given(recipeRepository.findByTitleContaining(keyword, pageable)).willReturn(recipePage);
            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndLikeFlagTrue(recipe.getId(), member.getId())).willReturn(false);
            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(recipe.getId(), member.getId())).willReturn(false);

            // when
            Page<RecipeHomeResponseDTO> result = recipeService.searchRecipe(keyword, pageable, categoryType, categorySlowAging, member);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).contains("감자조림");
            verify(recipeRepository).findByTitleContaining(keyword, pageable);
        }
    }
}