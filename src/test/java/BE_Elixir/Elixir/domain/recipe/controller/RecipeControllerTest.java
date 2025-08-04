package BE_Elixir.Elixir.domain.recipe.controller;

import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeHomeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Material;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.junit.jupiter.api.Test;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.domain.recipe.service.RecipeService;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.Difficulty;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
@MockitoBean(types = {RecipeService.class, RecipeRepository.class})
public class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RecipeService recipeService;

    private Member createMockMember() {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(1L);
        return member;
    }

    // 인증 객체
    private void setAuthentication() {
        Member mockMember = createMockMember();
        MemberDetails memberDetails = new MemberDetails(mockMember);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(memberDetails, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Recipe createMockRecipe() {
        Recipe recipe = mock(Recipe.class);
        Member mockMember = createMockMember();

        when(recipe.getMember()).thenReturn(mockMember);
        when(recipe.getId()).thenReturn(1L);
        when(recipe.getTitle()).thenReturn("테스트 레시피");
        when(recipe.getImageUrl()).thenReturn("https://example.com/image.jpg");
        when(recipe.getDescription()).thenReturn("테스트 설명");
        when(recipe.getCategorySlowAging()).thenReturn(CategorySlowAging.항산화강화);
        when(recipe.getCategoryType()).thenReturn(CategoryType.한식);
        when(recipe.getDifficulty()).thenReturn(Difficulty.보통);
        when(recipe.getTimeHours()).thenReturn(1);
        when(recipe.getTimeMinutes()).thenReturn(30);

        RecipeIngredient mockTag = mock(RecipeIngredient.class);
        Ingredient mockIngredient = mock(Ingredient.class);
        when(mockIngredient.getId()).thenReturn(1L);
        when(mockTag.getIngredient()).thenReturn(mockIngredient);
        when(recipe.getIngredientTags()).thenReturn(List.of(mockTag));

        Material mockMaterial = mock(Material.class);
        when(mockMaterial.getName()).thenReturn("재료/양념 1");
        when(mockMaterial.getValue()).thenReturn("100");
        when(mockMaterial.getUnit()).thenReturn("g");
        when(recipe.getIngredients()).thenReturn(List.of(mockMaterial));
        when(recipe.getSeasonings()).thenReturn(List.of(mockMaterial));

        when(recipe.getStepDescriptions()).thenReturn(List.of("1단계 설명"));
        when(recipe.getStepImageUrls()).thenReturn(List.of("https://example.com/step1.jpg"));
        when(recipe.getTips()).thenReturn("팁");
        when(recipe.getLikes()).thenReturn(10);
        when(recipe.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(1));
        when(recipe.getUpdatedAt()).thenReturn(LocalDateTime.now());
        when(recipe.getAllergy_우유()).thenReturn(true);
        when(recipe.getAllergy_밀()).thenReturn(true);
        return recipe;
    }

    @Test
    @DisplayName("레시피_등록")
    void createRecipe() throws Exception {
    }

    @Test
    @DisplayName("레시피_상세_조회")
    void getRecipe() throws Exception {
        // given
        String url = "/api/recipe/1";
        setAuthentication();
        Recipe recipe = createMockRecipe();

        RecipeDetailResponseDTO dto = new RecipeDetailResponseDTO(
                recipe,
                true,
                false,
                false
        );

        given(recipeService.getRecipeDetail(eq(1L), any())).willReturn(dto);

        // when - API 호출
        final ResultActions result = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("테스트 레시피"))
                .andExpect(jsonPath("$.data.likes").value(10 ))
                .andExpect(jsonPath("$.data.ingredients[0].name").value("재료/양념 1"))
                .andExpect(jsonPath("$.data.allergies").value(org.hamcrest.Matchers.containsInAnyOrder("우유", "밀")));
    }

    @Test
    @DisplayName("전체_레시피_목록_조회")
    void getRecipes() throws Exception {
        // given
        String url = "/api/recipe";
        setAuthentication();

        RecipeHomeResponseDTO dto1 = mock(RecipeHomeResponseDTO.class);
        RecipeHomeResponseDTO dto2 = mock(RecipeHomeResponseDTO.class);
        List<RecipeHomeResponseDTO> content = List.of(dto1, dto2);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<RecipeHomeResponseDTO> page = new PageImpl<>(content, pageable, content.size());
        given(recipeService.getRecipeList(any(Pageable.class), any())).willReturn(page);

        // when
        ResultActions result = mockMvc.perform(get(url)
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getSearchRecipe() {
    }

    @Test
    void getSearchKeyword() {
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