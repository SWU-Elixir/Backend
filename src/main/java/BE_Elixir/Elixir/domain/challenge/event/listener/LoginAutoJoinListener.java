package BE_Elixir.Elixir.domain.challenge.event.listener;


import BE_Elixir.Elixir.domain.challenge.event.events.LoginSuccessEvent;
import BE_Elixir.Elixir.domain.challenge.dto.service.ChallengeAchievementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginAutoJoinListener {
    private final ChallengeAchievementService challengeAchievementService;

    @EventListener
    public void onLogin(LoginSuccessEvent event) {
        challengeAchievementService.challengeParticipation(event.getEmail());
        log.info("자동 챌린지 참여 완료: {}", event.getEmail());
    }

}
