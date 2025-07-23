package BE_Elixir.Elixir.domain.achievement.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MemberStats {
    @Id
    private Long memberId;

    private int totalLoginDays;
    private int consecutiveLoginDays;
    private int totalDietLogs;
    private int totalRecipeLogs;
    private int totalScraps;
    private int totalFollowers;

    private LocalDateTime updatedAt;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}