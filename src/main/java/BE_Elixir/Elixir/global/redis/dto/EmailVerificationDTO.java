package BE_Elixir.Elixir.global.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationDTO {

    private String email;
    private String code;
    private Instant emailSendTime;

}
