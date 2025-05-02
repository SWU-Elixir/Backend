package BE_Elixir.Elixir.domain.ingredient.service;

import BE_Elixir.Elixir.domain.ingredient.dto.IngredientResponseDTO;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public List<IngredientResponseDTO> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(IngredientResponseDTO::new)
                .toList();
    }
}