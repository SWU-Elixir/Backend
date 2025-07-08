package BE_Elixir.Elixir.domain.achievement.service;

import BE_Elixir.Elixir.domain.achievement.dto.AchievementCategoryDTO;
import BE_Elixir.Elixir.domain.achievement.dto.AchievementProgressDTO;
import BE_Elixir.Elixir.domain.achievement.dto.AllAchievementStatusResponseDTO;
import BE_Elixir.Elixir.domain.achievement.entity.Achievement;
import BE_Elixir.Elixir.domain.achievement.entity.MemberAchievement;
import BE_Elixir.Elixir.domain.achievement.repository.AchievementRepository;
import BE_Elixir.Elixir.domain.achievement.repository.MemberAchievementRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {
    private final MemberAchievementRepository memberAchievementRepository;
    private final AchievementRepository achievementRepository;
    private final MemberRepository memberRepository;

    // 업적 통합 조회
    public AllAchievementStatusResponseDTO getMyAchievements(MemberDetails memberDetails) {
        Member member = memberRepository.findByEmail(memberDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<MemberAchievement> userAchievements = memberAchievementRepository.findAllByMember(member);
        Map<Long, MemberAchievement> userAchievementMap = userAchievements.stream()
                .collect(Collectors.toMap(ma -> ma.getAchievement().getId(), ma -> ma));

        List<Achievement> allAchievements = achievementRepository.findAll();

        Map<AchievementType, AchievementCategoryDTO> achievementMap = new HashMap<>();

        for (Achievement achievement : allAchievements) {
            AchievementType type = achievement.getType();
            AchievementCategoryDTO category = achievementMap.computeIfAbsent(type,
                    k -> new AchievementCategoryDTO(0, new ArrayList<>()));

            MemberAchievement ma = userAchievementMap.get(achievement.getId());

            boolean achieved = ma != null && ma.isCompleted();
            int currentProgress = ma != null ? ma.getCurrentProgress() : 0;
            LocalDateTime achievedAt = achieved ? ma.getCompletedAt() : null;

            if (currentProgress > category.getCurrentValue()) {
                category.setCurrentValue(currentProgress);
            }

            AchievementProgressDTO progressDTO = new AchievementProgressDTO(
                    achievement.getId(),
                    achievement.getAchievementName(),
                    achievement.getLevel(),
                    achievement.getTargetValue(),
                    achieved,
                    achieved ? null : currentProgress,
                    achievedAt
            );

            category.getAchievements().add(progressDTO);
        }

        return new AllAchievementStatusResponseDTO(achievementMap);
    }
}
