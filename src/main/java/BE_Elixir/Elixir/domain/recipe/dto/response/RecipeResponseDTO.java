package BE_Elixir.Elixir.domain.recipe.dto.response;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.recipe.dto.MaterialDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Material;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.Difficulty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class RecipeResponseDTO {
    private Long id;
    private String email;
    private String title;
    private String imageUrl;
    private String description;
    private CategorySlowAging categorySlowAging;
    private CategoryType categoryType;
    private Difficulty difficulty;
    private Integer timeHours;
    private Integer timeMinutes;

    // 태그된 식재료 정보
    private List<Long> ingredientTagIds;
    private List<MaterialDTO> ingredients; // 재료
    private List<MaterialDTO> seasonings;   // 양념

    private List<String> stepDescriptions;
    private List<String> stepImageUrls;

    private String tips;
    private Integer likes;   // 좋아요 수
    private Integer scraps;  // 스크랩 수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 알러지 정보
    private List<String> allergies;

    public List<String> getAllergies() {
        return allergies;
    }



    public RecipeResponseDTO(Recipe recipe) {
        this.id = recipe.getId();
        this.email = recipe.getMember().getEmail();
        this.title = recipe.getTitle();
        this.imageUrl = recipe.getImageUrl();
        this.description = recipe.getDescription();
        this.categorySlowAging = recipe.getCategorySlowAging();
        this.categoryType = recipe.getCategoryType();
        this.difficulty = recipe.getDifficulty();
        this.timeHours = recipe.getTimeHours();
        this.timeMinutes = recipe.getTimeMinutes();

        // 식재료 태그
        this.ingredientTagIds = recipe.getIngredientTags().stream()
                .map(tag -> tag.getIngredient().getId())
                .collect(Collectors.toList());

        // 재료 & 양념
        this.ingredients = recipe.getIngredients().stream()
                .map(material -> new MaterialDTO(material.getName(), material.getValue(), material.getUnit()))
                .collect(Collectors.toList());

        this.seasonings = recipe.getSeasonings().stream()
                .map(material -> new MaterialDTO(material.getName(), material.getValue(), material.getUnit()))
                .collect(Collectors.toList());


        // 레시피 순서
        this.stepDescriptions = recipe.getStepDescriptions();
        this.stepImageUrls = recipe.getStepImageUrls();

        this.tips = recipe.getTips();
        this.likes = recipe.getLikes();
        this.scraps = recipe.getScraps();
        this.createdAt = recipe.getCreatedAt();
        this.updatedAt = recipe.getUpdatedAt();

        // 알러지
        this.allergies = recipe.getAllergyList();

    }

}
