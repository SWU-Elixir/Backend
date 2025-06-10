package BE_Elixir.Elixir.domain.recipe.dto.response;

import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class RecipeCommentResponseDTO {
    private Long commentId;
    private Long recipeId;
    private String nickName; // 댓글 작성자의 닉네임
    private String title; // 댓글 작성자의 칭호
    private String authorProfileUrl; // 댓글 작성자의 프로필이미지

    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RecipeCommentResponseDTO(RecipeEvent recipeEvent) {
        this.commentId = recipeEvent.getId();
        this.recipeId = recipeEvent.getRecipe().getId(); // 직접 접근
        this.nickName = recipeEvent.getMember().getNickname();
        this.title = recipeEvent.getMember().getTitle();
        this.authorProfileUrl = recipeEvent.getMember().getProfileUrl();
        this.content = recipeEvent.getContent();
        this.createdAt = recipeEvent.getCreatedAt();
        this.updatedAt = recipeEvent.getUpdatedAt();
    }

}
