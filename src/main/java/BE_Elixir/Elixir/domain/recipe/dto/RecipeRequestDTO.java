package BE_Elixir.Elixir.domain.recipe.dto;

import BE_Elixir.Elixir.domain.member.entity.Member;
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
    private List<String> ingredientTagNames;
    private Map<String, String> ingredients; // 재료 (ex. "고등어" : "1개")
    private Map<String, String> seasoning;   // 양념 (ex. "간장" : "1T")
    private List<String> stepDescriptions; // 요리 순서

    private String tips;

    // 알러지 정보
    private List<String> allergies;

    public List<String> getAllergies() {
        return allergies;
    }
}
