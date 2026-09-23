package io.github.khram0v.gymcrm.integration.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;
import java.time.Duration;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    private static final Network NETWORK = Network.newNetwork();

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withNetwork(NETWORK);

    private static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7")
            .withNetwork(NETWORK)
            .withNetworkAliases("mongo");

    private static final GenericContainer<?> ACTIVE_MQ = new GenericContainer<>(
            DockerImageName.parse("apache/activemq-classic:6.1.6"))
            .withNetwork(NETWORK)
            .withNetworkAliases("activemq")
            .withExposedPorts(61616)
            .waitingFor(Wait.forListeningPort());

    private static final GenericContainer<?> TRAINER_WORKLOAD_SERVICE = new GenericContainer<>(
            new ImageFromDockerfile("trainer-workload-service-it", true)
                    .withDockerfile(Paths.get(trainerWorkloadServiceProjectDir(), "Dockerfile")))
            .withNetwork(NETWORK)
            .withExposedPorts(8082)
            .withEnv("MONGODB_URI", "mongodb://mongo:27017/trainer-workload")
            .withEnv("ACTIVEMQ_BROKER_URL", "tcp://activemq:61616")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8082).withStartupTimeout(Duration.ofMinutes(3)));

    static {
        POSTGRES.start();
        MONGO.start();
        ACTIVE_MQ.start();
        TRAINER_WORKLOAD_SERVICE.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.activemq.broker-url", () ->
                "tcp://" + ACTIVE_MQ.getHost() + ":" + ACTIVE_MQ.getMappedPort(61616));
    }

    public static int trainerWorkloadServicePort() {
        return TRAINER_WORKLOAD_SERVICE.getMappedPort(8082);
    }

    private static String trainerWorkloadServiceProjectDir() {
        return System.getProperty("trainerWorkloadServiceDir", "../trainer-workload-service");
    }
}
