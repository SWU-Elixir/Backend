package BE_Elixir.Elixir.domain.recipe.dto.request;

import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeCommentUpdateRequestDTO {
    private Long commentId;
    private Long recipeId;
    private String content;
}