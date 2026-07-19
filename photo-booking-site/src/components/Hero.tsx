"use client";

import Link from "next/link";
import Image from "next/image";
import { motion, useReducedMotion } from "framer-motion";
import { site, heroImage } from "@/config/site";

const ease = [0.22, 1, 0.36, 1] as const;

export default function Hero() {
  const reduce = useReducedMotion();
  const fadeUp = (delay: number) => ({
    initial: reduce ? false : { opacity: 0, y: 20 },
    animate: { opacity: 1, y: 0 },
    transition: { duration: 0.7, delay, ease },
  });

  return (
    <section className="relative isolate flex min-h-[85dvh] items-center overflow-hidden">
      <Image
        src={heroImage.src}
        alt={heroImage.alt}
        fill
        priority
        className="object-cover"
      />
      {/* Legibility scrim over the hero image */}
      <div className="absolute inset-0 bg-gradient-to-b from-paper/70 via-paper/40 to-paper" />

      <div className="relative mx-auto w-full max-w-6xl px-4 py-24 sm:px-6">
        <motion.p
          {...fadeUp(0.1)}
          className="mb-4 text-xs font-medium uppercase tracking-[0.35em] text-sage-deep"
        >
          {site.name} {site.nameSuffix}
        </motion.p>
        <motion.h1
          {...fadeUp(0.2)}
          className="max-w-2xl font-display text-5xl font-medium leading-[1.08] sm:text-6xl md:text-7xl"
        >
          {site.heroHeadline}
        </motion.h1>
        <motion.p {...fadeUp(0.35)} className="mt-6 max-w-xl text-lg leading-relaxed text-fog">
          {site.heroSub}
        </motion.p>
        <motion.div {...fadeUp(0.5)} className="mt-10 flex flex-wrap items-center gap-4">
          <Link
            href="/book"
            className="inline-flex min-h-13 items-center rounded-full bg-sage-deep px-8 text-base font-medium text-white shadow-sm transition-all duration-200 hover:bg-ink hover:shadow-md active:scale-[0.97]"
          >
            Book your shoot
          </Link>
          <Link
            href="/#shoots"
            className="inline-flex min-h-13 items-center px-2 text-base font-medium text-sage-deep underline-offset-4 transition-colors duration-200 hover:text-ink hover:underline"
          >
            See sessions &amp; pricing
          </Link>
        </motion.div>
      </div>
    </section>
  );
}
