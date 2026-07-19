import { useLayoutEffect, useRef } from 'react'
import { useReducedMotion } from 'framer-motion'
import { gsap } from '../lib/gsap'
import { MATERIALS } from '../lib/data'

/**
 * Vertical scroll translated into a horizontal gallery. Panels pick up a
 * velocity-driven skew that settles as scrolling slows.
 */
export function Materials() {
  const sectionRef = useRef<HTMLElement>(null)
  const trackRef = useRef<HTMLDivElement>(null)
  const reduced = useReducedMotion()

  useLayoutEffect(() => {
    const section = sectionRef.current
    const track = trackRef.current
    if (!section || !track || reduced) return

    const ctx = gsap.context(() => {
      const distance = () => track.scrollWidth - window.innerWidth
      const proxy = { skew: 0 }

      gsap.to(track, {
        x: () => -distance(),
        ease: 'none',
        scrollTrigger: {
          trigger: section,
          start: 'top top',
          end: () => `+=${distance()}`,
          pin: true,
          scrub: 1,
          invalidateOnRefresh: true,
          onUpdate: (self) => {
            const velocity = self.getVelocity()
            proxy.skew = gsap.utils.clamp(-7, 7, velocity / -350)
          },
        },
      })

      const panels = gsap.utils.toArray<HTMLElement>('.material-panel', track)
      const setters = panels.map((p) => gsap.quickSetter(p, 'skewX', 'deg'))
      const tick = () => {
        // settle back toward zero when scrolling slows
        proxy.skew = gsap.utils.interpolate(proxy.skew, 0, 0.08)
        for (const set of setters) set(proxy.skew)
      }
      gsap.ticker.add(tick)

      // staggered entrance as the gallery pins
      gsap.from(panels, {
        autoAlpha: 0,
        x: 120,
        duration: 1.2,
        ease: 'expo.out',
        stagger: 0.08,
        scrollTrigger: { trigger: section, start: 'top 65%' },
      })

      return () => gsap.ticker.remove(tick)
    }, section)

    return () => ctx.revert()
  }, [reduced])

  const header = (
    <header className="gutter mb-12 flex flex-wrap items-end justify-between gap-6">
      <div>
        <p className="label-caps">Materials</p>
        <h2
          className="mt-4 font-display font-light text-bone"
          style={{ fontSize: 'clamp(2rem, 4.5vw, 4rem)' }}
        >
          What the light is <span className="italic text-amber">made of.</span>
        </h2>
      </div>
      {!reduced && <p className="label-caps hidden md:block">Scroll to traverse</p>}
    </header>
  )

  if (reduced) {
    return (
      <section id="materials" className="py-[var(--space-section)]" aria-label="Materials">
        {header}
        <div className="gutter grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {MATERIALS.map((m) => (
            <figure key={m.name} className="relative aspect-[3/4] overflow-hidden" role="img" aria-label={m.alt}>
              <div className="absolute inset-0" style={{ background: m.surface }} />
              <figcaption className="absolute bottom-0 left-0 p-5">
                <span className="font-display text-xl font-light text-bone">{m.name}</span>
                <span className="label-caps mt-1 block">{m.note}</span>
              </figcaption>
            </figure>
          ))}
        </div>
      </section>
    )
  }

  return (
    <section id="materials" ref={sectionRef} aria-label="Materials" data-cursor="Drag">
      <div className="flex h-screen flex-col justify-center overflow-hidden py-8">
        {header}
        <div ref={trackRef} className="flex w-max gap-[4vw] pl-[var(--space-gutter)] pr-[12vw] will-change-transform">
          {MATERIALS.map((m, i) => (
            <figure
              key={m.name}
              className="material-panel relative h-[60vh] w-[clamp(240px,26vw,400px)] shrink-0 overflow-hidden will-change-transform"
              role="img"
              aria-label={m.alt}
            >
              <div className="absolute inset-0" style={{ background: m.surface }} />
              <span className="text-outline absolute right-4 top-4 font-display text-4xl font-light">
                {String(i + 1).padStart(2, '0')}
              </span>
              <figcaption className="absolute bottom-0 left-0 p-6">
                <span className="font-display text-2xl font-light text-bone">{m.name}</span>
                <span className="label-caps mt-2 block">{m.note}</span>
              </figcaption>
            </figure>
          ))}
        </div>
      </div>
    </section>
  )
}
