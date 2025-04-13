package BE_Elixir.Elixir.domain.recipe.repository;

import BE_Elixir.Elixir.domain.recipe.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}
