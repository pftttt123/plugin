import { useLayoutEffect, useRef } from 'react'
import { useReducedMotion } from 'framer-motion'
import { gsap, ScrollTrigger } from '../lib/gsap'

const PHRASE = 'Atelier — Bespoke — Luminaire — '

/**
 * Oversized outlined marquee whose speed and direction respond to scroll
 * velocity: scrolling fast whips it along, scrolling up reverses it, and it
 * always relaxes back to a slow drift.
 */
export function Manifesto() {
  const sectionRef = useRef<HTMLElement>(null)
  const trackRef = useRef<HTMLDivElement>(null)
  const reduced = useReducedMotion()

  useLayoutEffect(() => {
    const section = sectionRef.current
    const track = trackRef.current
    if (!section || !track || reduced) return

    const ctx = gsap.context(() => {
      const tween = gsap.to(track, {
        xPercent: -50,
        repeat: -1,
        duration: 28,
        ease: 'none',
      })

      const speed = { value: 1 }
      let direction = 1

      ScrollTrigger.create({
        trigger: section,
        start: 'top bottom',
        end: 'bottom top',
        onUpdate: (self) => {
          const velocity = self.getVelocity()
          if (Math.abs(velocity) < 10) return
          direction = velocity > 0 ? 1 : -1
          speed.value = gsap.utils.clamp(-6, 6, speed.value + velocity / 220)
        },
      })

      const tick = () => {
        speed.value = gsap.utils.interpolate(speed.value, direction, 0.05)
        tween.timeScale(speed.value)
      }
      gsap.ticker.add(tick)

      return () => {
        gsap.ticker.remove(tick)
      }
    }, section)

    return () => ctx.revert()
  }, [reduced])

  return (
    <section
      ref={sectionRef}
      className="overflow-hidden border-y border-bone/5 py-10 md:py-14"
      aria-label="Manifesto"
    >
      <p className="sr-only">Atelier. Bespoke. Luminaire.</p>
      <div
        ref={trackRef}
        aria-hidden
        className="flex w-max whitespace-nowrap will-change-transform"
      >
        {Array.from({ length: 4 }, (_, i) => (
          <span
            key={i}
            className="text-outline font-display font-light uppercase"
            style={{ fontSize: 'clamp(4rem, 11vw, 11rem)', lineHeight: 1.1 }}
          >
            {PHRASE}
          </span>
        ))}
      </div>
    </section>
  )
}
