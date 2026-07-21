/**
 * ─────────────────────────────────────────────────────────────
 *  EDIT ME — all site content lives in this one file.
 *  Change names, prices, descriptions, and photos here and the
 *  whole site updates. No other file needs touching.
 * ─────────────────────────────────────────────────────────────
 */

export const site = {
  name: "Saul Kivowitz",
  nameSuffix: "Photography",
  tagline: "Honest, light-filled photography for the moments that matter.",
  heroHeadline: "Every story deserves beautiful light.",
  heroSub:
    "Weddings, families, portraits, and celebrations — photographed with care, delivered with polish.",
  // Shown in the footer and on the confirmation screen.
  contactEmail: "therealsaulluca@gmail.com",
  instagram: "@saulkivowitz.photo", // set to "" to hide
  serviceArea: "Serving the greater metro area & beyond",

  /**
   * FORMSPREE — how bookings reach your inbox.
   * 1. Sign up free at https://formspree.io
   * 2. Create a form, copy its ID (looks like "mqkrzabc")
   * 3. Paste it below.
   * Until you do, the site runs in demo mode: submissions are
   * saved in the visitor's browser and logged to the console.
   */
  formspreeId: "YOUR_FORM_ID",
};

export type ShootType = {
  slug: string;
  name: string;
  description: string;
  /** Placeholder pricing — edit freely. Shown as "From $X". */
  startingPrice: number;
  /** Rough session length shown on the card. */
  duration: string;
};

export const shootTypes: ShootType[] = [
  {
    slug: "family",
    name: "Family",
    description:
      "Relaxed sessions at home, a park, or anywhere your family feels like themselves.",
    startingPrice: 250,
    duration: "1 hour",
  },
  {
    slug: "wedding",
    name: "Wedding",
    description:
      "Full-day coverage of your celebration, from getting ready to the last dance.",
    startingPrice: 2000,
    duration: "Full day",
  },
  {
    slug: "engagement",
    name: "Engagement",
    description:
      "A golden-hour session to celebrate your engagement — perfect for save-the-dates.",
    startingPrice: 300,
    duration: "1–2 hours",
  },
  {
    slug: "event",
    name: "Party / Event",
    description:
      "Birthdays, mitzvahs, corporate events — candid coverage of the whole occasion.",
    startingPrice: 400,
    duration: "2+ hours",
  },
  {
    slug: "portrait",
    name: "Portrait / Headshot",
    description:
      "Clean, professional portraits for LinkedIn, branding, acting, or just because.",
    startingPrice: 175,
    duration: "45 min",
  },
  {
    slug: "maternity",
    name: "Maternity / Newborn",
    description:
      "Gentle, unhurried sessions capturing the very beginning of a new chapter.",
    startingPrice: 275,
    duration: "1–2 hours",
  },
  {
    slug: "other",
    name: "Other",
    description:
      "Something different in mind? Tell me about it and we'll make it happen.",
    startingPrice: 0, // shows "Custom quote" instead of a price
    duration: "Flexible",
  },
];

export type AddOn = {
  id: string;
  name: string;
  description: string;
  /** Placeholder price. Shown as "+$X". */
  price: number;
};

export const addOns: AddOn[] = [
  {
    id: "editing",
    name: "Professional Lightroom editing & retouching",
    description:
      "Hand-edited color grading, skin retouching, and blemish removal on your final gallery.",
    price: 75,
  },
];

/**
 * GALLERY — replace these with your real photos.
 * Drop files into /public/images and update the paths below.
 * Keep alt text descriptive for accessibility.
 */
export const gallery = [
  { src: "/images/gallery-1.svg", alt: "Couple embracing at golden hour" },
  { src: "/images/gallery-2.svg", alt: "Family walking through a sunlit field" },
  { src: "/images/gallery-3.svg", alt: "Bride and groom's first dance" },
  { src: "/images/gallery-4.svg", alt: "Newborn sleeping in soft window light" },
  { src: "/images/gallery-5.svg", alt: "Professional headshot against neutral backdrop" },
  { src: "/images/gallery-6.svg", alt: "Guests laughing at an evening reception" },
];

export const heroImage = {
  src: "/images/hero.svg",
  alt: "Soft natural light falling across a quiet scene",
};
