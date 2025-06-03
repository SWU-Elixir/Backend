package BE_Elixir.Elixir.domain.recommendation.controller.api;

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

@Tag(name = "Recommendation API", description = "레시피 추천 API")
public interface RecommendationApi {


    // 추천 레시피 조회
    // 레시피 목록(홈) 조회
    @Operation(summary = "홈에서 추천 레시피 조회", description = "홈에서 추천 레시피를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "홈에서 추천 레시피 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "추천 레시피 조회 성공",
                                      "data": [
                                        {
                                          "id": 50,
                                          "title": "닭가슴살 덮밥",
                                          "imageUrl": "https://image.com",
                                          "categorySlowAging": "항산화강화",
                                          "categoryType": "한식",
                                          "ingredientTagIds": [
                                            2
                                          ],
                                          "scrappedByCurrentUser": false
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> getRecommendations(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 추천 검색어 조회
    @Operation(summary = "추천 검색어 조회", description = "추천 검색어를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 검색어 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "OK",
                                      "message": "추천 검색어 조회 성공",
                                      "data": [
                                        "닭가슴살",
                                        "덮밥",
                                        "곤약(구약나물)"
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> getSearchKeyword(
            @AuthenticationPrincipal MemberDetails memberDetails
    );
}
