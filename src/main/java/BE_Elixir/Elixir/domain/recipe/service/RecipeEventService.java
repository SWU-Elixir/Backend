package BE_Elixir.Elixir.domain.recipe.service;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentCreateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeCommentUpdateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.exception.OccupiedException;
import BE_Elixir.Elixir.global.s3.S3Service;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeEventService {

    private final RecipeRepository recipeRepository;
    private final RecipeEventRepository recipeEventRepository;
    private final S3Service s3Service;

    // 댓글 등록하기
    public RecipeCommentResponseDTO addComment(
            RecipeCommentCreateRequestDTO requestDTO,
            Member member
    ) {
        // 레시피 존재 여부 확인
        Recipe recipe = recipeRepository.findById(requestDTO.getRecipeId())
                .orElseThrow(() -> new OccupiedException(ErrorCode.RECIPE_NOT_FOUND));

        // 댓글 생성
        RecipeEvent comment = RecipeEvent.createRecipeComment(recipe, requestDTO, member);
        recipeEventRepository.save(comment);

        return new RecipeCommentResponseDTO(comment);
    }

    // 댓글 수정하기
    @Transactional
    public RecipeCommentResponseDTO editComment(
            RecipeCommentUpdateRequestDTO requestDTO,
            Member member
    ){
        // 기존 댓글 조회
        RecipeEvent existingComment = recipeEventRepository.findById(requestDTO.getCommentId())
                .orElseThrow(() -> new OccupiedException(ErrorCode.COMMENT_NOT_FOUND));

        // 댓글 작성자가 아닌 경우 예외 처리
        if (!existingComment.getMember().getEmail().equals(member.getEmail())) {
            throw new OccupiedException(ErrorCode.UNAUTHORIZED_OPERATION); // 수정 권한이 없으면 예외
        }

        // 댓글 내용 수정
        existingComment.updateContent(requestDTO.getContent());
        return new RecipeCommentResponseDTO(existingComment);
    }

    // 댓글 삭제하기
    @Transactional
    public void deleteComment(Long commentId, Member member) {
        // 기존 댓글 조회
        RecipeEvent existingComment = recipeEventRepository.findById(commentId)
                .orElseThrow(() -> new OccupiedException(ErrorCode.COMMENT_NOT_FOUND));

        // 댓글 작성자가 아닌 경우 예외 처리
        if (!existingComment.getMember().getEmail().equals(member.getEmail())) {
            throw new OccupiedException(ErrorCode.UNAUTHORIZED_OPERATION);
        }

        // 댓글(flag)이 맞는지 한 번 확인
        if (!existingComment.isCommentFlag()) {
            throw new OccupiedException(ErrorCode.INVALID_OPERATION);
        }

        // 댓글 삭제
        recipeEventRepository.delete(existingComment);
    }
}
