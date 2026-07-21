"use client";

import Image from "next/image";
import { motion, useReducedMotion } from "framer-motion";
import { gallery } from "@/config/site";
import Reveal from "@/components/Reveal";

export default function Gallery() {
  const reduce = useReducedMotion();

  return (
    <section id="gallery" className="scroll-mt-20">
      <div className="mx-auto max-w-6xl px-4 py-20 sm:px-6 sm:py-28">
        <Reveal>
          <p className="text-xs font-medium uppercase tracking-[0.35em] text-sage-deep">
            Portfolio
          </p>
          <h2 className="mt-3 font-display text-4xl font-medium sm:text-5xl">Recent work</h2>
        </Reveal>

        <div className="mt-12 grid grid-cols-2 gap-3 sm:gap-5 lg:grid-cols-3">
          {gallery.map((photo, i) => (
            <Reveal key={photo.src} delay={Math.min(i * 0.06, 0.3)}>
              <motion.div
                whileHover={reduce ? undefined : { scale: 1.02 }}
                transition={{ duration: 0.3, ease: "easeOut" }}
                className="overflow-hidden rounded-xl"
              >
                <Image
                  src={photo.src}
                  alt={photo.alt}
                  width={800}
                  height={1000}
                  loading="lazy"
                  className="aspect-[4/5] w-full object-cover"
                />
              </motion.div>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  );
}
