package BE_Elixir.Elixir.domain.recipe.controller;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.controller.api.RecipeApi;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeHomeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeSummaryResponse;
import BE_Elixir.Elixir.domain.recipe.service.RecipeService;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        Member member = memberDetails.getMember();
        RecipeDetailResponseDTO response = recipeService.createRecipe(dto, image, recipeStepImages, member);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.CREATED.toString(),
                "레시피 등록 성공", response
        ));
    }

    // 레시피 상세 조회
    @GetMapping("/{recipeId}")
    public ResponseEntity<CommonResponse<RecipeDetailResponseDTO>> getRecipe(
            @PathVariable("recipeId") Long recipeId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Member member = memberDetails.getMember();
        RecipeDetailResponseDTO response = recipeService.getRecipeDetail(recipeId, member);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "레시피 조회 성공", response
        ));
    }

    // 추후에 추천레시피 구현 후, 사용자 별 추천 레시피 내용 조회 추가
    // 레시피 목록(홈) 조회
    @GetMapping
    public ResponseEntity<CommonResponse<?>> getRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(required = false) CategorySlowAging categorySlowAging,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Member member = memberDetails.getMember();
        Page<RecipeHomeResponseDTO> response;

        if (categoryType != null || categorySlowAging != null) {
            response = recipeService.getRecipeListByCategory(categoryType, categorySlowAging, pageable, member);
            return ResponseEntity.ok(CommonResponse.success(
                    HttpStatus.OK.value(), HttpStatus.OK.toString(),
                    "카테고리별 레시피 조회 성공", response
            ));
        } else {
            response = recipeService.getRecipeList(pageable, member);
            return ResponseEntity.ok(CommonResponse.success(
                    HttpStatus.OK.value(), HttpStatus.OK.toString(),
                    "전체 레시피 조회 성공", response
            ));
        }
    }

    // 레시피 검색 결과 조회
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<?>> getSearchRecipe(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(required = false) CategorySlowAging categorySlowAging,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        // 검색어 저장
        recipeService.saveSearchKeyword(keyword);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Member member = memberDetails.getMember();

        Page<RecipeHomeResponseDTO> response = recipeService.searchRecipe(keyword, pageable, categoryType, categorySlowAging, member);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "레시피 검색 성공", response
        ));
    }

    // 추후에 추천 검색어 추가하기
    // 레시피 인기 검색어
    @GetMapping("/search/keyword")
    public ResponseEntity<CommonResponse<?>> getSearchKeyword(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        List<String> popularKeywords = recipeService.getPopularSearchKeywords();
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "인기 검색어 조회 성공 ", popularKeywords
        ));
    }

    // 레시피 수정
    @PatchMapping(value = "/{recipeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<?>> updateRecipe(
            @PathVariable Long recipeId,
            @RequestPart("dto") RecipeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "recipeStepImages", required = false) List<MultipartFile> recipeStepImages,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Member member = memberDetails.getMember();
        RecipeDetailResponseDTO response = recipeService.updateRecipe(recipeId, dto, image, recipeStepImages, member);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "레시피 수정 성공", response
        ));
    }
    
    // 레시피 삭제
    @DeleteMapping("/{recipeId}")
    public ResponseEntity<CommonResponse<?>> deleteRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Member member = memberDetails.getMember();
        recipeService.deleteRecipe(recipeId, member);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "레시피 삭제 성공", "recipeId: " + recipeId + " 삭제 완료"
        ));
    }

    // 로그인한 사용자가 작성한 레시피를 최대 10개까지 조회
    @GetMapping("/my")
    public ResponseEntity<CommonResponse<List<RecipeSummaryResponse>>> getMyRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestParam(defaultValue = "10") int size
    ) {
        Member member = memberDetails.getMember();
        List<RecipeSummaryResponse> recipes = recipeService.getMyRecipes(member, size);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "작성한 레시피 조회 성공", recipes
        ));
    }
}
