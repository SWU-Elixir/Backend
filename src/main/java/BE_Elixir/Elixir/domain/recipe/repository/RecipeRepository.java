package BE_Elixir.Elixir.domain.recipe.repository;

import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}
