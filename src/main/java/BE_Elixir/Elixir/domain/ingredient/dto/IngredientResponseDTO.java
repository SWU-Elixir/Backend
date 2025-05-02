package BE_Elixir.Elixir.domain.ingredient.dto;

import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientResponseDTO {
    private Long id;
    private String name;

    public IngredientResponseDTO(Ingredient ingredient) {
        this.id = ingredient.getId();
        this.name = ingredient.getName();
    }
}