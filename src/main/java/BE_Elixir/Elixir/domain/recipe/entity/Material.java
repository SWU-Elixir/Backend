package BE_Elixir.Elixir.domain.recipe.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Material {

    private String name;
    private String value;
    private String unit;

    public Material(String name, String value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }
}

