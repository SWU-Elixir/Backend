package BE_Elixir.Elixir.domain.member.controller;

import BE_Elixir.Elixir.domain.member.controller.api.MyPageApi;
import BE_Elixir.Elixir.domain.member.dto.request.MemberProfileRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SurveyRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.response.*;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.service.MyPageService;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeImageResponseDTO;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MyPageController implements MyPageApi {

    private final MyPageService myPageService;

    // 회원 정보 조회 (이메일, 닉네임, 젠더, 생년, 프로필 url)
    @GetMapping()
    public ResponseEntity<CommonResponse<MemberResponseDTO>> getMemberInfo(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request)
    {
        String email = memberDetails.getUsername();
        log.info("회원 정보 조회 요청 - email: {}", email);

        MemberResponseDTO response = myPageService.getMemberInfo(email);
        log.info("회원 정보 조회 성공 - email: {}", email);

        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "회원조회 성공", response));
    }

    // 로그인한 사용자 프로필 조회 (칭호, 닉네임, 프로필사진, 팔로워 수, 팔로잉 수)
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse<MemberProfileResponseDTO>> getMemberProfile(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();
        log.info("로그인한 사용자의 프로필 조회 요청 - id: {}", memberId);

        MemberProfileResponseDTO responseDTO = myPageService.getMemberProfile(memberId);

        log.info("로그인한 사용자의 프로필 조회 성공 - id: {}", memberId);

        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "로그인한 사용자의 프로필 조회 성공", responseDTO));
    }

    // 특정 사용자 프로필 조회 (칭호, 닉네임, 프로필사진, 팔로워 수, 팔로잉 수)
    @GetMapping("/{memberId}/profile")
    public ResponseEntity<CommonResponse<MemberProfileResponseDTO>> getOtherMemberProfile(
            @PathVariable("memberId") Long memberId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("특정 사용자의 프로필 조회 요청 - id: {}", memberId);

        MemberProfileResponseDTO responseDTO = myPageService.getMemberProfile(memberId);

        log.info("특정 사용자의 프로필 조회 성공 - id: {}", memberId);

        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "로그인한 사용자의 프로필 조회 성공", responseDTO));
    }

    // 로그인한 사용자 프로필 수정 시, 얻은 칭호 목록 조회
    @GetMapping("/achievement/title")
    public ResponseEntity<CommonResponse<MemberTitlesResponseDTO>> getTitles(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();
        log.info("칭호 목록 조회 요청 - id: {}", memberId);

        List<String> titles = myPageService.getTitles(memberId);
        MemberTitlesResponseDTO dto = MemberTitlesResponseDTO.builder()
                .memberId(memberId)
                .titles(titles)
                .build();

        log.info("칭호 목록 조회 성공 - id: {}", memberId);

        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "칭호 목록 성공", dto));
    }

    // 로그인한 사용자 프로필 수정하기
    @PatchMapping(value="/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<MemberResponseDTO>> updateMemberProfile(
            @RequestPart(value = "dto", required = false) MemberProfileRequestDTO dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();
        log.info("사용자 프로필 수정 요청 - id: {}", memberId);

        MemberResponseDTO responseDTO = myPageService.updateMemberProfile(memberId, dto, profileImage);

        log.info("사용자 프로필 수정 성공 - id: {}", memberId);

        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "사용자 프로필 수정 성공", responseDTO));
    }

    // 로그인한 사용자의 설문조사 결과 조회하기
    @GetMapping("/survey")
    public ResponseEntity<CommonResponse<SurveyResponseDTO>> getSurvey(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        SurveyResponseDTO responseDTO = myPageService.getSurvey(memberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "설문조사 결과 조회 성공, 회원 ID: " + memberId, responseDTO));
    }

    // 로그인한 사용자의 설문조사 결과 수정하기
    @PutMapping("/survey")
    public ResponseEntity<CommonResponse<SurveyResponseDTO>> updateSurvey(
            @RequestBody SurveyRequestDTO dto,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        SurveyResponseDTO responseDTO = myPageService.updateSurvey(memberId, dto);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "설문조사 수정 성공, 회원 ID: " + memberId, responseDTO));
    }

    // 로그인한 사용자가 업로드한 모든 레시피 조회하기
    @GetMapping("/recipe")
    public ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getMyRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<RecipeImageResponseDTO> recipes = myPageService.getMyRecipes(memberId);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(), "내 레시피 조회 성공", recipes));

    }

    // 로그인한 사용자가 스크랩한 레시피 조회하기
    @GetMapping("/recipe/scrap")
    public ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getMyScrapRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<RecipeImageResponseDTO> scrappedRecipes = myPageService.getMyScrapRecipes(memberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "내가 스크랩한 레시피 조회 성공", scrappedRecipes));
    }

    // 로그인한 사용자의 모든 챌린지 업적 조회
    @GetMapping("/challenge")
    public ResponseEntity<CommonResponse<List<MemberChallengeResponseDTO>>> getAllAchievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<MemberChallengeResponseDTO> achievements = myPageService.getAllAchievements(memberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "로그인한 사용자의 모든 챌린지 업적 조회 성공", achievements));
    }

    // 로그인한 사용자의 달성한 최신 챌린지 업적 3개 조회하기
    @GetMapping("/challenge/top3")
    public ResponseEntity<CommonResponse<List<MemberChallengeResponseDTO>>> getTop3Achievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<MemberChallengeResponseDTO> top3Achievements = myPageService.getTop3Achievements(memberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "로그인한 사용자가 달성한 최신 챌린지 업적 3개 조회 성공", top3Achievements));
    }

    // 다른 사용자가 업로드한 모든 레시피 조회하기
    @GetMapping("/{memberId}/recipes")
    public ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getUserRecipes(
            @PathVariable("memberId") Long memberId
    ) {
        List<RecipeImageResponseDTO> recipes = myPageService.getUserRecipes(memberId);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(), "다른 사용자 레시피 조회 성공", recipes));

    }

    // 다른 사용자의 모든 챌린지 업적 조회하기
    @GetMapping("/{memberId}/challenge")
    public ResponseEntity<CommonResponse<List<MemberChallengeResponseDTO>>> getAllAchievementsByMemberId(
            @PathVariable Long memberId
    ) {
        List<MemberChallengeResponseDTO> achievements = myPageService.getAllAchievementsByMemberId(memberId);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "다른 사용자의 모든 챌린지 업적 조회 성공", achievements));
    }

    // 다른 사용자의 달성한 최신 챌린지 업적 3개 조회하기
    @GetMapping("/{memberId}/challenge/top3")
    public ResponseEntity<CommonResponse<List<MemberChallengeResponseDTO>>> getTop3AchievementsByMemberId(
            @PathVariable Long memberId
    ) {
        List<MemberChallengeResponseDTO> achievements = myPageService.getTop3AchievementsByMemberId(memberId);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "다른 사용자가 달성한 최신 챌린지 업적 3개 조회 성공", achievements));
    }

    // 로그인한 사용자의 모든 업적 조회
    @GetMapping("/achievement")
    public ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getAllMyStatsAchievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        List<MemberAchievementResponseDTO> achievements = myPageService.getAllMyStatsAchievements(memberDetails.getId());

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "로그인한 사용자의 모든 업적 조회 성공", achievements));
    }

    // 로그인한 사용자가 달성한 최신 업적 3개 조회
    @GetMapping("/achievement/top3")
    public ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getMyTop3Achievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        List<MemberAchievementResponseDTO> top3 = myPageService.getTop3StatsAchievements(memberDetails.getId());

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "로그인한 사용자가 달성한 최신 업적 3개 조회 성공", top3));
    }

    // 다른 사용자의 모든 업적 조회
    @GetMapping("/{memberId}/achievement")
    public ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getAllStatsAchievementsByMemberId(
            @PathVariable Long memberId
    ) {
        List<MemberAchievementResponseDTO> achievements = myPageService.getAllMyStatsAchievements(memberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "다른 사용자의 모든 업적 조회 성공", achievements));
    }

}
