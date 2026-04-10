package com.mirra.automation.hooks;

import com.mirra.automation.config.TestConfig;
import com.mirra.automation.context.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class Hooks {

  private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

  @Before
  public void setUp() {
    WebDriver driver = createDriver();
    DRIVER.set(driver);
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(90));
    driver.manage().window().maximize();
  }

  @After
  public void tearDown() {
    ScenarioContext.clear();
    WebDriver driver = DRIVER.get();
    if (driver != null) {
     // driver.quit();
      DRIVER.remove();
    }
  }

  public static WebDriver getDriver() {
    return DRIVER.get();
  }

  private static WebDriver createDriver() {
    String browser = TestConfig.browser().toLowerCase();
    boolean headless = TestConfig.headless();
    return switch (browser) {
      case "firefox" -> {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions o = new FirefoxOptions();
        if (headless) {
          o.addArguments("-headless");
        }
        yield new FirefoxDriver(o);
      }
      case "edge" -> {
        WebDriverManager.edgedriver().setup();
        EdgeOptions o = new EdgeOptions();
        if (headless) {
          o.addArguments("--headless=new");
        }
        yield new EdgeDriver(o);
      }
      default -> {
        WebDriverManager.chromedriver().setup();
        ChromeOptions o = new ChromeOptions();
        o.addArguments("--disable-dev-shm-usage");
        if (headless) {
          o.addArguments("--headless=new");
        }
        yield new ChromeDriver(o);
      }
    };
  }
}
