package com.mirra.automation.pages;

import com.mirra.automation.config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SignUpPage {

  @FindBy(css = "input[placeholder='Visitor password']")
  private WebElement visitorPasswordInput;

  @FindBy(xpath = "//button[normalize-space()='Unlock']")
  private WebElement unlockButton;

  private final WebDriver driver;
  private final WebDriverWait wait;

  public SignUpPage(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    PageFactory.initElements(driver, this);
  }

  public void open() {
    driver.navigate().to(TestConfig.signUpUrl());
  }

  /**
   * Vercel deployment protection visitor page: title, password field, and Unlock control are
   * visible.
   */
  public boolean showsAuthenticationRequired() {
    try {
      wait.until(ExpectedConditions.titleContains("Authentication Required"));
      wait.until(ExpectedConditions.visibilityOf(visitorPasswordInput));
      wait.until(ExpectedConditions.visibilityOf(unlockButton));
      return true;
    } catch (org.openqa.selenium.TimeoutException e) {
      return false;
    }
  }

  public void enterVisitorPassword(String password) {
    wait.until(ExpectedConditions.visibilityOf(visitorPasswordInput));
    visitorPasswordInput.clear();
    visitorPasswordInput.sendKeys(password);
  }

  public void clickUnlock() {
    wait.until(ExpectedConditions.elementToBeClickable(unlockButton));
    unlockButton.click();
  }

  /** True after a successful unlock when the visitor password UI is gone. */
  public boolean visitorAuthenticationDismissed() {
    try {
      wait.until(ExpectedConditions.invisibilityOf(visitorPasswordInput));
      return true;
    } catch (org.openqa.selenium.TimeoutException e) {
      return false;
    }
  }

  /**
   * Waits for the visitor password field (PageFactory {@code FindBy} field) to disappear — e.g.
   * after unlock from another page object using the same {@code WebDriver}.
   */
  public void waitUntilVisitorPasswordInvisible(WebDriverWait w) {
    PageFactory.initElements(driver, this);
    w.until(ExpectedConditions.invisibilityOf(visitorPasswordInput));
  }
}
