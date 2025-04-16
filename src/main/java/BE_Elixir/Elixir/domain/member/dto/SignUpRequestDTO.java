package BE_Elixir.Elixir.domain.member.dto;

import BE_Elixir.Elixir.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequestDTO {

    private String email;
    private String password;
    private String nickname;
    private String profile_url;
    private String gender;
    private Integer birth_year;

    // allergy fields
    private boolean allergy_egg;
    private boolean allergy_milk;
    private boolean allergy_grain;
    private boolean allergy_wheat_product;
    private boolean allergy_dairy;
    private boolean allergy_buckwheat;
    private boolean allergy_peanut;
    private boolean allergy_soybean;
    private boolean allergy_wheat;
    private boolean allergy_mackerel;
    private boolean allergy_pork;
    private boolean allergy_peach;
    private boolean allergy_tomato;
    private boolean allergy_sulfite;
    private boolean allergy_walnut;
    private boolean allergy_chicken;
    private boolean allergy_beef;
    private boolean allergy_squid;
    private boolean allergy_shellfish;
    private boolean allergy_oyster;
    private boolean allergy_abalone;
    private boolean allergy_mussel;
    private boolean allergy_pine_nut;

    // meal style fields
    private boolean meal_style_meat_based;
    private boolean meal_style_vegetable_based;
    private boolean meal_style_mixed;

    // recipe style fields
    private boolean recipe_style_korean;
    private boolean recipe_style_chinese;
    private boolean recipe_style_japanese;
    private boolean recipe_style_western;
    private boolean recipe_style_dessert;
    private boolean recipe_style_beverage_tea;
    private boolean recipe_style_sauce_jam;

    // reason fields
    private boolean reason_antioxidant_boost;
    private boolean reason_blood_sugar_control;
    private boolean reason_inflammation_reduction;

    public Member toEntity(String encodedPassword, List<String> roles) {
        return Member.builder()
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .profile_url(this.profile_url)
                .gender(this.gender)
                .birth_year(this.birth_year)
                .roles(roles)
                .build();
    }
}