package io.github.khram0v.gymcrm.repository.specification;

import io.github.khram0v.gymcrm.model.Training;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class TrainingSpecification {

    private TrainingSpecification() {
    }

    public static Specification<Training> hasTraineeUsername(String username) {
        return (root, query, cb) ->
                cb.equal(root.join("trainee").get("username"), username);
    }

    public static Specification<Training> hasTrainerUsername(String username) {
        return (root, query, cb) ->
                cb.equal(root.join("trainer").get("username"), username);
    }

    public static Specification<Training> dateFrom(LocalDate from) {
        if (from == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("trainingDate"), from);
    }

    public static Specification<Training> dateTo(LocalDate to) {
        if (to == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("trainingDate"), to);
    }

    public static Specification<Training> trainerFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) ->
                cb.equal(root.join("trainer").get("firstName"), firstName);
    }

    public static Specification<Training> trainerLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) ->
                cb.equal(root.join("trainer").get("lastName"), lastName);
    }

    public static Specification<Training> traineeFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) ->
                cb.equal(root.join("trainee").get("firstName"), firstName);
    }

    public static Specification<Training> traineeLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) ->
                cb.equal(root.join("trainee").get("lastName"), lastName);
    }

    public static Specification<Training> trainingTypeName(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            return Specification.unrestricted();
        }
        return (root, query, cb) ->
                cb.equal(root.join("trainingType").get("trainingTypeName"), typeName);
    }
}
