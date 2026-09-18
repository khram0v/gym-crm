package io.github.khram0v.gymcrm.testsupport;

import lombok.Getter;
import lombok.Setter;

import java.net.http.HttpResponse;

@Getter
@Setter
public class ScenarioContext {

    private String username;
    private String password;
    private String accessToken;
    private String refreshToken;
    private String previousRefreshToken;
    private HttpResponse<String> lastResponse;
}
