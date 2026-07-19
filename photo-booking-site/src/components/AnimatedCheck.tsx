"use client";

import { motion, useReducedMotion } from "framer-motion";

/** Circle-and-checkmark that draws itself in on mount. */
export default function AnimatedCheck() {
  const reduce = useReducedMotion();
  const draw = (delay: number) =>
    reduce
      ? {}
      : {
          initial: { pathLength: 0, opacity: 0 },
          animate: { pathLength: 1, opacity: 1 },
          transition: { duration: 0.55, delay, ease: "easeOut" as const },
        };

  return (
    <motion.svg
      viewBox="0 0 64 64"
      className="h-20 w-20 text-sage-deep"
      fill="none"
      aria-hidden="true"
      initial={reduce ? false : { scale: 0.8, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
    >
      <motion.circle
        cx="32"
        cy="32"
        r="28"
        stroke="currentColor"
        strokeWidth="3"
        strokeLinecap="round"
        {...draw(0.1)}
      />
      <motion.path
        d="M20 33.5 28.5 42 44 24.5"
        stroke="currentColor"
        strokeWidth="4"
        strokeLinecap="round"
        strokeLinejoin="round"
        {...draw(0.5)}
      />
    </motion.svg>
  );
}
