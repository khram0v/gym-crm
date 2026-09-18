package io.github.khram0v.gymcrm.testsupport;

import io.cucumber.spring.ScenarioScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class ScenarioContextConfiguration {

    @Bean
    @ScenarioScope
    public ScenarioContext scenarioContext() {
        return new ScenarioContext();
    }
}
