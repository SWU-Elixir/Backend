package BE_Elixir.Elixir.global.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDTO {

    private String type;
    private List<Map<String, String>> history;

}