"use client";

import Link from "next/link";
import { motion, useReducedMotion } from "framer-motion";
import { shootTypes } from "@/config/site";
import Reveal from "@/components/Reveal";

export default function ShootTypes() {
  const reduce = useReducedMotion();

  return (
    <section id="shoots" className="scroll-mt-20 bg-mist">
      <div className="mx-auto max-w-6xl px-4 py-20 sm:px-6 sm:py-28">
        <Reveal>
          <p className="text-xs font-medium uppercase tracking-[0.35em] text-sage-deep">
            Sessions
          </p>
          <h2 className="mt-3 font-display text-4xl font-medium sm:text-5xl">
            Choose your kind of shoot
          </h2>
          <p className="mt-4 max-w-xl text-fog">
            Every session includes a planning chat beforehand and an online gallery of
            hand-picked, edited images afterward.
          </p>
        </Reveal>

        <ul className="mt-12 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {shootTypes.map((type, i) => (
            <Reveal key={type.slug} delay={Math.min(i * 0.07, 0.35)}>
              <motion.li
                whileHover={reduce ? undefined : { y: -4 }}
                transition={{ duration: 0.25, ease: "easeOut" }}
                className="h-full list-none"
              >
                <Link
                  href={`/book?type=${type.slug}`}
                  className="flex h-full flex-col rounded-2xl border border-line bg-paper p-6 shadow-sm transition-shadow duration-250 hover:shadow-md"
                >
                  <div className="flex items-baseline justify-between gap-3">
                    <h3 className="font-display text-2xl font-semibold">{type.name}</h3>
                    <span className="shrink-0 text-sm font-medium text-sage-deep">
                      {type.startingPrice > 0 ? `From $${type.startingPrice}` : "Custom quote"}
                    </span>
                  </div>
                  <p className="mt-3 flex-1 text-sm leading-relaxed text-fog">
                    {type.description}
                  </p>
                  <div className="mt-5 flex items-center justify-between border-t border-line pt-4">
                    <span className="text-xs uppercase tracking-wider text-fog">
                      {type.duration}
                    </span>
                    <span className="inline-flex items-center gap-1 text-sm font-medium text-sage-deep">
                      Book this
                      <svg
                        aria-hidden="true"
                        className="h-4 w-4 transition-transform duration-200 group-hover:translate-x-0.5"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                        strokeWidth={2}
                      >
                        <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 4.5 21 12l-7.5 7.5M21 12H3" />
                      </svg>
                    </span>
                  </div>
                </Link>
              </motion.li>
            </Reveal>
          ))}
        </ul>
      </div>
    </section>
  );
}
