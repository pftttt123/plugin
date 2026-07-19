import { useEffect, useRef } from 'react'
import { useReducedMotion } from 'framer-motion'
import { gsap } from '../lib/gsap'
import { EASE, LERP, STAGGER } from '../lib/motion'
import { useSplitText } from '../hooks/useSplitText'

interface HeroProps {
  ready: boolean
}

/**
 * Full-viewport hero: "LIGHT, SCULPTED." rises char-by-char from behind
 * clip masks, over a cursor-following radial glow with heavy lerp.
 */
export function Hero({ ready }: HeroProps) {
  const sectionRef = useRef<HTMLElement>(null)
  const headlineRef = useRef<HTMLHeadingElement>(null)
  const glowRef = useRef<HTMLDivElement>(null)
  const subRef = useRef<HTMLParagraphElement>(null)
  const cueRef = useRef<HTMLDivElement>(null)
  const reduced = useReducedMotion()
  const chars = useSplitText(headlineRef, 'chars')

  // Cursor-following glow: spring-damped, never 1:1, with a slow idle drift.
  useEffect(() => {
    const glow = glowRef.current
    const section = sectionRef.current
    if (!glow || !section || reduced) return

    const setX = gsap.quickSetter(glow, 'x', 'px')
    const setY = gsap.quickSetter(glow, 'y', 'px')
    const target = { x: window.innerWidth * 0.6, y: window.innerHeight * 0.4 }
    const eased = { ...target }

    const onMove = (e: MouseEvent) => {
      target.x = e.clientX
      target.y = e.clientY
    }

    const tick = (time: number) => {
      eased.x += (target.x - eased.x) * LERP.glow
      eased.y += (target.y - eased.y) * LERP.glow
      // idle drift keeps it alive when the cursor rests
      const driftX = Math.sin(time * 0.35) * 30
      const driftY = Math.cos(time * 0.27) * 24
      setX(eased.x - window.innerWidth / 2 + driftX)
      setY(eased.y - window.innerHeight / 2 + driftY)
    }

    window.addEventListener('mousemove', onMove, { passive: true })
    gsap.ticker.add(tick)
    return () => {
      window.removeEventListener('mousemove', onMove)
      gsap.ticker.remove(tick)
    }
  }, [reduced])

  // Choreographed entrance: glow → headline chars → sub copy → scroll cue.
  useEffect(() => {
    const section = sectionRef.current
    if (!section || chars.length === 0) return

    const ctx = gsap.context(() => {
      if (reduced) {
        gsap.set([glowRef.current, subRef.current, cueRef.current], { autoAlpha: 0 })
        gsap.set(chars, { autoAlpha: 0 })
        if (ready) {
          gsap.to([glowRef.current, chars, subRef.current, cueRef.current], {
            autoAlpha: 1,
            duration: 0.8,
            ease: 'power2.out',
            stagger: 0.02,
          })
        }
        return
      }

      gsap.set(chars, { yPercent: 120 })
      gsap.set(glowRef.current, { autoAlpha: 0, scale: 0.85 })
      gsap.set(subRef.current, { autoAlpha: 0, y: 18 })
      gsap.set(cueRef.current, { autoAlpha: 0 })

      if (!ready) return

      const tl = gsap.timeline()
      tl.to(glowRef.current, { autoAlpha: 1, scale: 1, duration: 1.6, ease: EASE.soft })
        .to(
          chars,
          { yPercent: 0, duration: 1.2, ease: EASE.out, stagger: STAGGER.chars },
          0.35,
        )
        .to(subRef.current, { autoAlpha: 1, y: 0, duration: 0.9, ease: EASE.out }, '-=0.7')
        .to(cueRef.current, { autoAlpha: 1, duration: 0.8, ease: 'power2.out' }, '-=0.4')
    }, section)

    return () => ctx.revert()
  }, [chars, ready, reduced])

  return (
    <section
      ref={sectionRef}
      className="relative flex min-h-screen flex-col justify-center overflow-hidden"
      aria-label="Introduction"
    >
      {/* drifting radial glow */}
      <div
        ref={glowRef}
        aria-hidden
        className="pointer-events-none absolute left-1/2 top-1/2 h-[85vmin] w-[85vmin] -translate-x-1/2 -translate-y-1/2 rounded-full will-change-transform"
        style={{
          background:
            'radial-gradient(closest-side, rgba(232,163,61,0.16), rgba(232,163,61,0.05) 45%, transparent 70%)',
          filter: 'blur(10px)',
        }}
      />

      <div className="gutter relative grid grid-cols-12 items-end gap-4">
        <div className="col-span-12 lg:col-span-11">
          <p className="label-caps mb-8">Architectural Lighting Atelier — Est. 2011</p>
          <h1
            ref={headlineRef}
            className="font-display font-light leading-[0.95] tracking-[-0.02em] text-bone"
            style={{ fontSize: 'clamp(3.2rem, 12vw, 13.5rem)' }}
          >
            Light, Sculpted.
          </h1>
        </div>
        <p
          ref={subRef}
          className="col-span-11 mt-10 max-w-md text-sm leading-relaxed text-bone-dim sm:col-span-8 md:col-span-5 lg:col-start-8 lg:mt-16"
        >
          We design sculptural light installations for hotels, museums, and
          private residences — pieces that are forged, not fitted, and tuned to
          the hour when architecture holds its breath.
        </p>
      </div>

      {/* scroll cue with infinite draw-on line */}
      <div
        ref={cueRef}
        className="absolute bottom-8 left-1/2 flex -translate-x-1/2 flex-col items-center gap-3"
        aria-hidden
      >
        <span className="label-caps">Scroll</span>
        <svg width="2" height="56" viewBox="0 0 2 56" fill="none" className="overflow-visible">
          <line
            x1="1"
            y1="0"
            x2="1"
            y2="56"
            stroke="var(--color-amber)"
            strokeWidth="1.5"
            strokeDasharray="56"
            className="cue-line"
          />
        </svg>
      </div>
    </section>
  )
}
