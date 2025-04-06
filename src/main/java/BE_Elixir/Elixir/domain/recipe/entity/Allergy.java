package BE_Elixir.Elixir.domain.recipe.entity;

import jakarta.persistence.*;
import BE_Elixir.Elixir.global.enums.AllergyType;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Allergy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알러지 타입 ENUM
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllergyType allergyType;
}
