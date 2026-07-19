export interface CaseStudy {
  index: string
  title: string
  location: string
  category: string
  year: string
  description: string
  image: string
  /** CSS gradient shown behind (and instead of, if the network fails) the photo */
  fallback: string
  alt: string
}

export const CASE_STUDIES: CaseStudy[] = [
  {
    index: '01',
    title: 'Helios Court',
    location: 'Marrakech',
    category: 'Hotel',
    year: '2025',
    description:
      'A suspended halo of two hundred and forty brass petals above the courtyard pool, each angled to return the last hour of desert light after dark.',
    image:
      'https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?q=80&w=1800&auto=format&fit=crop',
    fallback:
      'radial-gradient(120% 90% at 70% 20%, rgba(232,163,61,0.28), transparent 55%), linear-gradient(160deg, #1a1410 0%, #0b0b0d 70%)',
    alt: 'Warm amber light washing over a dark hotel courtyard at night',
  },
  {
    index: '02',
    title: 'Vespertine Gallery',
    location: 'Oslo',
    category: 'Museum',
    year: '2024',
    description:
      'Forty metres of hand-blown glass channel, dimming in step with the fjord twilight so the collection is always seen at dusk.',
    image:
      'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?q=80&w=1800&auto=format&fit=crop',
    fallback:
      'radial-gradient(100% 80% at 30% 30%, rgba(120,140,170,0.22), transparent 60%), linear-gradient(200deg, #101318 0%, #0b0b0d 70%)',
    alt: 'Cool dusk light falling through a minimal gallery corridor',
  },
  {
    index: '03',
    title: 'The Ember Suite',
    location: 'Kyoto',
    category: 'Private Residence',
    year: '2024',
    description:
      'A single blackened-steel filament traced along the ridge beam — a hearth-line that breathes with the occupants, never brighter than candlelight.',
    image:
      'https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?q=80&w=1800&auto=format&fit=crop',
    fallback:
      'radial-gradient(90% 70% at 60% 70%, rgba(232,120,61,0.25), transparent 55%), linear-gradient(140deg, #17100c 0%, #0b0b0d 65%)',
    alt: 'Low ember-toned lighting across a dark timber interior',
  },
  {
    index: '04',
    title: 'Filament House',
    location: 'Marfa',
    category: 'Private Residence',
    year: '2023',
    description:
      'Alabaster monoliths lit from within, calibrated to the exact colour temperature of the high-desert horizon eleven minutes after sundown.',
    image:
      'https://images.unsplash.com/photo-1613490493576-7fde63acd811?q=80&w=1800&auto=format&fit=crop',
    fallback:
      'radial-gradient(110% 80% at 40% 40%, rgba(242,239,232,0.14), transparent 60%), linear-gradient(180deg, #121110 0%, #0b0b0d 70%)',
    alt: 'A glowing minimal house against a black desert sky',
  },
]

export interface ProcessStage {
  number: string
  title: string
  copy: string
  /** Path data — every stage shares the same command structure so GSAP can tween between them */
  path: string
}

export const PROCESS_STAGES: ProcessStage[] = [
  {
    number: '01',
    title: 'Listen',
    copy: 'Every commission begins in silence. We sit in the space at dusk, watch where the dark gathers, and record what the architecture is already trying to say.',
    path: 'M20,100 C120,40 220,160 320,100 C370,70 420,130 460,100 C500,80 550,120 580,100',
  },
  {
    number: '02',
    title: 'Draft in Light',
    copy: 'Full-scale mockups, never renders. Prototypes are hung, dimmed, and re-hung until the beam does at midnight what the sketch promised at noon.',
    path: 'M20,140 C100,140 140,40 220,40 C300,40 340,160 420,160 C480,160 530,60 580,60',
  },
  {
    number: '03',
    title: 'Forge & Install',
    copy: 'Brass is spun, glass is blown, steel is blackened — all in our Copenhagen workshop. Our own hands carry each piece to site and stay until the first nightfall.',
    path: 'M20,160 C120,150 200,120 300,100 C360,88 420,70 480,52 C520,40 555,34 580,30',
  },
]

export interface Material {
  name: string
  note: string
  /** layered CSS background standing in for a photograph */
  surface: string
  alt: string
}

export const MATERIALS: Material[] = [
  {
    name: 'Spun Brass',
    note: 'Warmth that deepens with touch',
    surface:
      'radial-gradient(140% 100% at 80% 0%, rgba(232,163,61,0.5), transparent 55%), radial-gradient(120% 120% at 20% 100%, rgba(120,70,20,0.6), transparent 60%), linear-gradient(155deg, #2a1c0d, #0b0b0d 75%)',
    alt: 'Close texture of warm spun brass',
  },
  {
    name: 'Hand-Blown Glass',
    note: 'Breath, held at 1,100 °C',
    surface:
      'radial-gradient(100% 80% at 50% 20%, rgba(200,220,235,0.28), transparent 55%), radial-gradient(80% 60% at 70% 80%, rgba(140,170,200,0.18), transparent 60%), linear-gradient(180deg, #14181d, #0b0b0d 80%)',
    alt: 'Light refracting through hand-blown glass',
  },
  {
    name: 'Blackened Steel',
    note: 'Oil-quenched, wax-sealed',
    surface:
      'radial-gradient(120% 90% at 30% 10%, rgba(242,239,232,0.1), transparent 50%), linear-gradient(200deg, #17181b 0%, #08080a 80%)',
    alt: 'Deep matte surface of blackened steel',
  },
  {
    name: 'Alabaster',
    note: 'Stone that remembers the sun',
    surface:
      'radial-gradient(120% 90% at 60% 30%, rgba(242,235,220,0.4), transparent 55%), radial-gradient(100% 80% at 30% 90%, rgba(232,163,61,0.16), transparent 60%), linear-gradient(170deg, #1d1a15, #0b0b0d 85%)',
    alt: 'Veined alabaster glowing from within',
  },
  {
    name: 'Smoked Bronze',
    note: 'Patinated over open flame',
    surface:
      'radial-gradient(130% 100% at 70% 90%, rgba(180,110,50,0.32), transparent 55%), linear-gradient(150deg, #1c130c, #0b0b0d 75%)',
    alt: 'Dark bronze with a smoked patina',
  },
  {
    name: 'Woven Fibre-Optic',
    note: 'A thread of captive light',
    surface:
      'radial-gradient(90% 70% at 50% 50%, rgba(232,163,61,0.2), transparent 55%), repeating-linear-gradient(115deg, rgba(242,239,232,0.05) 0 1px, transparent 1px 9px), linear-gradient(180deg, #101013, #0b0b0d 80%)',
    alt: 'Fine threads of light woven through dark fabric',
  },
]

export const TESTIMONIAL = {
  quote:
    'They do not install fixtures. They rehearse the sunset, night after night, until it agrees to stay indoors.',
  attribution: 'Amara Sørensen',
  role: 'Director, Vespertine Gallery — Oslo',
}

export const SOCIALS = [
  { label: 'Instagram', href: 'https://instagram.com' },
  { label: 'Are.na', href: 'https://are.na' },
  { label: 'LinkedIn', href: 'https://linkedin.com' },
] as const

export const NAV_LINKS = [
  { label: 'Works', href: '#works' },
  { label: 'Process', href: '#process' },
  { label: 'Materials', href: '#materials' },
  { label: 'Contact', href: '#contact' },
] as const

export const CTA_IMAGE =
  'https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?q=80&w=800&auto=format&fit=crop'
