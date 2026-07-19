import { useLayoutEffect, useRef } from 'react'
import { useReducedMotion } from 'framer-motion'
import { gsap } from '../lib/gsap'
import { TESTIMONIAL } from '../lib/data'
import { useSplitText } from '../hooks/useSplitText'

/**
 * Oversized pull-quote revealed word by word: each word scrubs from 15% to
 * full opacity as the reader scrolls through the section.
 */
export function Testimonial() {
  const sectionRef = useRef<HTMLElement>(null)
  const quoteRef = useRef<HTMLParagraphElement>(null)
  const creditRef = useRef<HTMLElement>(null)
  const reduced = useReducedMotion()
  const words = useSplitText(quoteRef, 'words')

  useLayoutEffect(() => {
    const section = sectionRef.current
    if (!section || words.length === 0) return

    const ctx = gsap.context(() => {
      if (reduced) {
        gsap.set(words, { opacity: 1 })
        return
      }

      gsap.fromTo(
        words,
        { opacity: 0.15 },
        {
          opacity: 1,
          ease: 'none',
          stagger: 0.35,
          scrollTrigger: {
            trigger: section,
            start: 'top 70%',
            end: 'bottom 75%',
            scrub: true,
          },
        },
      )

      gsap.from(creditRef.current, {
        autoAlpha: 0,
        y: 16,
        duration: 0.9,
        ease: 'expo.out',
        scrollTrigger: { trigger: creditRef.current, start: 'top 88%' },
      })
    }, section)

    return () => ctx.revert()
  }, [words, reduced])

  return (
    <section
      ref={sectionRef}
      className="gutter py-[var(--space-section)]"
      aria-label="Testimonial"
    >
      <figure className="mx-auto max-w-5xl">
        <blockquote>
          <p
            ref={quoteRef}
            className="font-display font-light leading-[1.15] tracking-tight text-bone"
            style={{ fontSize: 'clamp(1.8rem, 4.6vw, 4.4rem)' }}
          >
            “{TESTIMONIAL.quote}”
          </p>
        </blockquote>
        <figcaption ref={creditRef} className="mt-12">
          <span className="block font-sans text-sm text-bone">
            {TESTIMONIAL.attribution}
          </span>
          <span className="label-caps mt-1 block">{TESTIMONIAL.role}</span>
        </figcaption>
      </figure>
    </section>
  )
}
