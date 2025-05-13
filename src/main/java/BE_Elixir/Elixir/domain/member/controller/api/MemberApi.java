package BE_Elixir.Elixir.domain.member.controller.api;

import BE_Elixir.Elixir.domain.member.dto.request.SignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.response.MemberAchievementResponseDTO;
import BE_Elixir.Elixir.domain.member.dto.response.MemberResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeHomeResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeImageResponseDTO;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Tag(name = "Member API", description = "회원 관련 API")
public interface MemberApi {

    @Operation(
            summary = "이메일 중복 여부 조회",
            description = "기존 가입 회원과 이메일이 중복되는지 조회합니다."
    )
    // 반환 상태 코드 및 의미
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "중복 여부 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "이메일 중복 체크 성공",
                                      "data": true
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<Boolean>> checkEmailDuplicate(@RequestParam String email);

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "회원가입 성공 - memberId: 1",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "회원가입 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                    examples = @ExampleObject(value = """
                                    {
                                      "status": 401,
                                      "code": "401 BAD_REQUEST",
                                      "message": "회원가입 실패: 회원가입 중 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> signUp(
            @RequestPart("dto") SignUpRequestDTO dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    );

    @Operation(summary = "회원탈퇴",
            description = "회원 정보를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원탈퇴 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "회원탈퇴 성공 - email: example@naver.com",
                                      "data": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 401,
                                      "code": "401 INTERNAL_SERVER_ERROR",
                                      "message": "회원탈퇴 실패 - 유효하지 않거나 만료된 Refresh Token",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> withdrawal(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );


    @Operation(summary = "회원 정보 조회",
            description = "회원의 기본적인 정보(id, 이메일, 닉네임, 젠더, 생년)를 조회합니다",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "회원 정보 조회 성공",
                                      "data": {
                                        "id": 1,
                                        "email": "example@naver.com",
                                        "nickname": "example",
                                        "gender": "female",
                                        "birthYear": 2002,
                                        "profileUrl": "https://s3elixir.s3..."
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "회원 정보 조회 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 401,
                                      "code": "401 INTERNAL_SERVER_ERROR",
                                      "message": "회원 정보 조회 중 오류가 발생했습니다.",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<MemberResponseDTO>> getMemberInfo(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    );


    // 로그인한 사용자가 작성한 레시피 조회하기
    @Operation(summary = "로그인한 사용자가 작성한 레시피 조회하기", description = "로그인한 사용자가 작성한 레시피를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내가 작성한 레시피 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "내가 작성한 레시피 조회 성공",
                                      "data": true
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getMyRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 로그인한 사용자가 스크랩한 레시피 조회하기
    @Operation(summary = "로그인한 사용자가 스크랩한 레시피 조회하기", description = "로그인한 사용자가 스크랩한 레시피를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내가 스크랩한 레시피 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "내가 스크랩한 레시피 조회 성공",
                                      "data": true
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<RecipeImageResponseDTO>>> getMyScrapRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 로그인한 사용자의 모든 챌린지 업적 정보 조회
    @Operation(summary = "로그인한 사용자의 모든 챌린지 업적 정보 조회하기", description = "로그인한 사용자의 모든 챌린지 업적 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인한 사용자의 모든 챌린지 업적 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "로그인한 사용자의 모든 챌린지 업적 정보 조회 성공",
                                      "data": true
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<MemberAchievementResponseDTO>>> getAllAchievements(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

}