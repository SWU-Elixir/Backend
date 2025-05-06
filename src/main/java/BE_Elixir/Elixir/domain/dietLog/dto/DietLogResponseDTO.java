package BE_Elixir.Elixir.domain.dietLog.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietLogResponseDTO {

    private Long id;
    private Long memberId;
    private String name;
    private String imageUrl;
    private String type;
    private int score;
    private List<Long> ingredientTagId;
    private LocalDateTime time;

}
