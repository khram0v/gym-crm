Feature: Cross-service workload synchronization

  Workload events published by gym-crm should be reliably consumed and reflected
  by trainer-workload-service over the real ActiveMQ broker.

  Background:
    Given a registered trainer and trainee in gym-crm

  Scenario: Adding a training is reflected in the trainer's workload
    When gym-crm adds a training of 60 minutes on "2024-06-10" for them
    Then gym-crm's response status should be 201
    And trainer-workload-service eventually reports 60 minutes for that trainer in 2024-6

  Scenario: Cancelling a future training reduces the trainer's workload
    Given a future training exists for them scheduled on "2024-07-15"
    And trainer-workload-service eventually reports 60 minutes for that trainer in 2024-7
    When gym-crm cancels that training
    Then gym-crm's response status should be 204
    And trainer-workload-service eventually reports 0 minutes for that trainer in 2024-7
