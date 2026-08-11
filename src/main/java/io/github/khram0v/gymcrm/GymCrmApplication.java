package io.github.khram0v.gymcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GymCrmApplication {

    static void main(String[] args) {
        SpringApplication.run(GymCrmApplication.class, args);
    }
}
