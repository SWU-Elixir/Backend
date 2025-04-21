package BE_Elixir.Elixir.domain.recipe.controller.api;

import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeResponseDTO;
import BE_Elixir.Elixir.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Recipe Event API", description = "레시피 이벤트 관련 API")
public interface RecipeEventApi {
    @Operation(summary = "레시피 댓글 등록", description = "레시피에 댓글을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 등록 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "댓글 등록 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<RecipeCommentDTO>> addComment(
            @PathVariable Long recipeId,
            @RequestBody RecipeCommentDTO requestDTO,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    @Operation(summary = "레시피 댓글 수정", description = "레시피에 등록된 댓글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "댓글 수정 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<RecipeCommentDTO>> editComment(
            @PathVariable Long recipeId,
            @PathVariable Long commentId,
            @RequestBody RecipeCommentDTO requestDTO,
            @AuthenticationPrincipal MemberDetails memberDetails
    );
}