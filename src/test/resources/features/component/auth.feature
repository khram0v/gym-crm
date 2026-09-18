Feature: Authentication

  As a registered trainer, I want to log in and manage my session so that I can access protected
  resources securely.

  Background:
    Given a registered trainer

  Scenario: Successful login returns an access and refresh token
    When I log in with the correct password
    Then the response status should be 200
    And the response should contain a non-blank access token
    And the response should contain a non-blank refresh token

  Scenario: Login with a wrong password is rejected
    When I log in with password "wrong-password"
    Then the response status should be 401

  Scenario: Login with an unknown username is rejected
    When I log in as unknown user "Ghost.User" with password "whatever"
    Then the response status should be 401

  Scenario: Account is locked after three failed login attempts
    Given I have failed to log in 3 times
    When I log in with the correct password
    Then the response status should be 423

  Scenario: A valid access token grants access to the owner's own profile
    Given I log in with the correct password
    When I request my own trainer profile using the access token
    Then the response status should be 200

  Scenario: Refreshing rotates both tokens and invalidates the old refresh token
    Given I log in with the correct password
    When I refresh my session
    Then the response status should be 200
    And the response should contain a non-blank access token
    And the response should contain a non-blank refresh token
    When I try to refresh again using the previous refresh token
    Then the response status should be 401

  Scenario: Logging out immediately invalidates the access token
    Given I log in with the correct password
    When I log out
    Then the response status should be 204
    When I request my own trainer profile using the access token
    Then the response status should be 401

  Scenario: Logging out with a refresh token header also invalidates the refresh token
    Given I log in with the correct password
    When I log out and also invalidate my refresh token
    Then the response status should be 204
    When I try to refresh again using the previous refresh token
    Then the response status should be 401
