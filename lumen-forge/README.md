# LUMEN & FORGE

A single-page marketing site for a fictional bespoke architectural lighting
atelier. Nocturnal, cinematic, gallery-like — built as an animation-craft
showcase piece.

**Stack:** Vite · React 18 · TypeScript · Tailwind CSS (+ CSS custom-property
design tokens) · GSAP 3 + ScrollTrigger · Lenis · Framer Motion

## Setup

```bash
npm install
npm run dev        # dev server with HMR
npm run build      # typecheck + production build
npm run preview    # serve the production build
```

No backend, no CMS — all content lives in `src/lib/data.ts`. Case-study
imagery uses Unsplash URLs with hand-tuned CSS gradient fallbacks, so the
site degrades gracefully offline; the materials gallery is fully
CSS-generated.

## Project structure

```
src/
  components/   Cursor, Preloader, Navbar, HoverText (letter-stagger swap)
  sections/     Hero, Manifesto, Works, Process, Materials, Testimonial, Contact
  hooks/        useLenis, useSplitText, useMagnetic, useLocalTime
  lib/          motion.ts (motion vocabulary), gsap.ts, data.ts (content)
  styles/       index.css — design tokens + base + animation plumbing
```

`src/lib/motion.ts` centralizes every duration, ease, stagger, and lerp
factor. Nothing animates with an ad-hoc ease; the whole site shares one
rhythmic vocabulary (`expo.out` / `cubic-bezier(0.16, 1, 0.3, 1)` for
entrances, `expo.inOut` for wipes).

## Animation architecture

Three libraries, three non-overlapping jobs — that's how they avoid
fighting:

1. **Lenis owns scroll transport.** A single instance (created in
   `useLenisRoot`, shared via `LenisContext`) smooths native window scroll.
   Its `raf` is driven by **GSAP's ticker** (`gsap.ticker.add`) rather than
   its own `requestAnimationFrame`, and every Lenis scroll event calls
   `ScrollTrigger.update()`. Because Lenis (in window mode) animates the
   *real* scroll position, ScrollTrigger reads correct values without a
   `scrollerProxy` — that pattern is only needed for transform-based fake
   scrolling. One clock, one scroll source of truth.

2. **GSAP + ScrollTrigger own scroll-linked and timeline animation.** Every
   section builds its animations inside `gsap.context()` scoped to the
   section root, and cleans up with `ctx.revert()` on unmount — no orphaned
   ScrollTriggers. Pinned sections (Process, Materials), scrubbed effects
   (parallax, word-by-word quote, marquee velocity), and the preloader/hero
   timelines all live here. Cursor-following elements (hero glow, cursor
   ring, CTA image trail) run lerp loops on the shared GSAP ticker with
   `gsap.quickSetter`, so there is exactly one rAF loop in the app.

3. **Framer Motion owns component lifecycle micro-interaction.** Mount/
   unmount transitions and the navbar's declarative hide/reveal state — the
   places where animation should follow React state rather than scroll. It
   also provides `useReducedMotion`, which every hook and section respects.

### Reduced motion

`prefers-reduced-motion` swaps the experience wholesale: Lenis is never
created (native scrolling), pinned sections re-render as static stacked
layouts, all masked/parallax reveals become simple opacity fades, the
marquee stops, and the custom cursor and magnetic effects disable
themselves. The preloader collapses to a brief fade (as it also does on
repeat visits via `sessionStorage`).

### Performance notes

- Only `transform` and `opacity` are animated on scroll; entrance
  clip-reveals are one-shot.
- Scroll/mouse handlers are passive and batched through rAF; reads and
  writes never interleave per-frame.
- Below-fold imagery is `loading="lazy" decoding="async"`.
- `will-change` is applied only to elements that continuously move.
- `ScrollTrigger.refresh()` re-runs once web fonts finish loading so
  trigger positions stay accurate.
