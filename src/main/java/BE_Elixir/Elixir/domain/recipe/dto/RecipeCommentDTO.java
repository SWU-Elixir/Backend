package BE_Elixir.Elixir.domain.recipe.dto;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeCommentDTO {
    private Long id;
    private Long recipeId;
    private String email;

    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RecipeCommentDTO(RecipeEvent recipeEvent) {
        this.id = recipeEvent.getId();
        this.recipeId = recipeEvent.getRecipe().getId(); // 직접 접근
        this.email = recipeEvent.getMember().getEmail();
        this.content = recipeEvent.getContent();
        this.createdAt = recipeEvent.getCreatedAt();
        this.updatedAt = recipeEvent.getUpdatedAt();
    }

}