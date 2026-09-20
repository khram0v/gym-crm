package io.github.khram0v.gymcrm.testsupport;

import lombok.Getter;
import lombok.Setter;

import java.net.http.HttpResponse;

@Getter
@Setter
public class ScenarioContext {

    private String traineeUsername;
    private String traineePassword;
    private String secondTraineeUsername;
    private String secondTraineePassword;

    private String trainerUsername;
    private String trainerPassword;
    private String secondTrainerUsername;
    private String secondTrainerPassword;

    private String accessToken;
    private String refreshToken;
    private String previousRefreshToken;
    private HttpResponse<String> lastResponse;
}
