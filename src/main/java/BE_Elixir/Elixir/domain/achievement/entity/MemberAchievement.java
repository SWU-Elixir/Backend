package BE_Elixir.Elixir.domain.achievement.entity;


import BE_Elixir.Elixir.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class MemberAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    private int currentProgress=0;    // 업적 진행도

    private boolean completed;  // 달성 여부

    private LocalDateTime updatedAt;    // 최근 활동 확인용 - 업데이트 시간

    private LocalDateTime completedAt;   // 달성 시간

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
