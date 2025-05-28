package BE_Elixir.Elixir.domain.recipe.dto.request;

import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeCommentCreateRequestDTO {
    private Long recipeId;
    private String content;
}