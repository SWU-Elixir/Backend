package BE_Elixir.Elixir.domain.recipe.repository;

import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // RecipeRepository.java
    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.ingredientTags " +
            "WHERE r.id = :id")
    Optional<Recipe> findWithAllById(@Param("id") Long id);

}
