import { useLayoutEffect, useRef, useState } from 'react'
import { useReducedMotion } from 'framer-motion'
import { gsap, ScrollTrigger } from '../lib/gsap'
import { PROCESS_STAGES } from '../lib/data'
import { EASE } from '../lib/motion'

/**
 * Pinned section: scrolling advances through three numbered stages. The
 * stage number and copy swap through clip masks, and an abstract SVG line
 * morphs between per-stage path shapes (same command structure, tweened
 * with an attr tween — no paid plugins).
 */
export function Process() {
  const sectionRef = useRef<HTMLElement>(null)
  const numberRef = useRef<HTMLSpanElement>(null)
  const titleRef = useRef<HTMLHeadingElement>(null)
  const copyRef = useRef<HTMLParagraphElement>(null)
  const pathRef = useRef<SVGPathElement>(null)
  const barRef = useRef<HTMLDivElement>(null)
  const [stage, setStage] = useState(0)
  const stageRef = useRef(0)
  const reduced = useReducedMotion()

  useLayoutEffect(() => {
    const section = sectionRef.current
    if (!section || reduced) return

    const ctx = gsap.context(() => {
      ScrollTrigger.create({
        trigger: section,
        start: 'top top',
        end: '+=260%',
        pin: true,
        onUpdate: (self) => {
          const next = Math.min(
            PROCESS_STAGES.length - 1,
            Math.floor(self.progress * PROCESS_STAGES.length),
          )
          if (next !== stageRef.current) {
            stageRef.current = next
            setStage(next)
          }
          if (barRef.current) {
            gsap.set(barRef.current, { scaleY: self.progress })
          }
        },
      })
    }, section)

    return () => ctx.revert()
  }, [reduced])

  // masked swap + path morph whenever the stage changes
  useLayoutEffect(() => {
    if (reduced) return
    const targets = [numberRef.current, titleRef.current, copyRef.current]
    const ctx = gsap.context(() => {
      gsap.fromTo(
        targets,
        { yPercent: 60, autoAlpha: 0 },
        { yPercent: 0, autoAlpha: 1, duration: 0.9, ease: EASE.out, stagger: 0.07 },
      )
      if (pathRef.current) {
        gsap.to(pathRef.current, {
          attr: { d: PROCESS_STAGES[stage].path },
          duration: 1.2,
          ease: EASE.out,
        })
      }
    })
    return () => ctx.revert()
  }, [stage, reduced])

  const current = PROCESS_STAGES[stage]

  if (reduced) {
    // reduced motion: the three stages stack vertically, no pinning
    return (
      <section id="process" className="gutter py-[var(--space-section)]" aria-label="Process">
        <p className="label-caps mb-12">Process</p>
        <div className="space-y-20">
          {PROCESS_STAGES.map((s) => (
            <div key={s.number} className="grid grid-cols-12 gap-6">
              <span className="text-outline-amber col-span-3 font-display text-6xl font-light">
                {s.number}
              </span>
              <div className="col-span-9 max-w-lg">
                <h3 className="font-display text-3xl font-light text-bone">{s.title}</h3>
                <p className="mt-4 text-sm leading-relaxed text-bone-dim">{s.copy}</p>
              </div>
            </div>
          ))}
        </div>
      </section>
    )
  }

  return (
    <section id="process" ref={sectionRef} aria-label="Process">
      <div className="gutter relative flex h-screen flex-col justify-center">
        <p className="label-caps absolute top-[12vh]">Process</p>

        {/* scroll progress bar */}
        <div
          aria-hidden
          className="absolute right-[var(--space-gutter)] top-1/2 h-32 w-px -translate-y-1/2 bg-bone/10"
        >
          <div
            ref={barRef}
            className="h-full w-full origin-top scale-y-0 bg-amber will-change-transform"
          />
        </div>

        <div className="grid grid-cols-12 items-center gap-6">
          <div className="col-span-12 overflow-hidden md:col-span-3">
            <span
              ref={numberRef}
              className="text-outline-amber block font-display font-light leading-none will-change-transform"
              style={{ fontSize: 'clamp(6rem, 14vw, 14rem)' }}
            >
              {current.number}
            </span>
          </div>
          <div className="col-span-12 md:col-span-5 md:col-start-5">
            <div className="overflow-hidden">
              <h3
                ref={titleRef}
                className="font-display font-light text-bone will-change-transform"
                style={{ fontSize: 'clamp(2.2rem, 4.5vw, 4rem)' }}
              >
                {current.title}
              </h3>
            </div>
            <div className="mt-6 overflow-hidden">
              <p
                ref={copyRef}
                className="max-w-md text-sm leading-relaxed text-bone-dim will-change-transform"
              >
                {current.copy}
              </p>
            </div>
          </div>
          <div className="col-span-12 mt-10 md:col-span-3 md:col-start-10 md:mt-0">
            <svg
              viewBox="0 0 600 200"
              fill="none"
              className="w-full max-w-xs"
              aria-hidden
            >
              <path
                ref={pathRef}
                d={PROCESS_STAGES[0].path}
                stroke="var(--color-amber)"
                strokeWidth="2"
                strokeLinecap="round"
                opacity="0.85"
              />
            </svg>
          </div>
        </div>
      </div>
    </section>
  )
}
