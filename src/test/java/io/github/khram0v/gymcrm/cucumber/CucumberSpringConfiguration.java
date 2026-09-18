package io.github.khram0v.gymcrm.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import io.github.khram0v.gymcrm.testsupport.PostgresTestContainerConfiguration;
import io.github.khram0v.gymcrm.testsupport.WorkloadClientTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({PostgresTestContainerConfiguration.class, WorkloadClientTestConfiguration.class})
public class CucumberSpringConfiguration {
}
