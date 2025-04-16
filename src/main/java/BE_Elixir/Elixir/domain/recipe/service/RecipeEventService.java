package BE_Elixir.Elixir.domain.recipe.service;

import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import BE_Elixir.Elixir.domain.recipe.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.s3.S3Service;
import lombok.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeEventService {

    private final RecipeRepository recipeRepository;
    private final RecipeEventRepository recipeEventRepository;
    private final S3Service s3Service;

    // 댓글 등록하기
    public RecipeCommentDTO addComment(RecipeCommentDTO requestDTO) {
        Recipe recipe = recipeRepository.findById(requestDTO.getRecipeId())
                .orElseThrow(() -> new RuntimeException("레시피가 존재하지 않습니다."));

        RecipeEvent comment = RecipeEvent.createRecipeComment(recipe, requestDTO);
        recipeEventRepository.save(comment);

        return new RecipeCommentDTO(requestDTO.getRecipeId(), requestDTO.getMemberId(), requestDTO.getContent(), comment.getCreatedAt());
    }

}
