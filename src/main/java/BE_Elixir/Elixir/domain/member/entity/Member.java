package BE_Elixir.Elixir.domain.member.entity;

import BE_Elixir.Elixir.domain.follow.entity.Follow;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true, nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Setter private String password;  // 인코딩된 비밀번호

    @Column(nullable = false, unique = true)
    @Setter private String nickname;

    @Setter private String profileUrl;
    @Setter private String gender;
    @Setter private Integer birthYear;

    // 회원이 팔로잉하는 경우
    @Builder.Default
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings = new ArrayList<>();

    // 회원을 팔로우하는 경우
    @Builder.Default
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers = new ArrayList<>();

    // 업적
    @Setter private String title;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    // 알러지 정보
    @Builder.Default @Setter private Boolean allergy_알류 = false;
    @Builder.Default @Setter private Boolean allergy_우유 = false;
    @Builder.Default @Setter private Boolean allergy_각류 = false;
    @Builder.Default @Setter private Boolean allergy_밀류 = false;
    @Builder.Default @Setter private Boolean allergy_유제품 = false;
    @Builder.Default @Setter private Boolean allergy_메밀 = false;
    @Builder.Default @Setter private Boolean allergy_땅콩 = false;
    @Builder.Default @Setter private Boolean allergy_대두 = false;
    @Builder.Default @Setter private Boolean allergy_밀 = false;
    @Builder.Default @Setter private Boolean allergy_고등어 = false;
    @Builder.Default @Setter private Boolean allergy_돼지고기 = false;
    @Builder.Default @Setter private Boolean allergy_복숭아 = false;
    @Builder.Default @Setter private Boolean allergy_토마토 = false;
    @Builder.Default @Setter private Boolean allergy_아황산류 = false;
    @Builder.Default @Setter private Boolean allergy_호두 = false;
    @Builder.Default @Setter private Boolean allergy_닭고기 = false;
    @Builder.Default @Setter private Boolean allergy_쇠고기 = false;
    @Builder.Default @Setter private Boolean allergy_오징어 = false;
    @Builder.Default @Setter private Boolean allergy_조개류 = false;
    @Builder.Default @Setter private Boolean allergy_굴 = false;
    @Builder.Default @Setter private Boolean allergy_전복 = false;
    @Builder.Default @Setter private Boolean allergy_홍합 = false;
    @Builder.Default @Setter private Boolean allergy_잣 = false;

    // 식사 스타일
    @Builder.Default @Setter private Boolean mealStyle_고기위주 = false;
    @Builder.Default @Setter private Boolean mealStyle_채소위주 = false;
    @Builder.Default @Setter private Boolean mealStyle_혼합식 = false;

    // 레시피 스타일
    @Builder.Default @Setter private Boolean recipeStyle_한식 = false;
    @Builder.Default @Setter private Boolean recipeStyle_중식 = false;
    @Builder.Default @Setter private Boolean recipeStyle_일식 = false;
    @Builder.Default @Setter private Boolean recipeStyle_양식 = false;
    @Builder.Default @Setter private Boolean recipeStyle_디저트 = false;
    @Builder.Default @Setter private Boolean recipeStyle_음료_차 = false;
    @Builder.Default @Setter private Boolean recipeStyle_양념_소스_잼 = false;

    // 식단 이유
    @Builder.Default @Setter private boolean reason_항산화강화 = false;
    @Builder.Default @Setter private boolean reason_혈당조절 = false;
    @Builder.Default @Setter private boolean reason_염증감소 = false;

    // 알러지 리스트 반환
    public List<String> getAllergies() {
        List<String> result = new ArrayList<>();

        if (allergy_알류) result.add("알류");
        if (allergy_우유) result.add("우유");
        if (allergy_각류) result.add("각류");
        if (allergy_밀류) result.add("밀류");
        if (allergy_유제품) result.add("유제품");
        if (allergy_메밀) result.add("메밀");
        if (allergy_땅콩) result.add("땅콩");
        if (allergy_대두) result.add("대두");
        if (allergy_밀) result.add("밀");
        if (allergy_고등어) result.add("고등어");
        if (allergy_돼지고기) result.add("돼지고기");
        if (allergy_복숭아) result.add("복숭아");
        if (allergy_토마토) result.add("토마토");
        if (allergy_아황산류) result.add("아황산류");
        if (allergy_호두) result.add("호두");
        if (allergy_닭고기) result.add("닭고기");
        if (allergy_쇠고기) result.add("쇠고기");
        if (allergy_오징어) result.add("오징어");
        if (allergy_조개류) result.add("조개류");
        if (allergy_굴) result.add("굴");
        if (allergy_전복) result.add("전복");
        if (allergy_홍합) result.add("홍합");
        if (allergy_잣) result.add("잣");

        return result;
    }

    // 식사 스타일 리스트 반환
    public List<String> getMealStyles() {
        List<String> result = new ArrayList<>();

        if (mealStyle_고기위주) result.add("고기위주");
        if (mealStyle_채소위주) result.add("채소위주");
        if (mealStyle_혼합식) result.add("혼합식");

        return result;
    }

    // 레시피 스타일 리스트 반환
    public List<String> getRecipeStyles() {
        List<String> result = new ArrayList<>();

        if (recipeStyle_한식) result.add("한식");
        if (recipeStyle_중식) result.add("중식");
        if (recipeStyle_일식) result.add("일식");
        if (recipeStyle_양식) result.add("양식");
        if (recipeStyle_디저트) result.add("디저트");
        if (recipeStyle_음료_차) result.add("음료_차");
        if (recipeStyle_양념_소스_잼) result.add("양념_소스_잼");

        return result;
    }

    // 식단 이유 리스트 반환
    public List<String> getReasons() {
        List<String> result = new ArrayList<>();

        if (reason_항산화강화) result.add("항산화강화");
        if (reason_혈당조절) result.add("혈당조절");
        if (reason_염증감소) result.add("염증감소");

        return result;
    }

    // 설문조사 결과를 member 객체에 적용 - 알러지
    public void applyAllergies(List<String> allergies) {
        for (String allergy : allergies) {
            switch (allergy) {
                case "알류" -> this.setAllergy_알류(true);
                case "우유" -> this.setAllergy_우유(true);
                case "각류" -> this.setAllergy_각류(true);
                case "밀류" -> this.setAllergy_밀류(true);
                case "유제품" -> this.setAllergy_유제품(true);
                case "메밀" -> this.setAllergy_메밀(true);
                case "땅콩" -> this.setAllergy_땅콩(true);
                case "대두" -> this.setAllergy_대두(true);
                case "밀" -> this.setAllergy_밀(true);
                case "고등어" -> this.setAllergy_고등어(true);
                case "돼지고기" -> this.setAllergy_돼지고기(true);
                case "복숭아" -> this.setAllergy_복숭아(true);
                case "토마토" -> this.setAllergy_토마토(true);
                case "아황산류" -> this.setAllergy_아황산류(true);
                case "호두" -> this.setAllergy_호두(true);
                case "닭고기" -> this.setAllergy_닭고기(true);
                case "쇠고기" -> this.setAllergy_쇠고기(true);
                case "오징어" -> this.setAllergy_오징어(true);
                case "조개류" -> this.setAllergy_조개류(true);
                case "굴" -> this.setAllergy_굴(true);
                case "전복" -> this.setAllergy_전복(true);
                case "홍합" -> this.setAllergy_홍합(true);
                case "잣" -> this.setAllergy_잣(true);
                default -> throw new CustomException(ErrorCode.INVALID_ALLERGY_VALUE);
            }
        }
    }

    // 설문조사 결과를 member 객체에 적용 - 식사 스타일
    public void applyMealStyles(List<String> styles) {
        for (String style : styles) {
            switch (style) {
                case "고기위주" -> this.setMealStyle_고기위주(true);
                case "채소위주" -> this.setMealStyle_채소위주(true);
                case "혼합식" -> this.setMealStyle_혼합식(true);
                default -> throw new CustomException(ErrorCode.INVALID_MEAL_STYLE);
            }
        }
    }

    // 설문조사 결과를 member 객체에 적용 - 레시피 스타일
    public void applyRecipeStyles(List<String> styles) {
        for (String style : styles) {
            switch (style) {
                case "한식" -> this.setRecipeStyle_한식(true);
                case "중식" -> this.setRecipeStyle_중식(true);
                case "일식" -> this.setRecipeStyle_일식(true);
                case "양식" -> this.setRecipeStyle_양식(true);
                case "디저트" -> this.setRecipeStyle_디저트(true);
                case "음료_차" -> this.setRecipeStyle_음료_차(true);
                case "양념_소스_잼" -> this.setRecipeStyle_양념_소스_잼(true);
                default -> throw new CustomException(ErrorCode.INVALID_RECIPE_STYLE);
            }
        }
    }

    // 설문조사 결과를 member 객체에 적용 - 식단 이유
    public void applyReasons(List<String> reasons) {
        for (String reason : reasons) {
            switch (reason) {
                case "항산화강화" -> this.setReason_항산화강화(true);
                case "혈당조절" -> this.setReason_혈당조절(true);
                case "염증감소" -> this.setReason_염증감소(true);
                default -> throw new CustomException(ErrorCode.INVALID_REASON);
            }
        }
    }

    // 알러지 필드 모두 false로 초기화
    public void resetAllergies() {
        this.setAllergy_알류(false);
        this.setAllergy_우유(false);
        this.setAllergy_각류(false);
        this.setAllergy_밀류(false);
        this.setAllergy_유제품(false);
        this.setAllergy_메밀(false);
        this.setAllergy_땅콩(false);
        this.setAllergy_대두(false);
        this.setAllergy_밀(false);
        this.setAllergy_고등어(false);
        this.setAllergy_돼지고기(false);
        this.setAllergy_복숭아(false);
        this.setAllergy_토마토(false);
        this.setAllergy_아황산류(false);
        this.setAllergy_호두(false);
        this.setAllergy_닭고기(false);
        this.setAllergy_쇠고기(false);
        this.setAllergy_오징어(false);
        this.setAllergy_조개류(false);
        this.setAllergy_굴(false);
        this.setAllergy_전복(false);
        this.setAllergy_홍합(false);
        this.setAllergy_잣(false);
    }

    // 식사 스타일 필드 초기화
    public void resetMealStyles() {
        this.setMealStyle_고기위주(false);
        this.setMealStyle_채소위주(false);
        this.setMealStyle_혼합식(false);
    }

    // 레시피 스타일 필드 초기화
    public void resetRecipeStyles() {
        this.setRecipeStyle_한식(false);
        this.setRecipeStyle_중식(false);
        this.setRecipeStyle_일식(false);
        this.setRecipeStyle_양식(false);
        this.setRecipeStyle_디저트(false);
        this.setRecipeStyle_음료_차(false);
        this.setRecipeStyle_양념_소스_잼(false);
    }

    // 이유 필드 초기화
    public void resetReasons() {
        this.setReason_항산화강화(false);
        this.setReason_혈당조절(false);
        this.setReason_염증감소(false);
    }
}