package BE_Elixir.Elixir.domain.recipe.controller.api;

import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentCreateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentUpdateRequestDTO;
import BE_Elixir.Elixir.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Recipe Event API", description = "레시피 이벤트 관련 API")
public interface RecipeEventApi {
    // 댓글 등록하기
    @Operation(summary = "레시피 댓글 등록", description = "레시피에 댓글을 등록합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 등록 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "댓글 등록 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<RecipeCommentResponseDTO>> addComment(
            @PathVariable Long recipeId,
            @RequestBody RecipeCommentCreateRequestDTO requestDTO,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 댓글 수정하기
    @Operation(summary = "레시피 댓글 수정", description = "레시피에 등록된 댓글을 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "댓글 수정 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<RecipeCommentResponseDTO>> editComment(
            @PathVariable Long recipeId,
            @PathVariable Long commentId,
            @RequestBody RecipeCommentUpdateRequestDTO requestDTO,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 댓글 삭제하기
    @Operation(summary = "레시피 댓글 삭제", description = "레시피에 등록된 댓글을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "댓글 삭제 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<String>> deleteComment(
            @PathVariable Long recipeId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 스크랩하기
    @Operation(summary = "레시피 스크랩하기", description = "레시피를 스크랩합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스크랩 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<String>> scrapRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 스크랩 취소하기
    @Operation(summary = "레시피 스크랩 취소하기", description = "레시피 스크랩을 취소합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스크랩 취소 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "스크랩 취소 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<String>> cancelScrapRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 좋아요하기
    @Operation(summary = "레시피 좋아요하기", description = "레시피에 좋아요를 누릅니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "좋아요 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "좋아요 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<String>> likeRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 좋아요 취소하기
    @Operation(summary = "레시피 좋아요 취소하기", description = "레시피에 누른 좋아요를 취소합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "좋아요 취소 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "좋아요 취소 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<String>> cancelLikeRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );
}