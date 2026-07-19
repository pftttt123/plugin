import { useEffect, useRef, useState } from 'react'
import { useReducedMotion } from 'framer-motion'
import { gsap } from '../lib/gsap'
import { LERP } from '../lib/motion'

/**
 * Custom cursor: a 1:1 dot plus a lagging ring. The ring scales up over
 * interactive elements and shows a contextual label over elements carrying
 * a `data-cursor` attribute (e.g. data-cursor="View"). Disabled for touch
 * pointers and reduced motion.
 */
export function Cursor() {
  const dotRef = useRef<HTMLDivElement>(null)
  const ringRef = useRef<HTMLDivElement>(null)
  const [label, setLabel] = useState('')
  const [active, setActive] = useState(false)
  const [enabled, setEnabled] = useState(false)
  const reduced = useReducedMotion()

  useEffect(() => {
    if (reduced || !window.matchMedia('(pointer: fine)').matches) return
    setEnabled(true)
    document.documentElement.classList.add('has-custom-cursor')
    return () => {
      document.documentElement.classList.remove('has-custom-cursor')
      setEnabled(false)
    }
  }, [reduced])

  useEffect(() => {
    if (!enabled) return
    const dot = dotRef.current
    const ring = ringRef.current
    if (!dot || !ring) return

    const setDotX = gsap.quickSetter(dot, 'x', 'px')
    const setDotY = gsap.quickSetter(dot, 'y', 'px')
    const setRingX = gsap.quickSetter(ring, 'x', 'px')
    const setRingY = gsap.quickSetter(ring, 'y', 'px')

    const target = { x: window.innerWidth / 2, y: window.innerHeight / 2 }
    const eased = { ...target }
    let seen = false

    const onMove = (e: MouseEvent) => {
      target.x = e.clientX
      target.y = e.clientY
      if (!seen) {
        seen = true
        eased.x = target.x
        eased.y = target.y
        gsap.to([dot, ring], { autoAlpha: 1, duration: 0.3 })
      }
      setDotX(target.x)
      setDotY(target.y)
    }

    const tick = () => {
      eased.x += (target.x - eased.x) * LERP.cursorRing
      eased.y += (target.y - eased.y) * LERP.cursorRing
      setRingX(eased.x)
      setRingY(eased.y)
    }

    const onOver = (e: MouseEvent) => {
      const el = e.target as HTMLElement | null
      const labelled = el?.closest<HTMLElement>('[data-cursor]')
      const interactive = el?.closest('a, button, [role="button"]')
      setLabel(labelled?.dataset.cursor ?? '')
      setActive(Boolean(labelled || interactive))
    }

    const onLeave = () => gsap.to([dot, ring], { autoAlpha: 0, duration: 0.3 })
    const onEnter = () => gsap.to([dot, ring], { autoAlpha: 1, duration: 0.3 })

    gsap.ticker.add(tick)
    window.addEventListener('mousemove', onMove, { passive: true })
    document.addEventListener('mouseover', onOver, { passive: true })
    document.documentElement.addEventListener('mouseleave', onLeave)
    document.documentElement.addEventListener('mouseenter', onEnter)
    return () => {
      gsap.ticker.remove(tick)
      window.removeEventListener('mousemove', onMove)
      document.removeEventListener('mouseover', onOver)
      document.documentElement.removeEventListener('mouseleave', onLeave)
      document.documentElement.removeEventListener('mouseenter', onEnter)
    }
  }, [enabled])

  useEffect(() => {
    const ring = ringRef.current
    if (!ring || !enabled) return
    gsap.to(ring, {
      scale: label ? 2.6 : active ? 1.7 : 1,
      duration: 0.5,
      ease: 'expo.out',
    })
  }, [label, active, enabled])

  if (!enabled) return null

  return (
    <div aria-hidden className="pointer-events-none fixed inset-0 z-cursor">
      <div
        ref={dotRef}
        className="invisible absolute left-0 top-0 h-[5px] w-[5px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-amber will-change-transform"
        style={{ marginLeft: '-2.5px', marginTop: '-2.5px' }}
      />
      <div
        ref={ringRef}
        className="invisible absolute left-0 top-0 flex h-9 w-9 items-center justify-center rounded-full border will-change-transform"
        style={{
          marginLeft: '-18px',
          marginTop: '-18px',
          borderColor: label
            ? 'var(--color-amber)'
            : 'rgba(242, 239, 232, 0.35)',
          backgroundColor: label ? 'rgba(232, 163, 61, 0.10)' : 'transparent',
          transition:
            'border-color 0.3s var(--ease-out-expo), background-color 0.3s var(--ease-out-expo)',
        }}
      >
        <span
          className="font-sans uppercase text-bone"
          style={{
            fontSize: '4px',
            letterSpacing: '0.18em',
            opacity: label ? 1 : 0,
            transition: 'opacity 0.25s var(--ease-out-expo)',
          }}
        >
          {label}
        </span>
      </div>
    </div>
  )
}
