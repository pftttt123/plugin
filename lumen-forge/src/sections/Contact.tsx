import { useEffect, useRef, useState } from 'react'
import { useReducedMotion } from 'framer-motion'
import { gsap } from '../lib/gsap'
import { CTA_IMAGE, SOCIALS } from '../lib/data'
import { LERP } from '../lib/motion'
import { useLocalTime } from '../hooks/useLocalTime'
import { useMagnetic } from '../hooks/useMagnetic'
import { HoverText } from '../components/HoverText'

/**
 * Studio CTA: a giant "Begin a Commission" line that swaps fill for outline
 * on hover while a small image trails the cursor. Below it, the footer with
 * the Copenhagen studio clock, socials, and fine print.
 */
export function Contact() {
  const sectionRef = useRef<HTMLElement>(null)
  const trailRef = useRef<HTMLDivElement>(null)
  const [imageOk, setImageOk] = useState(true)
  const reduced = useReducedMotion()
  const magneticRef = useMagnetic<HTMLDivElement>({ strength: 0.12, padding: 40 })
  const time = useLocalTime('Europe/Copenhagen')

  // trailing image reveal that follows the cursor across the CTA area
  useEffect(() => {
    const section = sectionRef.current
    const trail = trailRef.current
    if (!section || !trail || reduced || !window.matchMedia('(pointer: fine)').matches)
      return

    const setX = gsap.quickSetter(trail, 'x', 'px')
    const setY = gsap.quickSetter(trail, 'y', 'px')
    const target = { x: 0, y: 0 }
    const eased = { x: 0, y: 0 }
    let hovering = false

    const onMove = (e: MouseEvent) => {
      const rect = section.getBoundingClientRect()
      target.x = e.clientX - rect.left
      target.y = e.clientY - rect.top
      if (!hovering && (e.target as HTMLElement).closest('.cta-line')) {
        hovering = true
        eased.x = target.x
        eased.y = target.y
        gsap.to(trail, { autoAlpha: 1, scale: 1, duration: 0.6, ease: 'expo.out' })
      } else if (hovering && !(e.target as HTMLElement).closest('.cta-line')) {
        hovering = false
        gsap.to(trail, { autoAlpha: 0, scale: 0.85, duration: 0.5, ease: 'expo.out' })
      }
    }

    const tick = () => {
      eased.x += (target.x - eased.x) * LERP.trail
      eased.y += (target.y - eased.y) * LERP.trail
      setX(eased.x)
      setY(eased.y)
    }

    section.addEventListener('mousemove', onMove, { passive: true })
    gsap.ticker.add(tick)
    return () => {
      section.removeEventListener('mousemove', onMove)
      gsap.ticker.remove(tick)
    }
  }, [reduced])

  return (
    <section
      id="contact"
      ref={sectionRef}
      className="relative overflow-hidden pt-[var(--space-section)]"
      aria-label="Contact"
    >
      {/* trailing image */}
      <div
        ref={trailRef}
        aria-hidden
        className="pointer-events-none invisible absolute left-0 top-0 z-10 h-64 w-48 -translate-x-1/2 -translate-y-1/2 overflow-hidden will-change-transform"
      >
        {imageOk ? (
          <img
            src={CTA_IMAGE}
            alt=""
            loading="lazy"
            decoding="async"
            className="h-full w-full object-cover"
            onError={() => setImageOk(false)}
          />
        ) : (
          <div
            className="h-full w-full"
            style={{
              background:
                'radial-gradient(90% 70% at 50% 40%, rgba(232,163,61,0.5), transparent 65%), #131316',
            }}
          />
        )}
      </div>

      <div className="gutter relative text-center">
        <p className="label-caps mb-10">Begin at dusk — deliver at nightfall</p>
        <div ref={magneticRef} className="inline-block will-change-transform">
          <a
            href="mailto:studio@lumenforge.dk"
            className="cta-line block font-display font-light leading-[1.02] tracking-tight"
            style={{ fontSize: 'clamp(2.6rem, 9vw, 9rem)' }}
          >
            <span className="cta-fill block text-bone">Begin a Commission</span>
            <span className="cta-outline text-outline-amber block" aria-hidden>
              Begin a Commission
            </span>
          </a>
        </div>
        <p className="mx-auto mt-10 max-w-md text-sm leading-relaxed text-bone-dim">
          Commissions open for 2027. We take on eight projects a year — enough
          to know every piece by name.
        </p>
      </div>

      <footer className="gutter mt-[14vh] border-t border-bone/10 pb-8 pt-10">
        <div className="grid grid-cols-12 gap-y-10">
          <div className="col-span-12 md:col-span-4">
            <p className="label-caps">Copenhagen Studio</p>
            <p className="mt-3 text-sm text-bone-dim">
              Refshalevej 167A
              <br />
              1432 København K
            </p>
            <p
              className="mt-3 font-sans text-sm tabular-nums text-amber"
              aria-label={`Local studio time ${time}`}
            >
              {time} CPH
            </p>
          </div>
          <div className="col-span-12 md:col-span-4">
            <p className="label-caps">Enquiries</p>
            <ul className="mt-3 space-y-2 text-sm">
              <li>
                <a href="mailto:studio@lumenforge.dk" className="text-bone-dim transition-colors hover:text-amber">
                  studio@lumenforge.dk
                </a>
              </li>
              <li>
                <a href="tel:+4533121870" className="text-bone-dim transition-colors hover:text-amber">
                  +45 33 12 18 70
                </a>
              </li>
            </ul>
          </div>
          <div className="col-span-12 md:col-span-4">
            <p className="label-caps">Elsewhere</p>
            <ul className="mt-3 space-y-2">
              {SOCIALS.map((social) => (
                <li key={social.label} className="overflow-hidden">
                  <a
                    href={social.href}
                    target="_blank"
                    rel="noreferrer"
                    className="social-link inline-flex items-center gap-2 text-sm text-bone-dim transition-colors hover:text-bone"
                  >
                    <span
                      aria-hidden
                      className="inline-block h-px w-0 bg-amber transition-all duration-500 ease-out-expo group-hover:w-4 [.social-link:hover_&]:w-5"
                    />
                    <HoverText text={social.label} />
                  </a>
                </li>
              ))}
            </ul>
          </div>
        </div>
        <div className="mt-14 flex flex-wrap items-center justify-between gap-4 border-t border-bone/5 pt-6">
          <p className="label-caps">© 2026 Lumen &amp; Forge ApS</p>
          <p className="label-caps">All light reserved</p>
          <p className="label-caps hidden sm:block">CVR 34 71 22 08</p>
        </div>
      </footer>
    </section>
  )
}
