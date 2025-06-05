package BE_Elixir.Elixir.domain.ingredient.controller.api;

import BE_Elixir.Elixir.domain.ingredient.dto.ChallengeIngredientDTO;
import BE_Elixir.Elixir.domain.ingredient.dto.IngredientDTO;
import BE_Elixir.Elixir.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;


@Tag(name = "Ingredient API", description = "식재료 관련 API")
public interface IngredientApi {

    @Operation(
            summary = "모든 식재료 목록 조회",
            description = "모든 식재료 목록을 조회합니다."
    )
    // 반환 상태 코드 및 의미
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식재료 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "식재료 목록 조회 성공",
                                      "data": [
                                        {
                                          "id": 1,
                                          "name": "감자",
                                          "category": "농산물",
                                          "type": null
                                        },
                                        {
                                          "id": 2,
                                          "name": "곤약(구약나물)",
                                          "category": "농산물",
                                          "type": null
                                        }, ...
                                      ]
                                     }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<IngredientDTO>>> getAllIngredients();

    @Operation(
            summary = "당월 챌린지 식재료 목록 조회",
            description = "당월 챌린지 식재료 목록을 조회합니다."
    )
    // 반환 상태 코드 및 의미
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "당월 챌린지 식재료 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "200 OK",
                                      "message": "당월 챌린지 식재료 목록 조회 성공",
                                      "data": [
                                        {
                                          "id": 142,
                                          "name": "딸기",
                                          "category": "농산물",
                                          "month": 2
                                        },
                                        {
                                          "id": 1111,
                                          "name": "봄동",
                                          "category": "농산물",
                                          "month": 2
                                        }, ...
                                      ]
                                     }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<ChallengeIngredientDTO>>> getChallengeIngredients();
}