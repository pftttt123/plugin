# Saul Kivowitz Photography — Booking Website

A mobile-first booking site built with **Next.js 15**, **Tailwind CSS v4**, and
**Framer Motion**. It exports as a fully static site (`out/` folder), so it can be
hosted for free on Vercel, Netlify, Cloudflare Pages, or GitHub Pages.

## Run it locally

```bash
npm install
npm run dev        # http://localhost:3000
npm run build      # static site output in out/
```

## Everything you'll want to edit is in ONE file

**`src/config/site.ts`** contains:

- Business name, tagline, hero headline, contact email, Instagram handle
- All 7 shoot types with descriptions and starting prices (placeholders — edit freely)
- Add-ons (Lightroom editing/retouching) and their prices
- Gallery photo paths and alt text

Colors live in **`src/app/globals.css`** under `@theme` — swap the hex values to
rebrand the whole site.

## Replacing the placeholder photos

The hero and gallery currently use soft gradient SVG placeholders. To use real photos:

1. Export web-sized JPEGs (~1600px wide for the hero, ~800px for gallery; aim for
   under ~400 KB each — WebP is even better).
2. Drop them into `public/images/`.
3. Update the paths in `src/config/site.ts` (`heroImage` and `gallery`), including
   descriptive alt text.

Gallery images are lazy-loaded automatically.

## How booking submissions reach you

The form posts to **Formspree** (free tier: 50 submissions/month):

1. Sign up at [formspree.io](https://formspree.io) with your email.
2. Create a new form — you'll get an ID like `mqkrzabc`.
3. In `src/config/site.ts`, replace `formspreeId: "YOUR_FORM_ID"` with your ID.
4. Every booking request now arrives in your inbox with all fields formatted.

**Until the ID is set**, the site runs in demo mode: submissions still show the
confirmation screen but are only saved in the visitor's browser (localStorage) and
logged to the console — the confirmation screen tells the visitor this. As a safety
net, every submission is *also* saved to localStorage even after Formspree is
connected.

### Other backend options, if you outgrow Formspree

| Option | Effort | Notes |
|---|---|---|
| **Formspree** (current) | ~2 min | Free tier, spam filtering, email notifications |
| Netlify Forms | ~5 min | Free if you host on Netlify anyway |
| Google Sheets via Apps Script | ~30 min | Free, submissions in a spreadsheet |
| Small API + database | hours | Only worth it when you want calendars, payments, or an admin dashboard |

## Project structure

```
src/
  config/site.ts        ← ALL editable content (names, prices, photos)
  app/
    globals.css         ← colors & fonts (@theme block)
    layout.tsx          ← fonts, header/footer shell
    template.tsx        ← page-transition animation
    page.tsx            ← landing page (hero, shoot types, gallery, CTA)
    book/page.tsx       ← booking page
  components/
    BookingForm.tsx     ← the form + confirmation screen
    Hero.tsx, ShootTypes.tsx, Gallery.tsx
    Header.tsx, Footer.tsx
    Reveal.tsx          ← scroll-reveal animation wrapper
    AnimatedCheck.tsx   ← confirmation checkmark animation
public/images/          ← hero + gallery photos
```

## Accessibility & performance notes

- Every input has a visible label; errors are announced (`role="alert"`) and shown
  next to their field.
- All touch targets are ≥ 44 px; the form works one-handed on a phone.
- Animations respect `prefers-reduced-motion`.
- Text colors meet WCAG AA contrast (4.5:1+) on all backgrounds.
- Fonts are self-hosted via `next/font`; images lazy-load.
