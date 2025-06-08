package BE_Elixir.Elixir.domain.member.controller;

import BE_Elixir.Elixir.domain.follow.service.FollowService;
import BE_Elixir.Elixir.domain.member.controller.api.MemberApi;
import BE_Elixir.Elixir.domain.member.dto.request.*;
import BE_Elixir.Elixir.domain.member.dto.response.*;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.service.MemberService;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeImageResponseDTO;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.redis.RedisAuthService;
import BE_Elixir.Elixir.global.response.CommonResponse;
import BE_Elixir.Elixir.global.security.JwtProvider;
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
public class MemberController implements MemberApi {

    private final MemberService memberService;
    private final FollowService followService;
    private final JwtProvider jwtProvider;
    private final RedisAuthService redisAuthService;

    // 이메일 중복 체크
    @GetMapping("/check-email")
    public ResponseEntity<CommonResponse<Boolean>> checkEmailDuplicate(@RequestParam String email) {
        log.info("이메일 중복 체크 요청: {}", email);

        boolean isDuplicate = memberService.isEmailDuplicated(email);
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                        "이메일 중복 체크 성공", isDuplicate));
    }

    // 회원가입
    @PostMapping(value= "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<?>> signUp(
            @RequestPart("dto") SignUpRequestDTO dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        log.info("회원가입 요청 - 이메일: {}", dto.getEmail());

        Member member = memberService.signUp(dto, profileImage);
        log.info("회원가입 성공 - 회원 ID: {}", member.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.toString(),
                        "회원가입 성공 - memberId: " + member.getId()));
    }

    // 이메일 인증 요청하기
    @PostMapping("/email-verification")
    public ResponseEntity<CommonResponse<?>> sendVerificationCode(
            @RequestBody EmailVerificationRequestDTO dto
    ) {
        log.info("이메일 인증 요청 - 이메일: {}", dto.getEmail());

        memberService.sendVerificationCode(dto.getEmail());
        log.info("이메일 인증 요청 성공 - email: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "이메일 인증 요청 성공 - memberId: " + dto.getEmail()));

    }

    // 인증번호 검증하기
    @PostMapping("/email-verification/verify")
    public ResponseEntity<CommonResponse<?>> verifyCode(
            @RequestBody EmailVerificationCheckRequestDTO dto
    ) {
        log.info("이메일 인증 검증 요청 - 이메일: {}", dto.getEmail());

        boolean result = memberService.verifyCode(dto.getEmail(), dto.getCode());
        log.info("이메일 인증 검증 성공 - email: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                        "이메일 인증 검증 성공 - email: " + dto.getEmail(), result));
    }

    // 비밀번호 수정하기
    @PutMapping("/update-password")
    public ResponseEntity<CommonResponse<?>> updatePassword(
            @RequestBody UpdatePasswordRequestDTO dto
    ) {
        log.info("비밀번호 업데이트 요청 - 이메일: {}", dto.getEmail());

        memberService.updatePassword(dto.getEmail(), dto.getNewPassword());

        log.info("비밀번호 업데이트 성공 - email: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                        "비밀번호 업데이트 성공 - email: " + dto.getEmail()));
    }


    // 회원탈퇴
    @DeleteMapping("/withdrawal")
    public ResponseEntity<CommonResponse<?>> withdrawal(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    ) {
        String email = memberDetails.getUsername();
        log.info("회원탈퇴 요청 - email: {}", email);

        String accessToken = jwtProvider.resolveToken(request);
        String refreshToken = redisAuthService.getRefreshToken(memberDetails.getUsername());

        memberService.withdraw(email, accessToken, refreshToken);
        log.info("회원탈퇴 성공 - email: {}", email);

        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "회원탈퇴 성공"));
    }

    // 회원 정보 조회 (이메일, 닉네임, 젠더, 생년, 프로필 url)
    @GetMapping()
    public ResponseEntity<CommonResponse<MemberResponseDTO>> getMemberInfo(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request)
    {
        String email = memberDetails.getUsername();
        log.info("회원 정보 조회 요청 - email: {}", email);

        MemberResponseDTO response = memberService.getMemberInfo(email);
        log.info("회원 정보 조회 성공 - email: {}", email);

        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "회원조회 성공", response));
    }

    // 로그인한 사용자 프로필 수정 시, 얻은 칭호 목록 조회
    @GetMapping("/achievement/title")
    public ResponseEntity<CommonResponse<MemberTitlesResponseDTO>> getTitles(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();
        log.info("칭호 목록 조회 요청 - id: {}", memberId);

        List<String> titles = memberService.getTitles(memberId);
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

        MemberResponseDTO responseDTO = memberService.updateMemberProfile(memberId, dto, profileImage);

        log.info("사용자 프로필 수정 성공 - id: {}", memberId);

        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "사용자 프로필 수정 성공", responseDTO));
    }

    // 로그인한 사용자 프로필 조회 (칭호, 닉네임, 프로필사진, 팔로워 수, 팔로잉 수)
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse<MemberProfileResponseDTO>> getMemberProfile(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();
        log.info("로그인한 사용자의 프로필 조회 요청 - id: {}", memberId);

        MemberProfileResponseDTO responseDTO = memberService.getMemberProfile(memberId);

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

        MemberProfileResponseDTO responseDTO = memberService.getMemberProfile(memberId);

        log.info("특정 사용자의 프로필 조회 성공 - id: {}", memberId);

        return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "로그인한 사용자의 프로필 조회 성공", responseDTO));
    }

    // 로그인한 사용자가 업로드한 모든 레시피 조회하기
    @GetMapping("/recipe")
    public ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getMyRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<RecipeImageResponseDTO> recipes = memberService.getMyRecipes(memberId);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(), "내 레시피 조회 성공", recipes));

    }


    // 로그인한 사용자가 스크랩한 레시피 조회하기
    @GetMapping("/recipe/scrap")
    public ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getMyScrapRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<RecipeImageResponseDTO> scrappedRecipes = memberService.getMyScrapRecipes(memberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "내가 스크랩한 레시피 조회 성공", scrappedRecipes));
    }

    // 팔로우 하기
    @PostMapping("/{targetMemberId}/follow")
    public ResponseEntity<CommonResponse<?>> follow(
            @PathVariable("targetMemberId") Long followingId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long followerId = memberDetails.getId();

        followService.follow(followerId, followingId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "팔로우 성공"));
    }

    // 언팔로우 하기
    @DeleteMapping("/{targetMemberId}/follow")
    public ResponseEntity<CommonResponse<?>> unfollow(
            @PathVariable("targetMemberId") Long followingId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long followerId = memberDetails.getId();

        followService.unfollow(followerId, followingId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "언팔로우 성공"));
    }

    // (현재 사용자의) 팔로잉 목록 조회하기 (사용자가 팔로우하는 목록)
    @GetMapping("/following")
    public ResponseEntity<CommonResponse<List<MemberSummaryDTO>>> getFollowing(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<MemberSummaryDTO> dto = followService.getFollowings(memberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "현 사용자의 팔로잉 목록 조회 성공", dto));
    }

    // (현재 사용자의) 팔로우 목록 조회하기 (사용자를 팔로잉하는 목록)
    @GetMapping("/follower")
    public ResponseEntity<CommonResponse<List<MemberSummaryDTO>>> getFollower(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<MemberSummaryDTO> dto = followService.getFollowers(memberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "현 사용자의 팔로워 목록 조회 성공", dto));
    }


    // (특정 사용자의) 팔로잉 목록 조회하기 (사용자가 팔로우하는 목록)
    @GetMapping("/{targetMemberId}/following")
    public ResponseEntity<CommonResponse<List<MemberSummaryDTO>>> getFollowingByMemberId(
            @PathVariable("targetMemberId") Long targetMemberId
    ) {
        List<MemberSummaryDTO> dto = followService.getFollowings(targetMemberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "특정 사용자의 팔로잉 목록 조회 성공", dto));
    }


    // (특정 사용자의) 팔로우 목록 조회하기 (사용자를 팔로잉하는 목록)
    @GetMapping("/{targetMemberId}/follower")
    public ResponseEntity<CommonResponse<List<MemberSummaryDTO>>> getFollowerByMemberId(
            @PathVariable("targetMemberId") Long targetMemberId
    ) {
        List<MemberSummaryDTO> dto = followService.getFollowers(targetMemberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "특정 사용자의 팔로워 목록 조회 성공", dto));
    }

    // 로그인한 사용자의 모든 챌린지 업적 정보 조회
    @GetMapping("/achievement")
    public ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getAllAchievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<MemberAchievementResponseDTO> achievements = memberService.getAllAchievements(memberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "모든 챌린지 업적 조회 성공", achievements));
    }

    // 로그인한 사용자의 달성한 업적 최신 3개 조회하기
    @GetMapping("/achievement/top3")
    public ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getTop3Achievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        List<MemberAchievementResponseDTO> top3Achievements = memberService.getTop3Achievements(memberId);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "최근 업적 3개 조회 성공", top3Achievements));
    }

    // 로그인한 사용자의 설문조사 결과 조회하기
    @GetMapping("/survey")
    public ResponseEntity<CommonResponse<SurveyResponseDTO>> getSurvey(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Long memberId = memberDetails.getId();

        SurveyResponseDTO responseDTO = memberService.getSurvey(memberId);

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

        SurveyResponseDTO responseDTO = memberService.updateSurvey(memberId, dto);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "설문조사 수정 성공, 회원 ID: " + memberId, responseDTO));
    }

    // 다른 사용자가 업로드한 모든 레시피 조회하기
    @GetMapping("/{memberId}/recipes")
    public ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getUserRecipes(
            @PathVariable("memberId") Long memberId
    ) {
        List<RecipeImageResponseDTO> recipes = memberService.getUserRecipes(memberId);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(), "다른 사용자 레시피 조회 성공", recipes));

    }

    // 다른 사용자의 모든 챌린지 업적 정보 조회하기
    @GetMapping("/{memberId}/achievements")
    public ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getAllAchievementsByMemberId(
            @PathVariable Long memberId
    ) {
        List<MemberAchievementResponseDTO> achievements = memberService.getAllAchievementsByMemberId(memberId);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "다른 사용자의 모든 업적 조회 성공", achievements));
    }

    // 다른 사용자의 최근 3개 업적 조회하기
    @GetMapping("/{memberId}/achievements/top3")
    public ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getTop3AchievementsByMemberId(
            @PathVariable Long memberId
    ) {
        List<MemberAchievementResponseDTO> achievements = memberService.getTop3AchievementsByMemberId(memberId);
        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "다른 사용자의 최근 업적 3개 조회 성공", achievements));
    }
}
