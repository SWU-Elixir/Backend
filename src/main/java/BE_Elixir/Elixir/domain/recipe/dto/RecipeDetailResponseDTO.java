package BE_Elixir.Elixir.domain.recipe.dto;

import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.Difficulty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RecipeDetailResponseDTO {
    private Long id;
    private String authorNickname;
    private String title;
    private String imageUrl;
    private String description;
    private CategorySlowAging categorySlowAging;
    private CategoryType categoryType;
    private Difficulty difficulty;
    private Integer timeHours;
    private Integer timeMinutes;

    // 태그된 식재료 정보
    private List<String> ingredientTags;
    private Map<String, String> ingredients;
    private Map<String, String> seasoning;

    private List<String> stepDescriptions;
    private List<String> stepImageUrls;

    private String tips;
    private Integer likes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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


    // 댓글 리스트
    private List<RecipeCommentResponseDTO> comments;

    public RecipeDetailResponseDTO(Recipe recipe, List<RecipeCommentResponseDTO> comments) {
        this.id = recipe.getId();
        this.authorNickname = recipe.getMember().getNickname();
        this.title = recipe.getTitle();
        this.imageUrl = recipe.getImageUrl();
        this.description = recipe.getDescription();
        this.categorySlowAging = recipe.getCategorySlowAging();
        this.categoryType = recipe.getCategoryType();
        this.difficulty = recipe.getDifficulty();
        this.timeHours = recipe.getTimeHours();
        this.timeMinutes = recipe.getTimeMinutes();

        // 식재료 태그
        this.ingredientTags = recipe.getIngredientTags().stream()
                .map(tag -> tag.getIngredient().getName())
                .toList();

        this.ingredients = recipe.getIngredients();
        this.seasoning = recipe.getSeasoning();

        // 레시피 순서
        this.stepDescriptions = recipe.getStepDescriptions();
        this.stepImageUrls = recipe.getStepImageUrls();

        this.tips = recipe.getTips();
        this.likes = recipe.getLikes();
        this.createdAt = recipe.getCreatedAt();
        this.updatedAt = recipe.getUpdatedAt();

        // 알러지 정보
        this.allergy_알류 = recipe.getAllergy_알류();
        this.allergy_우유 = recipe.getAllergy_우유();
        this.allergy_각류 = recipe.getAllergy_각류();
        this.allergy_밀류 = recipe.getAllergy_밀류();
        this.allergy_유제품 = recipe.getAllergy_유제품();
        this.allergy_메밀 = recipe.getAllergy_메밀();
        this.allergy_땅콩 = recipe.getAllergy_땅콩();
        this.allergy_대두 = recipe.getAllergy_대두();
        this.allergy_밀 = recipe.getAllergy_밀();
        this.allergy_고등어 = recipe.getAllergy_고등어();
        this.allergy_돼지고기 = recipe.getAllergy_돼지고기();
        this.allergy_복숭아 = recipe.getAllergy_복숭아();
        this.allergy_토마토 = recipe.getAllergy_토마토();
        this.allergy_아황산류 = recipe.getAllergy_아황산류();
        this.allergy_호두 = recipe.getAllergy_호두();
        this.allergy_닭고기 = recipe.getAllergy_닭고기();
        this.allergy_쇠고기 = recipe.getAllergy_쇠고기();
        this.allergy_오징어 = recipe.getAllergy_오징어();
        this.allergy_조개류 = recipe.getAllergy_조개류();
        this.allergy_굴 = recipe.getAllergy_굴();
        this.allergy_전복 = recipe.getAllergy_전복();
        this.allergy_홍합 = recipe.getAllergy_홍합();
        this.allergy_잣 = recipe.getAllergy_잣();

        this.comments = comments;
    }

}
