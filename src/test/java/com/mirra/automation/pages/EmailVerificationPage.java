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
import java.util.List;
import java.util.stream.Collectors;

/** Mirra "Confirm Your Email" step with separate OTP digit inputs. */
public class EmailVerificationPage {

  @FindBy(
      xpath =
          "//*[contains(normalize-space(.), 'Confirm Your Email')][self::h1 or self::h2 or self::div]")
  private WebElement confirmHeading;

  @FindBy(xpath = "//button[contains(normalize-space(.), 'Verify Email Address')]")
  private WebElement verifyEmailButton;

  private final WebDriver driver;
  private final WebDriverWait wait;

  public EmailVerificationPage(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(45));
    PageFactory.initElements(driver, this);
  }

  private List<WebElement> resolveSixOtpInputs() {
    PageFactory.initElements(driver, this);
    wait.until(ExpectedConditions.visibilityOf(confirmHeading));

    List<WebElement> byMax =
        driver.findElements(By.cssSelector("input[maxlength='1']")).stream()
            .filter(WebElement::isDisplayed)
            .filter(e -> !"hidden".equals(e.getDomAttribute("type")))
            .collect(Collectors.toList());

    if (byMax.size() >= 6) {
      return byMax.subList(0, 6);
    }

    List<WebElement> numeric =
        driver.findElements(By.cssSelector("input[inputmode='numeric'], input[type='tel']")).stream()
            .filter(WebElement::isDisplayed)
            .limit(6)
            .collect(Collectors.toList());

    if (numeric.size() >= 6) {
      return numeric.subList(0, 6);
    }

    throw new org.openqa.selenium.TimeoutException(
        "Could not find 6 OTP inputs (tried maxlength=1 and inputmode=numeric). Found maxlength=1: "
            + byMax.size()
            + ", numeric: "
            + numeric.size());
  }

  public void enterSixDigitOtp(String otp) {
    if (otp == null || !otp.matches("\\d{6}")) {
      throw new IllegalArgumentException("Expected a 6-digit OTP for Mirra boxes, got: " + otp);
    }
    List<WebElement> boxes = resolveSixOtpInputs();
    for (int i = 0; i < 6; i++) {
      WebElement box = boxes.get(i);
      wait.until(ExpectedConditions.elementToBeClickable(box));
      box.click();
      box.clear();
      box.sendKeys(String.valueOf(otp.charAt(i)));
    }
  }

  public void clickVerifyEmailAddress() {
    PageFactory.initElements(driver, this);
    wait.until(ExpectedConditions.elementToBeClickable(verifyEmailButton)).click();
  }

  /** One-shot: type OTP and verify (no retry). Prefer {@link #enterSixDigitOtp} + retry flow in steps. */
  public void enterSixDigitOtpAndVerify(String otp) {
    enterSixDigitOtp(otp);
    clickVerifyEmailAddress();
  }

  public void clearSixDigitOtpFields() {
    PageFactory.initElements(driver, this);
    for (WebElement box : resolveSixOtpInputs()) {
      wait.until(ExpectedConditions.elementToBeClickable(box));
      box.click();
      box.clear();
    }
  }

  public void clickSendItAgainIfPresent() {
    PageFactory.initElements(driver, this);
    List<WebElement> links =
        driver.findElements(
            By.xpath(
                "//a[contains(., 'Send it Again') or contains(., 'send it again')]"
                    + " | //button[contains(., 'Send it Again')]"));
    for (WebElement el : links) {
      try {
        if (el.isDisplayed()) {
          el.click();
          return;
        }
      } catch (Exception ignored) {
        // try next
      }
    }
  }

  /** True if Mirra shows the invalid-code banner (sometimes flaky when the parsed OTP is wrong). */
  public boolean isIncorrectCodeErrorDisplayed() {
    PageFactory.initElements(driver, this);
    By locator =
        By.xpath(
            "//*[contains(., \"The code isn't correct\") or contains(., \"isn't correct\")"
                + " or contains(., \"code isn't\") or contains(., 'Please check your email')]"
                + "[not(self::script)]");
    List<WebElement> found = driver.findElements(locator);
    return found.stream().anyMatch(WebElement::isDisplayed);
  }

  /**
   * After clicking Verify, waits for either profile onboarding URL or an invalid-code message.
   *
   * @return true if URL reached onboarding profile; false if invalid-code UI is visible
   */
  public boolean waitForVerifyOutcome(Duration maxWait) {
    long deadline = System.currentTimeMillis() + maxWait.toMillis();
    while (System.currentTimeMillis() < deadline) {
      String url = driver.getCurrentUrl();
      if (MirraUrls.isOnboardingProfilePath(url)) {
        return true;
      }
      if (isIncorrectCodeErrorDisplayed()) {
        return false;
      }
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return MirraUrls.isOnboardingProfilePath(driver.getCurrentUrl());
      }
    }
    String url = driver.getCurrentUrl();
    if (MirraUrls.isEmailVerifyPath(url)) {
      return false;
    }
    return MirraUrls.isOnboardingProfilePath(url);
  }
}
