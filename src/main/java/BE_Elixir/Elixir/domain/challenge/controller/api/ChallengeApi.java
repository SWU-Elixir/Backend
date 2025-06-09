package BE_Elixir.Elixir.domain.challenge.controller.api;

import BE_Elixir.Elixir.domain.challenge.dto.request.ChallengeRequestDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.*;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "챌린지 등록 완료",
                                      "data": {
                                        "id": 6,
                                        "name": "챌린지 명",
                                        "description": "챌린지 설명",
                                        "purpose": "챌린지 목적",
                                        "month": 5,
                                        "year": 2025,
                                        "step1Goal1Type": "DIET_SEASONAL_ONCE",
                                        "step1Goal2Type": "DIET_LUNCH",
                                        "step2Goal1Type": "DIET_SEASONAL_ONCE",
                                        "step2Goal2Type": "DIET_BREAKFAST",
                                        "step3Goal1Type": "DIET_SEASONAL_ONCE",
                                        "step3Goal2Type": "DIET_THREE_MEALS",
                                        "step4Goal1Type": "RECIPE_SEASONAL_ONCE",
                                        "step4Goal2Type": "DIET_60_A_MONTH",
                                        "step1Goal1Desc": "하루 한 끼 재철 식재료를 포함한 식사",
                                        "step1Goal2Desc": "점심 챙겨 먹기",
                                        "step2Goal1Desc": "하루 한 끼 재철 식재료를 포함한 식사",
                                        "step2Goal2Desc": "아침 챙겨 먹기",
                                        "step3Goal1Desc": "하루 한 끼 재철 식재료를 포함한 식사",
                                        "step3Goal2Desc": "하루 3끼 식단 기록",
                                        "step4Goal1Desc": "재철 식재료를 활용한 레시피 작성",
                                        "step4Goal2Desc": "1달 동안 누적 60끼 식단 기록",
                                        "achievementName": "업적 명",
                                        "achievementImageUrl": "업적 달성 이미지",
                                        "grayAchievementImageUrl": "업적 미달성 이미지"
                                      }
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
                                      "message": "2025년도 챌린지 목록 조회 성공",
                                      "data": [
                                        {
                                          "id": 1,
                                          "name": "2월 챌린지",
                                          "month": 2,
                                          "year": 2025,
                                          "achievementName": "비타민 수호자"
                                        },
                                        {
                                          "id": 2,
                                          "name": "3월 챌린지",
                                          "month": 3,
                                          "year": 2025,
                                          "achievementName": "비타민 수호자"
                                        },
                                        {
                                          "id": 3,
                                          "name": "4월 챌린지",
                                          "month": 4,
                                          "year": 2025,
                                          "achievementName": "비타민 수호자"
                                        },
                                        {
                                          "id": 4,
                                          "name": "5월 봄맞이 챌린지",
                                          "month": 5,
                                          "year": 2025,
                                          "achievementName": "비타민 수호자"
                                        },
                                        {
                                          "id": 5,
                                          "name": "6월 챌린지",
                                          "month": 6,
                                          "year": 2025,
                                          "achievementName": "비타민 수호자"
                                        }
                                      ]
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
                                      "message": "챌린지 상세 조회 성공",
                                      "data": {
                                        "ingredients": [
                                          "냉이",
                                          "달래",
                                          "쑥"
                                        ],
                                        "name": "3월 챌린지",
                                        "period": "3월 1일 ~ 3월 31일",
                                        "description": "설명- 3월 챌린지입니다.(식재료: 쑥 id: 1142)",
                                        "purpose": "목적- 3월 챌린지입니다.",
                                        "step1Goal1Desc": "하루 한 끼 재철 식재료를 포함한 식사",
                                        "step1Goal2Desc": "점심 챙겨 먹기",
                                        "step2Goal1Desc": "하루 한 끼 재철 식재료를 포함한 식사",
                                        "step2Goal2Desc": "아침 챙겨 먹기",
                                        "step3Goal1Desc": "하루 한 끼 재철 식재료를 포함한 식사",
                                        "step3Goal2Desc": "하루 3끼 식단 기록",
                                        "step4Goal1Desc": "재철 식재료를 활용한 레시피 작성",
                                        "step4Goal2Desc": "1달 동안 누적 60끼 식단 기록",
                                        "achievementName": "비타민 수호자"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "특정 챌린지 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<ChallengeDetailResponseDTO>> getChallengeDetail(
            @PathVariable Long challengeId
    );

    // 로그인한 사용자의 현재 진행 상황 조회
    @Operation(summary = "사용자의 현재 진행 상황 조회", description = "사용자의 현재 진행 상황을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자의 현재 진행 상황 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "A@example.com 사용자의 현재 진행 상황 조회 성공",
                                      "data": {
                                        "challengeId": 5,
                                        "name": "6월 챌린지",
                                        "year": 2025,
                                        "month": 6,
                                        "step1Goal1Achieved": false,
                                        "step1Goal2Achieved": false,
                                        "step2Goal1Active": false,
                                        "step2Goal2Active": false,
                                        "step2Goal1Achieved": false,
                                        "step2Goal2Achieved": false,
                                        "step3Goal1Active": false,
                                        "step3Goal2Active": false,
                                        "step3Goal1Achieved": false,
                                        "step3Goal2Achieved": false,
                                        "step4Goal1Active": false,
                                        "step4Goal2Active": false,
                                        "step4Goal1Achieved": false,
                                        "step4Goal2Achieved": false,
                                        "challengeCompleted": false
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "사용자의 현재 진행 상황 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<ChallengeProgressResponseDTO>> getProgress(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 챌린지 최종 완료 여부 조회
    @Operation(summary = "챌린지 최종 완료 여부 조회", description = "챌린지 최종 완료 여부 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "챌린지 최종 완료 여부 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "챌린지 최종 완료 여부 조회 성공",
                                      "data": {
                                        "achievementName": "비타민 수호자",
                                        "message": "아직 챌린지를 달성하지 못했습니다.",
                                        "achievementImageUrl": null,
                                        "challengeCompleted": false
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "챌린지 최종 완료 여부 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<ChallengeCompletedResponseDTO>> getChallengeCompletion(
            @AuthenticationPrincipal MemberDetails memberDetails
    );


    // 로그인한 사용자의 이전 챌린지 진행 상황 조회
    @Operation(summary = "사용자의 이전 챌린지 진행 상황 조회", description = "사용자의 이전 챌린지 진행 상황 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자의 이전 챌린지 진행 상황 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "A@example.com 사용자의 이전 챌린지 진행 상황 조회 성공",
                                      "data": {
                                        "challengeId": 2,
                                        "name": "3월 챌린지",
                                        "year": 2025,
                                        "month": 3,
                                        "step1Goal1Achieved": true,
                                        "step1Goal2Achieved": true,
                                        "step2Goal1Active": true,
                                        "step2Goal2Active": true,
                                        "step2Goal1Achieved": true,
                                        "step2Goal2Achieved": true,
                                        "step3Goal1Active": true,
                                        "step3Goal2Active": true,
                                        "step3Goal1Achieved": true,
                                        "step3Goal2Achieved": true,
                                        "step4Goal1Active": true,
                                        "step4Goal2Active": true,
                                        "step4Goal1Achieved": true,
                                        "step4Goal2Achieved": true,
                                        "challengeCompleted": true
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "사용자의 이전 챌린지 진행 상황 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<ChallengeProgressResponseDTO>> getProgress(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long challengeId
    );
}
