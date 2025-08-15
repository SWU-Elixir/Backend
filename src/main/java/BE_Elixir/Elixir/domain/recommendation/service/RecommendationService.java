package BE_Elixir.Elixir.domain.recommendation.service;


import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.domain.recommendation.dto.RecommendationResponseDTO;
import BE_Elixir.Elixir.domain.recommendation.repository.RecommendationRepository;
import BE_Elixir.Elixir.global.redis.RedisRecipeService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecipeRepository recipeRepository;
    private final RedisRecipeService redisRecipeService;
    private final RecipeEventRepository recipeEventRepository;
    private final IngredientRepository ingredientRepository;
    private final RecommendationRepository recommendationRepository;


    // 사용자 맞춤형 레시피 추천
    @Transactional(readOnly = true)
    public List<RecommendationResponseDTO> getRecommendationsForUser(Member member) {
        // 동적 캐시 키 생성
        String cacheKey = createCacheKey(member);

        // 캐시 확인
        List<RecommendationResponseDTO> cached = redisRecipeService.getCachedRecommendations(cacheKey);
        if (cached != null) return cached;

        // 필터링 조건 추출
        List<String> mealStyles = member.getMealStyles();
        List<String> recipeStyles = member.getRecipeStyles();
        List<String> reasons = member.getReasons();
        List<String> allergies = member.getAllergies();

        // Repository에서 필터된 레시피 조회 (필요에 따라 쿼리 수정 필요)
        List<Recipe> filteredRecipes = recommendationRepository.findFilteredRecipes(recipeStyles, reasons);

        // 알러지 필터링 + 식사 스타일, 이유 조건 필터링 추가 로직 (필요시)
        filteredRecipes = filteredRecipes.stream()
                .filter(recipe -> !hasAllergyConflict(allergies, recipe))
                .filter(recipe -> mealStyles.isEmpty() || mealStyles.contains(recipe.getCategoryType().name()))
                .limit(3)
                .collect(Collectors.toList());

        // 필터링된 결과 없으면 랜덤 3개
        if (filteredRecipes.isEmpty()) {
            filteredRecipes = recommendationRepository.findRandom3();
        }

        // DTO 변환 및 스크랩 여부 체크
        List<RecommendationResponseDTO> recommendations = filteredRecipes.stream()
                .map(recipe -> {
                    boolean scrappedByUser = recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(recipe.getId(), member.getId());
                    return new RecommendationResponseDTO(recipe, scrappedByUser);
                })
                .collect(Collectors.toList());

        // 캐싱
        redisRecipeService.cacheRecommendations(cacheKey, recommendations, Duration.ofMinutes(15));

        return recommendations;
    }

    private String createCacheKey(Member member) {
        String mealKey = String.join(",", member.getMealStyles());
        String recipeKey = String.join(",", member.getRecipeStyles());
        String reasonKey = String.join(",", member.getReasons());
        String allergyKey = String.join(",", member.getAllergies());

        return "recommendations:"
                + member.getId()
                + ":mealStyles=" + mealKey
                + ":recipeStyles=" + recipeKey
                + ":reasons=" + reasonKey
                + ":allergies=" + allergyKey;
    }


    // 알러지가 포함된 레시피는 제외
    private boolean hasAllergyConflict(List<String> userAllergies, Recipe recipe) {
        for (String allergy : userAllergies) {
            if (recipe.getAllergyList().contains(allergy)) {
                return true;
            }
        }
        return false;
    }



    // 식사 스타일(육류기반, 채식기반, 혼합식)에 따라 레시피 필터링
    public boolean matchesMealStyle(Member member, Recipe recipe) {
        // 레시피의 식재료 카테고리 리스트 추출
        List<String> categories = recipe.getIngredientTags().stream()
                .map(ri -> ri.getIngredient().getCategory())
                .distinct()
                .collect(Collectors.toList());

        boolean hasMeat = categories.stream().anyMatch(this::isMeatCategory);     // 육류성 재료 포함 여부
        boolean hasPlant = categories.stream().anyMatch(this::isPlantCategory);   // 채식성 재료 포함 여부

        // 사용자가 선택한 식사 스타일
        boolean prefersMeat = member.getMealStyles().contains("고기위주");
        boolean prefersPlant = member.getMealStyles().contains("채소위주");
        boolean prefersMix = member.getMealStyles().contains("혼합식");

        // 각각의 스타일에 맞게 필터링
        if (prefersMeat) {
            return hasMeat;
        } else if (prefersPlant) {
            return !hasMeat; // 육류 없는 레시피만
        } else if (prefersMix) {
            return hasMeat && hasPlant;
        }

        // 선택하지 않은 경우 필터링 없이 통과
        return true;
    }
    // 육류 관련 카테고리 판별
    private boolean isMeatCategory(String category) {
        return category != null && (
                category.contains("식육가공품") ||
                        category.contains("포장육") ||
                        category.contains("난류") ||
                        category.contains("유가공품") ||
                        category.contains("우유") ||
                        category.contains("육류") ||
                        category.contains("유지류") ||
                        category.contains("기타동물성식품")
        );
    }

    // 채식 관련 카테고리 판별
    private boolean isPlantCategory(String category) {
        return category != null && (
                category.contains("채소류") ||
                        category.contains("곡류") ||
                        category.contains("견과") ||
                        category.contains("과일류") ||
                        category.contains("해조류") ||
                        category.contains("버섯류") ||
                        category.contains("두류") ||
                        category.contains("식용곤충류") ||
                        category.contains("감자") ||
                        category.contains("농산가공") ||
                        category.contains("자연산 수액") ||
                        category.contains("차류")
        );
    }


    // 요리 스타일 취향(한식, 중식, 일식 등)에 따라 레시피 필터링
    // 한 개라도 일치하면 해당 레시피는 필터 통과
    private boolean matchesRecipeStyle(Member member, Recipe recipe) {
        // 사용자가 선택한 ["중식", "한식", 등]
        List<String> memberStyles = member.getRecipeStyles();

        // 레시피의 요리 스타일 카테고리
        String recipeStyle = recipe.getCategoryType().name();

        // 회원 선호 스타일 리스트 중 하나라도 레시피 스타일과 일치하면 true 반환
        return memberStyles.stream().anyMatch(style -> style.equals(recipeStyle));
    }

    // 목적(항산화, 혈당 조절, 염증 감소)에 따라 레시피 필터링
    private boolean matchesReason(Member member, Recipe recipe) {
        // 사용자가 선택한 ["항산화강화", "혈당조절", "염증감소"]
        List<String> memberReasons = member.getReasons();

        // 사용자가 어떤 이유도 선택하지 않은 경우, 모든 레시피 통과
        if (memberReasons.isEmpty()) {
            return true;
        }

        // 레시피의 저속노화 카테고리
        String recipeReason = recipe.getCategorySlowAging().name();

        // 회원의 이유 리스트에 레시피 이유가 포함되어 있으면 true
        return memberReasons.contains(recipeReason);
    }

    // 추천 검색어 조회
    @Transactional(readOnly = true)
    public List<String> getRecommendedKeywords(Member member) {
        // 캐시된 추천 레시피 확인
        List<RecommendationResponseDTO> cached = redisRecipeService.getCachedRecommendations(String.valueOf(member.getId()));
        if (cached == null || cached.isEmpty()) return List.of();

        Set<String> keywords = new LinkedHashSet<>(); // 중복 제거 + 순서 유지
        Set<Long> ingredientIdSet = new HashSet<>();

        // 불용어(조사 등) 목록
        List<String> postpositions = List.of("은", "는", "이", "가", "을", "를", "에", "의", "도", "으로", "와", "과", "에게", "한테", "에서", "부터", "까지", "보다", "처럼", "만", "이나", "나", "이며", "든지", "라도", "조차");


        for (RecommendationResponseDTO dto : cached) {
            // 제목에서 키워드 추출
            if (dto.getTitle() != null) {
                String[] words = dto.getTitle().split("\\s+");
                for (String word : words) {
                    word = word.trim();
                    if (!word.isBlank()) {
                        // 단어에서 조사 제거
                        for (String josa : postpositions) {
                            if (word.endsWith(josa)) {
                                word = word.substring(0, word.length() - josa.length());
                                break; // 하나만 제거하고 탈출
                            }
                        }

                        // 최종 단어가 비어 있지 않으면 추가
                        if (!word.isBlank()) {
                            keywords.add(word);
                        }
                    }
                }
            }

            // 식재료 ID 수집
            if (dto.getIngredientTagIds() != null) {
                ingredientIdSet.addAll(dto.getIngredientTagIds());
            }

            // 원하는 개수만큼 제한 (예: 상위 5개)
            if (keywords.size() >= 5) break;
        }

        // 식재료 이름 조회
        if (!ingredientIdSet.isEmpty()) {
            List<Ingredient> ingredients = ingredientRepository.findByIdIn(ingredientIdSet.stream().toList());
            ingredients.forEach(ingredient -> keywords.add(ingredient.getName()));
        }

        return keywords.stream().limit(5).toList();
    }
}