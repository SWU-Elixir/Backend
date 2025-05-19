package BE_Elixir.Elixir.domain.recommendation.service;


import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.domain.recommendation.dto.RecommendationResponseDTO;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.redis.RedisRecipeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecipeRepository recipeRepository;
    private final RedisRecipeService redisRecipeService;
    private final RecipeEventRepository recipeEventRepository;

    // 사용자 맞춤형 레시피 추천
    @Transactional
    public List<RecommendationResponseDTO> getRecommendationsForUser(Member member) {
        // 캐시 확인
        List<RecommendationResponseDTO> cached = redisRecipeService.getCachedRecommendations(member.getId());
        if (cached != null) return cached;

        List<Recipe> allRecipes = recipeRepository.findAll();

        // 필터링
        List<Recipe> filtered = allRecipes.stream()
                .filter(recipe -> !hasAllergyConflict(member, recipe))
                .filter(recipe -> matchesMealStyle(member, recipe))
                .filter(recipe -> matchesRecipeStyle(member, recipe))
                .filter(recipe -> matchesReason(member, recipe))
                .limit(3)
                .collect(Collectors.toList());


        // 스크랩 여부 확인 및 DTO 변환
        List<RecommendationResponseDTO> recommendations = filtered.stream()
                .map(recipe -> {
                    boolean scrappedByCurrentUser = recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(recipe.getId(), member.getId());
                    return new RecommendationResponseDTO(recipe, scrappedByCurrentUser);
                })
                .collect(Collectors.toList());

        // 캐싱 (1시간)
        redisRecipeService.cacheRecommendations(member.getId(), recommendations, Duration.ofHours(1));
        return recommendations;
    }

    // 알러지가 포함된 레시피는 제외
    private boolean hasAllergyConflict(Member member, Recipe recipe) {
        return
                (member.isAllergyEgg() && Boolean.TRUE.equals(recipe.getAllergy_알류())) ||
                        (member.isAllergyMilk() && Boolean.TRUE.equals(recipe.getAllergy_우유())) ||
                        (member.isAllergyGrain() && Boolean.TRUE.equals(recipe.getAllergy_각류())) ||
                        (member.isAllergyWheatProduct() && Boolean.TRUE.equals(recipe.getAllergy_밀류())) ||
                        (member.isAllergyDairy() && Boolean.TRUE.equals(recipe.getAllergy_유제품())) ||
                        (member.isAllergyBuckwheat() && Boolean.TRUE.equals(recipe.getAllergy_메밀())) ||
                        (member.isAllergyPeanut() && Boolean.TRUE.equals(recipe.getAllergy_땅콩())) ||
                        (member.isAllergySoybean() && Boolean.TRUE.equals(recipe.getAllergy_대두())) ||
                        (member.isAllergyWheat() && Boolean.TRUE.equals(recipe.getAllergy_밀())) ||
                        (member.isAllergyMackerel() && Boolean.TRUE.equals(recipe.getAllergy_고등어())) ||
                        (member.isAllergyPork() && Boolean.TRUE.equals(recipe.getAllergy_돼지고기())) ||
                        (member.isAllergyPeach() && Boolean.TRUE.equals(recipe.getAllergy_복숭아())) ||
                        (member.isAllergyTomato() && Boolean.TRUE.equals(recipe.getAllergy_토마토())) ||
                        (member.isAllergySulfite() && Boolean.TRUE.equals(recipe.getAllergy_아황산류())) ||
                        (member.isAllergyWalnut() && Boolean.TRUE.equals(recipe.getAllergy_호두())) ||
                        (member.isAllergyChicken() && Boolean.TRUE.equals(recipe.getAllergy_닭고기())) ||
                        (member.isAllergyBeef() && Boolean.TRUE.equals(recipe.getAllergy_쇠고기())) ||
                        (member.isAllergySquid() && Boolean.TRUE.equals(recipe.getAllergy_오징어())) ||
                        (member.isAllergyShellfish() && Boolean.TRUE.equals(recipe.getAllergy_조개류())) ||
                        (member.isAllergyOyster() && Boolean.TRUE.equals(recipe.getAllergy_굴())) ||
                        (member.isAllergyAbalone() && Boolean.TRUE.equals(recipe.getAllergy_전복())) ||
                        (member.isAllergyMussel() && Boolean.TRUE.equals(recipe.getAllergy_홍합())) ||
                        (member.isAllergyPineNut() && Boolean.TRUE.equals(recipe.getAllergy_잣()));
    }


    // 식사 스타일(육류 기반, 채식 기반, 혼합)에 따라 레시피 필터링
    private boolean matchesMealStyle(Member member, Recipe recipe) {
        List<String> meatKeywords = List.of("고기", "닭", "소고기", "돼지고기", "오리", "양고기", "소", "돼지", "닭고기", "베이컨", "햄", "고등어", "삼겹살", "참치", "갈비", "스테이크", "육회", "정육");
        List<String> vegetableKeywords = List.of("채소", "상추", "깻잎", "시금치", "샐러리", "양상추", "브로콜리", "오이", "파", "양파", "당근", "고추", "마늘", "배추", "버섯", "콩", "두부");

        boolean hasMeat = false;
        boolean hasVegetable = false;

        for (RecipeIngredient ri : recipe.getIngredientTags()) {
            String name = ri.getIngredient().getName();

            if (meatKeywords.stream().anyMatch(name::contains)) {
                hasMeat = true;
            }
            if (vegetableKeywords.stream().anyMatch(name::contains)) {
                hasVegetable = true;
            }
        }

        // 사용자 선호 스타일과 매칭
        if (member.isMealStyleMeatBased() && hasMeat && !hasVegetable) return true;
        if (member.isMealStyleVegetableBased() && hasVegetable && !hasMeat) return true;
        if (member.isMealStyleMixed() && (hasMeat || hasVegetable)) return true;

        return false;
    }


    // 요리 스타일 취향(한식, 중식, 일식 등)에 따라 레시피 필터링
    // 한 개라도 일치하면 해당 레시피는 필터 통과
    private boolean matchesRecipeStyle(Member member, Recipe recipe) {
        if (member.isRecipeStyleKorean() && recipe.getCategoryType() == CategoryType.한식) return true;
        if (member.isRecipeStyleChinese() && recipe.getCategoryType() == CategoryType.중식) return true;
        if (member.isRecipeStyleJapanese() && recipe.getCategoryType() == CategoryType.일식) return true;
        if (member.isRecipeStyleWestern() && recipe.getCategoryType() == CategoryType.양식) return true;
        if (member.isRecipeStyleDessert() && recipe.getCategoryType() == CategoryType.디저트) return true;
        if (member.isRecipeStyleBeverageTea() && recipe.getCategoryType() == CategoryType.음료_차) return true;
        if (member.isRecipeStyleSauceJam() && recipe.getCategoryType() == CategoryType.양념_소스_잼) return true;
        return false;
    }

    // 목적(항산화, 혈당 조절, 염증 감소)에 따라 레시피 필터링
    private boolean matchesReason(Member member, Recipe recipe) {
        if (member.isReasonAntioxidantBoost() && recipe.getCategorySlowAging() == CategorySlowAging.항산화강화) return true;
        if (member.isReasonBloodSugarControl() && recipe.getCategorySlowAging() == CategorySlowAging.혈당조절) return true;
        if (member.isReasonInflammationReduction() && recipe.getCategorySlowAging() == CategorySlowAging.염증감소) return true;
        return false;
    }
}