package BE_Elixir.Elixir.domain.recipe.controller.api;

import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeDetailResponseDTO;
import BE_Elixir.Elixir.domain.recipe.dto.request.RecipeRequestDTO;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeSummaryResponse;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Recipe API", description = "레시피 관련 API")
public interface RecipeApi {
    
    // 레시피 등록
    @Operation(summary = "레시피 등록", description = "레시피를 새로 등록합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "레시피 등록 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "201 CREATED",
                                      "message": "레시피 등록 성공",
                                      "data": {
                                        "id": 49,
                                        "email": "A@example.com",
                                        "title": "닭가슴살 덮밥",
                                        "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00281_1.png",
                                        "description": "닭가슴살로 간단히 덮밥해먹기",
                                        "categorySlowAging": "염증감소",
                                        "categoryType": "한식",
                                        "difficulty": "쉬움",
                                        "timeHours": 0,
                                        "timeMinutes": 45,
                                        "ingredientTagIds": [
                                          1
                                        ],
                                        "ingredients": {
                                          "물": "2ml(1/3작은술)"
                                        },
                                        "seasoning": {
                                          "설탕": "2g(1/3작은술)"
                                        },
                                        "stepDescriptions": [
                                          "닭가슴살을 전자레인지에 데운다"
                                        ],
                                        "stepImageUrls": "http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00031_5.png",
                                        "tips": "닭가슴살을 잘게 자를수록 더 맛있음",
                                        "likes": 0,
                                        "scraps": 0,
                                        "createdAt": "2025-06-03T00:08:22.319638443",
                                        "updatedAt": "2025-06-03T00:08:22.31966641",
                                        "allergies": []
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "레시피 등록 실패",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 500,
                                      "code": "500 INTERNAL_SERVER_ERROR",
                                      "message": "레시피 등록 실패 - 재료 없음: 0",
                                      "data": null
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> createRecipe(
            @RequestPart("dto") RecipeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "recipeStepImages", required = false) List<MultipartFile> recipeStepImages,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 레시피 상세 조회
    @Operation(summary = "레시피 상세 조회", description = "레시피의 상세 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "레시피 조회 성공",
                                      "data": {
                                        "authorFollowByCurrentUser": false,
                                            "comments": [
                                              {
                                                "commentId": 1,
                                                "recipeId": 1,
                                                "nickName": "mj",
                                                "title": "비타민 수호자",
                                                "authorProfileUrl": "https://commentPostUserImage.com",
                                                "content": "mj로 댓글달기",
                                                "createdAt": "2025-06-17T07:52:47",
                                                "updatedAt": "2025-06-17T07:52:47"
                                              }
                                            ],
                                            "likedByCurrentUser": false,
                                            "scrappedByCurrentUser": false,
                                            "id": 1,
                                            "authorNickname": "mj",
                                            "authorTitle": "비타민 수호자",
                                            "authorId": 1,
                                            "authorProfileUrl": "https://recipePostUserImage.com",
                                        "title": "방울토마토 소박이",
                                        "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00031_1.png",
                                        "description": "기타",
                                        "categorySlowAging": "염증감소",
                                        "categoryType": "한식",
                                        "difficulty": "쉬움",
                                        "timeHours": 0,
                                        "timeMinutes": 45,
                                        "ingredientTagIds": [
                                          1106,
                                          1198,
                                          5,
                                          1101
                                        ],
                                        "ingredients": {
                                          "통깨": "약간",
                                          "방울토마토": "150g(5개)",
                                          "부추": "10g(5줄기)",
                                          "물": "2ml(1/3작은술)"
                                        },
                                        "seasoning": {
                                          "고춧가루": "4g(1작은술)",
                                          "멸치액젓": "3g(2/3작은술)",
                                          "다진 마늘": "2.5g(1/2쪽)",
                                          "매실액": "2g(1/3작은술)",
                                          "양파": "10g(3×1cm)",
                                          "설탕": "2g(1/3작은술)"
                                        },
                                        "stepDescriptions": [
                                          "물기를 빼고 2cm 정도의 크기로 썰은 부추와 양파를 양념장에 섞어 양념속을 만든다.",
                                          "깨끗이 씻은 방울토마토는 꼭지를 떼고 윗부분에 칼로 십자모양으로 칼집을 낸다.",
                                          "칼집을 낸 방울토마토에 양념속을 사이사이에 넣어 버무린다."
                                        ],
                                        "stepImageUrls": [
                                          "http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00031_1.png",
                                          "http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00031_4.png",
                                          "http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00031_5.png"
                                        ],
                                        "tips": "소금에 절이는 오이 대신 방울토마토를 사용하여 나트륨 섭취를 줄였어요. 토마토에는 과일에 대체로 없는 글루탐산이 풍부하여 감칠맛을 내주며, 겉절이 양념과 잘 어우러져 상큼함과 감칠맛을 내주어요.",
                                        "likes": 0,
                                        "createdAt": "2025-05-21T10:19:09",
                                        "updatedAt": "2025-05-21T10:19:09",
                                        "allergies": [
                                          "토마토"
                                        ]
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<RecipeDetailResponseDTO>> getRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 레시피 목록(홈) 조회
    @Operation(summary = "레시피 목록(홈) 조회", description = "레시피의 목록(홈) 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 목록(홈) 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "전체 레시피 조회 성공",
                                      "data": {
                                        "content": [
                                          {
                                            "likedByCurrentUser": false,
                                            "scrappedByCurrentUser": false,
                                            "id": 49,
                                            "title": "string",
                                            "imageUrl": null,
                                            "categorySlowAging": "항산화강화",
                                            "categoryType": "한식",
                                            "difficulty": "쉬움",
                                            "totalTimeMinutes": 0,
                                            "ingredientTagIds": [
                                              1
                                            ],
                                            "likes": 0
                                          },
                                          {
                                            "likedByCurrentUser": false,
                                            "scrappedByCurrentUser": false,
                                            "id": 1,
                                            "title": "새우 두부 계란찜",
                                            "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00028_1.png",
                                            "categorySlowAging": "염증감소",
                                            "categoryType": "한식",
                                            "difficulty": "보통",
                                            "totalTimeMinutes": 45,
                                            "ingredientTagIds": [
                                              280,
                                              599
                                            ],
                                            "likes": 0
                                          }
                                          .
                                          .
                                          .
                                        ],
                                        "pageable": {
                                          "pageNumber": 0,
                                          "pageSize": 10,
                                          "sort": {
                                            "empty": false,
                                            "sorted": true,
                                            "unsorted": false
                                          },
                                          "offset": 0,
                                          "paged": true,
                                          "unpaged": false
                                        },
                                        "last": false,
                                        "totalPages": 5,
                                        "totalElements": 49,
                                        "size": 10,
                                        "number": 0,
                                        "sort": {
                                          "empty": false,
                                          "sorted": true,
                                          "unsorted": false
                                        },
                                        "numberOfElements": 10,
                                        "first": true,
                                        "empty": false
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> getRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(required = false) CategorySlowAging categorySlowAging,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 레시피 검색 결과 조회
    @Operation(summary = "레시피 검색 결과 조회", description = "레시피의 검색 결과를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 검색 결과 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                       "status": 200,
                                       "code": "200 OK",
                                       "message": "레시피 검색 성공",
                                       "data": {
                                         "content": [
                                           {
                                             "likedByCurrentUser": false,
                                             "scrappedByCurrentUser": false,
                                             "id": 4,
                                             "title": "순두부 사과 소스 오이무침",
                                             "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00032_2.png",
                                             "categorySlowAging": "염증감소",
                                             "categoryType": "한식",
                                             "difficulty": "보통",
                                             "totalTimeMinutes": 45,
                                             "ingredientTagIds": [
                                               175,
                                               280,
                                               1166,
                                               1089
                                             ],
                                             "likes": 0
                                           }
                                         ],
                                         "pageable": {
                                           "pageNumber": 0,
                                           "pageSize": 10,
                                           "sort": {
                                             "empty": false,
                                             "sorted": true,
                                             "unsorted": false
                                           },
                                           "offset": 0,
                                           "paged": true,
                                           "unpaged": false
                                         },
                                         "last": true,
                                         "totalPages": 1,
                                         "totalElements": 1,
                                         "size": 10,
                                         "number": 0,
                                         "sort": {
                                           "empty": false,
                                           "sorted": true,
                                           "unsorted": false
                                         },
                                         "numberOfElements": 1,
                                         "first": true,
                                         "empty": false
                                       }
                                     }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> getSearchRecipe(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(required = false) CategorySlowAging categorySlowAging,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 레시피 인기 검색어 조회
    @Operation(summary = "인기 검색어 조회", description = "인기 검색어를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인기 검색어 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "인기 검색어 조회 성공 ",
                                      "data": [
                                        "사과"
                                      ]
                                    }
                                    """)))
    })
    ResponseEntity<CommonResponse<?>> getSearchKeyword(
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 레시피 수정
    @Operation(summary = "레시피 수정", description = "레시피를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 수정 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "레시피 수정 성공",
                                      "data": {
                                        "id": 49,
                                        "email": "A@example.com",
                                        "title": "닭가슴살 덮밥",
                                        "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00281_1.png",
                                        "description": "닭가슴살로 간단히 덮밥해먹기",
                                        "categorySlowAging": "염증감소",
                                        "categoryType": "한식",
                                        "difficulty": "쉬움",
                                        "timeHours": 0,
                                        "timeMinutes": 45,
                                        "ingredientTagIds": [
                                          1
                                        ],
                                        "ingredients": {
                                          "물": "2ml(1/3작은술)"
                                        },
                                        "seasoning": {
                                          "설탕": "2g(1/3작은술)"
                                        },
                                        "stepDescriptions": [
                                          "닭가슴살을 전자레인지에 데운다"
                                        ],
                                        "stepImageUrls": "http://www.foodsafetykorea.go.kr/uploadimg/cook/20_00031_5.png",
                                        "tips": "닭가슴살을 잘게 자를수록 더 맛있음",
                                        "likes": 0,
                                        "scraps": 0,
                                        "createdAt": "2025-06-03T00:08:22.319638443",
                                        "updatedAt": "2025-06-03T00:08:22.31966641",
                                        "allergies": []
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> updateRecipe(
            @PathVariable Long recipeId,
            @RequestPart("dto") RecipeRequestDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "recipeStepImages", required = false) List<MultipartFile> recipeStepImages,
            @AuthenticationPrincipal MemberDetails memberDetails
    );


    // 레시피 삭제
    @Operation(summary = "레시피 삭제", description = "레시피를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "레시피 삭제 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "레시피 삭제 성공",
                                      "data": "recipeId: 49 삭제 완료"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "레시피 없음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    ResponseEntity<CommonResponse<?>> deleteRecipe(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal MemberDetails memberDetails
    );

    // 로그인한 사용자가 작성한 레시피를 최대 10개까지 조회
    @Operation(summary = "작성한 레시피를 최대 10개까지 조회", description = "작성한 레시피를 최대 10개까지 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성한 레시피를 최대 10개까지 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                       "status": 200,
                                       "code": "200 OK",
                                       "message": "작성한 레시피 조회 성공",
                                       "data": [
                                         {
                                           "recipeId": 50,
                                           "imageUrl": "http://www.foodsafetykorea.go.kr/uploadimg/cook/10_00032_2.png",
                                           "title": "닭가슴살 덮밥",
                                           "ingredientTags": [
                                             2
                                           ]
                                         }
                                       ]
                                     }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<RecipeSummaryResponse>>> getMyRecipes(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestParam(defaultValue = "10") int size
    );
}