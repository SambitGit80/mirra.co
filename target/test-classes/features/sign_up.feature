@web @sign-up
Feature: Mirra sign-up page

  As a tester
  I want to verify the sign-up experience
  So that releases stay stable

  Scenario: Visitor authentication page is displayed
    Given I open the Mirra sign-up page
    Then the browser should show a page Authentication Required

  Scenario: Unlock staging with visitor password
    Given I open the Mirra sign-up page
    Then the browser should show a page Authentication Required
    When I enter the visitor password from configuration
    And I click the Unlock button
    Then the visitor password prompt should be gone and the Welcome to Mirra sign-up screen should be displayed

  @otp
  Scenario: Mirra email Continue and OTP from YOPmail
    Given I open the Mirra sign-up page
    Then the browser should show a page Authentication Required
    When I enter the visitor password from configuration
    And I click the Unlock button
    Then the visitor password prompt should be gone and the Welcome to Mirra sign-up screen should be displayed
    When I enter email "karan@yopmail.com" and click Continue on Mirra
    And I fetch the OTP from YOPmail for "karan@yopmail.com"
    And I enter the fetched OTP into the Mirra email verification fields
    Then the Add Profile Details onboarding page should be displayed
    When I fill the profile form with random name and phone and pick a random use case for Elnk
    And I pick a random role or profession, team size, and continue
    And the OTP from YOPmail should be stored
