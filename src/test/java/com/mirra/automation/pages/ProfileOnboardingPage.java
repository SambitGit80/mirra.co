package com.mirra.automation.pages;

import com.mirra.automation.util.MirraUrls;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;

/** Mirra onboarding at {@code /onboarding/profile} after email OTP verification. */
public class ProfileOnboardingPage {

  @FindBy(xpath = "//*[self::h1 or self::h2][contains(normalize-space(.), 'Add Profile Details')]")
  private WebElement pageHeading;

  @FindBy(xpath = "//*[contains(normalize-space(.), 'Tell us a bit about yourself')]")
  private WebElement subtitle;

  @FindBy(
      xpath =
          "//input[contains(@placeholder, 'First Name') or contains(@placeholder, 'first name')"
              + " or contains(@placeholder, 'Enter First Name')]")
  private WebElement firstNameInput;

  @FindBy(
      xpath =
          "//input[contains(@placeholder, 'Last Name') or contains(@placeholder, 'last name')"
              + " or contains(@placeholder, 'Enter Last Name')]")
  private WebElement lastNameInput;

  @FindBy(
      xpath =
          "//input[contains(@placeholder, 'Your Phone Number') or contains(@placeholder, 'Phone Number')"
              + " or (contains(@placeholder, 'phone') and not(contains(@placeholder,'First')))]")
  private WebElement phoneNumberInput;

  @FindBy(
      xpath =
          "//button[contains(normalize-space(.), 'Continue') and not(contains(., 'Google'))]")
  private WebElement continueButton;

  private final WebDriver driver;
  private final WebDriverWait wait;

  public ProfileOnboardingPage(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(45));
    PageFactory.initElements(driver, this);
  }

  /**
   * True when navigation reached profile onboarding: URL, heading, subtitle, and core fields are
   * visible.
   */
  public boolean isAddProfileDetailsDisplayed() {
    PageFactory.initElements(driver, this);
    try {
      wait.until(d -> MirraUrls.isOnboardingProfilePath(d.getCurrentUrl()));
      wait.until(ExpectedConditions.visibilityOf(pageHeading));
      wait.until(ExpectedConditions.visibilityOf(subtitle));
      wait.until(ExpectedConditions.visibilityOf(firstNameInput));
      wait.until(ExpectedConditions.visibilityOf(lastNameInput));
      wait.until(ExpectedConditions.visibilityOf(phoneNumberInput));
      return true;
    } catch (org.openqa.selenium.TimeoutException e) {
      return false;
    }
  }

  public void enterFirstName(String value) {
    PageFactory.initElements(driver, this);
    wait.until(ExpectedConditions.visibilityOf(firstNameInput));
    firstNameInput.clear();
    firstNameInput.sendKeys(value);
  }

  public void enterLastName(String value) {
    PageFactory.initElements(driver, this);
    wait.until(ExpectedConditions.visibilityOf(lastNameInput));
    lastNameInput.clear();
    lastNameInput.sendKeys(value);
  }

  public void enterPhoneNumber(String digits) {
    PageFactory.initElements(driver, this);
    wait.until(ExpectedConditions.visibilityOf(phoneNumberInput));
    phoneNumberInput.clear();
    phoneNumberInput.sendKeys(digits);
  }

  /**
   * Clicks the chip/button whose visible text matches the option (e.g. "Personal Branding").
   * Section title may say "Elnk" or "Mirra" in the app.
   */
  public void selectUseCaseOption(String optionLabel) {
    PageFactory.initElements(driver, this);
    Objects.requireNonNull(optionLabel);
    wait.until(ExpectedConditions.visibilityOf(pageHeading));
    String target = optionLabel.replaceAll("\\s+", " ").trim();
    wait.until(
        d ->
            clickableChoiceElements(d)
                .anyMatch(
                    b -> {
                      try {
                        return target.equals(b.getText().replaceAll("\\s+", " ").trim());
                      } catch (Exception e) {
                        return false;
                      }
                    }));
    WebElement chip =
        clickableChoiceElements(driver)
            .filter(b -> target.equals(b.getText().replaceAll("\\s+", " ").trim()))
            .findFirst()
            .orElseThrow(() -> new org.openqa.selenium.NoSuchElementException("Use case: " + target));
    wait.until(ExpectedConditions.elementToBeClickable(chip)).click();
  }

  public void clickContinue() {
    PageFactory.initElements(driver, this);
    wait.until(ExpectedConditions.elementToBeClickable(continueButton)).click();
  }

  private static Stream<WebElement> clickableChoiceElements(WebDriver d) {
    return Stream.concat(
            d.findElements(By.tagName("button")).stream(),
            d.findElements(By.cssSelector("[role='button']")).stream())
        .filter(WebElement::isDisplayed);
  }
}
