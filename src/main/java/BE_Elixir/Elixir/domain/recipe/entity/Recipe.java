package BE_Elixir.Elixir.domain.recipe.entity;

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

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne // 일단 로그인 없이 진행 → 나중에 Member로 수정
    @JoinColumn(name = "member_id", nullable = false)
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

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private Integer timeHours;

    private Integer timeMinutes;

    // 알러지 태그
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<RecipeAllergy> recipeAllergies = new ArrayList<>();

    // 식재료 태그
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();

    // 재료 JSON
    @Lob
    @Column(columnDefinition = "json")
    private String ingredients;

    // 양념 JSON
    @Lob
    @Column(columnDefinition = "json")
    private String seasoning;

    // 요리 순서 JSON
    @Lob
    @Column(columnDefinition = "json")
    private String recipeOrder;

    @Lob
    private String tips;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer likes;

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
}
