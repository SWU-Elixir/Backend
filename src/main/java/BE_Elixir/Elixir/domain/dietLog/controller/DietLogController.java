package BE_Elixir.Elixir.domain.dietLog.controller;

import BE_Elixir.Elixir.domain.dietLog.controller.api.DietLogApi;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogRequestDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogResponseDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.MonthlyDietScoreDTO;
import BE_Elixir.Elixir.domain.dietLog.service.DietLogService;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/diet-log")
@RequiredArgsConstructor
public class DietLogController implements DietLogApi {

    private final DietLogService dietLogService;

    // 식단 기록하기
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<DietLogResponseDTO>> createDietLog(
            @RequestPart("dto") DietLogRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("식단 기록 요청");
        Long memberId = memberDetails.getId();

        try {
            DietLogResponseDTO responseDTO = dietLogService.createDietLog(dto, memberId, image);
            log.info("식단 기록 성공 - 회원 ID: {}, 식단 ID: {}", memberId, responseDTO.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CommonResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.toString(),
                            "식단 기록 성공 - 회원 ID:" + memberId + ",  식단 ID: " + responseDTO.getId(), responseDTO));

        } catch (Exception e) {
            log.error("식단 기록 실패 - 회원 ID: {}, 메시지: {}", memberId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "식단 기록 실패: " + e.getMessage()));
        }
    }

    // 식단 삭제하기
    @DeleteMapping("/{DietLogId}")
    public ResponseEntity<CommonResponse<?>> deleteDietLog(
            @PathVariable("DietLogId") Long DietLogId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("식단 기록 삭제 요청");
        Long memberId = memberDetails.getId();

        try {
            dietLogService.deleteDietLog(DietLogId, memberId);
            log.info("식단 삭제 성공 - 회원 ID: {}, 식단 ID: {}", memberId, DietLogId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "식단 삭제 성공 - 회원 ID:" + memberId + ",  식단 ID: " + DietLogId));

        } catch (Exception e) {
            log.error("식단 삭제 실패 - 회원 ID: {}, 메시지: {}", memberId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "식단 삭제 실패: " + e.getMessage()));
        }
    }

    // 식단 수정하기
    @PatchMapping(value="/{dietLogId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<DietLogResponseDTO>> updateDietLog(
            @PathVariable Long dietLogId,
            @RequestPart("dto") DietLogRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("식단 기록 수정 요청");
        Long memberId = memberDetails.getId();

        try {
            DietLogResponseDTO responseDTO = dietLogService.updateDietLog(dietLogId, memberId, dto, image);
            log.info("식단 수정 성공 - 회원 ID: {}, 식단 ID: {}", memberId, dietLogId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                            "식단 수정 성공 - 회원 ID:" + memberId + ",  식단 ID: " + dietLogId, responseDTO));

        } catch (Exception e) {
            log.error("식단 수정 실패 - 회원 ID: {}, 식단 ID: {}, 메시지: {}", memberId, e.getMessage(), dietLogId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "식단 수정 실패: " + e.getMessage()));
        }
    }

    // 식단 조회하기
    @GetMapping("/{DietLogId}")
    public ResponseEntity<CommonResponse<DietLogResponseDTO>> getDietLog(
            @PathVariable("DietLogId") Long DietLogId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("식단 조회 요청");
        Long memberId = memberDetails.getId();

        try {
            DietLogResponseDTO responseDTO = dietLogService.getDietLog(DietLogId);
            log.info("식단 조회 성공 - 회원 ID: {}, 식단 ID: {}", memberId, DietLogId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                            "식단 조회 성공 - 회원 ID:" + memberId + ",  식단 ID: " + DietLogId, responseDTO));

        } catch (Exception e) {
            log.error("식단 조회 실패 - 회원 ID: {}, 메시지: {}", memberId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "식단 조회 실패: " + e.getMessage()));
        }
    }

    // 일별 식단 조회하기 (List<DietLogDTO>)
    @GetMapping("/by-date/{date}")
    public ResponseEntity<CommonResponse<List<DietLogResponseDTO>>> getDietLogByDate(
            @PathVariable("date") LocalDate date,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("일별 식단 목록 조회 요청");
        Long memberId = memberDetails.getId();

        try {
            List<DietLogResponseDTO> responseDTO = dietLogService.getDietLogByDate(date, memberId);
            log.info("일별 식단 목록 조회 성공 - 회원 ID: {}, 날짜: {}", memberId, date);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                            "일별 식단 목록 조회 성공 - 회원 ID:" + memberId + ",  날짜: " + date, responseDTO));

        } catch (Exception e) {
            log.error("일별 식단 목록 조회 실패 - 회원 ID: {}, 날짜: {}, 메시지: {}", memberId, e.getMessage(), date, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "일별 식단 목록 조회 실패: " + e.getMessage()));
        }
    }

    // 월별 식단별 점수 조회하기 (List<점수 DTO>)
    @GetMapping("/monthly-score/{year}/{month}")
    public ResponseEntity<CommonResponse<List<MonthlyDietScoreDTO>>> getMonthlyDietScores(
            @PathVariable("year") int year,
            @PathVariable("month") int month,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("월별 식단별 점수 조회 요청");
        Long memberId = memberDetails.getId();

        try {
            List<MonthlyDietScoreDTO> responseDTO = dietLogService.getMonthlyDietScores(memberId, year, month);
            log.info("월별 식단별 점수 조회 성공 - 회원 ID: {}, 연도: {}, 월: {}", memberId, year, month);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                            "월별 식단별 점수 조회 성공 - 회원 ID:" + memberId + ",  연도: " + year + ", 월: " + month, responseDTO));

        } catch (Exception e) {
            log.error("월별 식단별 점수 조회 실패 - 회원 ID: {}, 연도: {}, 월: {}, 메시지: {}", memberId, e.getMessage(), year, month, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "월별 식단별 점수 조회 실패: " + e.getMessage()));
        }
    }

    // 최근 N일 식단 조회하기
    @GetMapping("/recent")
    public ResponseEntity<CommonResponse<List<DietLogResponseDTO>>> getRecentDietLogs(
            @RequestParam(value = "days", defaultValue = "14") int days,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("최근 N일 식단 목록 조회 요청");
        Long memberId = memberDetails.getId();

        try {
            List<DietLogResponseDTO> responseDTO = dietLogService.getRecentDietLogs(memberId, days);
            log.info("최근 N일 식단 목록 조회 성공 - 회원 ID: {}", memberId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                            "최근 N일 식단 목록 조회 성공 - 회원 ID:" + memberId, responseDTO));

        } catch (Exception e) {
            log.error("최근 N일 식단 목록 조회 실패 - 회원 ID: {}, 메시지: {}", memberId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "최근 N일 식단 목록 조회 실패: " + e.getMessage()));
        }
    }
}