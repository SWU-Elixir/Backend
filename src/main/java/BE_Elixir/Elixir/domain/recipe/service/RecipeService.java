package BE_Elixir.Elixir.domain.recipe.service;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.*;
import BE_Elixir.Elixir.domain.recipe.entity.Ingredient;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import BE_Elixir.Elixir.domain.recipe.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.exception.OccupiedException;
import BE_Elixir.Elixir.global.s3.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeEventRepository recipeEventRepository;
    private final IngredientRepository ingredientRepository;
    private final S3Service s3Service;

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
        List<RecipeIngredient> tagList = dto.getIngredientTagIds().stream()
                .map(id -> {
                    Ingredient ingredient = ingredientRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("재료 없음: " + id));
                    return new RecipeIngredient(recipe, ingredient);
                }).collect(Collectors.toList());
        recipe.setIngredientTags(tagList);

        recipeRepository.save(recipe);

        // 응답용 태그 이름 추출
        List<String> tagNames = tagList.stream()
                .map(ri -> ri.getIngredient().getName())
                .collect(Collectors.toList());

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
        return recipes.map(recipe -> {
            boolean liked = recipeEventRepository.existsByRecipeIdAndMemberIdAndLikeFlagTrue(recipe.getId(), member.getId());
            boolean scrapped = recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(recipe.getId(), member.getId());
            return new RecipeHomeResponseDTO(recipe, liked, scrapped);
        });
    }

    // 카테고리로 필터링된 레시피 조회
    public Page<RecipeHomeResponseDTO> getRecipeListByCategory(CategoryType categoryType, CategorySlowAging categorySlowAging, Pageable pageable, Member member) {
        Page<Recipe> recipes;

        if (categoryType != null && categorySlowAging != null) {
            recipes = recipeRepository.findByCategoryTypeAndCategorySlowAging(categoryType, categorySlowAging, pageable);
        } else if (categoryType != null) {
            recipes = recipeRepository.findByCategoryType(categoryType, pageable);
        } else {
            recipes = recipeRepository.findByCategorySlowAging(categorySlowAging, pageable);
        }

        return recipes.map(recipe -> {
            boolean liked = recipeEventRepository.existsByRecipeIdAndMemberIdAndLikeFlagTrue(recipe.getId(), member.getId());
            boolean scrapped = recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(recipe.getId(), member.getId());
            return new RecipeHomeResponseDTO(recipe, liked, scrapped);
        });
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
            recipe.setStepImageUrls(stepUrls);
        }

        // 재료 태그 재설정
        List<RecipeIngredient> tagList = dto.getIngredientTagIds().stream()
                .map(id -> {
                    Ingredient ingredient = ingredientRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("재료 없음: " + id));
                    return new RecipeIngredient(recipe, ingredient);
                }).collect(Collectors.toList());

        recipe.setIngredientTags(tagList);

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
