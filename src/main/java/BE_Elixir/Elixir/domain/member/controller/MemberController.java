package BE_Elixir.Elixir.domain.member.controller;

import BE_Elixir.Elixir.domain.member.controller.api.MemberApi;
import BE_Elixir.Elixir.domain.member.dto.request.SignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.response.MemberResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.service.MemberService;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeHomeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeImageResponseDTO;
import BE_Elixir.Elixir.global.redis.RedisService;
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
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

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

        try {
            Member member = memberService.signUp(dto, profileImage);
            log.info("회원가입 성공 - 회원 ID: {}", member.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CommonResponse.success(HttpStatus.CREATED.value(), HttpStatus.CREATED.toString(), "회원가입 성공 - memberId: " + member.getId()));

        } catch (Exception e) {
            log.error("회원가입 실패 - 이메일: {}, 메시지: {}", dto.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponse.success(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.toString(),
                            "회원가입 실패: " + e.getMessage(), null));
        }
    }

    // 회원탈퇴
    @DeleteMapping("/withdrawal")
    public ResponseEntity<CommonResponse<?>> withdrawal(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    ) {
        String email = memberDetails.getUsername();
        log.info("회원탈퇴 요청 - email: {}", email);

        try {
            String accessToken = jwtProvider.resolveToken(request);
            String refreshToken = redisService.getRefreshToken(memberDetails.getUsername());

            memberService.withdraw(email, accessToken, refreshToken);
            log.info("회원탈퇴 성공 - email: {}", email);

            return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "회원탈퇴 성공"));

        } catch (Exception e) {
            log.error("회원탈퇴 실패 - {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "회원탈퇴 실패 - " + e.getMessage()));
        }
    }

    // 회원 정보 조회 (이메일, 닉네임, 젠더, 생년, 프로필 url)
    @GetMapping("")
    public ResponseEntity<CommonResponse<MemberResponseDTO>> getMemberInfo(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request)
    {
        String email = memberDetails.getUsername();
        log.info("회원 정보 조회 요청 - email: {}", email);

        try {
            MemberResponseDTO response = memberService.getMemberInfo(email);
            log.info("회원 정보 조회 성공 - email: {}", email);

            return ResponseEntity.ok(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(), "회원조회 성공", response));

        } catch (Exception e) {
            log.warn("회원 정보 조회 실패 - email: {}, message: {}", email, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "회원 정보 조회 실패 - " + e.getMessage()));
        }
    }

    // 로그인한 사용자가 업로드한 모든 레시피 조회하기
    @GetMapping("/recipe")
    public ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getMyRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        String email = memberDetails.getUsername();

        try {
            List<RecipeImageResponseDTO> recipes = memberService.getMyRecipes(email);
            return ResponseEntity.ok(CommonResponse.success(
                    HttpStatus.OK.value(), HttpStatus.OK.toString(), "내 레시피 조회 성공", recipes));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            "내 레시피 조회 실패 - " + e.getMessage()));
        }
    }


}
