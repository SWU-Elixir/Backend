package BE_Elixir.Elixir.domain.recipe.repository;

import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.ingredientTags " +
            "WHERE r.id = :id")
    Optional<Recipe> findWithAllById(@Param("id") Long id);


    Page<Recipe> findAll(Pageable pageable);
    Page<Recipe> findByCategoryType(CategoryType categoryType, Pageable pageable);

    Page<Recipe> findByCategorySlowAging(CategorySlowAging categorySlowAging, Pageable pageable);

    Page<Recipe> findByCategoryTypeAndCategorySlowAging(CategoryType categoryType, CategorySlowAging categorySlowAging, Pageable pageable);

    // 제목에 keyword가 포함된 레시피 찾기
    Page<Recipe> findByTitleContaining(String keyword, Pageable pageable);

}

