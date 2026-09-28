Feature: Trainer profile access control

  Background:
    Given a registered trainer

  Scenario: Owner can view their own profile
    Given I am logged in as the trainer
    When I request the trainer profile for the registered trainer
    Then the response status should be 200

  Scenario: A different trainer cannot view someone else's profile
    Given a second registered trainer
    And I am logged in as the second trainer
    When I request the trainer profile for the registered trainer
    Then the response status should be 403

  Scenario: A trainee cannot access a trainer profile endpoint
    Given a registered trainee
    And I am logged in as the trainee
    When I request the trainer profile for the registered trainer
    Then the response status should be 403

  Scenario: The admin can view any trainer's profile
    Given I am logged in as the admin user
    When I request the trainer profile for the registered trainer
    Then the response status should be 200

  Scenario: The admin gets a 404 for a trainer that does not exist
    Given I am logged in as the admin user
    When I request the trainer profile for "Ghost.Trainer"
    Then the response status should be 404

  Scenario: Owner can update their own profile
    Given I am logged in as the trainer
    When I update the trainer profile for the registered trainer with first name "Updated"
    Then the response status should be 200

  Scenario: A different trainer cannot update someone else's profile
    Given a second registered trainer
    And I am logged in as the second trainer
    When I update the trainer profile for the registered trainer with first name "Hacked"
    Then the response status should be 403

  Scenario: Owner can deactivate their own account
    Given I am logged in as the trainer
    When I set the active status of the registered trainer to false
    Then the response status should be 200

  Scenario: Setting the same active status twice is rejected
    Given I am logged in as the trainer
    When I set the active status of the registered trainer to true
    Then the response status should be 409
