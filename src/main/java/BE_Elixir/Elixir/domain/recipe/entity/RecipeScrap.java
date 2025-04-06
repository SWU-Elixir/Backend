package BE_Elixir.Elixir.domain.recipe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RecipeScrap {

    // Member 엔티티 생성 이후에 수정
    // @Id
    // @ManyToOne
    // @JoinColumn(name = "member_id", nullable = false)
    // private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;
}

