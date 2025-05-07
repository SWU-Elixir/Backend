package BE_Elixir.Elixir.domain.ingredient.dto;

import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeIngredientDTO {
    private Long id;
    private String name;
    private int month;

    public ChallengeIngredientDTO(Ingredient ingredient) {
        this.id = ingredient.getId();
        this.name = ingredient.getName();
        this.month = ingredient.getChallengeMonth();
    }
}