package BE_Elixir.Elixir.domain.challenge.event.events;

import BE_Elixir.Elixir.global.enums.DietLogType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
// 사용자가 식단을 기록할 때 발생하는 이벤트
public class DietLogEvent {
    private final Long memberId;
    private Long dietLogId;  // 생성된 식단 기록 ID
    private DietLogType type;  // 아침, 점심, 저녁
    private LocalDateTime timestamp;  // 식단 기록 시간

    public DietLogEvent(Long memberId, Long dietLogId, DietLogType type, LocalDateTime timestamp) {
        this.memberId = memberId;
        this.dietLogId = dietLogId;
        this.type = type;
        this.timestamp = timestamp;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDateTime getTimeStamp() {
        return timestamp;
    }
}
