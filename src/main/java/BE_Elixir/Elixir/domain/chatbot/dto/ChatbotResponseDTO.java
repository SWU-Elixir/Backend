package BE_Elixir.Elixir.domain.chatbot.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotResponseDTO {

    String chatSessionId;   // 챗봇 세션 ID
    String message;  // 사용자 입력 메시지

}