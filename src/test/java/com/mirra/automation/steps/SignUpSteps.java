package com.mirra.automation.steps;

import com.mirra.automation.config.TestConfig;
import com.mirra.automation.context.ScenarioContext;
import com.mirra.automation.hooks.Hooks;
import com.mirra.automation.pages.EmailVerificationPage;
import com.mirra.automation.pages.ProfessionOnboardingPage;
import com.mirra.automation.pages.ProfileOnboardingPage;
import com.mirra.automation.pages.SignUpPage;
import com.mirra.automation.pages.WelcomePage;
import com.mirra.automation.util.OtpParser;
import com.mirra.automation.util.RandomTestData;
import com.mirra.automation.yopmail.YopmailInboxReader;
import io.cucumber.java.en.Given;
import org.openqa.selenium.WebDriver;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.time.Duration;

public class SignUpSteps {

  private SignUpPage signUpPage() {
    return new SignUpPage(Hooks.getDriver());
  }

  private WelcomePage welcomePage() {
    return new WelcomePage(Hooks.getDriver());
  }

  private ProfileOnboardingPage profileOnboardingPage() {
    return new ProfileOnboardingPage(Hooks.getDriver());
  }

  private ProfessionOnboardingPage professionOnboardingPage() {
    return new ProfessionOnboardingPage(Hooks.getDriver());
  }

  @Given("I open the Mirra sign-up page")
  public void i_open_the_mirra_sign_up_page() {
    signUpPage().open();
  }

  @Then("the browser should show a page Authentication Required")
  public void the_browser_should_show_a_page_authentication_required() {
    Assert.assertTrue(
        signUpPage().showsAuthenticationRequired(),
        "Expected Vercel visitor page: title 'Authentication Required', "
            + "input placeholder 'Visitor password', and Unlock button visible.");
  }

  @When("I enter the visitor password from configuration")
  public void i_enter_the_visitor_password_from_configuration() {
    String password = TestConfig.visitorPassword();
    Assert.assertFalse(
        password.isEmpty(),
        "Set visitor.password in config.properties or VISITOR_PASSWORD in the environment.");
    signUpPage().enterVisitorPassword(password);
  }

  @When("I click the Unlock button")
  public void i_click_the_unlock_button() {
    signUpPage().clickUnlock();
  }

  @Then("the visitor password prompt should be gone and the Welcome to Mirra sign-up screen should be displayed")
  public void visitor_prompt_gone_and_welcome_displayed() {
    Assert.assertTrue(
        welcomePage().isDisplayedAfterVisitorUnlock(),
        "After unlock: visitor password field should disappear, then Welcome to Mirra text, email field, "
            + "Continue (not Google), and Continue with Google should be visible.");
  }

  /** Use when the app loads sign-up without the visitor gate (e.g. protection bypass URL). */
  @Then("the Welcome to Mirra sign-up screen should be displayed")
  public void the_welcome_to_mirra_sign_up_screen_should_be_displayed() {
    Assert.assertTrue(
        welcomePage().isWelcomeScreenDisplayed(),
        "Expected Welcome to Mirra, email field, Continue, and Continue with Google to be visible.");
  }

  @When("I enter email {string} and click Continue on Mirra")
  public void i_enter_email_and_click_continue_on_mirra(String email) {
    ScenarioContext.setSignUpEmail(email);
    welcomePage().enterEmail(email);
    welcomePage().clickContinue();
  }

  @When("I fetch the OTP from YOPmail for {string}")
  public void i_fetch_the_otp_from_yopmail_for(String email) {
    String local = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
    String otp =
        YopmailInboxReader.fetchLatestOtp(
            Hooks.getDriver(), local, Duration.ofSeconds(120));
    Assert.assertNotNull(otp, "No OTP digits found in YOPmail message body for " + email);
    ScenarioContext.setLastOtp(otp);
  }

