package BE_Elixir.Elixir.domain.recipe.controller;


import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.controller.api.RecipeEventApi;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentCreateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentUpdateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.service.RecipeEventService;
import BE_Elixir.Elixir.global.response.CommonResponse;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeEventController implements RecipeEventApi {

    private final RecipeEventService recipeEventService;

    // 댓글 등록하기
    @PostMapping("/{recipeId}/comment")
    public ResponseEntity<CommonResponse<RecipeCommentResponseDTO>> addComment(
            @PathVariable Long recipeId,
            @RequestBody RecipeCommentCreateRequestDTO requestDTO,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        try {
            Member member = memberDetails.getMember();
            requestDTO.setRecipeId(recipeId);
            RecipeCommentResponseDTO createdComment = recipeEventService.addComment(requestDTO, member);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CommonResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.toString(),
                            "댓글 등록 성공 ", createdComment
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "댓글 등록 실패 " + e.getMessage()
                    ));
        }
    }

    // 댓글 수정하기
    @PutMapping("/{recipeId}/comment/{commentId}")
    public ResponseEntity<CommonResponse<RecipeCommentResponseDTO>>editComment(
        @PathVariable Long recipeId,
        @PathVariable Long commentId,
        @RequestBody RecipeCommentUpdateRequestDTO requestDTO,
        @AuthenticationPrincipal MemberDetails memberDetails
    ){
        try {
            Member member = memberDetails.getMember();
            requestDTO.setRecipeId(recipeId);
            requestDTO.setCommentId(commentId);
            RecipeCommentResponseDTO editedComment = recipeEventService.editComment(requestDTO, member);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                            "댓글 수정 성공", editedComment
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "댓글 수정 실패 " + e.getMessage()
                    ));
        }
    }

    // 댓글 삭제하기
    @DeleteMapping("/{recipeId}/comment/{commentId}")
    public ResponseEntity<CommonResponse<String>> deleteComment(
            @PathVariable Long recipeId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        try {
            Member member = memberDetails.getMember();
            recipeEventService.deleteComment(commentId, member);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                            "댓글 삭제 성공", "commentId: " + commentId + " 삭제 완료"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "댓글 삭제 실패 " + e.getMessage()));
        }
    }
}
