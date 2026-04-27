"use client";

export const AUTH_CHANGED_EVENT = "auth-changed";

export type UserRole = "CAR_OWNER" | "SHOP_OWNER";

export type AuthState = {
  token: string | null;
  role: UserRole | null;
};

export function normalizeRole(role: string | null): UserRole | null {
  if (role === "CAR_OWNER" || role === "car_owner") {
    return "CAR_OWNER";
  }

  if (role === "SHOP_OWNER" || role === "shop_owner") {
    return "SHOP_OWNER";
  }

  return null;
}

export function getLocaleFromPath(pathname: string): "en" | "et" {
  const locale = pathname.split("/")[1];
  return locale === "et" ? "et" : "en";
}

export function getRoleHome(locale: string, role: UserRole) {
  return role === "CAR_OWNER" ? `/${locale}/account` : `/${locale}/business`;
}

export function readAuthState(): AuthState {
  if (typeof window === "undefined") {
    return {
      token: null,
      role: null,
    };
  }

  return {
    token: window.sessionStorage.getItem("authToken"),
    role: normalizeRole(window.sessionStorage.getItem("authRole")),
  };
}

export function writeAuthState(token: string, role: string | null) {
  if (typeof window === "undefined") {
    return;
  }

  window.sessionStorage.setItem("authToken", token);

  const normalizedRole = normalizeRole(role);
  if (normalizedRole) {
    window.sessionStorage.setItem("authRole", normalizedRole);
  } else if (role) {
    window.sessionStorage.setItem("authRole", role);
  } else {
    window.sessionStorage.removeItem("authRole");
  }

  window.dispatchEvent(new Event(AUTH_CHANGED_EVENT));
}

export function clearAuthState() {
  if (typeof window === "undefined") {
    return;
  }

  window.sessionStorage.removeItem("authToken");
  window.sessionStorage.removeItem("authRole");
  window.dispatchEvent(new Event(AUTH_CHANGED_EVENT));
}
