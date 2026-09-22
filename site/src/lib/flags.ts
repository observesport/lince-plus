// Feature flags, resolved at build time from PUBLIC_* environment variables so
// they can be flipped in CI without a code change. Defaults are the shipped
// state.
//
//   PUBLIC_FLAG_LEARN_RESOURCES=true npm run build
//
function flag(name: string, fallback: boolean): boolean {
  const raw = import.meta.env[`PUBLIC_FLAG_${name}`];
  if (raw === undefined || raw === '') return fallback;
  return raw === 'true' || raw === '1';
}

export const flags = {
  /** Online guide, workshop material, wiki and sample files. Off until the
   *  links are ready; the Learn section shows a collaboration call instead
   *  and the footer hides the same links. */
  learnResources: flag('LEARN_RESOURCES', false),
} as const;

export type FlagName = keyof typeof flags;
