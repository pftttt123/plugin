/**
 * Central motion vocabulary. Every duration, ease, and stagger on the site
 * comes from here so the whole page moves to one rhythm.
 */

/** GSAP ease names */
export const EASE = {
  out: 'expo.out',
  inOut: 'expo.inOut',
  soft: 'power4.out',
} as const

/** CSS-side equivalents (used by Tailwind tokens + Framer Motion) */
export const EASE_CSS = {
  out: [0.16, 1, 0.3, 1] as const,
  inOut: [0.87, 0, 0.13, 1] as const,
}

export const DUR = {
  fast: 0.35,
  base: 0.7,
  slow: 1.1,
  epic: 1.6,
} as const

export const STAGGER = {
  chars: 0.028,
  words: 0.06,
  items: 0.12,
} as const

/** Lerp factors for cursor-following elements (lower = heavier lag) */
export const LERP = {
  glow: 0.055,
  cursorRing: 0.16,
  trail: 0.09,
} as const

export const PRELOADER = {
  /** total time budget for the counter, seconds */
  count: 1.6,
  /** curtain panel wipe, seconds */
  wipe: 0.9,
  sessionKey: 'lumen-forge:visited',
} as const
