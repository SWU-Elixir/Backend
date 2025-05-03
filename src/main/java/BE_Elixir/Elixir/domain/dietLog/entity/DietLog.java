package BE_Elixir.Elixir.domain.dietLog.entity;

import BE_Elixir.Elixir.domain.dietLog.dto.DietLogResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.global.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class DietLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 255)
    private String name;

    @Setter
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private DietLogType type;

    private int score;

    @OneToMany(mappedBy = "dietLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DietLogIngredient> ingredientTags = new ArrayList<>();

    private LocalDateTime time;

    public void setIngredientTags(List<DietLogIngredient> ingredientTags) {
        this.ingredientTags.clear();
        this.ingredientTags.addAll(ingredientTags);
    }

    // DietLogResponseDTO 로 변환
    public DietLogResponseDTO convertToResponseDTO() {
        List<Long> ingredientTagIds = this.getIngredientTags().stream()
                .map(dietLogIngredient -> dietLogIngredient.getIngredient().getId())
                .toList();

        return DietLogResponseDTO.builder()
                .id(this.getId())
                .memberId(this.getMember().getId())
                .name(this.getName())
                .imageUrl(this.getImageUrl())
                .type(this.getType().toString())
                .score(this.getScore())
                .ingredientTagId(ingredientTagIds)
                .time(this.getTime())
                .build();
    }


}