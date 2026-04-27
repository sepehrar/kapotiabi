"use client";

import { type ReactNode, useEffect, useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import {
  AUTH_CHANGED_EVENT,
  getLocaleFromPath,
  getRoleHome,
  readAuthState,
  type AuthState,
  type UserRole,
} from "@/lib/authSession";

type AuthRouteGuardProps = {
  children: ReactNode;
  requiredRole: UserRole;
};

export default function AuthRouteGuard({
  children,
  requiredRole,
}: AuthRouteGuardProps) {
  const pathname = usePathname();
  const router = useRouter();
  const locale = getLocaleFromPath(pathname);
  const [authState, setAuthState] = useState<AuthState>({
    token: null,
    role: null,
  });
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    const syncAuthState = () => {
      setAuthState(readAuthState());
      setIsHydrated(true);
    };

    syncAuthState();

    window.addEventListener(AUTH_CHANGED_EVENT, syncAuthState);
    window.addEventListener("storage", syncAuthState);

    return () => {
      window.removeEventListener(AUTH_CHANGED_EVENT, syncAuthState);
      window.removeEventListener("storage", syncAuthState);
    };
  }, []);

  useEffect(() => {
    if (!isHydrated) {
      return;
    }

    if (!authState.token || !authState.role) {
      router.replace(`/${locale}/login`);
      return;
    }

    if (authState.role !== requiredRole) {
      router.replace(getRoleHome(locale, authState.role));
    }
  }, [authState, isHydrated, locale, requiredRole, router]);

  if (
    !isHydrated ||
    !authState.token ||
    !authState.role ||
    authState.role !== requiredRole
  ) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-black text-sm text-white/50">
        Loading...
      </div>
    );
  }

  return children;
}
