package BE_Elixir.Elixir.domain.recipe.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeCommentDTO {
    private Long recipeId;
    private Long memberId; // 나중엔 Member로 바꾸기
    private String content;
    private LocalDateTime createdAt;
}