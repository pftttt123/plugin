import { useEffect, useRef, type RefObject } from 'react'
import { useReducedMotion } from 'framer-motion'
import { gsap } from '../lib/gsap'

interface MagneticOptions {
  /** how far the element travels toward the cursor (0–1 of the offset) */
  strength?: number
  /** extra hover padding around the element, in px */
  padding?: number
}

/**
 * Attracts an element toward the cursor while hovered, springing back on
 * leave. Transform-only, driven by gsap.quickTo. No-ops for touch input and
 * reduced motion.
 */
export function useMagnetic<T extends HTMLElement>({
  strength = 0.35,
  padding = 24,
}: MagneticOptions = {}): RefObject<T> {
  const ref = useRef<T>(null)
  const reduced = useReducedMotion()

  useEffect(() => {
    const el = ref.current
    if (!el || reduced || !window.matchMedia('(pointer: fine)').matches) return

    const xTo = gsap.quickTo(el, 'x', { duration: 0.9, ease: 'expo.out' })
    const yTo = gsap.quickTo(el, 'y', { duration: 0.9, ease: 'expo.out' })

    const onMove = (e: MouseEvent) => {
      const rect = el.getBoundingClientRect()
      const cx = rect.left + rect.width / 2
      const cy = rect.top + rect.height / 2
      const inRange =
        e.clientX > rect.left - padding &&
        e.clientX < rect.right + padding &&
        e.clientY > rect.top - padding &&
        e.clientY < rect.bottom + padding
      if (inRange) {
        xTo((e.clientX - cx) * strength)
        yTo((e.clientY - cy) * strength)
      } else {
        xTo(0)
        yTo(0)
      }
    }

    window.addEventListener('mousemove', onMove, { passive: true })
    return () => {
      window.removeEventListener('mousemove', onMove)
      gsap.set(el, { x: 0, y: 0 })
    }
  }, [strength, padding, reduced])

  return ref
}
