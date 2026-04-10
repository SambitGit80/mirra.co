package com.mirra.automation.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Default run excludes {@code @otp} (needs Mirra to send mail to YOPmail). Run OTP flow: {@code
 * mvn test -Dcucumber.filter.tags=@otp}
 */
@CucumberOptions(
    features = "classpath:features",
    glue = {"com.mirra.automation.steps", "com.mirra.automation.hooks"},
    plugin = {"pretty", "html:target/cucumber-report.html"},
    monochrome = true,
    tags = "not @wip and not @otp")
public class RunCucumberTest extends AbstractTestNGCucumberTests {

  @Override
  @DataProvider(parallel = false)
  public Object[][] scenarios() {
    return super.scenarios();
  }
}
