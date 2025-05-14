package BE_Elixir.Elixir.domain.recipe.service;


import BE_Elixir.Elixir.domain.challenge.event.events.RecipeEvent;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeCommentResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeHomeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.exception.OccupiedException;
import BE_Elixir.Elixir.global.redis.RedisRecipeService;
import BE_Elixir.Elixir.global.s3.S3Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeEventRepository recipeEventRepository;
    private final IngredientRepository ingredientRepository;
    private final S3Service s3Service;
    private final RedisRecipeService redisRecipeService;
    private final ApplicationEventPublisher eventPublisher;

    // 레시피 등록하기
    @Transactional
    public RecipeResponseDTO createRecipe(
            RecipeRequestDTO dto,
            MultipartFile image,
            List<MultipartFile> recipeStepImages,
            Member member
    ) throws IOException {
        // 기본 필드 세팅
        Recipe recipe = Recipe.from(dto, member);

        // 대표 이미지 업로드
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.upload(image, "recipe/main");
            recipe.setImageUrl(imageUrl);
        }

        // 단계별 이미지 업로드
        if (recipeStepImages != null && !recipeStepImages.isEmpty()) {
            List<String> stepUrls = new ArrayList<>();
            for (MultipartFile file : recipeStepImages) {
                String url = s3Service.upload(file, "recipe/steps");
                stepUrls.add(url);
            }
            recipe.setStepImageUrls(stepUrls);
        }

        // 재료 태그 설정
        List<RecipeIngredient> tagList = dto.getIngredientTagNames().stream()
                .map(name -> {
                    Ingredient ingredient = ingredientRepository.findByName(name)
                            .orElseThrow(() -> new RuntimeException("재료 없음: " + name));
                    return new RecipeIngredient(recipe, ingredient);
                }).collect(Collectors.toList());

        recipe.setIngredientTags(tagList);

        recipeRepository.save(recipe);

        // 응답용 태그 이름 추출
        List<String> tagNames = tagList.stream()
                .map(ri -> ri.getIngredient().getName())
                .collect(Collectors.toList());

        // 챌린지 달성을 위한 이벤트 발행
        eventPublisher.publishEvent(new RecipeEvent(member.getId(), recipe.getId(), LocalDateTime.now()));

        return new RecipeResponseDTO(recipe);
    }

    // 레시피 상세 조회
    @Transactional(readOnly = true)
    public RecipeDetailResponseDTO getRecipeDetail(Long recipeId, Member member) {
        // 레시피 조회
        Recipe recipe = recipeRepository.findWithAllById(recipeId)
                .orElseThrow(() -> new OccupiedException(ErrorCode.RECIPE_NOT_FOUND));

        // 댓글 가져오기
        List<RecipeCommentResponseDTO> comments = recipeEventRepository.findAllByRecipeId(recipeId)
                .stream()
                .map(RecipeCommentResponseDTO::new)
                .collect(Collectors.toList());

        // 좋아요 및 스크랩 여부 확인
        boolean likedByCurrentUser = recipeEventRepository.existsByRecipeIdAndMemberIdAndLikeFlagTrue(recipeId, member.getId());
        boolean scrappedByCurrentUser = recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(recipeId, member.getId());
        return new RecipeDetailResponseDTO(recipe, comments, likedByCurrentUser, scrappedByCurrentUser);
    }

    // 레시피 목록(홈) 조회
    // 전체 레시피 조회
    public Page<RecipeHomeResponseDTO> getRecipeList(Pageable pageable, Member member) {
        Page<Recipe> recipes = recipeRepository.findAll(pageable);
        // 좋아요 / 스크랩 한 레시피 ID Set 미리 조회
        Set<Long> likedRecipeIds = new HashSet<>(recipeEventRepository.findLikedRecipeIdsByMemberId(member.getId()));
        Set<Long> scrappedRecipeIds = new HashSet<>(recipeEventRepository.findScrappedRecipeIdsByMemberId(member.getId()));

        return recipes.map(recipe -> {
            boolean liked = likedRecipeIds.contains(recipe.getId());
            boolean scrapped = scrappedRecipeIds.contains(recipe.getId());
            return new RecipeHomeResponseDTO(recipe, liked, scrapped);
        });
    }

    // 카테고리로 필터링된 레시피 조회
    public Page<RecipeHomeResponseDTO> getRecipeListByCategory(
            CategoryType categoryType,
            CategorySlowAging categorySlowAging,
            Pageable pageable,
            Member member
    ) {
        Page<Recipe> recipes;

        if (categoryType != null && categorySlowAging != null) {
            recipes = recipeRepository.findByCategoryTypeAndCategorySlowAging(categoryType, categorySlowAging, pageable);
        } else if (categoryType != null) {
            recipes = recipeRepository.findByCategoryType(categoryType, pageable);
        } else {
            recipes = recipeRepository.findByCategorySlowAging(categorySlowAging, pageable);
        }

        Set<Long> likedRecipeIds = new HashSet<>(recipeEventRepository.findLikedRecipeIdsByMemberId(member.getId()));
        Set<Long> scrappedRecipeIds = new HashSet<>(recipeEventRepository.findScrappedRecipeIdsByMemberId(member.getId()));

        return recipes.map(recipe -> {
            boolean liked = likedRecipeIds.contains(recipe.getId());
            boolean scrapped = scrappedRecipeIds.contains(recipe.getId());
            return new RecipeHomeResponseDTO(recipe, liked, scrapped);
        });
    }

    // 레시피 검색 결과 조회
    @Transactional(readOnly = true)
    public Page<RecipeHomeResponseDTO> searchRecipe(
            String keyword,
            Pageable pageable,
            CategoryType categoryType,
            CategorySlowAging categorySlowAging,
            Member member
    ) {
        Page<Recipe> recipes;

        if (categoryType != null && categorySlowAging != null) {
            recipes = recipeRepository.findByTitleContainingAndCategoryTypeAndCategorySlowAging(
                    keyword, categoryType, categorySlowAging, pageable);
        } else if (categoryType != null) {
            recipes = recipeRepository.findByTitleContainingAndCategoryType(
                    keyword, categoryType, pageable);
        } else if (categorySlowAging != null) {
            recipes = recipeRepository.findByTitleContainingAndCategorySlowAging(
                    keyword, categorySlowAging, pageable);
        } else {
            recipes = recipeRepository.findByTitleContaining(keyword, pageable);
        }

        return recipes.map(recipe -> {
            boolean liked = recipeEventRepository.existsByRecipeIdAndMemberIdAndLikeFlagTrue(recipe.getId(), member.getId());
            boolean scrapped = recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(recipe.getId(), member.getId());
            return new RecipeHomeResponseDTO(recipe, liked, scrapped);
        });
    }

    // 추후에 추천 검색어 추가
    // 레시피 인기 검색어
    @Transactional(readOnly = true)
    public List<String> getPopularSearchKeywords() {
        return redisRecipeService.getTopKeywords(5);
    }
    public void saveSearchKeyword(String keyword) {
        redisRecipeService.incrementKeyword(keyword);
    }


    // 레시피 수정
    @Transactional
    public RecipeResponseDTO updateRecipe(
            Long recipeId,
            RecipeRequestDTO dto,
            MultipartFile image,
            List<MultipartFile> recipeStepImages,
            Member member
    ) throws IOException {
        // 레시피 조회
        Recipe recipe = recipeRepository.findWithAllById(recipeId)
                .orElseThrow(() -> new OccupiedException(ErrorCode.RECIPE_NOT_FOUND));

        if (!recipe.getMember().getEmail().equals(member.getEmail())){
            throw new OccupiedException(ErrorCode.UNAUTHORIZED_OPERATION); // 권한 체크
        }

        // 기본 필드 업데이트
        recipe.updateFrom(dto);

        // 대표 이미지 업데이트
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.upload(image, "recipe/main");
            recipe.setImageUrl(imageUrl);
        }

        // 단계 이미지 업데이트
        if (recipeStepImages != null && !recipeStepImages.isEmpty()) {
            List<String> stepUrls = new ArrayList<>();
            for (MultipartFile file : recipeStepImages) {
                String url = s3Service.upload(file, "recipe/steps");
                stepUrls.add(url);
            }
            if (!stepUrls.isEmpty()) {
                recipe.setStepImageUrls(stepUrls);
            }
            recipe.setStepImageUrls(stepUrls);
        }

        recipe.getIngredientTags().clear(); // 참조 유지

        recipe.getIngredientTags().addAll(
                dto.getIngredientTagNames().stream()
                        .map(name -> {
                            Ingredient ingredient = ingredientRepository.findByName(name)
                                    .orElseThrow(() -> new RuntimeException("재료 없음: " + name));
                            return new RecipeIngredient(recipe, ingredient);
                        })
                        .collect(Collectors.toList())
        );

        recipeRepository.save(recipe);
        return new RecipeResponseDTO(recipe);
    }

    // 레시피 삭제
    @Transactional
    public void deleteRecipe(Long recipeId, Member member) {
        Recipe recipe = recipeRepository.findWithAllById(recipeId)
                .orElseThrow(() -> new OccupiedException(ErrorCode.RECIPE_NOT_FOUND));

        // 작성자 본인만 삭제 가능
        if (!recipe.getMember().getEmail().equals(member.getEmail())) {
            throw new OccupiedException(ErrorCode.UNAUTHORIZED_OPERATION);
        }

        // 레시피에 달린 댓글 먼저 삭제
        recipeEventRepository.deleteAllByRecipeId(recipeId);

        // 레시피에 달린 재료 태그(RecipeIngredient) 모두 삭제
        recipe.clearIngredientTags();

        recipeRepository.delete(recipe);
    }
}
