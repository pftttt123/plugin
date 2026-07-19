import { useLayoutEffect, useRef } from 'react'
import { useReducedMotion } from 'framer-motion'
import { gsap } from '../lib/gsap'
import { EASE, PRELOADER } from '../lib/motion'

interface PreloaderProps {
  onComplete: () => void
}

/**
 * 0→100 counter beside the wordmark, then a two-panel curtain wipe.
 * On repeat visits (sessionStorage) the whole thing collapses to a
 * quick fade so returning users aren't held hostage.
 */
export function Preloader({ onComplete }: PreloaderProps) {
  const rootRef = useRef<HTMLDivElement>(null)
  const counterRef = useRef<HTMLSpanElement>(null)
  const markRef = useRef<HTMLDivElement>(null)
  const panelARef = useRef<HTMLDivElement>(null)
  const panelBRef = useRef<HTMLDivElement>(null)
  const reduced = useReducedMotion()
  const completeRef = useRef(onComplete)
  completeRef.current = onComplete

  useLayoutEffect(() => {
    const root = rootRef.current
    const counter = counterRef.current
    if (!root || !counter) return

    const visited = sessionStorage.getItem(PRELOADER.sessionKey) === '1'
    sessionStorage.setItem(PRELOADER.sessionKey, '1')

    const ctx = gsap.context(() => {
      if (reduced || visited) {
        const tl = gsap.timeline({
          onComplete: () => completeRef.current(),
        })
        tl.to(root, { autoAlpha: 0, duration: 0.45, ease: 'power2.out', delay: 0.15 })
        return
      }

      const progress = { value: 0 }
      const tl = gsap.timeline({
        onComplete: () => completeRef.current(),
      })

      tl.fromTo(
        markRef.current,
        { autoAlpha: 0, y: 12 },
        { autoAlpha: 1, y: 0, duration: 0.6, ease: EASE.out },
        0,
      )
        .to(
          progress,
          {
            value: 100,
            duration: PRELOADER.count,
            ease: 'power2.inOut',
            onUpdate: () => {
              counter.textContent = String(Math.round(progress.value)).padStart(3, '0')
            },
          },
          0.1,
        )
        // tuck the counter + mark away before the wipe
        .to([markRef.current, counter.parentElement], {
          yPercent: -120,
          autoAlpha: 0,
          duration: 0.5,
          ease: EASE.inOut,
          stagger: 0.06,
        })
        // curtain: two staggered panels sliding up
        .to(
          [panelARef.current, panelBRef.current],
          {
            yPercent: -100,
            duration: PRELOADER.wipe,
            ease: EASE.inOut,
            stagger: 0.12,
          },
          '-=0.15',
        )
        .set(root, { autoAlpha: 0 })
    }, root)

    return () => ctx.revert()
  }, [reduced])

  return (
    <div
      ref={rootRef}
      className="fixed inset-0 z-preloader"
      aria-hidden
      role="presentation"
    >
      {/* curtain panels — the second sits behind, tinted, for the staggered double-wipe */}
      <div ref={panelBRef} className="absolute inset-0 bg-ink-soft will-change-transform" />
      <div ref={panelARef} className="absolute inset-0 bg-ink will-change-transform">
        <div className="gutter flex h-full items-end justify-between pb-10">
          <div ref={markRef} className="flex items-baseline gap-4 opacity-0">
            <span className="font-display text-2xl font-light tracking-tight text-bone">
              Lumen <span className="text-amber">&amp;</span> Forge
            </span>
            <span className="label-caps hidden sm:inline">Atelier of Light — Copenhagen</span>
          </div>
          <div className="overflow-hidden">
            <span
              ref={counterRef}
              className="block font-display text-7xl font-light tabular-nums text-bone md:text-8xl"
            >
              000
            </span>
          </div>
        </div>
      </div>
    </div>
  )
}
