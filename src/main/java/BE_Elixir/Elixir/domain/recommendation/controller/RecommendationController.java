package BE_Elixir.Elixir.domain.recommendation.controller;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.recommendation.controller.api.RecommendationApi;
import BE_Elixir.Elixir.domain.recommendation.dto.RecommendationResponseDTO;
import BE_Elixir.Elixir.domain.recommendation.service.RecommendationService;
import BE_Elixir.Elixir.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recipe/recommend")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationApi {

    private final RecommendationService recommendationService;

    // 추천 레시피 조회
    @GetMapping
    public ResponseEntity<CommonResponse<?>> getRecommendations(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Member member = memberDetails.getMember();
        List<RecommendationResponseDTO> recommendations = recommendationService.getRecommendationsForUser(member);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.toString(),
                "추천 레시피 조회 성공", recommendations
        ));
    }

    // 추천 검색어 조회
    @GetMapping("/search/keyword")
    public ResponseEntity<CommonResponse<?>> getSearchKeyword(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        Member member = memberDetails.getMember();
        List<String> recommendationsKeywords = recommendationService.getRecommendedKeywords(member);

        return ResponseEntity.ok(CommonResponse.success(
                HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(),
                "추천 검색어 조회 성공", recommendationsKeywords
        ));
    }
}

