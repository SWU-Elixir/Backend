package BE_Elixir.Elixir.domain.ingredient.repository;

import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}
