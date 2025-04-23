package BE_Elixir.Elixir.domain.recipe.dto;

import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class RecipeCommentResponseDTO {
    private Long commentId;
    private Long recipeId;
    private String email;

    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RecipeCommentResponseDTO(RecipeEvent recipeEvent) {
        this.commentId = recipeEvent.getId();
        this.recipeId = recipeEvent.getRecipe().getId(); // 직접 접근
        this.email = recipeEvent.getMember().getEmail();
        this.content = recipeEvent.getContent();
        this.createdAt = recipeEvent.getCreatedAt();
        this.updatedAt = recipeEvent.getUpdatedAt();
    }

}
