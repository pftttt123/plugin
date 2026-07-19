/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: 'var(--color-ink)',
        'ink-soft': 'var(--color-ink-soft)',
        bone: 'var(--color-bone)',
        'bone-dim': 'var(--color-bone-dim)',
        amber: 'var(--color-amber)',
      },
      fontFamily: {
        display: 'var(--font-display)',
        sans: 'var(--font-sans)',
      },
      transitionTimingFunction: {
        'out-expo': 'var(--ease-out-expo)',
        'out-quart': 'var(--ease-out-quart)',
      },
      letterSpacing: {
        caps: '0.22em',
      },
      zIndex: {
        nav: '60',
        cursor: '90',
        preloader: '80',
        grain: '70',
      },
    },
  },
  plugins: [],
}
