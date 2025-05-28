package BE_Elixir.Elixir.domain.recipe.dto.response;

import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class RecipeSummaryResponse {
    private Long recipeId;
    private String imageUrl;
    private String title;
    private List<Long> ingredientTags;

    public static RecipeSummaryResponse from(Recipe recipe) {
        return new RecipeSummaryResponse(
                recipe.getId(),
                recipe.getImageUrl(),
                recipe.getTitle(),
                recipe.getIngredientTags().stream()
                        .map(tag -> tag.getIngredient().getId())
                        .collect(Collectors.toList())
        );
    }
}
