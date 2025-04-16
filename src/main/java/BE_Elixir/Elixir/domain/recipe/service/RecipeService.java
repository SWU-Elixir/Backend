package BE_Elixir.Elixir.domain.recipe.service;

import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Ingredient;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import BE_Elixir.Elixir.domain.recipe.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.s3.S3Service;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
            List<MultipartFile> recipeStepImages
    ) throws IOException {

        // 1. 기본 필드만 세팅
        Recipe recipe = Recipe.from(dto);

        // 2. 대표 이미지 업로드
        if (image != null && !image.isEmpty()) {
            String imageUrl = s3Service.upload(image, "recipe/main");
            recipe.setImageUrl(imageUrl);
        }

        // 3. 단계별 이미지 업로드
        if (recipeStepImages != null && !recipeStepImages.isEmpty()) {
            List<String> stepUrls = new ArrayList<>();
            for (MultipartFile file : recipeStepImages) {
                String url = s3Service.upload(file, "recipe/steps");
                stepUrls.add(url);
            }
            recipe.setStepImageUrls(stepUrls);
        }

        // 4. 재료 태그 설정
        List<RecipeIngredient> tagList = dto.getIngredientTagIds().stream()
                .map(id -> {
                    Ingredient ingredient = ingredientRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("재료 없음: " + id));
                    return new RecipeIngredient(recipe, ingredient);
                }).collect(Collectors.toList());
        recipe.setIngredientTags(tagList);

        // 5. 저장
        recipeRepository.save(recipe);

        // 6. 응답용 태그 이름 추출
        List<String> tagNames = tagList.stream()
                .map(ri -> ri.getIngredient().getName())
                .collect(Collectors.toList());

        return new RecipeResponseDTO(recipe);

    }

    // 레시피 상세 조회
    @Transactional(readOnly = true)
    public RecipeDetailResponseDTO getRecipeDetail(Long recipeId) {
        Recipe recipe = recipeRepository.findWithAllById(recipeId)
                .orElseThrow(() -> new RuntimeException("레시피가 존재하지 않습니다."));
        // 댓글 가져오기
        List<RecipeCommentDTO> comments = recipeEventRepository.findAllByRecipeId(recipeId)
                .stream()
                .map(comment -> new RecipeCommentDTO(
                        comment.getRecipe().getId(),
                        comment.getMemberId(),
                        comment.getContent(),
                        comment.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new RecipeDetailResponseDTO(recipe, comments);
    }
}
