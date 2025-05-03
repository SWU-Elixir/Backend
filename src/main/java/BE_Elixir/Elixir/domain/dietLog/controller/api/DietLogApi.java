package BE_Elixir.Elixir.domain.dietLog.controller.api;

import BE_Elixir.Elixir.domain.dietLog.dto.DietLogRequestDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.DietLogResponseDTO;
import BE_Elixir.Elixir.domain.dietLog.dto.MonthlyDietScoreDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "DietLog API", description = "식단 기록 관련 API")
public interface DietLogApi {

    @Operation(summary = "식단 기록하기",
            description = "새로운 식단을 기록합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
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
            @ApiResponse(responseCode = "400", description = "식단 기록 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "code": "400 BAD_REQUEST",
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


    @Operation(summary = "식단 삭제",
            description = "기록된 식단 정보를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식단 삭제 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "식단 삭제 성공 - 회원 Id: 1, 식단 ID: 1",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "code": "400 BAD_REQUEST",
                                      "message": "식단 삭제 실패 - 해당 식단이 존재하지 않습니다. 식단 ID: 1",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> deleteDietLog(
            @PathVariable("DietLogId") Long DietLogId,
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );

    @Operation(summary = "식단 기록 정보 조회",
            description = "id를 기반으로 하나의 식단 기록 정보를 조회합니다",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식단 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "식단 조회 성공 - 회원 ID:1,  식단 ID: 5",
                                      "data": {
                                        "id": 5,
                                        "memberId": 1,
                                        "name": "부대찌개",
                                        "imageUrl": "https://s3elixir.s3.ap-northeast-2.amazonaws.com/diet_log/...",
                                        "type": "저녁",
                                        "score": 3,
                                        "ingredientTagId": [
                                          10,
                                          15
                                        ],
                                        "time": "2025-05-03T08:52:46.034"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "식단 정보 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "code": "400 BAD_REQUEST",
                                      "message": "식단 조회 실패: 해당 식단이 존재하지 않습니다. 식단 ID: 2",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<DietLogResponseDTO>> getDietLog(
            @PathVariable("DietLogId") Long DietLogId,
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );

    @Operation(summary = "일별 식단 목록 조회",
            description = "날짜를 기반으로 일별 식단 목록을 조회합니다",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "일별 식단 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "일별 식단 목록 조회 성공 - 회원 ID:1,  날짜: 2025-05-03",
                                      "data": [
                                        {
                                          "id": 5,
                                          "memberId": 1,
                                          "name": "부대찌개",
                                          "imageUrl": "https://s3elixir.s3.ap-northeast-2.amazonaws.com/diet_log/...",
                                          "type": "저녁",
                                          "score": 3,
                                          "ingredientTagId": [
                                            10,
                                            15,
                                          ],
                                          "time": "2025-05-03T08:52:46.034"
                                        },
                                        ...
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "일별 식단 목록 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "code": "400 BAD_REQUEST",
                                      "message": "일별 식단 목록 조회 실패: 해당 식단이 존재하지 않습니다. 식단 ID: 2",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<DietLogResponseDTO>>> getDietLogByDate(
            @PathVariable("date") LocalDate date,
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );

    @Operation(summary = "월별 식단별 점수 조회",
            description = "해당 연, 월의 식단별 점수를 조회합니다",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "월별 식단별 점수 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "월별 식단별 점수 조회 성공 - 회원 ID: 1, 연도: 2025, 월: 5",
                                      "data": [
                                        {
                                          "id": 5,
                                          "time": "2025-05-03T08:52:46.034",
                                          "score": 3
                                        },
                                        ...
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "일별 식단 목록 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "code": "400 BAD_REQUEST",
                                      "message": "월별 식단별 점수 조회 실패",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MonthlyDietScoreDTO>>> getMonthlyDietScores(
            @PathVariable("year") int year,
            @PathVariable("month") int month,
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );

}
