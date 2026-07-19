import { useEffect, useRef, useState, type MouseEvent } from 'react'
import { motion, useReducedMotion } from 'framer-motion'
import { NAV_LINKS } from '../lib/data'
import { DUR, EASE_CSS } from '../lib/motion'
import { useLenis } from '../hooks/useLenis'
import { useMagnetic } from '../hooks/useMagnetic'
import { HoverText } from './HoverText'

interface NavbarProps {
  ready: boolean
}

function NavLink({ label, href }: { label: string; href: string }) {
  const magneticRef = useMagnetic<HTMLLIElement>({ strength: 0.25, padding: 14 })
  const lenis = useLenis()

  const onClick = (e: MouseEvent<HTMLAnchorElement>) => {
    const target = document.querySelector<HTMLElement>(href)
    if (!target) return
    e.preventDefault()
    if (lenis) lenis.scrollTo(target, { offset: 0, duration: 1.4 })
    else target.scrollIntoView({ behavior: 'auto' })
  }

  return (
    <li ref={magneticRef} className="will-change-transform">
      <a
        href={href}
        onClick={onClick}
        className="label-caps !text-bone transition-colors duration-300 hover:!text-amber"
      >
        <HoverText text={label} />
      </a>
    </li>
  )
}

/**
 * Fixed navbar: hides on scroll down, reveals on scroll up, and gains a
 * blurred-glass background once the hero is out of view.
 */
export function Navbar({ ready }: NavbarProps) {
  const [hidden, setHidden] = useState(false)
  const [glassy, setGlassy] = useState(false)
  const lastY = useRef(0)
  const ticking = useRef(false)
  const reduced = useReducedMotion()
  const lenis = useLenis()

  useEffect(() => {
    const update = () => {
      ticking.current = false
      const y = window.scrollY
      const delta = y - lastY.current
      if (Math.abs(delta) > 6) {
        setHidden(delta > 0 && y > 140)
        lastY.current = y
      }
      setGlassy(y > window.innerHeight * 0.75)
    }
    const onScroll = () => {
      if (!ticking.current) {
        ticking.current = true
        requestAnimationFrame(update)
      }
    }
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  const scrollTop = (e: MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault()
    if (lenis) lenis.scrollTo(0, { duration: 1.6 })
    else window.scrollTo({ top: 0 })
  }

  return (
    <motion.header
      initial={reduced ? { opacity: 0 } : { y: -32, opacity: 0 }}
      animate={
        ready
          ? reduced
            ? { opacity: 1 }
            : { y: hidden ? '-100%' : 0, opacity: 1 }
          : undefined
      }
      transition={{ duration: DUR.base, ease: EASE_CSS.out }}
      className={`fixed inset-x-0 top-0 z-nav transition-[background-color,backdrop-filter,border-color] duration-500 ${
        glassy
          ? 'border-b border-bone/5 bg-ink/60 backdrop-blur-md'
          : 'border-b border-transparent bg-transparent'
      }`}
    >
      <nav
        aria-label="Primary"
        className="gutter flex items-center justify-between py-5"
      >
        <a
          href="#top"
          onClick={scrollTop}
          className="font-display text-lg font-light tracking-tight text-bone"
          aria-label="Lumen & Forge — back to top"
        >
          Lumen <span className="text-amber">&amp;</span> Forge
        </a>
        <ul className="hidden items-center gap-10 md:flex">
          {NAV_LINKS.map((link) => (
            <NavLink key={link.href} {...link} />
          ))}
        </ul>
        {/* mobile: single contact shortcut instead of a menu */}
        <a
          href="#contact"
          className="label-caps !text-amber md:hidden"
          onClick={(e) => {
            const target = document.querySelector<HTMLElement>('#contact')
            if (!target) return
            e.preventDefault()
            if (lenis) lenis.scrollTo(target, { duration: 1.4 })
            else target.scrollIntoView()
          }}
        >
          Contact
        </a>
      </nav>
    </motion.header>
  )
}
