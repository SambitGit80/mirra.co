package com.mirra.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Mirra auth / sign-up screen after deployment protection (e.g. /auth/sign-up). */
public class WelcomePage {

  private static final String XPATH_WELCOME_HEADING =
      "//*[self::h1 or self::h2][contains(normalize-space(.), 'Welcome to Mirra')]";
  private static final String XPATH_EMAIL =
      "//input[@type='email' or @name='email' or @autocomplete='email']"
          + " | //input[contains(translate(@placeholder,'EMAIL','email'),'email')]";
  /** Primary action before Google; excludes "Continue with Google". */
  private static final String XPATH_CONTINUE_PRIMARY =
      "//button[contains(normalize-space(.), 'Continue') and not(contains(., 'Google'))]";
  private static final String XPATH_GOOGLE =
      "//*[self::button or self::a][contains(., 'Continue with Google')]";

  @FindBy(xpath = XPATH_WELCOME_HEADING)
  private WebElement welcomeHeading;

  @FindBy(xpath = XPATH_EMAIL)
  private WebElement emailInput;

  @FindBy(xpath = XPATH_CONTINUE_PRIMARY)
  private WebElement continueButton;

  @FindBy(xpath = XPATH_GOOGLE)
  private WebElement continueWithGoogleButton;

  private final WebDriver driver;
  private final WebDriverWait wait;

  public WelcomePage(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(45));
    PageFactory.initElements(driver, this);
  }

  private void waitUntilWelcomeTextOrHeading(WebDriverWait w) {
    w.until(
        d -> {
          String src = d.getPageSource();
          if (src != null && src.contains("Welcome to Mirra")) {
            return true;
          }
          return d.findElements(By.xpath(XPATH_WELCOME_HEADING)).stream().anyMatch(WebElement::isDisplayed);
        });
  }

  /**
   * After Vercel visitor unlock: visitor field disappears, then Mirra welcome / sign-up controls
   * appear (SPA-safe: accepts heading in DOM or in page source while hydrating).
   */
  public boolean isDisplayedAfterVisitorUnlock() {
    WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(45));
    PageFactory.initElements(driver, this);
    try {
      new SignUpPage(driver).waitUntilVisitorPasswordInvisible(w);
      waitUntilWelcomeTextOrHeading(w);
      w.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_EMAIL)));
      w.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_CONTINUE_PRIMARY)));
      w.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_GOOGLE)));
      return true;
    } catch (org.openqa.selenium.TimeoutException e) {
      return false;
    }
  }

  /**
   * True when the welcome screen is visible (e.g. URL bypass with no visitor gate, or after
   * unlock).
   */
  public boolean isWelcomeScreenDisplayed() {
    PageFactory.initElements(driver, this);
    try {
      waitUntilWelcomeTextOrHeading(wait);
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_EMAIL)));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_CONTINUE_PRIMARY)));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_GOOGLE)));
      return true;
    } catch (org.openqa.selenium.TimeoutException e) {
      return false;
    }
  }

  public void enterEmail(String email) {
    PageFactory.initElements(driver, this);
    wait.until(ExpectedConditions.visibilityOf(emailInput));
    emailInput.clear();
    emailInput.sendKeys(email);
  }

  public void clickContinue() {
    PageFactory.initElements(driver, this);
    wait.until(ExpectedConditions.elementToBeClickable(continueButton)).click();
  }
}
