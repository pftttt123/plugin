import { useLayoutEffect, useRef } from 'react'
import { useReducedMotion } from 'framer-motion'
import { gsap } from '../lib/gsap'
import { CASE_STUDIES, type CaseStudy } from '../lib/data'
import { EASE, STAGGER } from '../lib/motion'
import { useMagnetic } from '../hooks/useMagnetic'
import { useSplitText } from '../hooks/useSplitText'
import { HoverText } from '../components/HoverText'

function WorkRow({ work, flip }: { work: CaseStudy; flip: boolean }) {
  const rowRef = useRef<HTMLElement>(null)
  const frameRef = useRef<HTMLDivElement>(null)
  const imageRef = useRef<HTMLDivElement>(null)
  const titleRef = useRef<HTMLHeadingElement>(null)
  const metaRef = useRef<HTMLDivElement>(null)
  const magneticRef = useMagnetic<HTMLDivElement>({ strength: 0.3, padding: 28 })
  const reduced = useReducedMotion()
  const chars = useSplitText(titleRef, 'chars')

  useLayoutEffect(() => {
    const row = rowRef.current
    if (!row || chars.length === 0) return

    const ctx = gsap.context(() => {
      if (reduced) {
        gsap.from([frameRef.current, metaRef.current], {
          autoAlpha: 0,
          duration: 0.8,
          ease: 'power2.out',
          scrollTrigger: { trigger: row, start: 'top 78%' },
        })
        return
      }

      // image moves slower than its clipping frame
      gsap.fromTo(
        imageRef.current,
        { yPercent: -10 },
        {
          yPercent: 10,
          ease: 'none',
          scrollTrigger: {
            trigger: frameRef.current,
            start: 'top bottom',
            end: 'bottom top',
            scrub: true,
          },
        },
      )

      // clip-reveal of the frame itself
      gsap.fromTo(
        frameRef.current,
        { clipPath: 'inset(8% 6% 8% 6%)', scale: 0.98 },
        {
          clipPath: 'inset(0% 0% 0% 0%)',
          scale: 1,
          duration: 1.4,
          ease: EASE.out,
          scrollTrigger: { trigger: frameRef.current, start: 'top 82%' },
        },
      )

      gsap.fromTo(
        chars,
        { yPercent: 120 },
        {
          yPercent: 0,
          duration: 1.1,
          ease: EASE.out,
          stagger: STAGGER.chars,
          scrollTrigger: { trigger: titleRef.current, start: 'top 85%' },
        },
      )

      gsap.from(metaRef.current, {
        autoAlpha: 0,
        y: 24,
        duration: 1,
        ease: EASE.out,
        scrollTrigger: { trigger: metaRef.current, start: 'top 88%' },
      })
    }, row)

    return () => ctx.revert()
  }, [chars, reduced])

  return (
    <article
      ref={rowRef}
      className="gutter grid grid-cols-12 items-center gap-y-10 py-[8vh] md:gap-x-8"
    >
      {/* image bleeds toward its outer edge */}
      <div
        className={`col-span-12 md:col-span-7 ${
          flip
            ? 'md:order-2 md:-mr-[var(--space-gutter)]'
            : 'md:-ml-[var(--space-gutter)]'
        }`}
      >
        <div
          ref={frameRef}
          data-cursor="View"
          className="relative aspect-[4/3] overflow-hidden md:aspect-[16/10]"
          style={{ background: work.fallback }}
        >
          <div ref={imageRef} className="absolute inset-0 scale-[1.22] will-change-transform">
            <img
              src={work.image}
              alt={work.alt}
              loading="lazy"
              decoding="async"
              className="h-full w-full object-cover opacity-80"
              onError={(e) => {
                // fall back to the CSS gradient behind the image
                e.currentTarget.style.display = 'none'
              }}
            />
          </div>
        </div>
      </div>

      <div
        ref={metaRef}
        className={`col-span-12 md:col-span-5 ${flip ? 'md:order-1' : ''}`}
      >
        <span className="text-outline font-display text-6xl font-light md:text-7xl">
          {work.index}
        </span>
        <h3
          ref={titleRef}
          className="mt-6 font-display font-light leading-[1.02] tracking-tight text-bone"
          style={{ fontSize: 'clamp(2.2rem, 4.5vw, 4.2rem)' }}
        >
          {work.title}
        </h3>
        <p className="label-caps mt-4">
          {work.location} — {work.category} — {work.year}
        </p>
        <p className="mt-6 max-w-sm text-sm leading-relaxed text-bone-dim">
          {work.description}
        </p>
        <div ref={magneticRef} className="mt-10 inline-block will-change-transform">
          <a
            href="#contact"
            className="label-caps !text-bone"
            aria-label={`View the ${work.title} case study`}
          >
            <span className="draw-line inline-block">
              <HoverText text="View Case" />
              <span aria-hidden className="ml-3 inline-block text-amber">
                →
              </span>
            </span>
          </a>
        </div>
      </div>
    </article>
  )
}

export function Works() {
  return (
    <section id="works" className="py-[var(--space-section)]" aria-label="Selected works">
      <header className="gutter mb-[6vh] grid grid-cols-12">
        <p className="label-caps col-span-12 md:col-span-3">Selected Works</p>
        <h2
          className="col-span-12 mt-6 font-display font-light leading-none text-bone md:col-span-9 md:mt-0"
          style={{ fontSize: 'clamp(2.4rem, 6vw, 6rem)' }}
        >
          Four rooms,
          <br />
          <span className="italic text-amber">lit from memory.</span>
        </h2>
      </header>
      {CASE_STUDIES.map((work, i) => (
        <WorkRow key={work.index} work={work} flip={i % 2 === 1} />
      ))}
    </section>
  )
}
