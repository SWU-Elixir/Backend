package BE_Elixir.Elixir.domain.dietLog.service;

import BE_Elixir.Elixir.domain.achievement.service.MemberStatsService;
import BE_Elixir.Elixir.domain.challenge.event.events.DietLogEvent;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogRequestDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogResponseDTO;
import BE_Elixir.Elixir.domain.dietLog.entity.DietLog;
import BE_Elixir.Elixir.domain.dietLog.entity.DietLogIngredient;
import BE_Elixir.Elixir.domain.dietLog.repository.DietLogRepository;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.enums.DietLogType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.s3.S3Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DietLogService 테스트")
class DietLogServiceTest {

    @InjectMocks
    private DietLogService dietLogService;

    @Mock private DietLogRepository dietLogRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private IngredientRepository ingredientRepository;
    @Mock private S3Service s3Service;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private MemberStatsService memberStatsService;
    @Mock private MultipartFile image;

    private Member member;
    private DietLog dietLog;
    private DietLogRequestDTO dietLogRequestDTO;
    private List<Ingredient> ingredients;
    private List<DietLogIngredient> dietLogIngredients;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testUser")
                .build();

        ingredients = Arrays.asList(
                Ingredient.builder().id(1L).name("계란").build(),
                Ingredient.builder().id(2L).name("토마토").build(),
                Ingredient.builder().id(3L).name("양파").build()
        );

        dietLog = DietLog.builder()
                .id(1L)
                .name("아침식사")
                .type(DietLogType.아침)
                .time(LocalDateTime.now())
                .score(5)
                .imageUrl("http://s3.com/image.jpg")
                .member(member)
                .build();

        // DietLogIngredient 중간 엔티티 설정
        dietLogIngredients = ingredients.stream()
                .map(ingredient -> new DietLogIngredient(dietLog, ingredient))
                .toList();

        // DietLog에 ingredientTags 설정
        dietLog.setIngredientTags(dietLogIngredients);

