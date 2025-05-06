package BE_Elixir.Elixir.domain.dietLog.dto;

import BE_Elixir.Elixir.domain.dietLog.entity.DietLog;
import BE_Elixir.Elixir.domain.dietLog.entity.DietLogIngredient;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.global.enums.DietLogType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietLogRequestDTO {

    private String name;
    private String type;
    private int score;
    private List<Long> ingredientTagId;
    private LocalDateTime time;

    public DietLog toEntity(DietLogType typeEnum, Member member) {
        return DietLog.builder()
                .member(member)
                .name(this.name)
                .type(typeEnum)
                .score(this.score)
                .time(this.time)
                .build();
    }
}
