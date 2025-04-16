package BE_Elixir.Elixir.domain.recipe.controller;

import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.service.RecipeEventService;
import BE_Elixir.Elixir.domain.recipe.service.RecipeService;
import BE_Elixir.Elixir.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    // 레시피 등록
    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestPart("dto") RecipeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "recipeStepImages", required = false) List<MultipartFile> recipeStepImages
    ) throws IOException {
        String email = memberDetails.getUsername();
        RecipeResponseDTO response = recipeService.createRecipe(dto, image, recipeStepImages);
        return ResponseEntity.ok(response);
    }

    // 레시피 상세 조회
    @GetMapping("/{recipeId}")
    public ResponseEntity<RecipeDetailResponseDTO> getRecipe(@PathVariable Long recipeId) {
        RecipeDetailResponseDTO response = recipeService.getRecipeDetail(recipeId);
        return ResponseEntity.ok(response);
    }



}
