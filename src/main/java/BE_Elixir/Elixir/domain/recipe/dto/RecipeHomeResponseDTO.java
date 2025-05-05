package BE_Elixir.Elixir.domain.recipe.dto;

import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.Difficulty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class RecipeHomeResponseDTO {
    private Long id;
    private String title;
    private String imageUrl;
    
    // 카테고리
    private CategorySlowAging categorySlowAging;
    private CategoryType categoryType;
    
    private Difficulty difficulty;
    // 통합 시간 필드
    private Integer totalTimeMinutes;
    
    // 태그된 식재료 정보
    private List<String> ingredientTags;

    private Integer likes;
    private Boolean likedByCurrentUser; // 현재 사용자가 좋아요를 눌렀는지
    private Boolean scrappedByCurrentUser; // 현재 사용자가 스크랩을 눌렀는지

    public RecipeHomeResponseDTO(Recipe recipe, boolean likedByCurrentUser, boolean scrappedByCurrentUser) {
        this.id = recipe.getId();
        this.title = recipe.getTitle();
        this.imageUrl = recipe.getImageUrl();
        this.categorySlowAging = recipe.getCategorySlowAging();
        this.categoryType = recipe.getCategoryType();
        this.difficulty = recipe.getDifficulty();

        // 시간 계산: (시 * 60) + 분
        Integer hours = recipe.getTimeHours() != null ? recipe.getTimeHours() : 0;
        Integer minutes = recipe.getTimeMinutes() != null ? recipe.getTimeMinutes() : 0;
        this.totalTimeMinutes = hours * 60 + minutes;

        this.ingredientTags = recipe.getIngredientTags().stream()
                .map(recipeIngredient -> recipeIngredient.getIngredient().getName())  // Ingredient 안의 name
                .limit(5) // 식재료 태그 5개로 제한
                .collect(Collectors.toList());
        this.likes = recipe.getLikes();
        this.likedByCurrentUser = likedByCurrentUser;
        this.scrappedByCurrentUser = scrappedByCurrentUser;
    }
}
