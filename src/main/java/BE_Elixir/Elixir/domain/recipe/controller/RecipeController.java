package BE_Elixir.Elixir.domain.recipe.controller;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.controller.api.RecipeApi;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.service.RecipeService;
import BE_Elixir.Elixir.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController implements RecipeApi {
    private final RecipeService recipeService;

    // 레시피 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<?>> createRecipe(
            @RequestPart("dto") RecipeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "recipeStepImages", required = false) List<MultipartFile> recipeStepImages,
            @AuthenticationPrincipal MemberDetails memberDetails
    ){
        try {
            Member member = memberDetails.getMember();
            RecipeResponseDTO response = recipeService.createRecipe(dto, image, recipeStepImages, member);
            return ResponseEntity.ok(CommonResponse.success(
                    HttpStatus.OK.value(), HttpStatus.CREATED.toString(),
                    "레시피 등록 성공", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "레시피 등록 실패 - " + e.getMessage()
            ));
        }
    }

    // 레시피 상세 조회
    @GetMapping("/{recipeId}")
    public ResponseEntity<CommonResponse<RecipeDetailResponseDTO>> getRecipe(
            @PathVariable Long recipeId
    ) {
        try {
            RecipeDetailResponseDTO response = recipeService.getRecipeDetail(recipeId);

            return ResponseEntity.ok(CommonResponse.success(
                    HttpStatus.OK.value(), HttpStatus.OK.toString(),
                    "레시피 조회 성공", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "레시피 조회 실패 - " + e.getMessage()
                    ));
        }
    }

    // 레시피 수정
    @PutMapping(value = "/{recipeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<?>> updateRecipe(
            @PathVariable Long recipeId,
            @RequestPart("dto") RecipeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "recipeStepImages", required = false) List<MultipartFile> recipeStepImages,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        try {
            Member member = memberDetails.getMember();
            RecipeResponseDTO response = recipeService.updateRecipe(recipeId, dto, image, recipeStepImages, member);
            return ResponseEntity.ok(CommonResponse.success(
                    HttpStatus.OK.value(), HttpStatus.OK.toString(),
                    "레시피 수정 성공", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "레시피 수정 실패 - " + e.getMessage()));
        }
    }

}
