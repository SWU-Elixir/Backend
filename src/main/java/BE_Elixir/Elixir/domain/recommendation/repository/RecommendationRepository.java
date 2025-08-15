package BE_Elixir.Elixir.domain.recommendation.repository;

import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import org.springframework.data.domain.Pageable;
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

    // categoryType(레시피 스타일), categorySlowAging(이유) 기준 필터링
    @Query("SELECT r FROM Recipe r " +
            "WHERE (:categoryTypes IS NULL OR r.categoryType IN :categoryTypes) " +
            "AND (:categorySlowAgings IS NULL OR r.categorySlowAging IN :categorySlowAgings)")
    List<Recipe> findByCategoryFilters(
            @Param("categoryTypes") List<CategoryType> categoryTypes,
            @Param("categorySlowAgings") List<CategorySlowAging> categorySlowAgings
    );

    // 랜덤 3개 레시피 (native query)
    @Query(value = "SELECT * FROM recipe ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<Recipe> findRandom3();
}

