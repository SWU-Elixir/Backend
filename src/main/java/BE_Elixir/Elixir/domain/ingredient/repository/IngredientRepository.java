package BE_Elixir.Elixir.domain.ingredient.repository;

import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    
    // 식재료 명 찾기
    Optional<Ingredient> findByName(String name);

    // 당월 챌린지 식재료 조회
    List<Ingredient> findByChallengeMonth(int month);
}
