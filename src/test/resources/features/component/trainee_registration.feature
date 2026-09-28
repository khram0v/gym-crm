Feature: Trainee registration

  As an anonymous visitor, I want to register as a trainee so that I can start booking trainings.

  Scenario: Successful registration returns generated credentials
    When a new trainee registers with first name "Alice" and last name "Norman"
    Then the response status should be 201
    And the response should contain username "Alice.Norman"
    And the response should contain a non-blank password

  Scenario: Registration without a first name is rejected
    When a new trainee registers with first name "" and last name "Norman"
    Then the response status should be 400
