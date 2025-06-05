package BE_Elixir.Elixir.domain.ingredient.dto;

import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientDTO {
    private Long id;
    private String name;
    private String categoryGroup;
    private String type;

    public IngredientDTO(Ingredient ingredient) {
        this.id = ingredient.getId();
        this.name = ingredient.getName();
        this.categoryGroup = ingredient.getCategoryGroup();

        if (ingredient.isNova4()) {
            this.type = "초가공식품";
        } else if (ingredient.getChallengeMonth() != null && ingredient.getChallengeMonth() == LocalDate.now().getMonthValue()) {
            this.type = "챌린지";
        } else {
            this.type = null;
        }
    }

}