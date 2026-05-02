import assert from "node:assert/strict";
import test from "node:test";

import {
  clearAuthState,
  getLocaleFromPath,
  getRoleHome,
  normalizeRole,
  readAuthState,
  writeAuthState,
} from "../src/lib/authSession.js";

class MemorySessionStorage {
  private readonly values = new Map<string, string>();

  getItem(key: string) {
    return this.values.get(key) ?? null;
  }

  setItem(key: string, value: string) {
    this.values.set(key, value);
  }

  removeItem(key: string) {
    this.values.delete(key);
  }
}

function installWindow() {
  const events: string[] = [];

  Object.defineProperty(globalThis, "window", {
    configurable: true,
    value: {
      sessionStorage: new MemorySessionStorage(),
      dispatchEvent(event: Event) {
        events.push(event.type);
        return true;
      },
    },
  });

  return events;
}

test("auth session helpers normalize roles and expose post-login homes", () => {
  assert.equal(normalizeRole("car_owner"), "CAR_OWNER");
  assert.equal(normalizeRole("SHOP_OWNER"), "SHOP_OWNER");
  assert.equal(normalizeRole("admin"), null);
  assert.equal(getLocaleFromPath("/et/login"), "et");
  assert.equal(getLocaleFromPath("/en/account"), "en");
  assert.equal(getRoleHome("et", "CAR_OWNER"), "/et/account");
  assert.equal(getRoleHome("en", "SHOP_OWNER"), "/en/business");
});

test("auth session writes normalized state and clears it cleanly", () => {
  const events = installWindow();

  writeAuthState("token-123", "car_owner");
  assert.deepEqual(readAuthState(), {
    token: "token-123",
    role: "CAR_OWNER",
  });
  assert.deepEqual(events, ["auth-changed"]);

  clearAuthState();
  assert.deepEqual(readAuthState(), {
    token: null,
    role: null,
  });
  assert.deepEqual(events, ["auth-changed", "auth-changed"]);
});
