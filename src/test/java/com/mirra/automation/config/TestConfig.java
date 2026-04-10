package com.mirra.automation.config;

import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {

  private static final Properties PROPS = new Properties();

  static {
    try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
      if (in != null) {
        PROPS.load(in);
      }
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private TestConfig() {}

  public static String baseUrl() {
    return PROPS.getProperty("base.url", "https://staging-app.mirra.co/auth/sign-up");
  }

  public static String browser() {
    return PROPS.getProperty("browser", "chrome");
  }

  public static boolean headless() {
    return Boolean.parseBoolean(PROPS.getProperty("headless", "false"));
  }

  public static String vercelBypass() {
    return PROPS.getProperty("vercel.protection.bypass", "").trim();
  }

  public static String signUpUrl() {
    String base = baseUrl();
    String token = vercelBypass();
    if (token.isEmpty()) {
      return base;
    }
    String sep = base.contains("?") ? "&" : "?";
    return base + sep + "x-vercel-set-bypass-cookie=true&x-vercel-protection-bypass=" + token;
  }

  /** Visitor password for Vercel deployment protection; env {@code VISITOR_PASSWORD} wins if set. */
  public static String visitorPassword() {
    String env = System.getenv("VISITOR_PASSWORD");
    if (env != null && !env.isBlank()) {
      return env.trim();
    }
    return PROPS.getProperty("visitor.password", "").trim();
  }
}
