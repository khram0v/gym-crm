package io.github.khram0v.gymcrm.cucumber;

import io.cucumber.java.Before;
import io.github.khram0v.gymcrm.client.TrainerWorkloadClient;
import lombok.RequiredArgsConstructor;

import static org.mockito.Mockito.reset;

@RequiredArgsConstructor
public class CucumberHooks {

    private final TrainerWorkloadClient trainerWorkloadClient;

    @Before
    public void resetMocks() {
        reset(trainerWorkloadClient);
    }
}
