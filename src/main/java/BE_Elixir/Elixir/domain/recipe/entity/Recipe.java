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

    @ManyToOne
    @Column(name = "member_id", nullable = false)
    private Long memberId;

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
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
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
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Recipe from(RecipeRequestDTO dto) {
        Recipe recipe = new Recipe();

        recipe.setMemberId(dto.getMemberId());
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

        recipe.setAllergy_알류(dto.getAllergy_알류());
        recipe.setAllergy_우유(dto.getAllergy_우유());
        recipe.setAllergy_각류(dto.getAllergy_각류());
        recipe.setAllergy_밀류(dto.getAllergy_밀류());
        recipe.setAllergy_유제품(dto.getAllergy_유제품());
        recipe.setAllergy_메밀(dto.getAllergy_메밀());
        recipe.setAllergy_땅콩(dto.getAllergy_땅콩());
        recipe.setAllergy_대두(dto.getAllergy_대두());
        recipe.setAllergy_밀(dto.getAllergy_밀());
        recipe.setAllergy_고등어(dto.getAllergy_고등어());
        recipe.setAllergy_돼지고기(dto.getAllergy_돼지고기());
        recipe.setAllergy_복숭아(dto.getAllergy_복숭아());
        recipe.setAllergy_토마토(dto.getAllergy_토마토());
        recipe.setAllergy_아황산류(dto.getAllergy_아황산류());
        recipe.setAllergy_호두(dto.getAllergy_호두());
        recipe.setAllergy_닭고기(dto.getAllergy_닭고기());
        recipe.setAllergy_쇠고기(dto.getAllergy_쇠고기());
        recipe.setAllergy_오징어(dto.getAllergy_오징어());
        recipe.setAllergy_조개류(dto.getAllergy_조개류());
        recipe.setAllergy_굴(dto.getAllergy_굴());
        recipe.setAllergy_전복(dto.getAllergy_전복());
        recipe.setAllergy_홍합(dto.getAllergy_홍합());
        recipe.setAllergy_잣(dto.getAllergy_잣());

        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());

        return recipe;
    }

}
