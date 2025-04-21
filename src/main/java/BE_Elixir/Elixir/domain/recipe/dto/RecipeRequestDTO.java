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
    private Member member;
    private String title;
    private String description;
    private CategorySlowAging categorySlowAging;
    private CategoryType categoryType;
    private Difficulty difficulty;
    private Integer timeHours;
    private Integer timeMinutes;

    // 태그용 식재료 id 리스트
    private List<Long> ingredientTagIds;
    private Map<String, String> ingredients; // 재료 (ex. "고등어" : "1개")
    private Map<String, String> seasoning;   // 양념 (ex. "간장" : "1T")
    private List<String> stepDescriptions; // 요리 순서

    private String tips;

    // 알러지 정보
    private Boolean allergy_알류;
    private Boolean allergy_우유;
    private Boolean allergy_각류;
    private Boolean allergy_밀류;
    private Boolean allergy_유제품;
    private Boolean allergy_메밀;
    private Boolean allergy_땅콩;
    private Boolean allergy_대두;
    private Boolean allergy_밀;
    private Boolean allergy_고등어;
    private Boolean allergy_돼지고기;
    private Boolean allergy_복숭아;
    private Boolean allergy_토마토;
    private Boolean allergy_아황산류;
    private Boolean allergy_호두;
    private Boolean allergy_닭고기;
    private Boolean allergy_쇠고기;
    private Boolean allergy_오징어;
    private Boolean allergy_조개류;
    private Boolean allergy_굴;
    private Boolean allergy_전복;
    private Boolean allergy_홍합;
    private Boolean allergy_잣;
}
