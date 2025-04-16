package BE_Elixir.Elixir.domain.recipe.entity;

import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
// 댓글, 좋아요, 스크랩 통합
public class RecipeEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne // 일단 로그인 없이 진행 → 나중에 Member로 수정
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    private boolean likeFlag;
    private boolean scrapFlag;
    private boolean commentFlag;

    @Column(length = 200)
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static RecipeEvent createRecipeComment(Recipe recipe, RecipeCommentDTO dto) {
        RecipeEvent comment = new RecipeEvent();
        comment.setRecipe(recipe);
        comment.setMemberId(dto.getMemberId());
        comment.setCommentFlag(true);
        comment.setContent(dto.getContent());
        comment.setCreatedAt(dto.getCreatedAt());
        return comment;
    }
}
