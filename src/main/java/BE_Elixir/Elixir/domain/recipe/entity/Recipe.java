package BE_Elixir.Elixir.domain.recipe.entity;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeRequestDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.Difficulty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 255)
    private String title;

    private String imageUrl;

    @Column(length = 200)
    private String description;

    // 카테고리 - 저속노화
    @Enumerated(EnumType.STRING)
    private CategorySlowAging categorySlowAging;

    // 카테고리 - 음식 유형
    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;

    // 난이도
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private Integer timeHours;

    private Integer timeMinutes;

    // 식재료 태그
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredientTags;

    // 재료
    @ElementCollection
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    @MapKeyColumn(name = "ingredient_name")
    @Column(name = "ingredient_amount")
    private Map<String, String> ingredients;

    // 양념
    @ElementCollection
    @CollectionTable(name = "recipe_seasonings", joinColumns = @JoinColumn(name = "recipe_id"))
    @MapKeyColumn(name = "seasoning_name")
    @Column(name = "seasoning_amount")
    private Map<String, String> seasoning;

    // 순서 설명 (문장) + 이미지 URL
    @ElementCollection
    @CollectionTable(name = "recipe_step_descriptions", joinColumns = @JoinColumn(name = "recipe_id"))
    private List<String> stepDescriptions;

    @ElementCollection
    @CollectionTable(name = "recipe_step_images", joinColumns = @JoinColumn(name = "recipe_id"))
    private List<String> stepImageUrls;
    @Lob
    private String tips;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer likes;
    private Integer scraps;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeEvent> recipeEvents = new ArrayList<>();

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

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.likes = 0;
        this.scraps = 0;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void clearIngredientTags() {
        if (this.ingredientTags != null) {
            this.ingredientTags.clear();
        }
    }

    public List<String> getAllergyList() {
        List<String> result = new ArrayList<>();

        if (Boolean.TRUE.equals(allergy_알류)) result.add("알류");
        if (Boolean.TRUE.equals(allergy_우유)) result.add("우유");
        if (Boolean.TRUE.equals(allergy_각류)) result.add("각류");
        if (Boolean.TRUE.equals(allergy_밀류)) result.add("밀류");
        if (Boolean.TRUE.equals(allergy_유제품)) result.add("유제품");
        if (Boolean.TRUE.equals(allergy_메밀)) result.add("메밀");
        if (Boolean.TRUE.equals(allergy_땅콩)) result.add("땅콩");
        if (Boolean.TRUE.equals(allergy_대두)) result.add("대두");
        if (Boolean.TRUE.equals(allergy_밀)) result.add("밀");
        if (Boolean.TRUE.equals(allergy_고등어)) result.add("고등어");
        if (Boolean.TRUE.equals(allergy_돼지고기)) result.add("돼지고기");
        if (Boolean.TRUE.equals(allergy_복숭아)) result.add("복숭아");
        if (Boolean.TRUE.equals(allergy_토마토)) result.add("토마토");
        if (Boolean.TRUE.equals(allergy_아황산류)) result.add("아황산류");
        if (Boolean.TRUE.equals(allergy_호두)) result.add("호두");
        if (Boolean.TRUE.equals(allergy_닭고기)) result.add("닭고기");
        if (Boolean.TRUE.equals(allergy_쇠고기)) result.add("쇠고기");
        if (Boolean.TRUE.equals(allergy_오징어)) result.add("오징어");
        if (Boolean.TRUE.equals(allergy_조개류)) result.add("조개류");
        if (Boolean.TRUE.equals(allergy_굴)) result.add("굴");
        if (Boolean.TRUE.equals(allergy_전복)) result.add("전복");
        if (Boolean.TRUE.equals(allergy_홍합)) result.add("홍합");
        if (Boolean.TRUE.equals(allergy_잣)) result.add("잣");

        return result;
    }

    public static Recipe from(RecipeRequestDTO dto, Member member) {
        Recipe recipe = new Recipe();
        recipe.setMember(member);
        recipe.setTitle(dto.getTitle());
        recipe.setDescription(dto.getDescription());
        recipe.setCategorySlowAging(dto.getCategorySlowAging());
        recipe.setCategoryType(dto.getCategoryType());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setTimeHours(dto.getTimeHours());
        recipe.setTimeMinutes(dto.getTimeMinutes());
        recipe.setIngredients(dto.getIngredients());
        recipe.setSeasoning(dto.getSeasoning());
        recipe.setTips(dto.getTips());
        recipe.setStepDescriptions(dto.getStepDescriptions());

        List<String> allergies = dto.getAllergies();
        if (allergies != null) {
            recipe.setAllergy_알류(allergies.contains("알류"));
            recipe.setAllergy_우유(allergies.contains("우유"));
            recipe.setAllergy_각류(allergies.contains("각류"));
            recipe.setAllergy_밀류(allergies.contains("밀류"));
            recipe.setAllergy_유제품(allergies.contains("유제품"));
            recipe.setAllergy_메밀(allergies.contains("메밀"));
            recipe.setAllergy_땅콩(allergies.contains("땅콩"));
            recipe.setAllergy_대두(allergies.contains("대두"));
            recipe.setAllergy_밀(allergies.contains("밀"));
            recipe.setAllergy_고등어(allergies.contains("고등어"));
            recipe.setAllergy_돼지고기(allergies.contains("돼지고기"));
            recipe.setAllergy_복숭아(allergies.contains("복숭아"));
            recipe.setAllergy_토마토(allergies.contains("토마토"));
            recipe.setAllergy_아황산류(allergies.contains("아황산류"));
            recipe.setAllergy_호두(allergies.contains("호두"));
            recipe.setAllergy_닭고기(allergies.contains("닭고기"));
            recipe.setAllergy_쇠고기(allergies.contains("쇠고기"));
            recipe.setAllergy_오징어(allergies.contains("오징어"));
            recipe.setAllergy_조개류(allergies.contains("조개류"));
            recipe.setAllergy_굴(allergies.contains("굴"));
            recipe.setAllergy_전복(allergies.contains("전복"));
            recipe.setAllergy_홍합(allergies.contains("홍합"));
            recipe.setAllergy_잣(allergies.contains("잣"));
        }

        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());

        return recipe;
    }

    public void updateFrom(RecipeRequestDTO dto) {
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.categorySlowAging = dto.getCategorySlowAging();
        this.categoryType = dto.getCategoryType();
        this.difficulty = dto.getDifficulty();
        this.timeHours = dto.getTimeHours();
        this.timeMinutes = dto.getTimeMinutes();
        this.ingredients = dto.getIngredients();
        this.seasoning = dto.getSeasoning();
        this.tips = dto.getTips();
        this.stepDescriptions = dto.getStepDescriptions();

        List<String> allergies = dto.getAllergies();
        this.allergy_알류 = allergies.contains("알류");
        this.allergy_우유 = allergies.contains("우유");
        this.allergy_각류 = allergies.contains("각류");
        this.allergy_밀류 = allergies.contains("밀류");
        this.allergy_유제품 = allergies.contains("유제품");
        this.allergy_메밀 = allergies.contains("메밀");
        this.allergy_땅콩 = allergies.contains("땅콩");
        this.allergy_대두 = allergies.contains("대두");
        this.allergy_밀 = allergies.contains("밀");
        this.allergy_고등어 = allergies.contains("고등어");
        this.allergy_돼지고기 = allergies.contains("돼지고기");
        this.allergy_복숭아 = allergies.contains("복숭아");
        this.allergy_토마토 = allergies.contains("토마토");
        this.allergy_아황산류 = allergies.contains("아황산류");
        this.allergy_호두 = allergies.contains("호두");
        this.allergy_닭고기 = allergies.contains("닭고기");
        this.allergy_쇠고기 = allergies.contains("쇠고기");
        this.allergy_오징어 = allergies.contains("오징어");
        this.allergy_조개류 = allergies.contains("조개류");
        this.allergy_굴 = allergies.contains("굴");
        this.allergy_전복 = allergies.contains("전복");
        this.allergy_홍합 = allergies.contains("홍합");
        this.allergy_잣 = allergies.contains("잣");
    }
}