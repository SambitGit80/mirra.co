package com.mirra.automation.yopmail;

import com.mirra.automation.util.OtpParser;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Opens YOPmail in a new browser tab, waits for the latest message, reads {@code ifmail} body, and
 * extracts digits via {@link OtpParser}. Closes the tab and returns to the original window.
 */
public final class YopmailInboxReader {

  private YopmailInboxReader() {}

  public static String fetchLatestOtp(WebDriver driver, String yopmailLocalPart, Duration overallTimeout) {
    String original = driver.getWindowHandle();
    driver.switchTo().newWindow(WindowType.TAB);
    try {
      WebDriverWait wait = new WebDriverWait(driver, overallTimeout);
      driver.get("https://yopmail.com/en/");
      WebElement login = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));
      login.clear();
      login.sendKeys(yopmailLocalPart);
      wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#refreshbut button"))).click();

      wait.until(
          d ->
              d.getCurrentUrl().contains("/wm")
                  || !d.findElements(By.id("ifinbox")).isEmpty());

      waitForInboxMessage(driver, overallTimeout);
      refreshInboxAndSettle(driver);
      openFirstInboxRow(driver, wait);
      driver.switchTo().defaultContent();
      switchToMailContentFrame(driver, wait);
      try {
        waitForMailBodyWithPossibleCode(driver, wait);
      } catch (TimeoutException e) {
        // Template may omit six consecutive digits in HTML/text until late; still parse below.
      }
      return readOtpFromMailFrame(driver);
    } finally {
      driver.close();
      driver.switchTo().window(original);
    }
  }

  private static void waitForInboxMessage(WebDriver driver, Duration overallTimeout) {
    long deadline = System.currentTimeMillis() + overallTimeout.toMillis();
    while (System.currentTimeMillis() < deadline) {
      driver.switchTo().defaultContent();
      List<WebElement> inboxFrames = driver.findElements(By.id("ifinbox"));
      if (!inboxFrames.isEmpty()) {
        driver.switchTo().frame(inboxFrames.get(0));
        List<WebElement> rows =
            driver.findElements(
                By.cssSelector("div.m, button.lm, a.lm, tr.lm, div[class*='m'][onclick]"));
        for (WebElement row : rows) {
          try {
            if (row.isDisplayed() && !row.getText().isBlank()) {
              driver.switchTo().defaultContent();
              return;
            }
          } catch (org.openqa.selenium.StaleElementReferenceException ignored) {
            break;
          }
        }
        driver.switchTo().defaultContent();
      }
      List<WebElement> refresh = driver.findElements(By.id("refresh"));
      if (!refresh.isEmpty()) {
        try {
          refresh.get(0).click();
        } catch (Exception ignored) {
          // ignore stale / intercept
        }
      }
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new TimeoutException("Interrupted while waiting for YOPmail");
      }
    }
    throw new TimeoutException("No message appeared in YOPmail inbox within " + overallTimeout);
  }

  private static void refreshInboxAndSettle(WebDriver driver) {
    driver.switchTo().defaultContent();
    List<WebElement> refresh = driver.findElements(By.id("refresh"));
    if (!refresh.isEmpty()) {
      try {
        refresh.get(0).click();
      } catch (Exception ignored) {
        // stale / overlay
      }
    }
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static void waitForMailBodyWithPossibleCode(WebDriver driver, WebDriverWait wait) {
    Pattern sixDigits = Pattern.compile("\\d{6}");
    wait.until(
        d -> {
          try {
            String t = d.findElement(By.tagName("body")).getText();
            String html = d.findElement(By.tagName("body")).getDomAttribute("innerHTML");
            if (html == null) {
              html = "";
            }
            String flat = html.replaceAll("<[^>]+>", " ");
            String combined = (t != null ? t : "") + "\n" + flat;
            return sixDigits.matcher(combined).find();
          } catch (Exception e) {
            return false;
          }
        });
  }

  private static String readOtpFromMailFrame(WebDriver driver) {
    WebElement body = driver.findElement(By.tagName("body"));
    String bodyText = body.getText();
    String inner = body.getDomAttribute("innerHTML");
    if (inner == null) {
      inner = "";
    }
    String stripped = inner.replaceAll("<[^>]+>", " ");
    String combined = (bodyText != null ? bodyText : "") + "\n" + stripped;
    String otp = OtpParser.bestOtpFromEmailBody(combined);
    if (otp == null) {
      otp = OtpParser.sixDigitMirraCode(combined);
    }
    if (otp == null) {
      otp = OtpParser.sixDigitsFromDigitRun(combined);
    }
    return otp;
  }

  private static void openFirstInboxRow(WebDriver driver, WebDriverWait wait) {
    driver.switchTo().defaultContent();
    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("ifinbox")));
    List<WebElement> rows =
        driver.findElements(By.cssSelector("div.m, button.lm, a.lm, tr.lm"));
    boolean opened = false;
    for (WebElement row : rows) {
      if (row.isDisplayed() && !row.getText().isBlank() && looksLikeMirraOrVerifyMail(row)) {
        row.click();
        opened = true;
        break;
      }
    }
    if (!opened) {
      for (WebElement row : rows) {
        if (row.isDisplayed() && !row.getText().isBlank()) {
          row.click();
          opened = true;
          break;
        }
      }
    }
    driver.switchTo().defaultContent();
    if (!opened) {
      throw new TimeoutException("YOPmail inbox list had no clickable row");
    }
  }

  /** Prefer the row for the current sign-up (reused inboxes often have older OTPs on top). */
  private static boolean looksLikeMirraOrVerifyMail(WebElement row) {
    try {
      String t = row.getText();
      if (t == null || t.isBlank()) {
        return false;
      }
      String u = t.toLowerCase(java.util.Locale.ROOT);
      return u.contains("mirra")
          || u.contains("elnk")
          || u.contains("verify")
          || u.contains("verification")
          || u.contains("confirm")
          || u.contains("code")
          || u.contains("sign-up")
          || u.contains("signup");
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Message pane {@code ifmail} lives on the parent document, not inside {@code ifinbox}. Must be
   * called from {@code defaultContent}.
   */
  private static void switchToMailContentFrame(WebDriver driver, WebDriverWait wait) {
    wait.until(
        d -> {
          d.switchTo().defaultContent();
          for (By by : new By[] {By.id("ifmail"), By.name("ifmail"), By.cssSelector("iframe#ifmail")}) {
            List<WebElement> frames = d.findElements(by);
            if (!frames.isEmpty()) {
              try {
                d.switchTo().frame(frames.get(0));
                return true;
              } catch (Exception e) {
                d.switchTo().defaultContent();
              }
            }
          }
          return false;
        });
  }
}
