package BE_Elixir.Elixir.domain.recipe.entity;

import jakarta.persistence.*;
import BE_Elixir.Elixir.global.enums.IngredientCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IngredientCategory category;
}
