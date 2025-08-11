package BE_Elixir.Elixir.domain.member.service;

import BE_Elixir.Elixir.domain.achievement.service.MemberStatsService;
import BE_Elixir.Elixir.domain.challenge.event.events.LoginSuccessEvent;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAccessService {

    private final MemberRepository memberRepository;
    private final MemberStatsService memberStatsService;
    private final ApplicationEventPublisher eventPublisher;

    public void handleAppAccess(Long memberId, String email) {
        // 로그인 성공 이벤트 발행
        eventPublisher.publishEvent(new LoginSuccessEvent(email));

        // 총 로그인 일수 증가
        memberStatsService.increaseStat(memberId, AchievementType.TOTAL_LOGIN_DAYS, 1);
        // 연속 로그인 일수 갱신
        memberStatsService.increaseStat(memberId, AchievementType.CONSECUTIVE_LOGIN_DAYS, 1);
    }
}