package BE_Elixir.Elixir.domain.dietLog.service;

import BE_Elixir.Elixir.domain.challenge.event.events.DietLogEvent;
import BE_Elixir.Elixir.domain.challenge.event.events.RecipeEvent;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogRequestDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogResponseDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.MonthlyDietScoreDTO;
import BE_Elixir.Elixir.domain.dietLog.entity.DietLog;
import BE_Elixir.Elixir.domain.dietLog.entity.DietLogIngredient;
import BE_Elixir.Elixir.domain.dietLog.repository.DietLogRepository;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.DietLogType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.s3.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DietLogService {

    private final DietLogRepository dietLogRepository;
    private final MemberRepository memberRepository;
    private final IngredientRepository ingredientRepository;
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;



    // 식단 기록하기
    public DietLogResponseDTO createDietLog(DietLogRequestDTO dto, Long memberId, MultipartFile image) {

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. member id: " + memberId));

        // 식단 타입을 enum 타입으로 변환 및 검사
        DietLogType typeEnum = DietLogType.valueOf(dto.getType().toUpperCase());
        validateDuplicateDietLogType(memberId, typeEnum, dto.getTime());

        // 객체 생성
        DietLog dietLog = dto.toEntity(typeEnum, member);

        // 식단 이미지 업로드 및 url 세팅
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = s3Service.upload(image, "diet_log");

                log.info("이미지 S3에 업로드 성공 imageUrl: {}", imageUrl);
                dietLog.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
            }
        }

        // Ingredient ID 목록으로 Ingredient 엔티티들 조회
        List<Ingredient> ingredients = ingredientRepository.findAllById(dto.getIngredientTagId());

        // DietLogIngredient 리스트 생성
        List<DietLogIngredient> dietLogIngredients = ingredients.stream()
                .map(ingredient -> new DietLogIngredient(dietLog, ingredient))
                .toList();

        // DietLog에 연관관계 설정
        dietLog.setIngredientTags(dietLogIngredients);

        dietLogRepository.save(dietLog);

        // 챌린지 달성을 위한 이벤트 발행
        eventPublisher.publishEvent(new DietLogEvent(member.getId(), dietLog.getId(), dietLog.getType(), LocalDateTime.now()));

        return dietLog.convertToResponseDTO();
    }

    // 식단 기록 삭제하기
    public void deleteDietLog(Long dietLogId, Long memberId) {
        // 식단 기록 객체 찾기
        DietLog dietLog = dietLogRepository.findById(dietLogId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단이 존재하지 않습니다. 식단 ID: " + dietLogId));

        // 권한 확인: 본인만 삭제 가능
        if (!dietLog.getMember().getId().equals(memberId)) {
            throw new SecurityException("해당 식단을 삭제할 권한이 없습니다.");
        }

        // S3 버킷에서 이미지 삭제
        if (dietLog.getImageUrl() != null) {
            s3Service.deleteS3(dietLog.getImageUrl(), "diet_log");
        }
        // 식단 삭제
        dietLogRepository.delete(dietLog);
    }

    // 식단 수정하기
    public DietLogResponseDTO updateDietLog(Long dietLogId, Long memberId, DietLogRequestDTO dto, MultipartFile image) {
        // 기존 식단 조회
        DietLog dietLog = dietLogRepository.findById(dietLogId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단이 존재하지 않습니다. 식단 ID: " + dietLogId));

        // 권한 확인: 본인만 수정 가능
        if (!dietLog.getMember().getId().equals(memberId)) {
            throw new SecurityException("해당 식단을 수정할 권한이 없습니다.");
        }

        // 이름 수정
        if (dto.getName() != null) {
            dietLog.setName(dto.getName());
        }

        // 식단 타입 수정
        if (dto.getType() != null) {
            DietLogType typeEnum = DietLogType.valueOf(dto.getType().toUpperCase());

            // 타입 중복 검사
            if (dto.getTime() != null) {
                validateDuplicateDietLogType(memberId, typeEnum, dto.getTime(), dietLogId);
            }
            else {
                LocalDateTime time = dietLog.getTime();
                validateDuplicateDietLogType(memberId, typeEnum, time, dietLogId);
            }
            dietLog.setType(typeEnum);
        }

        // 시간 수정
        if (dto.getTime() != null) {
            dietLog.setTime(dto.getTime());
        }

        // 이미지 수정
        if (image != null && !image.isEmpty()) {
            // 기존 이미지 삭제
            if (dietLog.getImageUrl() != null) {
                s3Service.deleteS3(dietLog.getImageUrl(), "diet_log");
            }
            // 새 이미지 업로드
            try {
                String imageUrl = s3Service.upload(image, "diet_log");
                dietLog.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
            }
        }

        // 식단 점수 수정
        if (dto.getScore() > 0) {
            dietLog.setScore(dto.getScore());
        }

        // 재료 태그 수정
        if (dto.getIngredientTagId() != null && !dto.getIngredientTagId().isEmpty()) {
            List<Ingredient> ingredients = ingredientRepository.findAllById(dto.getIngredientTagId());

            List<DietLogIngredient> newDietLogIngredients = ingredients.stream()
                    .map(ingredient -> new DietLogIngredient(dietLog, ingredient))
                    .toList();

            dietLog.setIngredientTags(newDietLogIngredients);
        }

        dietLogRepository.save(dietLog);

        return dietLog.convertToResponseDTO();

    }


    // 식단 기록 조회하기
    public DietLogResponseDTO getDietLog(Long dietLogId) {

        // 식단 기록 객체 찾기
        DietLog dietLog = dietLogRepository.findById(dietLogId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단이 존재하지 않습니다. 식단 ID: " + dietLogId));

        return dietLog.convertToResponseDTO();

    }

    // 일별 식단 목록 조회하기
    public List<DietLogResponseDTO> getDietLogByDate(LocalDate date, Long memberId) {

        // 해당 회원이 기록한 특정 날짜의 식단들을 조회 (DB에는 LocalDateTime으로 저장되어 있기 때문)
        // 시작 시각: 00:00:00 / 종료 시각: 23:59:59.999999999
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return dietLogRepository.findAllByMemberIdAndTimeBetween(memberId, startOfDay, endOfDay).stream()
                .map(DietLog::convertToResponseDTO)
                .toList();
    }

    // 월별 식단 점수 조회
    public List<MonthlyDietScoreDTO> getMonthlyDietScores(Long memberId, int year, int month) {
        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        List<DietLog> dietLogs = dietLogRepository.findByMemberIdAndMonthBetween(memberId, start, end);

        return dietLogs.stream()
                .map(dietLog -> MonthlyDietScoreDTO.builder()
                        .id(dietLog.getId())
                        .score(dietLog.getScore())
                        .time(dietLog.getTime())
                        .build())
                .collect(Collectors.toList());
    }

    // 최근 N일 간 식단 기록 조회
    public List<DietLogResponseDTO> getRecentDietLogs(Long memberId, int recentDays){
        LocalDateTime from = LocalDate.now().minusDays(recentDays).atStartOfDay();


        return dietLogRepository.findByMemberIdAndTimeAfter(memberId, from).stream()
                .map(DietLog::convertToResponseDTO)
                .toList();
    }

    // 모든 식단 기록 조회(최신순)
    public List<DietLogResponseDTO> getAllDietLogs(Long memberId) {
        return dietLogRepository.findByMemberIdOrderByTimeDesc(memberId).stream()
                .map(DietLog::convertToResponseDTO)
                .toList();
    }

    // 중복 식사 타입 검사 (create인 경우)
    private void validateDuplicateDietLogType(Long memberId, DietLogType typeEnum, LocalDateTime time) {
        if (typeEnum == DietLogType.간식) return;

        LocalDate date = time.toLocalDate();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 중복 검사 시 현재 수정 대상은 제외
        boolean exists = dietLogRepository.existsByMemberIdAndTypeAndTimeBetween(
                memberId, typeEnum, startOfDay, endOfDay
        );

        if (exists) {
            throw new CustomException(ErrorCode.DIET_LOG_TYPE_DUPLICATE);
        }
    }

    // 중복 식사 타입 검사 (update인 경우)
    private void validateDuplicateDietLogType(Long memberId, DietLogType typeEnum, LocalDateTime time, Long excludeDietLogId) {
        if (typeEnum == DietLogType.간식) return;

        LocalDate date = time.toLocalDate();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 중복 검사 시 현재 수정 대상은 제외
        boolean exists = dietLogRepository.existsByMemberIdAndTypeAndTimeBetweenAndIdNot(
                memberId, typeEnum, startOfDay, endOfDay, excludeDietLogId
        );

        if (exists) {
            throw new CustomException(ErrorCode.DIET_LOG_TYPE_DUPLICATE);
        }
    }


}