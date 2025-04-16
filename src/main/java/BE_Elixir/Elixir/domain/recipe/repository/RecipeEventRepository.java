package BE_Elixir.Elixir.domain.recipe.repository;

import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;
public interface RecipeEventRepository extends JpaRepository<RecipeEvent, Long> {
    List<RecipeEvent> findAllByRecipeId(Long recipeId);

}