        dietLogRequestDTO = DietLogRequestDTO.builder()
                .name("아침식사")
                .type("아침")
                .time(LocalDateTime.now())
                .score(5)
                .ingredientTagId(Arrays.asList(1L, 2L, 3L))
                .build();
    }

    @Nested
    @DisplayName("식단 기록하기 테스트")
    class CreateDietLog {

        @Test
        @DisplayName("성공: 이미지 있음")
        void should_CreateDietLogSuccessfully_When_ValidRequestWithImage() throws IOException {
            // given
            Long memberId = 1L;
            String imageUrl = "http://s3.com/diet_log/image.jpg";

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(dietLogRepository.existsByMemberIdAndTypeAndTimeBetween(anyLong(), any(DietLogType.class), any(), any()))
                    .willReturn(false);
            given(image.isEmpty()).willReturn(false);
            given(s3Service.upload(image, "diet_log")).willReturn(imageUrl);
            given(ingredientRepository.findAllById(anyList())).willReturn(ingredients);
            given(dietLogRepository.save(any(DietLog.class))).willReturn(dietLog);

            // when
            DietLogResponseDTO result = dietLogService.createDietLog(dietLogRequestDTO, memberId, image);

            // then
            assertThat(result).isNotNull();
            verify(memberRepository).findById(memberId);
            verify(s3Service).upload(image, "diet_log");
            verify(ingredientRepository).findAllById(dietLogRequestDTO.getIngredientTagId());
            verify(dietLogRepository).save(any(DietLog.class));
            verify(eventPublisher).publishEvent(any(DietLogEvent.class));
            verify(memberStatsService).increaseStat(memberId, AchievementType.TOTAL_DIET_LOGS, 1);
        }

        @Test
        @DisplayName("성공: 이미지 없음")
        void should_CreateDietLogSuccessfully_When_ValidRequestWithoutImage() throws IOException {
            // given
            Long memberId = 1L;

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(dietLogRepository.existsByMemberIdAndTypeAndTimeBetween(anyLong(), any(DietLogType.class), any(), any()))
                    .willReturn(false);
            given(ingredientRepository.findAllById(anyList())).willReturn(ingredients);
            given(dietLogRepository.save(any(DietLog.class))).willReturn(dietLog);

            // when
            DietLogResponseDTO result = dietLogService.createDietLog(dietLogRequestDTO, memberId, null);

            // then
            assertThat(result).isNotNull();
            verify(memberRepository).findById(memberId);
            verify(s3Service, never()).upload(any(), anyString());
            verify(ingredientRepository).findAllById(dietLogRequestDTO.getIngredientTagId());
            verify(dietLogRepository).save(any(DietLog.class));
            verify(eventPublisher).publishEvent(any(DietLogEvent.class));
            verify(memberStatsService).increaseStat(memberId, AchievementType.TOTAL_DIET_LOGS, 1);
        }

        @Test
        @DisplayName("실패: 회원 존재하지 않음")
        void should_ThrowException_When_MemberNotFound() {
            // given
            Long memberId = 999L;
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> dietLogService.createDietLog(dietLogRequestDTO, memberId, image))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            verify(memberRepository).findById(memberId);
            verify(dietLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 중복된 식단 타입")
        void should_ThrowException_When_DuplicateDietLogType() {
            // given
            Long memberId = 1L;

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(dietLogRepository.existsByMemberIdAndTypeAndTimeBetween(anyLong(), any(DietLogType.class), any(), any()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> dietLogService.createDietLog(dietLogRequestDTO, memberId, image))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIETLOG_TYPE_DUPLICATE);

            verify(memberRepository).findById(memberId);
            verify(dietLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: S3 업로드 실패")
        void should_ThrowException_When_S3UploadFails() throws IOException {
            // given
            Long memberId = 1L;

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(dietLogRepository.existsByMemberIdAndTypeAndTimeBetween(anyLong(), any(DietLogType.class), any(), any()))
                    .willReturn(false);
            given(image.isEmpty()).willReturn(false);
            given(s3Service.upload(image, "diet_log")).willThrow(new IOException());

            // when & then
            assertThatThrownBy(() -> dietLogService.createDietLog(dietLogRequestDTO, memberId, image))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.S3_UPLOAD_ERROR);

            verify(memberRepository).findById(memberId);
            verify(s3Service).upload(image, "diet_log");
            verify(dietLogRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("식단 수정하기 테스트")
    class UpdateDietLog {

        @Test
        @DisplayName("성공: 모든 필드 수정")
        void should_UpdateDietLogSuccessfully_When_ValidRequest() throws IOException {
            // given
            Long dietLogId = 1L;
            Long memberId = 1L;
            String newImageUrl = "http://s3.com/new_image.jpg";
            String imageUrl = dietLog.getImageUrl();

            DietLogRequestDTO updateDTO = DietLogRequestDTO.builder()
                    .name("점심식사")
                    .type("점심")
                    .time(LocalDateTime.now().plusHours(4))
                    .score(4)
                    .ingredientTagId(Arrays.asList(4L, 5L))
                    .build();

            given(dietLogRepository.findById(dietLogId)).willReturn(Optional.of(dietLog));
            given(dietLogRepository.existsByMemberIdAndTypeAndTimeBetweenAndIdNot(
                    anyLong(), any(DietLogType.class), any(), any(), anyLong())).willReturn(false);
            given(image.isEmpty()).willReturn(false);
            given(s3Service.upload(image, "diet_log")).willReturn(newImageUrl);
            given(ingredientRepository.findAllById(anyList())).willReturn(ingredients);
            given(dietLogRepository.save(any(DietLog.class))).willReturn(dietLog);

            // when
            DietLogResponseDTO result = dietLogService.updateDietLog(dietLogId, memberId, updateDTO, image);

            // then
            assertThat(result).isNotNull();
            verify(dietLogRepository).findById(dietLogId);
            verify(s3Service).deleteS3(imageUrl, "diet_log");
            verify(s3Service).upload(image, "diet_log");
            verify(ingredientRepository).findAllById(updateDTO.getIngredientTagId());
            verify(dietLogRepository).save(any(DietLog.class));
        }

        @Test
        @DisplayName("성공: 일부 필드만 수정")
        void should_UpdateDietLogSuccessfully_When_PartialUpdate() throws IOException {
            // given
            Long dietLogId = 1L;
            Long memberId = 1L;

            DietLogRequestDTO partialUpdateDTO = DietLogRequestDTO.builder()
                    .name("수정된 아침식사")
                    .score(4)
                    .build();

            given(dietLogRepository.findById(dietLogId)).willReturn(Optional.of(dietLog));
            given(dietLogRepository.save(any(DietLog.class))).willReturn(dietLog);

            // when
            DietLogResponseDTO result = dietLogService.updateDietLog(dietLogId, memberId, partialUpdateDTO, null);

            // then
            assertThat(result).isNotNull();
            verify(dietLogRepository).findById(dietLogId);
            verify(s3Service, never()).upload(any(), anyString());
            verify(s3Service, never()).deleteS3(anyString(), anyString());
            verify(dietLogRepository).save(any(DietLog.class));
        }

        @Test
        @DisplayName("실패: 식단 존재하지 않음")
        void should_ThrowException_When_DietLogNotFound() {
            // given
            Long dietLogId = 999L;
            Long memberId = 1L;

            given(dietLogRepository.findById(dietLogId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> dietLogService.updateDietLog(dietLogId, memberId, dietLogRequestDTO, image))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIETLOG_NOT_FOUND);

            verify(dietLogRepository).findById(dietLogId);
            verify(dietLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 식단 수정 권한 없음 (본인 식단이 아님)")
        void should_ThrowException_When_NoPermissionToUpdate() {
            // given
            Long dietLogId = 1L;
            Long memberId = 2L; // 다른 회원 ID

            given(dietLogRepository.findById(dietLogId)).willReturn(Optional.of(dietLog));

            // when & then
            assertThatThrownBy(() -> dietLogService.updateDietLog(dietLogId, memberId, dietLogRequestDTO, image))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);

            verify(dietLogRepository).findById(dietLogId);
            verify(dietLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 중복된 식단 타입")
        void should_ThrowException_When_DuplicateDietLogTypeOnUpdate() {
            // given
            Long dietLogId = 1L;
            Long memberId = 1L;

            DietLogRequestDTO updateDTO = DietLogRequestDTO.builder()
                    .type("점심")
                    .time(LocalDateTime.now())
                    .build();

            given(dietLogRepository.findById(dietLogId)).willReturn(Optional.of(dietLog));
            given(dietLogRepository.existsByMemberIdAndTypeAndTimeBetweenAndIdNot(
                    anyLong(), any(DietLogType.class), any(), any(), anyLong())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> dietLogService.updateDietLog(dietLogId, memberId, updateDTO, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIETLOG_TYPE_DUPLICATE);

            verify(dietLogRepository).findById(dietLogId);
            verify(dietLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: S3 업로드 실패")
        void should_ThrowException_When_S3UploadFailsOnUpdate() throws IOException {
            // given
            Long dietLogId = 1L;
            Long memberId = 1L;

            given(dietLogRepository.findById(dietLogId)).willReturn(Optional.of(dietLog));
            given(image.isEmpty()).willReturn(false);
            given(s3Service.upload(image, "diet_log")).willThrow(new IOException());

            // when & then
            assertThatThrownBy(() -> dietLogService.updateDietLog(dietLogId, memberId, dietLogRequestDTO, image))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.S3_UPLOAD_ERROR);

            verify(dietLogRepository).findById(dietLogId);
            verify(s3Service).deleteS3(dietLog.getImageUrl(), "diet_log");
            verify(s3Service).upload(image, "diet_log");
            verify(dietLogRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("식단 삭제하기 테스트")
    class DeleteDietLog {

        @Test
        @DisplayName("성공: 이미지 있는 식단 삭제")
        void should_DeleteDietLogSuccessfully_When_ValidRequestWithImage() {
            // given
            Long dietLogId = 1L;
            Long memberId = 1L;

            given(dietLogRepository.findById(dietLogId)).willReturn(Optional.of(dietLog));

            // when
            dietLogService.deleteDietLog(dietLogId, memberId);

            // then
            verify(dietLogRepository).findById(dietLogId);
            verify(s3Service).deleteS3(dietLog.getImageUrl(), "diet_log");
            verify(dietLogRepository).delete(dietLog);
        }

        @Test
        @DisplayName("성공: 이미지 없는 식단 삭제")
        void should_DeleteDietLogSuccessfully_When_ValidRequestWithoutImage() throws IOException {
            // given
            Long dietLogId = 1L;
            Long memberId = 1L;

            DietLog dietLogWithoutImage = DietLog.builder()
                    .id(1L)
                    .name("아침식사")
                    .type(DietLogType.아침)
                    .time(LocalDateTime.now())
                    .score(5)
                    .imageUrl(null)
                    .member(member)
                    .build();

            given(dietLogRepository.findById(dietLogId)).willReturn(Optional.of(dietLogWithoutImage));

            // when
            dietLogService.deleteDietLog(dietLogId, memberId);

            // then
            verify(dietLogRepository).findById(dietLogId);
            verify(s3Service, never()).deleteS3(anyString(), anyString());
            verify(dietLogRepository).delete(dietLogWithoutImage);
        }

        @Test
        @DisplayName("실패: 식단 존재하지 않음")
        void should_ThrowException_When_DietLogNotFoundOnDelete() {
            // given
            Long dietLogId = 999L;
            Long memberId = 1L;

            given(dietLogRepository.findById(dietLogId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> dietLogService.deleteDietLog(dietLogId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DIETLOG_NOT_FOUND);

            verify(dietLogRepository).findById(dietLogId);
            verify(dietLogRepository, never()).delete(any());
        }

        @Test
        @DisplayName("실패: 식단 삭제 권한 없음 (본인 식단이 아님)")
        void should_ThrowException_When_NoPermissionToDelete() {
            // given
            Long dietLogId = 1L;
            Long memberId = 2L; // 다른 회원 ID

            given(dietLogRepository.findById(dietLogId)).willReturn(Optional.of(dietLog));

            // when & then
            assertThatThrownBy(() -> dietLogService.deleteDietLog(dietLogId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);

            verify(dietLogRepository).findById(dietLogId);
            verify(dietLogRepository, never()).delete(any());
        }
    }
}