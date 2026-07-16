package io.github.khram0v.gymcrm.validation;

import io.github.khram0v.gymcrm.exception.ValidationException;
import io.github.khram0v.gymcrm.model.Trainee;
import io.github.khram0v.gymcrm.model.Trainer;
import io.github.khram0v.gymcrm.model.Training;
import io.github.khram0v.gymcrm.model.TrainingType;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityValidatorTest {

    private ValidatorFactory factory;
    private EntityValidator entityValidator;

    @BeforeEach
    void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        entityValidator = new EntityValidator(factory.getValidator());
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    private Trainee validTrainee() {
        Trainee t = new Trainee("John", "Doe", LocalDate.of(1995, Month.MAY, 20), "123 Main St");
        t.setUsername("John.Doe");
        t.setPassword("pass123456");
        t.setActive(true);
        return t;
    }

    @Test
    void validate_whenTraineeValid_passes() {
        assertThatCode(() -> entityValidator.validate(validTrainee()))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_whenFirstNameBlank_throws() {
        Trainee t = validTrainee();
        t.setFirstName("");

        assertThatThrownBy(() -> entityValidator.validate(t))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    void validate_whenUsernameBlank_throws() {
        Trainee t = validTrainee();
        t.setUsername("  ");

        assertThatThrownBy(() -> entityValidator.validate(t))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("username");
    }

    @Test
    void validate_whenPasswordNull_throws() {
        Trainee t = validTrainee();
        t.setPassword(null);

        assertThatThrownBy(() -> entityValidator.validate(t))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("password");
    }

    @Test
    void validate_whenMultipleViolations_messageContainsAll() {
        Trainee t = validTrainee();
        t.setFirstName("");
        t.setLastName("");

        assertThatThrownBy(() -> entityValidator.validate(t))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("firstName")
                .hasMessageContaining("lastName");
    }

    @Test
    void validate_whenTrainingValid_passes() {
        Training training = buildValidTraining();

        assertThatCode(() -> entityValidator.validate(training))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_whenTrainingDurationNotPositive_throws() {
        Training training = buildValidTraining();
        training.setTrainingDuration(0);

        assertThatThrownBy(() -> entityValidator.validate(training))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("trainingDuration");
    }

    @Test
    void validate_whenTrainingDateNull_throws() {
        Training training = buildValidTraining();
        training.setTrainingDate(null);

        assertThatThrownBy(() -> entityValidator.validate(training))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("trainingDate");
    }

    private Training buildValidTraining() {
        Trainee trainee = validTrainee();

        TrainingType type = new TrainingType("Fitness");

        Trainer trainer = new Trainer("Jane", "Smith", type);
        trainer.setUsername("Jane.Smith");
        trainer.setPassword("trainerPass");
        trainer.setActive(true);

        return new Training(trainee, trainer, "Morning Session", type,
                LocalDate.of(2026, Month.JANUARY, 15), 60);
    }
}
