export const LOCALES = ["en", "et"] as const;

export type Locale = (typeof LOCALES)[number];

export const LOCALE_LABEL: Record<Locale, string> = {
  en: "EN",
  et: "ET",
};
