import { useEffect, useState } from 'react'
import { useReducedMotion } from 'framer-motion'
import { ScrollTrigger } from './lib/gsap'
import { LenisContext, useLenisRoot } from './hooks/useLenis'
import { Cursor } from './components/Cursor'
import { Preloader } from './components/Preloader'
import { Navbar } from './components/Navbar'
import { Hero } from './sections/Hero'
import { Manifesto } from './sections/Manifesto'
import { Works } from './sections/Works'
import { Process } from './sections/Process'
import { Materials } from './sections/Materials'
import { Testimonial } from './sections/Testimonial'
import { Contact } from './sections/Contact'

export default function App() {
  const reduced = useReducedMotion() ?? false
  const [ready, setReady] = useState(false)
  const [preloading, setPreloading] = useState(true)
  const lenis = useLenisRoot(!reduced, ready)

  // ScrollTrigger measured positions shift once the display fonts arrive
  useEffect(() => {
    let cancelled = false
    document.fonts?.ready.then(() => {
      if (!cancelled) ScrollTrigger.refresh()
    })
    return () => {
      cancelled = true
    }
  }, [])

  // hold the page still while the preloader owns the screen
  useEffect(() => {
    document.body.style.overflow = preloading ? 'hidden' : ''
    return () => {
      document.body.style.overflow = ''
    }
  }, [preloading])

  return (
    <LenisContext.Provider value={lenis}>
      <div className="grain">
        {preloading && (
          <Preloader
            onComplete={() => {
              setPreloading(false)
              setReady(true)
            }}
          />
        )}
        <Cursor />
        <Navbar ready={ready} />
        <main id="top">
          <Hero ready={ready} />
          <Manifesto />
          <Works />
          <Process />
          <Materials />
          <Testimonial />
          <Contact />
        </main>
      </div>
    </LenisContext.Provider>
  )
}
