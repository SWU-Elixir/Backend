package BE_Elixir.Elixir.domain.ingredient.service;

import BE_Elixir.Elixir.domain.ingredient.dto.ChallengeIngredientDTO;
import BE_Elixir.Elixir.domain.ingredient.dto.IngredientDTO;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    // 모든 식재료 목록 조회
    public List<IngredientDTO> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(IngredientDTO::new)
                .toList();
    }

    // 챌린지 식재료 목록 조회
    public List<ChallengeIngredientDTO> getChallengeIngredients() {
        // 현재 월 조회
        int month = LocalDate.now().getMonthValue();

        return ingredientRepository.findByChallengeMonth(month).stream()
                .map(ChallengeIngredientDTO::new)
                .toList();

    }
}