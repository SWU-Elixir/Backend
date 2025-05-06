package BE_Elixir.Elixir.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipeImageResponseDTO {
    private Long recipeId;
    private String imageUrl;
}
