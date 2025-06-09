package BE_Elixir.Elixir.domain.recipe.dto.request;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.MaterialDTO;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.Difficulty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RecipeRequestDTO {
    private String title;
    private String description;
    private CategorySlowAging categorySlowAging;
    private CategoryType categoryType;
    private Difficulty difficulty;
    private Integer timeHours;
    private Integer timeMinutes;

    // 태그용 식재료 리스트
    private List<Long> ingredientTagIds;
    private List<MaterialDTO> ingredients; // 재료
    private List<MaterialDTO> seasonings;   // 양념
    private List<String> stepDescriptions; // 요리 순서

    private String tips;

    // 알러지 정보
    private List<String> allergies;

    public List<String> getAllergies() {
        return allergies;
    }
}
