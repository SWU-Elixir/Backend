package BE_Elixir.Elixir.domain.recipe.controller;

import BE_Elixir.Elixir.domain.recipe.dto.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.service.RecipeService;
import BE_Elixir.Elixir.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    // 레시피 등록
    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            @RequestPart("dto") RecipeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "recipeStepImages", required = false) List<MultipartFile> recipeStepImages
    ) throws IOException {
        RecipeResponseDTO response = recipeService.createRecipe(dto, image, recipeStepImages);
        return ResponseEntity.ok(response);
    }

    // 레시피 상세 조회
    @GetMapping("/{recipeId}")
    public ResponseEntity<RecipeResponseDTO> getRecipe(@PathVariable Long recipeId) {
        RecipeResponseDTO response = recipeService.getRecipeDetail(recipeId);
        return ResponseEntity.ok(response);
    }
}
