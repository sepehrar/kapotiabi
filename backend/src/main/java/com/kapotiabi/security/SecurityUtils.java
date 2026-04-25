package com.kapotiabi.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

  private SecurityUtils() {}

  public static Long getAuthUserId() {
    return Long.parseLong(
        (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }
}
