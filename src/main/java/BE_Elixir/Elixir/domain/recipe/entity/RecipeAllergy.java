package BE_Elixir.Elixir.domain.recipe.entity;

import jakarta.persistence.*;

@Entity
public class RecipeAllergy {

    @Id
    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Id
    @ManyToOne
    @JoinColumn(name = "allergy_id", nullable = false)
    private Allergy allergy;
}
