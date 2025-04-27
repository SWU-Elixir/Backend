package BE_Elixir.Elixir.domain.recipe.repository;

import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;
public interface RecipeEventRepository extends JpaRepository<RecipeEvent, Long> {
    List<RecipeEvent> findAllByRecipeId(Long recipeId);
    void deleteAllByRecipeId(Long recipeId);

    // 스크랩 존재 여부
    boolean existsByRecipeIdAndMemberIdAndScrapFlagTrue(Long recipeId, Long memberId);
    // 스크랩 가져오기
    Optional<RecipeEvent> findByRecipeIdAndMemberIdAndScrapFlagTrue(Long recipeId, Long memberId);


    // 좋아요 존재 여부
    boolean existsByRecipeIdAndMemberIdAndLikeFlagTrue(Long recipeId, Long memberId);
}

