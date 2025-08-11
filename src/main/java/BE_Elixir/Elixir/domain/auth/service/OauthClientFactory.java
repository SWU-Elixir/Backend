package BE_Elixir.Elixir.domain.auth.service;

import BE_Elixir.Elixir.global.enums.LoginType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class OauthClientFactory {
    private final Map<LoginType, OauthClient> clientMap;

    public OauthClientFactory(List<OauthClient> clients) {
        this.clientMap = clients.stream()
                .collect(Collectors.toMap(OauthClient::getType, Function.identity()));
    }

    public OauthClient getClient(LoginType provider) {
        return Optional.ofNullable(clientMap.get(provider))
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_TYPE_MISMATCH));
    }
}
