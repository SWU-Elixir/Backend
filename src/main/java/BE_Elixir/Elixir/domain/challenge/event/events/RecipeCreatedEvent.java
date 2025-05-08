package BE_Elixir.Elixir.domain.challenge.event.events;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
// 사용자가 새로운 레시피를 등록할 때 발생하는 이벤트
public class RecipeCreatedEvent {
    private final Long memberId;
    private Long recipeId;  // 생성된 레시피 ID
    private LocalDateTime timestamp;  // 레시피 등록 시간

    public RecipeCreatedEvent(Long memberId, LocalDateTime timestamp) {
        this.memberId = memberId;
        this.timestamp = timestamp;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDateTime getTimeStamp() {
        return timestamp;
    }
}
