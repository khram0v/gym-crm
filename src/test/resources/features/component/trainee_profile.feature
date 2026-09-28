Feature: Trainee profile access control

  Background:
    Given a registered trainee

  Scenario: Owner can view their own profile
    Given I am logged in as the trainee
    When I request the trainee profile for the registered trainee
    Then the response status should be 200

  Scenario: A different trainee cannot view someone else's profile
    Given a second registered trainee
    And I am logged in as the second trainee
    When I request the trainee profile for the registered trainee
    Then the response status should be 403

  Scenario: A trainer cannot access a trainee profile endpoint
    Given a registered trainer
    And I am logged in as the trainer
    When I request the trainee profile for the registered trainee
    Then the response status should be 403

  Scenario: The admin can view any trainee's profile
    Given I am logged in as the admin user
    When I request the trainee profile for the registered trainee
    Then the response status should be 200

  Scenario: The admin gets a 404 for a trainee that does not exist
    Given I am logged in as the admin user
    When I request the trainee profile for "Ghost.Trainee"
    Then the response status should be 404

  Scenario: Owner can update their own profile
    Given I am logged in as the trainee
    When I update the trainee profile for the registered trainee with first name "Updated"
    Then the response status should be 200

  Scenario: A different trainee cannot update someone else's profile
    Given a second registered trainee
    And I am logged in as the second trainee
    When I update the trainee profile for the registered trainee with first name "Hacked"
    Then the response status should be 403

  Scenario: Owner can deactivate their own account
    Given I am logged in as the trainee
    When I set the active status of the registered trainee to false
    Then the response status should be 200

  Scenario: Setting the same active status twice is rejected
    Given I am logged in as the trainee
    When I set the active status of the registered trainee to true
    Then the response status should be 409

  Scenario: Owner can delete their own profile
    Given I am logged in as the trainee
    When I delete the registered trainee profile
    Then the response status should be 204

  Scenario: A different trainee cannot delete someone else's profile
    Given a second registered trainee
    And I am logged in as the second trainee
    When I delete the registered trainee profile
    Then the response status should be 403
