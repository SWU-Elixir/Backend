package BE_Elixir.Elixir.domain.challenge.controller;


import BE_Elixir.Elixir.domain.challenge.controller.api.ChallengeApi;
import BE_Elixir.Elixir.domain.challenge.dto.request.ChallengeRequestDTO;
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
}
