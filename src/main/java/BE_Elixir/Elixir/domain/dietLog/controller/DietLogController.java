package BE_Elixir.Elixir.domain.dietLog.controller;

import BE_Elixir.Elixir.domain.dietLog.controller.api.DietLogApi;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogRequestDTO;
import BE_Elixir.Elixir.domain.dietLog.entity.DietLog;
import BE_Elixir.Elixir.domain.dietLog.service.DietLogService;
import BE_Elixir.Elixir.domain.member.dto.request.SignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.response.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/diet-log")
@RequiredArgsConstructor
public class DietLogController implements DietLogApi {

    private final DietLogService dietLogService;

    // 식단 기록하기
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<?>> createDietLog(
            @RequestPart("dto") DietLogRequestDTO dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile image,
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    ) {
       log.info("식단 기록 요청");
       Long memberId = memberDetails.getId();

       try {
           DietLog dietLog = dietLogService.createDietLog(dto, memberId, image);
           log.info("식단 기록 성공 - 회원 ID: {}, 식단 ID: {}", memberId, dietLog.getId());
           return ResponseEntity.status(HttpStatus.CREATED)
                   .body(CommonResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.toString(), "식단 기록 성공 - 회원 ID:" + memberId + ",  식단 ID: " + dietLog.getId()));

       } catch (Exception e) {
           log.error("식단 기록 실패 - 회원 ID: {}, 메시지: {}", memberId, e.getMessage(), e);
           return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(CommonResponse.success(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.toString(),
                           "식단 기록 실패: " + e.getMessage(), null));
       }
   }



    // 식단 수정하기


    // 식단 삭제하기


    // 식단 상세 조회하기

    // 일별 식단 조회하기

    // 월별 식단별 점수 조회하기


}
