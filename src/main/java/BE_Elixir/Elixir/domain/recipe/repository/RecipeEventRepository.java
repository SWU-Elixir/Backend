package BE_Elixir.Elixir.domain.recipe.repository;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    // 좋아요 가져오기
    Optional<RecipeEvent> findByRecipeIdAndMemberIdAndLikeFlagTrue(Long recipeId, Long memberId);


    // 좋아요한 레시피 ID 목록 조회
    @Query("SELECT r.recipe.id FROM RecipeEvent r WHERE r.member.id = :memberId AND r.likeFlag = true")
    List<Long> findLikedRecipeIdsByMemberId(@Param("memberId") Long memberId);

    // 스크랩한 레시피 ID 목록 조회
    @Query("SELECT r.recipe.id FROM RecipeEvent r WHERE r.member.id = :memberId AND r.scrapFlag = true")
    List<Long> findScrappedRecipeIdsByMemberId(@Param("memberId") Long memberId);

    // 로그인한 사용자가 스크랩한 레시피 조회
    List<RecipeEvent> findByMemberAndScrapFlagTrue(Member member);

}

