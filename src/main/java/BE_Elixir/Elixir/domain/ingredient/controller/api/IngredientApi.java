package BE_Elixir.Elixir.domain.ingredient.controller.api;

import BE_Elixir.Elixir.domain.ingredient.dto.IngredientResponseDTO;
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
                                          "name": "가시오가피"
                                        },
                                        {
                                          "id": 2,
                                          "name": "가지"
                                        }, ...
                                      ]
                                     }
                                    """)))
    })
    ResponseEntity<CommonResponse<List<IngredientResponseDTO>>> getAllIngredients();
}