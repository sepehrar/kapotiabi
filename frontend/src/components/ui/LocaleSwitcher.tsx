"use client";

import { Popover, PopoverButton, PopoverPanel } from "@headlessui/react";
import { ChevronDownIcon } from "@heroicons/react/20/solid";
import Link from "next/link";
import { usePathname, useSearchParams } from "next/navigation";
import { useMemo } from "react";
import { LOCALES, LOCALE_LABEL, type Locale } from "@/lib/locale";

function buildHref(target: Locale, pathname: string, search: string) {
  const segs = pathname.split("/");

  if (LOCALES.includes(segs[1] as Locale)) {
    segs[1] = target;
  } else {
    segs.splice(1, 0, target);
  }

  const base = segs.join("/") || "/";
  return search ? `${base}?${search}` : base;
}

export default function LanguageSwitcher({ current }: { current?: Locale }) {
  const pathname = usePathname();
  const search = useSearchParams()?.toString() || "";

  const detected: Locale = useMemo(() => {
    const code = pathname.split("/")[1];
    return LOCALES.includes(code as Locale) ? (code as Locale) : "en";
  }, [pathname]);

  const active = current ?? detected;

  return (
    <Popover className="relative">
      <PopoverButton className="inline-flex items-center gap-x-1 rounded-full border border-gray-200 bg-white px-3 py-2 text-sm font-medium text-gray-700 shadow-sm transition hover:border-indigo-200 hover:text-indigo-600">
        <span>{LOCALE_LABEL[active]}</span>
        <ChevronDownIcon aria-hidden="true" className="size-4" />
      </PopoverButton>

      <PopoverPanel
        anchor="bottom"
        transition
        className="z-50 mt-2 w-40 rounded-2xl border border-gray-200 bg-white p-2 shadow-lg transition data-closed:translate-y-1 data-closed:opacity-0 data-enter:duration-200 data-leave:duration-150"
      >
        <div className="space-y-1">
          {LOCALES.map((loc) => {
            const href = buildHref(loc, pathname, search);
            const isActive = loc === active;

            return (
              <Link
                key={loc}
                href={href}
                prefetch={false}
                aria-current={isActive ? "page" : undefined}
                className={`block rounded-xl px-3 py-2 text-sm font-medium transition ${
                  isActive
                    ? "bg-indigo-50 text-indigo-600"
                    : "text-gray-700 hover:bg-gray-50 hover:text-indigo-600"
                }`}
              >
                {LOCALE_LABEL[loc]}
              </Link>
            );
          })}
        </div>
      </PopoverPanel>
    </Popover>
  );
}
