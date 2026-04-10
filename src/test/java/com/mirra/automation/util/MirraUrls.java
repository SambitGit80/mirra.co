package com.mirra.automation.util;

import java.net.URI;
import java.net.URISyntaxException;

/** Avoid false positives when {@code contains("/onboarding/profile")} matches query params on /verify. */
public final class MirraUrls {

  private MirraUrls() {}

  public static boolean isOnboardingProfilePath(String fullUrl) {
    if (fullUrl == null || fullUrl.isBlank()) {
      return false;
    }
    try {
      String path = new URI(fullUrl).getPath();
      if (path == null) {
        return false;
      }
      return path.equals("/onboarding/profile") || path.startsWith("/onboarding/profile/");
    } catch (URISyntaxException e) {
      return false;
    }
  }

  public static boolean isEmailVerifyPath(String fullUrl) {
    if (fullUrl == null || fullUrl.isBlank()) {
      return false;
    }
    try {
      String path = new URI(fullUrl).getPath();
      return path != null && path.contains("/verify");
    } catch (URISyntaxException e) {
      return fullUrl.contains("/verify");
    }
  }
}
