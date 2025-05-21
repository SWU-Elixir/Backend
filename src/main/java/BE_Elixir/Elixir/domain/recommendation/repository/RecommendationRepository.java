package BE_Elixir.Elixir.domain.recommendation.repository;

import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recipe, Long> {
}
