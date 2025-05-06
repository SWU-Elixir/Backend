package BE_Elixir.Elixir.domain.challenge.controller.api;

import BE_Elixir.Elixir.domain.challenge.dto.request.ChallengeRequestDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeDetailResponseDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeListResponseDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeResponseDTO;
import BE_Elixir.Elixir.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Challenge API", description = "챌린지 관련 API")
public interface ChallengeApi {

    // 챌린지 등록하기
    @Operation(summary = "챌린지 등록", description = "챌린지를 새로 등록합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "챌린지 등록 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 201,
                                      "code": "201 OK",
                                      "message": "챌린지 등록 성공",
                                      "data": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "챌린지 등록 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<ChallengeResponseDTO>> registerAchievement(
            @RequestPart("dto") ChallengeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "grayImage", required = false) MultipartFile grayImage
    );

    // 연도 별 챌린지 조회하기
    @Operation(summary = "연도 별 챌린지 조회", description = "연도 별 챌린지를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연도 별 챌린지 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "연도 별 챌린지 조회 성공",
                                      "data": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "연도 별 챌린지 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<List<ChallengeListResponseDTO>>> getChallengesByYear(
            @PathVariable int year
    );
    
    
    // 특정 챌린지 상세 조회하기
    @Operation(summary = "특정 챌린지 조회", description = "특정 챌린지를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "특정 챌린지 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "특정 챌린지 조회 성공",
                                      "data": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "특정 챌린지 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<ChallengeDetailResponseDTO>> getChallengeDetail(
            @PathVariable Long challengeId
    );
}
