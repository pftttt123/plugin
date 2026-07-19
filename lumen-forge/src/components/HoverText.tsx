import type { CSSProperties } from 'react'

interface HoverTextProps {
  text: string
  className?: string
}

/**
 * Duplicate-text hover swap: on hover the visible copy slides up out of a
 * clip while a ghost copy slides in from below, letter by letter. Purely
 * CSS-driven (see .swap rules); works from any ancestor <a>/<button> or
 * an element with the .swap-trigger class.
 */
export function HoverText({ text, className = '' }: HoverTextProps) {
  const chars = [...text]
  const renderLayer = (ghost: boolean) => (
    <span
      className={`swap-layer${ghost ? ' swap-layer--ghost' : ''}`}
      aria-hidden={ghost || undefined}
    >
      {chars.map((char, i) => (
        <span
          key={i}
          className="swap-char"
          style={{ '--i': i } as CSSProperties}
        >
          {char}
        </span>
      ))}
    </span>
  )

  return (
    <span className={`swap ${className}`} aria-label={text}>
      {renderLayer(false)}
      {renderLayer(true)}
    </span>
  )
}
