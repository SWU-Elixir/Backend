package BE_Elixir.Elixir.domain.dietLog.service;

import BE_Elixir.Elixir.domain.dietLog.dto.DietLogRequestDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogResponseDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.MonthlyDietScoreDTO;
import BE_Elixir.Elixir.domain.dietLog.entity.DietLog;
import BE_Elixir.Elixir.domain.dietLog.entity.DietLogIngredient;
import BE_Elixir.Elixir.domain.dietLog.repository.DietLogRepository;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.member.dto.response.MemberResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.DietLogType;
import BE_Elixir.Elixir.global.s3.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    // 식단 기록하기
    public DietLog createDietLog(DietLogRequestDTO dto, Long memberId, MultipartFile image) {
        try {
            // 회원 조회
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. member id: " + memberId));

            // 식단 타입을 enum 타입으로 변환
            DietLogType typeEnum = DietLogType.valueOf(dto.getType().toUpperCase());

            // 객체 생성
            DietLog dietLog = dto.toEntity(typeEnum, member);

            // 식단 이미지 업로드 및 url 세팅
            if (image != null && !image.isEmpty()) {
                String imageUrl = s3Service.upload(image, "diet_log");
                log.info("이미지 S3에 업로드 성공 imageUrl: {}", imageUrl);
                dietLog.setImageUrl(imageUrl);
            }

            // Ingredient ID 목록으로 Ingredient 엔티티들 조회
            List<Ingredient> ingredients = ingredientRepository.findAllById(dto.getIngredientTagId());

            // DietLogIngredient 리스트 생성
            List<DietLogIngredient> dietLogIngredients = ingredients.stream()
                    .map(ingredient -> new DietLogIngredient(dietLog, ingredient))
                    .toList();

            // DietLog에 연관관계 설정
            dietLog.setIngredientTags(dietLogIngredients);

            return dietLogRepository.save(dietLog);

        } catch (Exception e) {
            throw new RuntimeException("식단 기록 중 오류가 발생했습니다.", e);
        }
    }

    // 식단 기록 삭제하기
    public void deleteDietLog(Long dietLogId, Long memberId) {
        // 식단 기록 객체 찾기
        DietLog dietLog = dietLogRepository.findById(dietLogId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단이 존재하지 않습니다. 식단 ID: " + dietLogId));

        // 삭제 권한 확인 (잘못된 id가 들어올 일은 없겠지만, 본인의 식단 기록만 삭제 가능하도록)
        if (!dietLog.getMember().getId().equals(memberId)) {
            throw new SecurityException("해당 식단을 삭제할 권한이 없습니다.");
        }

        // S3 버킷에서 프로필 이미지 삭제
        s3Service.deleteS3(dietLog.getImageUrl(), "diet_log");

        // 회원 삭제
        dietLogRepository.delete(dietLog);
    }

    // 식단 기록 조회하기
    public DietLogResponseDTO getDietLog(Long dietLogId, Long memberId) {
        // 식단 기록 객체 찾기
        DietLog dietLog = dietLogRepository.findById(dietLogId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식단이 존재하지 않습니다. 식단 ID: " + dietLogId));

        // DietLogIngredient -> Ingredient -> id 추출
        List<Long> ingredientTagIds = dietLog.getIngredientTags().stream()
                .map(dietLogIngredient -> dietLogIngredient.getIngredient().getId())
                .toList();

        return DietLogResponseDTO.builder()
                .id(dietLog.getId())
                .memberId(memberId)
                .name(dietLog.getName())
                .imageUrl(dietLog.getImageUrl())
                .type(dietLog.getType().toString())
                .score(dietLog.getScore())
                .ingredientTagId(ingredientTagIds)
                .time(dietLog.getTime())
                .build();
    }

    // 일별 식단 목록 조회하기
    public List<DietLogResponseDTO> getDietLogByDate(LocalDate date, Long memberId) {

        // 해당 회원이 기록한 특정 날짜의 식단들을 조회 (DB에는 LocalDateTime으로 저장되어 있기 때문)
        // 시작 시각: 00:00:00 / 종료 시각: 23:59:59.999999999
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<DietLog> dietLogs = dietLogRepository.findAllByMemberIdAndTimeBetween(memberId, startOfDay, endOfDay);

        return dietLogs.stream()
                .map(dietLog -> {
                    List<Long> ingredientTagIds = dietLog.getIngredientTags().stream()
                            .map(dietLogIngredient -> dietLogIngredient.getIngredient().getId())
                            .toList();

                    return DietLogResponseDTO.builder()
                            .id(dietLog.getId())
                            .memberId(memberId)
                            .name(dietLog.getName())
                            .imageUrl(dietLog.getImageUrl())
                            .type(dietLog.getType().toString())
                            .score(dietLog.getScore())
                            .ingredientTagId(ingredientTagIds)
                            .time(dietLog.getTime())
                            .build();
                })
                .toList();
    }

    // 월별 식단 점수 조회
    public List<MonthlyDietScoreDTO> getMonthlyDietScores(Long memberId, int year, int month) {
        // 해당 월에 대한 모든 식단 조회
        List<DietLog> dietLogs = dietLogRepository.findByMemberIdAndYearAndMonth(memberId, year, month);

        return dietLogs.stream()
                .map(dietLog -> MonthlyDietScoreDTO.builder()
                        .id(dietLog.getId())
                        .score(dietLog.getScore())
                        .time(dietLog.getTime())
                        .build())
                .collect(Collectors.toList());

    }
}