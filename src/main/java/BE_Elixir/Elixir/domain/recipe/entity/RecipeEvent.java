package BE_Elixir.Elixir.domain.recipe.entity;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentCreateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentUpdateRequestDTO;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

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

    public static RecipeEvent createRecipeComment(Recipe recipe, RecipeCommentCreateRequestDTO dto, Member member) {
        RecipeEvent comment = new RecipeEvent();
        comment.setRecipe(recipe);
        comment.setMember(member);
        comment.setCommentFlag(true);
        comment.setContent(dto.getContent());
        return comment;
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now(); // 변경 시간도 업데이트
    }

}
