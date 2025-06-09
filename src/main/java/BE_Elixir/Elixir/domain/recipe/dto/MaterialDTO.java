package BE_Elixir.Elixir.domain.recipe.dto;

import lombok.*;

@Getter
@Setter
public class MaterialDTO {
    private String name;
    private String value;
    private String unit;

    public MaterialDTO(String name, String value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }
}
