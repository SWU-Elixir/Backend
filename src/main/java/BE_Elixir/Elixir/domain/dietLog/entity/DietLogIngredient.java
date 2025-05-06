package BE_Elixir.Elixir.domain.dietLog.entity;

import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class DietLogIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "diet_log_id", nullable = false)
    private DietLog dietLog;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    public DietLogIngredient(DietLog dietLog, Ingredient ingredient) {
        this.dietLog = dietLog;
        this.ingredient = ingredient;
    }

}
