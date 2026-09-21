// Shared site constants. The version and release link come from the root
// lince-version.json, the same file the desktop app polls for update notices,
// so the website can never advertise a version the app does not know about.
import version from '../../../lince-version.json';

export const SITE_NAME = 'LINCE PLUS';
export const REPO_URL = 'https://github.com/observesport/lince-plus';
export const CONTACT_EMAIL = 'alberto.soto@gmail.com';
export const GA_MEASUREMENT_ID = 'G-PGTBMEJ65G';

export const VERSION: string = version.version;
export const RELEASE_URL: string = version.link;
export const RELEASE_TAG = `v${VERSION}`;

// Installer file names follow the Install4j naming used by the release
// pipeline (see .github/workflows/maven.yml, `update-docs` verification step).
const fileVersion = VERSION.replace(/\./g, '_');
const downloadBase = `${REPO_URL}/releases/download/${RELEASE_TAG}`;

export interface Installer {
  id: string;
  os: 'macOS' | 'Windows' | 'Linux';
  label: string;
  file: string;
  url: string;
}

export const INSTALLERS: Installer[] = [
  {
    id: 'mac-arm',
    os: 'macOS',
    label: 'Apple Silicon',
    file: `lince-plus_macos_${fileVersion}_mac-arm.dmg`,
  },
  {
    id: 'mac-x86',
    os: 'macOS',
    label: 'Intel',
    file: `lince-plus_macos_${fileVersion}_mac-x86.dmg`,
  },
  {
    id: 'windows',
    os: 'Windows',
    label: 'x64, Windows 10 or later',
    file: `lince-plus_windows-x64_${fileVersion}_Windows.exe`,
  },
  {
    id: 'linux',
    os: 'Linux',
    label: 'x64 installer script',
    file: `lince-plus_unix_${fileVersion}_linux-x64.sh`,
  },
].map((i) => ({ ...i, url: `${downloadBase}/${i.file}` }));

export const LEGACY_LINCE_URL = `${REPO_URL}/raw/master/docs/Lince_1.4.exe`;

/** Prefix an absolute site path with the configured base (`/lince-plus`). */
export function href(path: string): string {
  const base = import.meta.env.BASE_URL.replace(/\/$/, '');
  return path.startsWith('/') ? `${base}${path}` : `${base}/${path}`;
}

/** Render an ISO date (YYYY-MM-DD) as "18 Sep 2026". */
export function formatDate(iso: string): string {
  const [y, m, d] = iso.split('-').map(Number);
  const date = new Date(Date.UTC(y, m - 1, d));
  return date.toLocaleDateString('en-GB', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: 'UTC',
  });
}
