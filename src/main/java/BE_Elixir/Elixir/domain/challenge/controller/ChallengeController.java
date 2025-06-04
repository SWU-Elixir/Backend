package BE_Elixir.Elixir.domain.challenge.controller;


import BE_Elixir.Elixir.domain.challenge.controller.api.ChallengeApi;
import BE_Elixir.Elixir.domain.challenge.dto.request.ChallengeRequestDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.*;
import BE_Elixir.Elixir.domain.challenge.service.ChallengeAchievementService;
import BE_Elixir.Elixir.domain.challenge.service.ChallengeService;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.response.CommonResponse;
import BE_Elixir.Elixir.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/challenge")
@RequiredArgsConstructor
public class ChallengeController implements ChallengeApi {
    private final ChallengeService challengeService;
    private final ChallengeAchievementService challengeAchievementService;
    private final S3Service s3Service;

    // 챌린지 등록하기(admin)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<ChallengeResponseDTO>> registerAchievement(
            @RequestPart("dto") ChallengeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "grayImage", required = false) MultipartFile grayImage
    ) {
        ChallengeResponseDTO response = challengeService.registerChallenge(dto, image, grayImage);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "챌린지 등록 완료", response));
    }

    // 연도 별 챌린지 조회하기
    @GetMapping("/year/{year}")
    public ResponseEntity<CommonResponse<List<ChallengeListResponseDTO>>> getChallengesByYear(
            @PathVariable int year
    ) {
        List<ChallengeListResponseDTO> result = challengeService.getChallengesByYear(year);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                year + "년도 챌린지 목록 조회 성공", result));
    }
    // 특정 챌린지 상세 조회하기
    @GetMapping("/{challengeId}")
    public ResponseEntity<CommonResponse<ChallengeDetailResponseDTO>> getChallengeDetail(
            @PathVariable Long challengeId
    ) {
        ChallengeDetailResponseDTO result = challengeService.getChallengeDetail(challengeId);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "챌린지 상세 조회 성공", result));
    }

    // 로그인한 사용자의 현재 진행 상황 조회
    @GetMapping("/progress")
    public ResponseEntity<CommonResponse<ChallengeProgressResponseDTO>> getProgress(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Member member = memberDetails.getMember();
        ChallengeProgressResponseDTO dto = challengeAchievementService.getProgress(member.getId());
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                memberDetails.getUsername() + " 사용자의 현재 진행 상황 조회 성공", dto));
    }

    // 챌린지 최종 완료 여부 조회
    @GetMapping("/completion")
    public ResponseEntity<CommonResponse<ChallengeCompletedResponseDTO>> getChallengeCompletion(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Member member = memberDetails.getMember();
        ChallengeCompletedResponseDTO dto = challengeAchievementService.getChallengeCompletion(member.getId());
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "챌린지 최종 완료 여부 조회 성공", dto));
    }

    // 로그인한 사용자의 이전 챌린지 진행 상황 조회
    @GetMapping("/{challengeId}/progress")
    public ResponseEntity<CommonResponse<ChallengeProgressResponseDTO>> getProgress(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long challengeId
    ) {
        Member member = memberDetails.getMember();
        ChallengeProgressResponseDTO dto = challengeAchievementService.getBeforeProgress(member.getId(), challengeId);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                memberDetails.getUsername() + " 사용자의 이전 챌린지 진행 상황 조회 성공", dto));
    }
}
