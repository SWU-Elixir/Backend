package BE_Elixir.Elixir.domain.ingredient.controller;

import BE_Elixir.Elixir.domain.ingredient.controller.api.IngredientApi;
import BE_Elixir.Elixir.domain.ingredient.dto.IngredientResponseDTO;
import BE_Elixir.Elixir.domain.ingredient.service.IngredientService;
import BE_Elixir.Elixir.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ingredient")
@RequiredArgsConstructor
public class IngredientController implements IngredientApi {

    private final IngredientService ingredientService;

    // 모든 식재료 조회
    @GetMapping()
    public ResponseEntity<CommonResponse<List<IngredientResponseDTO>>> getAllIngredients() {
        log.info("식재료 목록 조회");

        List<IngredientResponseDTO> ingredients = ingredientService.getAllIngredients();

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                        "식재료 목록 조회 성공", ingredients));
    }
}
