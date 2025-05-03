package BE_Elixir.Elixir.domain.dietLog.entity;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.global.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
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
    @Setter
    private List<DietLogIngredient> ingredientTags;

    private LocalDateTime time;
}