import { useLayoutEffect, useState, type RefObject } from 'react'

export type SplitMode = 'chars' | 'words'

/**
 * Splits an element's text into masked units (`.split-mask > .split-unit`)
 * ready for clipped-reveal animation, and returns the unit elements.
 * Restores the original markup on unmount. Text must be static.
 */
export function useSplitText(
  ref: RefObject<HTMLElement | null>,
  mode: SplitMode = 'chars',
): HTMLElement[] {
  const [units, setUnits] = useState<HTMLElement[]>([])

  useLayoutEffect(() => {
    const el = ref.current
    if (!el) return

    const original = el.innerHTML
    const text = (el.textContent ?? '').trim()
    el.textContent = ''

    const created: HTMLElement[] = []
    const words = text.split(/\s+/)

    words.forEach((word, wi) => {
      if (mode === 'words') {
        const mask = document.createElement('span')
        mask.className = 'split-mask'
        const unit = document.createElement('span')
        unit.className = 'split-unit'
        unit.textContent = word
        mask.appendChild(unit)
        el.appendChild(mask)
        created.push(unit)
      } else {
        // keep chars of one word inside a nowrap group so words never break
        const group = document.createElement('span')
        group.style.whiteSpace = 'nowrap'
        group.style.display = 'inline-block'
        for (const char of word) {
          const mask = document.createElement('span')
          mask.className = 'split-mask'
          const unit = document.createElement('span')
          unit.className = 'split-unit'
          unit.textContent = char
          mask.appendChild(unit)
          group.appendChild(mask)
          created.push(unit)
        }
        el.appendChild(group)
      }
      if (wi < words.length - 1) el.appendChild(document.createTextNode(' '))
    })

    setUnits(created)

    return () => {
      el.innerHTML = original
      setUnits([])
    }
  }, [ref, mode])

  return units
}
