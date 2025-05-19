package BE_Elixir.Elixir.domain.recommendation.dto;

import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RecommendationResponseDTO {
    private Long id;
    private String title;
    private String imageUrl;

    // 카테고리
    private CategorySlowAging categorySlowAging;
    private CategoryType categoryType;


    // 태그된 식재료 정보
    private List<Long> ingredientTagIds;

    // 현재 사용자가 스크랩을 눌렀는지
    private Boolean scrappedByCurrentUser;

    public RecommendationResponseDTO(Recipe recipe, boolean scrappedByCurrentUser) {
        this.id = recipe.getId();
        this.title = recipe.getTitle();
        this.imageUrl = recipe.getImageUrl();
        this.categorySlowAging = recipe.getCategorySlowAging();
        this.categoryType = recipe.getCategoryType();

        this.ingredientTagIds = recipe.getIngredientTags() != null
                ? recipe.getIngredientTags().stream()
                .map(tag -> tag.getIngredient().getId())
                .limit(3)
                .collect(Collectors.toList())
                : List.of();


        this.scrappedByCurrentUser = scrappedByCurrentUser;
    }
}
