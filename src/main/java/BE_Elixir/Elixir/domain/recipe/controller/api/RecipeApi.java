package BE_Elixir.Elixir.domain.recipe.controller.api;

import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeSummaryResponse;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Recipe API", description = "레시피 관련 API")
public interface RecipeApi {
    
    // 레시피 등록
    @Operation(summary = "레시피 등록", description = "레시피를 새로 등록합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "레시피 등록 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 201,
                                      "code": "201 OK",
                                      "message": "레시피 등록 성공",
                                      "data": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "레시피 등록 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> createRecipe(
            @RequestPart("dto") RecipeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "recipeStepImages", required = false) List<MultipartFile> recipeStepImages,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 레시피 상세 조회
    @Operation(summary = "레시피 상세 조회", description = "레시피의 상세 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "레시피 상세 조회 성공",
                                      "data": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<RecipeDetailResponseDTO>> getRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 레시피 목록(홈) 조회
    @Operation(summary = "레시피 목록(홈) 조회", description = "레시피의 목록(홈) 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 목록(홈) 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "레시피 목록(홈) 조회 성공",
                                      "data": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> getRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(required = false) CategorySlowAging categorySlowAging,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 레시피 검색 결과 조회
    @Operation(summary = "레시피 검색 결과 조회", description = "레시피의 검색 결과를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 검색 결과 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "레시피 검색 결과 조회 성공",
                                      "data": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> getSearchRecipe(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(required = false) CategorySlowAging categorySlowAging,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 레시피 인기 검색어 조회
    @Operation(summary = "인기 검색어 조회", description = "인기 검색어를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 검색어 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "인기 검색어 조회 성공",
                                      "data": true
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> getSearchKeyword(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 레시피 수정
    @Operation(summary = "레시피 수정", description = "레시피를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 수정 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "레시피 수정 성공",
                                      "data": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> updateRecipe(
            @PathVariable Long recipeId,
            @RequestPart("dto") RecipeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "recipeStepImages", required = false) List<MultipartFile> recipeStepImages,
            @AuthenticationPrincipal MemberDetails memberDetails
    );


    // 레시피 삭제
    @Operation(summary = "레시피 삭제", description = "레시피를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 삭제 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "레시피 삭제 성공",
                                      "data": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> deleteRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 로그인한 사용자가 작성한 레시피를 최대 10개까지 조회
    @Operation(summary = "작성한 레시피를 최대 10개까지 조회", description = "작성한 레시피를 최대 10개까지 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성한 레시피를 최대 10개까지 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "작성한 레시피를 최대 10개까지 조회 성공",
                                      "data": true
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<RecipeSummaryResponse>>> getMyRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestParam(defaultValue = "10") int size
    );
}