package BE_Elixir.Elixir.domain.recipe.controller.api;

import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeRequestDTO;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Recipe API", description = "레시피 관련 API")
public interface RecipeApi {
    
    // 레시피 등록
    @Operation(summary = "레시피 등록", description = "레시피를 새로 등록합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 등록 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
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
}