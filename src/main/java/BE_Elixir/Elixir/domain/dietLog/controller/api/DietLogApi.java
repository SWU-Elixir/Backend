package BE_Elixir.Elixir.domain.dietLog.controller.api;

import BE_Elixir.Elixir.domain.dietLog.dto.DietLogRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "DietLog API", description = "식단 기록 관련 API")
public interface DietLogApi {

    @Operation(summary = "식단 기록하기", description = "새로운 식단을 기록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식단 기록 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "식단 기록 성공 - 회원 Id: 1",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "식단 기록 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 401,
                                      "code": "401 BAD_REQUEST",
                                      "message": "식단 기록 실패: 식단 기록 중 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> createDietLog(
            @RequestPart("dto") DietLogRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );
}
