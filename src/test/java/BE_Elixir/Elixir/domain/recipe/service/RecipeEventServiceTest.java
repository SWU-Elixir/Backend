package BE_Elixir.Elixir.domain.recipe.service;

import BE_Elixir.Elixir.domain.achievement.service.MemberStatsService;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.MaterialDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeCommentCreateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeCommentUpdateRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeCommentResponseDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeIngredient;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.Difficulty;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeEventService 테스트")
class RecipeEventServiceTest {
    @InjectMocks
    RecipeEventService recipeEventService;

    @Mock RecipeRepository recipeRepository;
    @Mock RecipeEventRepository recipeEventRepository;
    @Mock MemberStatsService memberStatsService;

    private RecipeRequestDTO requestDTO;
    private Recipe recipe;
    private Member member;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testUser")
                .build();

        pageable = PageRequest.of(0, 10);

        requestDTO = createSampleRecipeRequestDTO();

        recipe = Recipe.from(requestDTO, member);
        Ingredient ingredient = new Ingredient();
        ingredient.setId(10L);
        ingredient.setName("감자");

        RecipeIngredient recipeIngredient = new RecipeIngredient(recipe, ingredient);
        recipe.setIngredientTags(List.of(recipeIngredient));
    }

    private RecipeRequestDTO createSampleRecipeRequestDTO() {
        RecipeRequestDTO dto = new RecipeRequestDTO();
        dto.setTitle("감자조림");
        dto.setDescription("감자조림에 대한 설명");
        dto.setCategoryType(CategoryType.한식);
        dto.setCategorySlowAging(CategorySlowAging.항산화강화);
        dto.setDifficulty(Difficulty.보통);
        dto.setTimeHours(1);
        dto.setTimeMinutes(20);
        dto.setIngredientTagIds(List.of(10L));

        MaterialDTO ingredient = new MaterialDTO("감자", "2", "개");
        MaterialDTO seasoning1 = new MaterialDTO("간장", "2", "큰술");
        MaterialDTO seasoning2 = new MaterialDTO("설탕", "1", "큰술");
        dto.setIngredients(List.of(ingredient));
        dto.setSeasonings(List.of(seasoning1, seasoning2));

        dto.setStepDescriptions(List.of("1단계"));
        dto.setTips("팁");
        dto.setAllergies(List.of("우유", "밀"));

        return dto;
    }


    @Nested
    @DisplayName("댓글 등록 테스트")
    class AddCommentTests {

        @Test
        @DisplayName("성공: 댓글 등록")
        void addComment_Success() {
            // given
            RecipeCommentCreateRequestDTO requestDTO = new RecipeCommentCreateRequestDTO(1L, "내용");
            given(recipeRepository.findById(1L)).willReturn(Optional.of(recipe));

            RecipeEvent savedEvent = RecipeEvent.createRecipeComment(recipe, requestDTO, member);
            given(recipeEventRepository.save(any())).willReturn(savedEvent);

            // when
            RecipeCommentResponseDTO result = recipeEventService.addComment(requestDTO, member);

            // then
            assertEquals("내용", result.getContent());
            verify(recipeRepository).findById(1L);
            verify(recipeEventRepository).save(any());
        }

        @Test
        @DisplayName("예외: 존재하지 않는 레시피에 댓글 등록 시도")
        void addComment_Failure_RecipeNotFound() {
            // given
            RecipeCommentCreateRequestDTO requestDTO = new RecipeCommentCreateRequestDTO(1L, "내용");
            given(recipeRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recipeEventService.addComment(requestDTO, member))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.RECIPE_NOT_FOUND.getMessage());

            verify(recipeEventRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    class EditCommentTests {

        @Test
        @DisplayName("성공: 댓글 수정")
        void editComment_Success() {
            // given
            RecipeCommentUpdateRequestDTO requestDTO = new RecipeCommentUpdateRequestDTO(1L, 1L, "수정된 내용");
            RecipeEvent comment = RecipeEvent.createRecipeComment(recipe, new RecipeCommentCreateRequestDTO(1L, "원래 내용"), member);
            given(recipeEventRepository.findById(1L)).willReturn(Optional.of(comment));

            // when
            RecipeCommentResponseDTO result = recipeEventService.editComment(requestDTO, member);

            // then
            assertEquals("수정된 내용", result.getContent());
        }

        @Test
        @DisplayName("예외: 댓글 작성자가 아님")
        void editComment_Failure_ForbiddenAccess() {
            // given
            Member other = Member.builder().id(2L).email("other@test.com").build();
            RecipeEvent comment = RecipeEvent.createRecipeComment(recipe, new RecipeCommentCreateRequestDTO(1L, "내용"), member);
            given(recipeEventRepository.findById(1L)).willReturn(Optional.of(comment));

            RecipeCommentUpdateRequestDTO requestDTO = new RecipeCommentUpdateRequestDTO(1L, 1L, "변경");

            // when & then
            assertThatThrownBy(() -> recipeEventService.editComment(requestDTO, other))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.FORBIDDEN_ACCESS.getMessage());
        }

        @Test
        @DisplayName("예외: 댓글이 존재하지 않음")
        void editComment_Failure_CommentNotFound() {
            // given
            given(recipeEventRepository.findById(1L)).willReturn(Optional.empty());
            RecipeCommentUpdateRequestDTO requestDTO = new RecipeCommentUpdateRequestDTO(1L, 1L, "변경");

            // when & then
            assertThatThrownBy(() -> recipeEventService.editComment(requestDTO, member))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.COMMENT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("댓글 삭제 테스트")
    class DeleteCommentTests {

        @Test
        @DisplayName("성공: 댓글 삭제")
        void deleteComment_Success() {
            // given
            RecipeEvent comment = RecipeEvent.createRecipeComment(recipe, new RecipeCommentCreateRequestDTO(1L, "내용"), member);
            given(recipeEventRepository.findById(1L)).willReturn(Optional.of(comment));

            // when
            recipeEventService.deleteComment(1L, member);

            // then
            verify(recipeEventRepository).delete(comment);
        }

        @Test
        @DisplayName("예외: 댓글 작성자가 아님")
        void deleteComment_Failure_ForbiddenAccess() {
            // given
            Member other = Member.builder().id(2L).email("other@test.com").build();
            RecipeEvent comment = RecipeEvent.createRecipeComment(recipe, new RecipeCommentCreateRequestDTO(1L, "내용"), member);
            given(recipeEventRepository.findById(1L)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> recipeEventService.deleteComment(1L, other))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.FORBIDDEN_ACCESS.getMessage());
        }

        @Test
        @DisplayName("예외: 댓글이 존재하지 않음")
        void deleteComment_Failure_CommentNotFound() {
            // given
            given(recipeEventRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recipeEventService.deleteComment(1L, member))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.COMMENT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("댓글 조회 테스트")
    class GetCommentsByRecipeId {

        @Test
        @DisplayName("성공: 레시피 ID로 댓글 목록 조회")
        void getComments_Success() {
            // given
            RecipeEvent comment1 = new RecipeEvent();
            comment1.setRecipe(recipe);
            comment1.setMember(member);
            RecipeEvent comment2 = new RecipeEvent();
            comment2.setRecipe(recipe);
            comment2.setMember(member);
            given(recipeEventRepository.findAllByRecipeId(1L))
                    .willReturn(Arrays.asList(comment1, comment2));

            // when
            List<RecipeCommentResponseDTO> result = recipeEventService.getCommentsByRecipeId(1L);

            // then
            assertThat(result).hasSize(2);
            verify(recipeEventRepository, times(1)).findAllByRecipeId(1L);
        }

        @Test
        @DisplayName("성공: 댓글이 없는 경우 빈 리스트 반환")
        void getComments_EmptyList() {
            // given
            given(recipeEventRepository.findAllByRecipeId(1L))
                    .willReturn(Collections.emptyList());

            // when
            List<RecipeCommentResponseDTO> result = recipeEventService.getCommentsByRecipeId(1L);

            // then
            assertThat(result).isEmpty();
            verify(recipeEventRepository, times(1)).findAllByRecipeId(1L);
        }
    }

    @Nested
    @DisplayName("스크랩 등록 테스트")
    class ScrapRecipeTests {

        @Test
        @DisplayName("성공: 레시피 스크랩")
        void scrapRecipe_Success() {
            // given
            given(recipeRepository.findById(1L)).willReturn(Optional.of(recipe));
            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(1L, 1L))
                    .willReturn(false);

            // when
            recipeEventService.scrapRecipe(1L, member);

            // then
            verify(recipeEventRepository).save(any(RecipeEvent.class));
            verify(memberStatsService).increaseStat(1L, AchievementType.TOTAL_SCRAPS, 1);
        }

        @Test
        @DisplayName("예외: 레시피 없음")
        void scrapRecipe_NotFound() {
            // given
            given(recipeRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recipeEventService.scrapRecipe(1L, member))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.RECIPE_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예외: 이미 스크랩함")
        void scrapRecipe_AlreadyScrapped() {
            // given
            given(recipeRepository.findById(1L)).willReturn(Optional.of(recipe));
            given(recipeEventRepository.existsByRecipeIdAndMemberIdAndScrapFlagTrue(1L, 1L))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> recipeEventService.scrapRecipe(1L, member))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.ALREADY_SCRAPPED.getMessage());
        }
    }

    @Nested
    @DisplayName("스크랩 취소 테스트")
    class CancelScrapRecipeTests {

        @Test
        @DisplayName("성공: 스크랩 취소")
        void cancelScrap_Success() {
            // given
            RecipeEvent scrap = new RecipeEvent();
            scrap.setMember(member);
            scrap.setScrapFlag(true);
            given(recipeEventRepository.findByRecipeIdAndMemberIdAndScrapFlagTrue(1L, 1L))
                    .willReturn(Optional.of(scrap));

            // when
            recipeEventService.cancelScrapRecipe(1L, member);

            // then
            verify(recipeEventRepository).delete(scrap);
            verify(memberStatsService).increaseStat(1L, AchievementType.TOTAL_SCRAPS, -1);
        }

        @Test
        @DisplayName("예외: 스크랩 내역 없음")
        void cancelScrap_NotFound() {
            // given
            given(recipeEventRepository.findByRecipeIdAndMemberIdAndScrapFlagTrue(1L, 1L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> recipeEventService.cancelScrapRecipe(1L, member))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.SCRAP_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예외: 스크랩 취소할 권한이 없음")
        void cancelScrap_Forbidden() {
            // given
            Member otherMember = Member.builder()
                    .id(2L)
                    .email("otherMember@test.com")
                    .nickname("otherMember")
                    .build();

            RecipeEvent scrap = new RecipeEvent();
            scrap.setMember(otherMember);
            scrap.setScrapFlag(true);
            given(recipeEventRepository.findByRecipeIdAndMemberIdAndScrapFlagTrue(1L, 1L))
                    .willReturn(Optional.of(scrap));

            // when & then
            assertThatThrownBy(() -> recipeEventService.cancelScrapRecipe(1L, member))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ErrorCode.FORBIDDEN_ACCESS.getMessage());
        }
    }
}