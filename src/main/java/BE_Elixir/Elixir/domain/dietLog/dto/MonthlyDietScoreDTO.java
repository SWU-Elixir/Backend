package BE_Elixir.Elixir.domain.dietLog.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyDietScoreDTO {
    private Long id;
    private LocalDateTime time;
    private int score;
}
