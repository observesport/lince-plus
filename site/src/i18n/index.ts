// Locale plumbing. Astro's i18n routing (astro.config.mjs) serves English at
// the site root and the other languages under /es, /de and /ca.
import { href } from '../lib/site';
import { ui } from './ui';

export const locales = ['en', 'es', 'de', 'ca'] as const;
export type Locale = (typeof locales)[number];
export const defaultLocale: Locale = 'en';

export const localeNames: Record<Locale, string> = {
  en: 'English',
  es: 'Español',
  de: 'Deutsch',
  ca: 'Català',
};

const intlLocales: Record<Locale, string> = {
  en: 'en-GB',
  es: 'es-ES',
  de: 'de-DE',
  ca: 'ca-ES',
};

export function isLocale(value: unknown): value is Locale {
  return typeof value === 'string' && (locales as readonly string[]).includes(value);
}

/** Locale of the page being rendered (falls back to English). */
export function getLocale(astro: { currentLocale?: string | undefined }): Locale {
  return isLocale(astro.currentLocale) ? astro.currentLocale : defaultLocale;
}

/** Site path for a locale: `/changelog` -> `/lince-plus/es/changelog`. */
export function localePath(locale: Locale, path = '/'): string {
  const clean = path.startsWith('/') ? path : `/${path}`;
  return href(locale === defaultLocale ? clean : `/${locale}${clean === '/' ? '' : clean}`);
}

/** The same page in another locale, given the current pathname. */
export function switchLocalePath(locale: Locale, target: Locale, pathname: string): string {
  const base = href('/').replace(/\/$/, '');
  let rest = pathname.startsWith(base) ? pathname.slice(base.length) : pathname;
  if (locale !== defaultLocale) {
    rest = rest.replace(new RegExp(`^/${locale}(?=/|$)`), '');
  }
  if (rest === '') rest = '/';
  return localePath(target, rest);
}

type Dict = { [key: string]: string | Dict };

function lookup(dict: Dict, key: string): string | undefined {
  const value = key.split('.').reduce<string | Dict | undefined>((acc, part) => {
    if (acc && typeof acc === 'object') return acc[part];
    return undefined;
  }, dict);
  return typeof value === 'string' ? value : undefined;
}

/** Translator: `t('hero.title')`, with `{name}` placeholders. */
export function useTranslations(locale: Locale) {
  return function t(key: string, vars: Record<string, string | number> = {}): string {
    const raw = lookup(ui[locale] as Dict, key) ?? lookup(ui[defaultLocale] as Dict, key) ?? key;
    return raw.replace(/\{(\w+)\}/g, (_, name) => String(vars[name] ?? `{${name}}`));
  };
}

/** Render an ISO date (YYYY-MM-DD) in the locale, e.g. "18 Sept 2026". */
export function formatDate(iso: string, locale: Locale): string {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(Date.UTC(y, m - 1, d)).toLocaleDateString(intlLocales[locale], {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: 'UTC',
  });
}