  @When("I enter the fetched OTP into the Mirra email verification fields")
  public void i_enter_the_fetched_otp_into_the_mirra_email_verification_fields() {
    String email = ScenarioContext.getSignUpEmail();
    Assert.assertNotNull(
        email, "Sign-up email was not stored; enter email on Mirra before OTP (needed for retries).");

    WebDriver driver = Hooks.getDriver();
    EmailVerificationPage ev = new EmailVerificationPage(driver);
    final int maxAttempts = 5;

    for (int attempt = 0; attempt < maxAttempts; attempt++) {
      if (attempt > 0) {
        ev.clearSixDigitOtpFields();
        ev.clickSendItAgainIfPresent();
        try {
          Thread.sleep(12000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        String local = email.substring(0, email.indexOf('@'));
        String otp = null;
        for (int refetch = 0; refetch < 4; refetch++) {
          if (refetch > 0) {
            try {
              Thread.sleep(5000);
            } catch (InterruptedException ie) {
              Thread.currentThread().interrupt();
            }
          }
          otp = YopmailInboxReader.fetchLatestOtp(driver, local, Duration.ofSeconds(120));
          if (otp != null) {
            break;
          }
        }
        Assert.assertNotNull(
            otp,
            "YOPmail refetch returned no OTP after retries (attempt " + (attempt + 1) + ")");
        ScenarioContext.setLastOtp(otp);
      }

      String raw = ScenarioContext.getLastOtp();
      Assert.assertNotNull(raw, "Fetch the OTP from YOPmail first.");
      String sixDigits = OtpParser.sixDigitMirraCode(raw);
      Assert.assertNotNull(
          sixDigits, "Need a 6-digit Mirra OTP; raw from mail/parser: " + raw);

      ev = new EmailVerificationPage(driver);
      ev.enterSixDigitOtp(sixDigits);
      ev.clickVerifyEmailAddress();

      if (ev.waitForVerifyOutcome(Duration.ofSeconds(50))) {
        return;
      }

      if (attempt == maxAttempts - 1) {
        Assert.fail(
            "Email verification did not reach /onboarding/profile after "
                + maxAttempts
                + " attempts (wrong/stale OTP from YOPmail or slow navigation). URL="
                + driver.getCurrentUrl());
      }
    }
  }

  @Then("the OTP from YOPmail should be stored")
  public void the_otp_from_yopmail_should_be_stored() {
    String otp = ScenarioContext.getLastOtp();
    Assert.assertNotNull(otp, "OTP was not stored; fetch step may have failed.");
    Assert.assertTrue(otp.matches("\\d{4,8}"), "OTP should be 4–8 digits: " + otp);
  }

  @Then("the Add Profile Details onboarding page should be displayed")
  public void the_add_profile_details_onboarding_page_should_be_displayed() {
    Assert.assertTrue(
        profileOnboardingPage().isAddProfileDetailsDisplayed(),
        "Expected URL to contain /onboarding/profile with Add Profile Details, subtitle, "
            + "and first/last name and phone fields visible.");
  }

  @When("I fill the profile form with random name and phone and pick a random use case for Elnk")
  public void i_fill_the_profile_form_with_random_name_and_phone_and_pick_a_random_use_case_for_elnk() {
    ProfileOnboardingPage p = profileOnboardingPage();
    p.enterFirstName(RandomTestData.randomFirstName());
    p.enterLastName(RandomTestData.randomLastName());
    p.enterPhoneNumber(RandomTestData.randomUsPhoneDigits());
    p.selectUseCaseOption(RandomTestData.randomElnkUseCaseLabel());
    p.clickContinue();
  }

  @When("I pick a random role or profession, team size, and continue")
  public void i_pick_a_random_role_or_profession_team_size_and_continue() {
    ProfessionOnboardingPage p = professionOnboardingPage();
    Assert.assertTrue(
        p.isRoleOrProfessionStepDisplayed(),
        "Expected role/profession step (heading about role or profession) on onboarding profile URL.");
    p.selectRoleOption(RandomTestData.randomProfessionRoleLabel());
    p.selectTeamSizeOption(RandomTestData.randomTeamSizeLabel());
    p.clickContinue();
  }
}
