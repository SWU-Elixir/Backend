package BE_Elixir.Elixir.domain.member.controller;

import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.follow.service.FollowService;
import BE_Elixir.Elixir.domain.member.controller.api.MemberApi;
import BE_Elixir.Elixir.domain.member.dto.SocialSignUpDTO;
import BE_Elixir.Elixir.domain.member.dto.request.*;
import BE_Elixir.Elixir.domain.member.dto.response.*;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.service.MemberService;
import BE_Elixir.Elixir.global.enums.LoginType;
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

    // 일반 회원용 회원가입
    @PostMapping(value= "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<?>> signUp(
            @RequestPart("dto") SignUpRequestDTO dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        log.info("일반 회원용 회원가입 요청 - 이메일: {}", dto.getEmail());

        Member member = memberService.localSignUp(dto, profileImage);
        log.info("일반 회원용 회원가입 성공 - 회원 ID: {}", member.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.toString(),
                        "일반 회원용 회원가입 성공 - memberId: " + member.getId()));
    }

    // 소셜 회원용 회원가입
    @PostMapping(value= "/signup/{loginType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse<TokenResponseDTO>> socialSignUp(
            @PathVariable(name="loginType") LoginType loginType,
            @RequestPart("dto") SocialSignUpRequestDTO dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        log.info("소셜 회원용 회원가입 요청 - 이메일: {}", dto.getEmail());

        SocialSignUpDTO socialSignUpDTO = memberService.socialSignUp(loginType, dto, profileImage);
        log.info("소셜 회원용 회원가입 성공 - 회원 ID: {}", socialSignUpDTO.getMember().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.toString(),
                        "소셜 회원용 회원가입 성공 - memberId: " + socialSignUpDTO.getMember().getId(), socialSignUpDTO.getTokenResponseDTO()));
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
}
