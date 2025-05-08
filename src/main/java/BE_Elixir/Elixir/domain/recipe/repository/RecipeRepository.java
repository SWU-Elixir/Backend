package BE_Elixir.Elixir.domain.recipe.repository;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("SELECT r FROM Recipe r " +
            "LEFT JOIN FETCH r.ingredientTags " +
            "WHERE r.id = :id")
    Optional<Recipe> findWithAllById(@Param("id") Long id);


    // 목록, 카테고리 별 조회
    Page<Recipe> findAll(Pageable pageable);
    Page<Recipe> findByCategoryType(CategoryType categoryType, Pageable pageable);

    Page<Recipe> findByCategorySlowAging(CategorySlowAging categorySlowAging, Pageable pageable);

    Page<Recipe> findByCategoryTypeAndCategorySlowAging(CategoryType categoryType, CategorySlowAging categorySlowAging, Pageable pageable);

    // 제목에 keyword가 포함된 레시피 찾기(검색 결과 조회)
    Page<Recipe> findByTitleContaining(String keyword, Pageable pageable);
    Page<Recipe> findByTitleContainingAndCategoryType(String keyword, CategoryType categoryType, Pageable pageable);
    Page<Recipe> findByTitleContainingAndCategorySlowAging(String keyword, CategorySlowAging categorySlowAging, Pageable pageable);
    Page<Recipe> findByTitleContainingAndCategoryTypeAndCategorySlowAging(String keyword, CategoryType categoryType, CategorySlowAging categorySlowAging, Pageable pageable);

    // 로그인한 사용자가 작성한 레시피 조회
    List<Recipe> findAllByMember(Member member);

    // 챌린지 목표 조건 확인 - 제철 식재료를 포함한 레시피 등록
    @Query("SELECT ri.ingredient.name " +
            "FROM Recipe r " +
            "JOIN r.ingredientTags ri " +
            "WHERE r.member.id = :memberId " +
            "AND r.createdAt >= :openedAt")
    List<String> findIngredientsByMemberIdAndTimeAfter(@Param("memberId") Long memberId, @Param("openedAt") LocalDateTime openedAt);
}


