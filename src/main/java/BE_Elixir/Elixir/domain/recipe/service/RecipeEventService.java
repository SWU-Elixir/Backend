package BE_Elixir.Elixir.domain.recipe.service;

import BE_Elixir.Elixir.domain.achievement.service.MemberStatsService;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeCommentCreateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeCommentResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeCommentUpdateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.s3.S3Service;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeEventService {

    private final RecipeRepository recipeRepository;
    private final RecipeEventRepository recipeEventRepository;
    private final MemberStatsService memberStatsService;

    // 레시피별 댓글 조회
    public List<RecipeCommentResponseDTO> getCommentsByRecipeId(Long recipeId) {
        return recipeEventRepository.findAllByRecipeId(recipeId)
                .stream()
                .map(RecipeCommentResponseDTO::new)
                .collect(Collectors.toList());
    }

    // 댓글 등록하기
    public RecipeCommentResponseDTO addComment(
            RecipeCommentCreateRequestDTO requestDTO,
            Member member
    ) {
        // 레시피 존재 여부 확인
        Recipe recipe = recipeRepository.findById(requestDTO.getRecipeId())
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));

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
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 댓글 작성자가 아닌 경우 예외 처리
        if (!existingComment.getMember().getEmail().equals(member.getEmail())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS); // 수정 권한이 없으면 예외
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
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 댓글 작성자가 아닌 경우 예외 처리
        if (!existingComment.getMember().getEmail().equals(member.getEmail())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 댓글(flag)이 맞는지 한 번 확인
        if (!existingComment.isCommentFlag()) {
            throw new CustomException(ErrorCode.INVALID_EVENT_OPERATION);
        }

        existingComment.setCommentFlag(false); // 댓글 플래그 끄기

        // 댓글 삭제
        recipeEventRepository.delete(existingComment);
    }

    // 레시피 스크랩하기
    @Transactional
    public void scrapRecipe(Long recipeId, Member member) {
        // 레시피 존재 여부 확인
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));

        // 기존에 스크랩한 게 있는지 확인
        boolean alreadyScrapped = recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(recipeId, member.getId());
        if (alreadyScrapped) {
            throw new CustomException(ErrorCode.ALREADY_SCRAPPED);
        }

        RecipeEvent scrap = new RecipeEvent();
        scrap.setRecipe(recipe);
        scrap.setMember(member);
        scrap.setScrapFlag(true);
        recipeEventRepository.save(scrap);

        // 업적 달성을 위한 스크랩 증가
        memberStatsService.increaseStat(member.getId(), AchievementType.TOTAL_SCRAPS, 1);
    }

    // 레시피 스크랩 취소하기
    @Transactional
    public void cancelScrapRecipe(Long recipeId, Member member) {
        // 스크랩한 거 가져오기
        RecipeEvent scrap = recipeEventRepository.findByRecipeIdAndMemberIdAndScrapFlagTrue(recipeId, member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCRAP_NOT_FOUND));


        // 스크랩한 사용자가 아닌 경우 예외 처리
        if (!scrap.getMember().getEmail().equals(member.getEmail())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 스크랩(flag)이 맞는지 한 번 확인
        if (!scrap.isScrapFlag()) {
            throw new CustomException(ErrorCode.INVALID_EVENT_OPERATION);
        }

        scrap.setScrapFlag(false); // 스크랩 플래그 끄기

        recipeEventRepository.delete(scrap);

        // 업적 달성을 위한 스크랩 감소
        memberStatsService.increaseStat(member.getId(), AchievementType.TOTAL_SCRAPS, -1);
    }

    // 레시피 좋아요하기
    @Transactional
    public void likeRecipe(Long recipeId, Member member) {
        // 레시피 존재 여부 확인
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECIPE_NOT_FOUND));

        // 기존에 좋아요한 게 있는지 확인
        boolean alreadyLiked = recipeEventRepository.existsByRecipeIdAndMemberIdAndLikeFlagTrue(recipeId, member.getId());
        if (alreadyLiked) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        RecipeEvent like = new RecipeEvent();
        like.setRecipe(recipe);
        like.setMember(member);
        like.setLikeFlag(true);
        recipeEventRepository.save(like);

        // 좋아요 수 증가
        recipe.setLikes(recipe.getLikes() + 1);
    }

    // 레시피 좋아요 취소하기
    @Transactional
    public void cancelLikeRecipe(Long recipeId, Member member) {
        RecipeEvent like = recipeEventRepository.findByRecipeIdAndMemberIdAndLikeFlagTrue(recipeId, member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.LIKE_NOT_FOUND));

        // 좋아요한 사용자가 아닌 경우 예외 처리
        if (!like.getMember().getEmail().equals(member.getEmail())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 좋아요(flag)가 맞는지 한 번 확인
        if (!like.isLikeFlag()) {
            throw new CustomException(ErrorCode.INVALID_EVENT_OPERATION);
        }

        like.setLikeFlag(false); // 좋아요 플래그 끄기

        // 좋아요 감소하기
        Recipe recipe = like.getRecipe();
        int currentLikes = recipe.getLikes();
        if (currentLikes > 0) {
            recipe.setLikes(currentLikes - 1);
        }
    }
}
