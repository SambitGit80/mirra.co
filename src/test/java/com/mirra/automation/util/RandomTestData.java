package com.mirra.automation.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Lightweight random values for forms (no external faker dependency). */
public final class RandomTestData {

  public static final List<String> ELNK_USE_CASE_LABELS =
      List.of(
          "Personal Branding",
          "Business Networking",
          "Client Presentations",
          "Team Profiles",
          "Social Media Links",
          "Event or Portfolio Sharing",
          "Sales Outreach",
          "Recruiting / Hiring",
          "I'm Not Sure Yet");

  /** Labels from onboarding: "What best describes your role or profession?" */
  public static final List<String> PROFESSION_ROLE_LABELS =
      List.of(
          "Founder",
          "CEO",
          "Entrepreneur",
          "Sales",
          "Small Business Owner",
          "Influencer",
          "Real Estate Agent",
          "Marketing",
          "Freelancer",
          "Content Creator",
          "Designer",
          "Creative",
          "Developer",
          "Branding Professional",
          "Consultant",
          "Coach",
          "Recruiter",
          "HR",
          "Student",
          "Agency Owner",
          "Other");

  /** Labels from onboarding: "What is the size of your team?" */
  public static final List<String> TEAM_SIZE_LABELS =
      List.of(
          "Just me",
          "2-5 people",
          "6-10 people",
          "11-25 people",
          "26-50 people",
          "51-100 people",
          "100+ people");

  private RandomTestData() {}

  public static String randomFirstName() {
    return "Auto" + ThreadLocalRandom.current().nextInt(10000, 99999);
  }

  public static String randomLastName() {
    return "User" + ThreadLocalRandom.current().nextInt(10000, 99999);
  }

  /** US-style 10 digits (555 prefix for test ranges). */
  public static String randomUsPhoneDigits() {
    int n = ThreadLocalRandom.current().nextInt(0, 10_000_000);
    return "555" + String.format("%07d", n);
  }

  public static String randomElnkUseCaseLabel() {
    return ELNK_USE_CASE_LABELS.get(
        ThreadLocalRandom.current().nextInt(ELNK_USE_CASE_LABELS.size()));
  }

  public static String randomProfessionRoleLabel() {
    return PROFESSION_ROLE_LABELS.get(
        ThreadLocalRandom.current().nextInt(PROFESSION_ROLE_LABELS.size()));
  }

  public static String randomTeamSizeLabel() {
    return TEAM_SIZE_LABELS.get(ThreadLocalRandom.current().nextInt(TEAM_SIZE_LABELS.size()));
  }
}
