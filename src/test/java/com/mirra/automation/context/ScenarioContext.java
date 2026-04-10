package com.mirra.automation.context;

/** Holds per-scenario values for step glue (same thread as Cucumber scenario). */
public final class ScenarioContext {

  private static final ThreadLocal<String> LAST_OTP = new ThreadLocal<>();
  private static final ThreadLocal<String> SIGN_UP_EMAIL = new ThreadLocal<>();

  private ScenarioContext() {}

  public static void setLastOtp(String otp) {
    LAST_OTP.set(otp);
  }

  public static String getLastOtp() {
    return LAST_OTP.get();
  }

  public static void setSignUpEmail(String email) {
    SIGN_UP_EMAIL.set(email);
  }

  public static String getSignUpEmail() {
    return SIGN_UP_EMAIL.get();
  }

  public static void clear() {
    LAST_OTP.remove();
    SIGN_UP_EMAIL.remove();
  }
}
