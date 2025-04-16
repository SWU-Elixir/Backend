package BE_Elixir.Elixir.domain.recipe.controller;


import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentDTO;
import BE_Elixir.Elixir.domain.recipe.service.RecipeEventService;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeEventController {

    private final RecipeEventService recipeEventService;

    // 댓글 등록하기
    @PostMapping("/{recipeId}/comment")
    public ResponseEntity<RecipeCommentDTO> addComment(
            @PathVariable Long recipeId,
            @RequestBody RecipeCommentDTO requestDTO
    ) {
        requestDTO.setRecipeId(recipeId);
        // 댓글 추가
        RecipeCommentDTO createdComment = recipeEventService.addComment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }
}
