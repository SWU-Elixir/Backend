package BE_Elixir.Elixir.domain.recommendation.repository;

import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recipe, Long> {

    @Query("""
        SELECT r FROM Recipe r 
        WHERE (:recipeStyles IS NULL OR r.categoryType IN :recipeStyles)
        AND (:reasons IS NULL OR r.categorySlowAging IN :reasons)
    """)
    List<Recipe> findFilteredRecipes(
            @Param("recipeStyles") List<String> recipeStyles,
            @Param("reasons") List<String> reasons
    );
}
