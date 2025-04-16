package BE_Elixir.Elixir.domain.recipe.dto;

import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import BE_Elixir.Elixir.global.enums.IngredientCategory;
import lombok.*;

@Getter
@Setter
public class RecipeIngredientDTO {
    private Long id;
    private String name;
    private IngredientCategory category;

    public RecipeIngredientDTO(RecipeIngredient ingredient) {
        this.id = ingredient.getId();
        this.name = ingredient.getIngredient().getName(); // 연관된 식재료 이름
    }
}