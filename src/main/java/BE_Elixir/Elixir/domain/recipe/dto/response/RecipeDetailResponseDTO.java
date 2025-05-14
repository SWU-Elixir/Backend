package BE_Elixir.Elixir.domain.recipe.dto.response;

import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeCommentResponseDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.global.enums.CategorySlowAging;
import BE_Elixir.Elixir.global.enums.CategoryType;
import BE_Elixir.Elixir.global.enums.Difficulty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class RecipeDetailResponseDTO {
    private Long id;
    private String authorNickname; // 작성자의 닉네임
    private String authorTitle; // 작성자의 칭호
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
    private Map<String, String> ingredients;
    private Map<String, String> seasoning;

    private List<String> stepDescriptions;
    private List<String> stepImageUrls;

    private String tips;
    private Integer likes;
    private Boolean likedByCurrentUser; // 현재 사용자가 좋아요를 눌렀는지
    private Boolean scrappedByCurrentUser; // 현재 사용자가 스크랩을 눌렀는지
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 알러지 정보
    private List<String> allergies;

    // 댓글 리스트
    private List<RecipeCommentResponseDTO> comments;

    public RecipeDetailResponseDTO(Recipe recipe, List<RecipeCommentResponseDTO> comments, Boolean likedByCurrentUser, Boolean scrappedByCurrentUser) {
        this.id = recipe.getId();
        this.authorNickname = recipe.getMember().getNickname();
        this.authorTitle = recipe.getMember().getTitle();
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

        this.ingredients = recipe.getIngredients();
        this.seasoning = recipe.getSeasoning();

        // 레시피 순서
        this.stepDescriptions = recipe.getStepDescriptions();
        this.stepImageUrls = recipe.getStepImageUrls();

        this.tips = recipe.getTips();
        this.likes = recipe.getLikes();
        this.likedByCurrentUser = likedByCurrentUser; // 좋아요 여부
        this.scrappedByCurrentUser = scrappedByCurrentUser; // 스크랩 여부
        this.createdAt = recipe.getCreatedAt();
        this.updatedAt = recipe.getUpdatedAt();

        // 알러지 정보
        this.allergies = new java.util.ArrayList<>();
        if (Boolean.TRUE.equals(recipe.getAllergy_알류())) allergies.add("알류");
        if (Boolean.TRUE.equals(recipe.getAllergy_우유())) allergies.add("우유");
        if (Boolean.TRUE.equals(recipe.getAllergy_각류())) allergies.add("각류");
        if (Boolean.TRUE.equals(recipe.getAllergy_밀류())) allergies.add("밀류");
        if (Boolean.TRUE.equals(recipe.getAllergy_유제품())) allergies.add("유제품");
        if (Boolean.TRUE.equals(recipe.getAllergy_메밀())) allergies.add("메밀");
        if (Boolean.TRUE.equals(recipe.getAllergy_땅콩())) allergies.add("땅콩");
        if (Boolean.TRUE.equals(recipe.getAllergy_대두())) allergies.add("대두");
        if (Boolean.TRUE.equals(recipe.getAllergy_밀())) allergies.add("밀");
        if (Boolean.TRUE.equals(recipe.getAllergy_고등어())) allergies.add("고등어");
        if (Boolean.TRUE.equals(recipe.getAllergy_돼지고기())) allergies.add("돼지고기");
        if (Boolean.TRUE.equals(recipe.getAllergy_복숭아())) allergies.add("복숭아");
        if (Boolean.TRUE.equals(recipe.getAllergy_토마토())) allergies.add("토마토");
        if (Boolean.TRUE.equals(recipe.getAllergy_아황산류())) allergies.add("아황산류");
        if (Boolean.TRUE.equals(recipe.getAllergy_호두())) allergies.add("호두");
        if (Boolean.TRUE.equals(recipe.getAllergy_닭고기())) allergies.add("닭고기");
        if (Boolean.TRUE.equals(recipe.getAllergy_쇠고기())) allergies.add("쇠고기");
        if (Boolean.TRUE.equals(recipe.getAllergy_오징어())) allergies.add("오징어");
        if (Boolean.TRUE.equals(recipe.getAllergy_조개류())) allergies.add("조개류");
        if (Boolean.TRUE.equals(recipe.getAllergy_굴())) allergies.add("굴");
        if (Boolean.TRUE.equals(recipe.getAllergy_전복())) allergies.add("전복");
        if (Boolean.TRUE.equals(recipe.getAllergy_홍합())) allergies.add("홍합");
        if (Boolean.TRUE.equals(recipe.getAllergy_잣())) allergies.add("잣");

        this.comments = comments;
    }

}
