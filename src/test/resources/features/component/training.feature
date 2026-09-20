Feature: Training management

  Background:
    Given a registered trainer
    And a registered trainee

  Scenario: A trainer can add a training for their own trainee
    Given I am logged in as the trainer
    When I add a training for the registered trainer and trainee scheduled tomorrow
    Then the response status should be 201
    And the workload service is notified of an ADD event for the registered trainer

  Scenario: A trainee cannot add a training
    Given I am logged in as the trainee
    When I add a training for the registered trainer and trainee scheduled tomorrow
    Then the response status should be 403
    And the workload service is not notified

  Scenario: A trainer cannot add a training under another trainer's identity
    Given a second registered trainer
    And I am logged in as the second trainer
    When I add a training for the registered trainer and trainee scheduled tomorrow
    Then the response status should be 403
    And the workload service is not notified

  Scenario: The admin can add a training for any trainer and trainee
    Given I am logged in as the admin user
    When I add a training for the registered trainer and trainee scheduled tomorrow
    Then the response status should be 201

  Scenario: Adding a training for an unknown trainer is rejected
    Given I am logged in as the admin user
    When I add a training for trainer "Ghost.Trainer" and the registered trainee scheduled tomorrow
    Then the response status should be 404

  Scenario: Adding a training for an unknown trainee is rejected
    Given I am logged in as the trainer
    When I add a training for the registered trainer and trainee "Ghost.Trainee" scheduled tomorrow
    Then the response status should be 404

  Scenario: Adding a training with a blank name is rejected
    Given I am logged in as the trainer
    When I add a training with a blank name for the registered trainer and trainee
    Then the response status should be 400

  Scenario: Adding a training with a non-positive duration is rejected
    Given I am logged in as the trainer
    When I add a training with a non-positive duration for the registered trainer and trainee
    Then the response status should be 400

  Scenario: A trainer can cancel their own future training
    Given I am logged in as the trainer
    And a future training exists for the registered trainer and trainee
    When I cancel that training
    Then the response status should be 204
    And the workload service is notified of a DELETE event for the registered trainer

  Scenario: A trainer cannot cancel another trainer's training
    Given I am logged in as the trainer
    And a future training exists for the registered trainer and trainee
    And a second registered trainer
    And I am logged in as the second trainer
    When I cancel that training
    Then the response status should be 403

  Scenario: A training that has already occurred cannot be cancelled
    Given I am logged in as the trainer
    And a past training exists for the registered trainer and trainee
    When I cancel that training
    Then the response status should be 409

  Scenario: Cancelling an unknown training is rejected
    Given I am logged in as the trainer
    When I cancel training "999999"
    Then the response status should be 404

  Scenario: The admin can cancel any trainer's training
    Given I am logged in as the trainer
    And a future training exists for the registered trainer and trainee
    And I am logged in as the admin user
    When I cancel that training
    Then the response status should be 204
