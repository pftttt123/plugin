import Lenis from 'lenis'
import { createContext, useContext, useEffect, useState } from 'react'
import { gsap, ScrollTrigger } from '../lib/gsap'

export const LenisContext = createContext<Lenis | null>(null)

/** Access the root Lenis instance anywhere in the tree (null when reduced motion is on). */
export function useLenis(): Lenis | null {
  return useContext(LenisContext)
}

/**
 * Creates the single Lenis instance that drives the whole page and wires it
 * into GSAP: Lenis raf runs on the GSAP ticker, and every Lenis scroll event
 * pings ScrollTrigger.update so triggers track the smoothed scroll position.
 */
export function useLenisRoot(enabled: boolean, started: boolean): Lenis | null {
  const [lenis, setLenis] = useState<Lenis | null>(null)

  useEffect(() => {
    if (!enabled) return

    const instance = new Lenis({
      duration: 1.15,
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
      smoothWheel: true,
      touchMultiplier: 1.4,
    })

    instance.on('scroll', ScrollTrigger.update)
    const raf = (time: number) => instance.raf(time * 1000)
    gsap.ticker.add(raf)
    gsap.ticker.lagSmoothing(0)

    setLenis(instance)

    return () => {
      gsap.ticker.remove(raf)
      instance.destroy()
      setLenis(null)
    }
  }, [enabled])

  // Freeze the page while the preloader owns the screen.
  useEffect(() => {
    if (!lenis) return
    if (started) lenis.start()
    else lenis.stop()
  }, [lenis, started])

  return lenis
}
