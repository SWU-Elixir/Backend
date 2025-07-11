package BE_Elixir.Elixir.domain.achievement.service;

import BE_Elixir.Elixir.domain.achievement.dto.AchievementCategoryDTO;
import BE_Elixir.Elixir.domain.achievement.dto.AchievementProgressDTO;
import BE_Elixir.Elixir.domain.achievement.dto.AllAchievementStatusResponseDTO;
import BE_Elixir.Elixir.domain.achievement.entity.Achievement;
import BE_Elixir.Elixir.domain.achievement.entity.MemberAchievement;
import BE_Elixir.Elixir.domain.achievement.entity.MemberStats;
import BE_Elixir.Elixir.domain.achievement.repository.AchievementRepository;
import BE_Elixir.Elixir.domain.achievement.repository.MemberAchievementRepository;
import BE_Elixir.Elixir.domain.achievement.repository.MemberStatsRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final MemberStatsRepository memberStatsRepository;


    // 업적 통합 조회
    @Transactional
    public AllAchievementStatusResponseDTO getMyAchievements(MemberDetails memberDetails) {
        Member member = memberRepository.findByEmail(memberDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 회원의 누적 상태 조회 (없으면 기본값)
        MemberStats stats = memberStatsRepository.findById(member.getId())
                .orElseGet(() -> {
                    MemberStats ms = new MemberStats();
                    ms.setMemberId(member.getId());
                    ms.setTotalLoginDays(0);
                    ms.setConsecutiveLoginDays(0);
                    ms.setTotalDietLogs(0);
                    ms.setTotalRecipeLogs(0);
                    ms.setTotalScraps(0);
                    ms.setTotalFollowers(0);
                    return ms;
                });

        // 회원의 업적 진행 상황 목록
        List<MemberAchievement> userAchievements = memberAchievementRepository.findAllByMember(member);
        Map<Long, MemberAchievement> userAchievementMap = userAchievements.stream()
                .collect(Collectors.toMap(ma -> ma.getAchievement().getId(), ma -> ma));

        // 모든 업적 목록
        List<Achievement> allAchievements = achievementRepository.findAll();

        // 누적 데이터 매핑 (업적 타입별 진행도)
        Map<AchievementType, Integer> progressMap = Map.of(
                AchievementType.TOTAL_LOGIN_DAYS, stats.getTotalLoginDays(),
                AchievementType.CONSECUTIVE_LOGIN_DAYS, stats.getConsecutiveLoginDays(),
                AchievementType.TOTAL_DIET_LOGS, stats.getTotalDietLogs(),
                AchievementType.TOTAL_RECIPE_LOGS, stats.getTotalRecipeLogs(),
                AchievementType.TOTAL_SCRAPS, stats.getTotalScraps(),
                AchievementType.TOTAL_FOLLOWERS, stats.getTotalFollowers()
        );

        // 업적 진행 상황 동기화(진행도 업데이트 및 달성 처리)
        syncUserAchievements(member, allAchievements, userAchievementMap, progressMap);

        // 다시 사용자 업적 목록을 조회 (반영된 상태)
        userAchievements = memberAchievementRepository.findAllByMember(member);

        // 카테고리별로 묶어 리턴 DTO 생성
        Map<AchievementType, AchievementCategoryDTO> achievementMap = new HashMap<>();
        for (MemberAchievement ma : userAchievements) {
            AchievementType type = ma.getAchievement().getType();
            AchievementCategoryDTO category = achievementMap.computeIfAbsent(type,
                    k -> new AchievementCategoryDTO(0, new ArrayList<>()));

            int currentProgress = ma.getCurrentProgress();
            if (currentProgress > category.getCurrentValue()) {
                category.setCurrentValue(currentProgress);
            }

            Achievement a = ma.getAchievement();
            AchievementProgressDTO progressDTO = new AchievementProgressDTO(
                    a.getId(),
                    a.getAchievementName(),
                    a.getLevel(),
                    a.getTargetValue(),
                    ma.isCompleted(),
                    ma.isCompleted() ? null : currentProgress,
                    ma.isCompleted() ? ma.getCompletedAt() : null
            );

            category.getAchievements().add(progressDTO);
        }

        return new AllAchievementStatusResponseDTO(achievementMap);
    }

    // 업적 진행 상황 동기화
    @Transactional
    public void syncUserAchievements(
            Member member,
            List<Achievement> allAchievements,
            Map<Long, MemberAchievement> userAchievementMap,
            Map<AchievementType, Integer> progressMap
    ) {
        for (Achievement achievement : allAchievements) {
            int currentProgress = progressMap.getOrDefault(achievement.getType(), 0);

            MemberAchievement ma = userAchievementMap.get(achievement.getId());
            if (ma == null) {
                ma = new MemberAchievement();
                ma.setMember(member);
                ma.setAchievement(achievement);
                ma.setCurrentProgress(currentProgress);
                ma.setCompleted(currentProgress >= achievement.getTargetValue());
                if (ma.isCompleted()) {
                    ma.setCompletedAt(LocalDateTime.now());
                }
                memberAchievementRepository.save(ma);
                userAchievementMap.put(achievement.getId(), ma); // map 갱신
            } else {
                if (currentProgress > ma.getCurrentProgress()) {
                    ma.setCurrentProgress(currentProgress);
                    if (!ma.isCompleted() && currentProgress >= achievement.getTargetValue()) {
                        ma.setCompleted(true);
                        ma.setCompletedAt(LocalDateTime.now());
                    }
                    memberAchievementRepository.save(ma);
                }
            }
        }
    }
    @Transactional
    public void syncUserAchievements(Long memberId, AchievementType type, int newProgress) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Achievement> allAchievements = achievementRepository.findAllByType(type);
        List<MemberAchievement> userAchievements = memberAchievementRepository.findAllByMember(member);
        Map<Long, MemberAchievement> userAchievementMap = userAchievements.stream()
                .collect(Collectors.toMap(ma -> ma.getAchievement().getId(), ma -> ma));

        Map<AchievementType, Integer> progressMap = Map.of(type, newProgress);

        syncUserAchievements(member, allAchievements, userAchievementMap, progressMap);
    }
}