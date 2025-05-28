package BE_Elixir.Elixir.domain.member.dto.request;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyRequestDTO {

    private List<String> allergies;
    private List<String> mealStyles;
    private List<String> recipeStyles;
    private List<String> reasons;

}