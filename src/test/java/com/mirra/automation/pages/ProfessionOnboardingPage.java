package com.mirra.automation.pages;

import com.mirra.automation.util.MirraUrls;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** Second onboarding screen: "What best describes your role or profession?" (often still {@code /onboarding/profile}). */
public class ProfessionOnboardingPage {

  @FindBy(
      xpath =
          "//*[self::h1 or self::h2][contains(normalize-space(.), 'role or profession')"
              + " or contains(normalize-space(.), 'What best describes your role')]")
  private WebElement pageHeading;

  private final WebDriver driver;
  private final WebDriverWait wait;

  public ProfessionOnboardingPage(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(45));
    PageFactory.initElements(driver, this);
  }

  public boolean isRoleOrProfessionStepDisplayed() {
    PageFactory.initElements(driver, this);
    try {
      wait.until(d -> MirraUrls.isOnboardingProfilePath(d.getCurrentUrl()));
      wait.until(ExpectedConditions.visibilityOf(pageHeading));
      return true;
    } catch (org.openqa.selenium.TimeoutException e) {
      return false;
    }
  }

  /** Clicks the chip whose visible text matches (e.g. "Founder"). */
  public void selectRoleOption(String optionLabel) {
    PageFactory.initElements(driver, this);
    Objects.requireNonNull(optionLabel);
    wait.until(ExpectedConditions.visibilityOf(pageHeading));
    selectChipByExactVisibleText(optionLabel.replaceAll("\\s+", " ").trim(), "Role chip");
  }

  /**
   * Team size pills under "What is the size of your team?" (same URL as role step; Continue stays
   * disabled until one is selected).
   */
  public void selectTeamSizeOption(String optionLabel) {
    PageFactory.initElements(driver, this);
    Objects.requireNonNull(optionLabel);
    wait.until(ProfessionOnboardingPage::teamSizeSectionPresent);
    driver.findElements(teamSizeHeadingLocator()).stream()
        .filter(WebElement::isDisplayed)
        .findFirst()
        .ifPresent(
            h ->
                ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block: 'center'});", h));
    selectChipByExactVisibleText(optionLabel.replaceAll("\\s+", " ").trim(), "Team size chip");
  }

  private static boolean teamSizeSectionPresent(WebDriver d) {
    return d.findElements(teamSizeHeadingLocator()).stream().anyMatch(WebElement::isDisplayed);
  }

  private static By teamSizeHeadingLocator() {
    String lower =
        "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')";
    return By.xpath(
        "//*[self::h1 or self::h2][contains(" + lower + ", 'team') and contains(" + lower + ", 'size')]");
  }

  private void selectChipByExactVisibleText(String target, String kindForError) {
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
            .orElseThrow(
                () -> new org.openqa.selenium.NoSuchElementException(kindForError + ": " + target));
    wait.until(ExpectedConditions.elementToBeClickable(chip)).click();
  }

  /**
   * Clicks the primary Continue for this step. Earlier wizard steps can leave another Continue
   * in the DOM (often first in tree, disabled); we use the last suitable match. Some UIs keep the
   * native {@code disabled} flag until hydration; we fall back to a JS click when needed.
   */
  public void clickContinue() {
    PageFactory.initElements(driver, this);
    By continueCandidates = continueCtaLocator();
    WebElement btn =
        wait.until(
            d -> {
              WebElement picked = pickLastActivatableContinue(d, continueCandidates);
              if (picked != null) {
                return picked;
              }
              List<WebElement> visible =
                  d.findElements(continueCandidates).stream()
                      .filter(WebElement::isDisplayed)
                      .toList();
              if (visible.isEmpty()) {
                return null;
              }
              return visible.get(visible.size() - 1);
            });
    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
    try {
      wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
    } catch (TimeoutException e) {
      ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }
  }

  private static By continueCtaLocator() {
    String lower =
        "translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')";
    String lowerAria =
        "translate(normalize-space(@aria-label), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')";
    String lowerVal =
        "translate(normalize-space(@value), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')";
    String hasContinue = "contains(" + lower + ", 'continue')";
    String hasContinueAria = "contains(" + lowerAria + ", 'continue')";
    String hasContinueVal = "contains(" + lowerVal + ", 'continue')";
    String notGoogle = "not(contains(" + lower + ", 'google'))";
    String notGoogleAria = "not(contains(" + lowerAria + ", 'google'))";
    String notGoogleVal = "not(contains(" + lowerVal + ", 'google'))";
    return By.xpath(
        "//button["
            + hasContinue
            + " and "
            + notGoogle
            + "] | //*[@role='button']["
            + hasContinue
            + " and "
            + notGoogle
            + "] | //a["
            + hasContinue
            + " and "
            + notGoogle
            + "] | //button["
            + hasContinueAria
            + " and "
            + notGoogleAria
            + "] | //*[@role='button']["
            + hasContinueAria
            + " and "
            + notGoogleAria
            + "] | //input[@type='submit' or @type='button']["
            + hasContinueVal
            + " and "
            + notGoogleVal
            + "]");
  }

  /** Prefer last displayed control that is not clearly inert (enabled or aria-disabled≠true). */
  private static WebElement pickLastActivatableContinue(WebDriver d, By continueCandidates) {
    List<WebElement> candidates =
        d.findElements(continueCandidates).stream().filter(WebElement::isDisplayed).toList();
    List<WebElement> ok =
        candidates.stream().filter(ProfessionOnboardingPage::isLikelyActivatableContinue).toList();
    if (ok.isEmpty()) {
      return null;
    }
    return ok.get(ok.size() - 1);
  }

  private static boolean isLikelyActivatableContinue(WebElement e) {
    try {
      if (!e.isDisplayed()) {
        return false;
      }
      String aria = e.getDomAttribute("aria-disabled");
      if (aria != null && "true".equalsIgnoreCase(aria.trim())) {
        return false;
      }
      return e.isEnabled();
    } catch (Exception ex) {
      return false;
    }
  }

  private static Stream<WebElement> clickableChoiceElements(WebDriver d) {
    return Stream.concat(
            d.findElements(By.tagName("button")).stream(),
            d.findElements(By.cssSelector("[role='button']")).stream())
        .filter(WebElement::isDisplayed);
  }
}
