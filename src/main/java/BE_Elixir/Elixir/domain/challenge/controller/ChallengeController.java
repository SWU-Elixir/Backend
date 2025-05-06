package BE_Elixir.Elixir.domain.challenge.controller;


import BE_Elixir.Elixir.domain.challenge.controller.api.ChallengeApi;
import BE_Elixir.Elixir.domain.challenge.dto.request.ChallengeRequestDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeDetailResponseDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeListResponseDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeResponseDTO;
import BE_Elixir.Elixir.domain.challenge.service.ChallengeService;
import BE_Elixir.Elixir.global.response.CommonResponse;
import BE_Elixir.Elixir.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/challenge")
@RequiredArgsConstructor
public class ChallengeController implements ChallengeApi {
    private final ChallengeService challengeService;
    private final S3Service s3Service;

    // 챌린지 등록하기(admin)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<ChallengeResponseDTO>> registerAchievement(
            @RequestPart("dto") ChallengeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "grayImage", required = false) MultipartFile grayImage
    ) {
        try {
            ChallengeResponseDTO response = challengeService.registerChallenge(dto, image, grayImage);
            return ResponseEntity.ok(CommonResponse.success(
                    HttpStatus.OK.value(), HttpStatus.OK.toString(),
                    "챌린지 등록 완료", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "챌린지 등록 실패 - " + e.getMessage()));
        }
    }

    // 연도 별 챌린지 조회하기
    @GetMapping("/year/{year}")
    public ResponseEntity<CommonResponse<List<ChallengeListResponseDTO>>> getChallengesByYear(
            @PathVariable int year
    ) {
        try {
            List<ChallengeListResponseDTO> result = challengeService.getChallengesByYear(year);
            return ResponseEntity.ok(CommonResponse.success(
                    HttpStatus.OK.value(), HttpStatus.OK.toString(),
                    year + "년도 챌린지 목록 조회 성공", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            year + "년도 챌린지 목록 조회 실패 - " + e.getMessage()));
        }

    }
    // 특정 챌린지 상세 조회하기
    @GetMapping("/{challengeId}")
    public ResponseEntity<CommonResponse<ChallengeDetailResponseDTO>> getChallengeDetail(
            @PathVariable Long challengeId
    ) {
        try {
            ChallengeDetailResponseDTO result = challengeService.getChallengeDetail(challengeId);
            return ResponseEntity.ok(CommonResponse.success(
                    HttpStatus.OK.value(), HttpStatus.OK.toString(),
                    "챌린지 상세 조회 성공", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "챌린지 상세 조회 실패 - " + e.getMessage()));
        }
    }
}
