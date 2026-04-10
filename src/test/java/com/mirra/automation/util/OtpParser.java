package com.mirra.automation.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OtpParser {

  private static final Pattern SIX_DIGITS = Pattern.compile("\\b(\\d{6})\\b");
  private static final Pattern FOUR_TO_EIGHT = Pattern.compile("\\b(\\d{4,8})\\b");
  /** Reduces picking unrelated numbers (dates, IDs) in long emails. */
  private static final Pattern SIX_NEAR_VERIFICATION =
      Pattern.compile(
          "(?i)(code|otp|verification|one[- ]?time|sign[- ]?in)[^0-9]{0,80}(\\d{6})\\b");

  private OtpParser() {}

  /**
   * Prefer a 6-digit token near verification wording, then first 6-digit word, then 4–8 digits.
   */
  public static String bestOtpFromEmailBody(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    Matcher near = SIX_NEAR_VERIFICATION.matcher(text);
    if (near.find()) {
      return near.group(2);
    }
    String fromWord = firstOtpDigits(text);
    if (fromWord != null) {
      return fromWord;
    }
    return sixDigitsFromDigitRun(text);
  }

  /** When {@code \\b} boundaries fail (HTML/punctuation) or digits are split, take first 6-digit run. */
  public static String sixDigitsFromDigitRun(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    Matcher m = Pattern.compile("\\d{6}").matcher(text);
    if (m.find()) {
      return m.group();
    }
    String collapsed = text.replaceAll("\\D", "");
    return collapsed.length() >= 6 ? collapsed.substring(0, 6) : null;
  }

  /**
   * Produces exactly six digits for Mirra's OTP boxes. Avoids {@code last 6 of all digits}, which
   * breaks when the body has several numbers (dates, IDs) or the parser returned a long snippet.
   */
  public static String sixDigitMirraCode(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String compact = raw.replaceAll("\\s+", "").trim();
    if (compact.matches("\\d{6}")) {
      return compact;
    }
    List<String> sixes = new ArrayList<>();
    Matcher m = Pattern.compile("\\d{6}").matcher(raw);
    while (m.find()) {
      sixes.add(m.group());
    }
    if (sixes.isEmpty()) {
      return null;
    }
    if (sixes.size() == 1) {
      return sixes.get(0);
    }
    String lower = raw.toLowerCase(Locale.ROOT);
    int anchor = Integer.MAX_VALUE;
    for (String needle :
        new String[] {"verification", "verify", "code", "otp", "confirm", "mirra", "elnk", "email"}) {
      int i = lower.indexOf(needle);
      if (i >= 0 && i < anchor) {
        anchor = i;
      }
    }
    if (anchor < Integer.MAX_VALUE) {
      Matcher m2 = Pattern.compile("\\d{6}").matcher(raw);
      while (m2.find()) {
        if (m2.start() >= anchor && m2.end() <= anchor + 160) {
          return m2.group();
        }
      }
    }
    return sixes.get(sixes.size() - 1);
  }

  /** Prefers a 6-digit code, then any 4–8 digit token (common OTP shapes). */
  public static String firstOtpDigits(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    Matcher m = SIX_DIGITS.matcher(text);
    if (m.find()) {
      return m.group(1);
    }
    m = FOUR_TO_EIGHT.matcher(text);
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }
}
