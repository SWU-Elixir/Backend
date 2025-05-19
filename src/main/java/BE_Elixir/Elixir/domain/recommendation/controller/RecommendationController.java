package BE_Elixir.Elixir.domain.recommendation.controller;

import BE_Elixir.Elixir.domain.recommendation.controller.api.RecommendationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipe/recommend")
@RequiredArgsConstructor
public class RecommendationController implements RecommendationApi {
}
