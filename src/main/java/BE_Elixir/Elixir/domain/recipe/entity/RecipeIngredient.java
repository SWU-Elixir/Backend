package BE_Elixir.Elixir.domain.recipe.entity;


import jakarta.persistence.*;

// 중간테이블
@Entity
public class RecipeIngredient {
    @Id
    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Id
    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;
}
