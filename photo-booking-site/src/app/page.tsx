import Link from "next/link";
import Hero from "@/components/Hero";
import ShootTypes from "@/components/ShootTypes";
import Gallery from "@/components/Gallery";
import Reveal from "@/components/Reveal";

export default function HomePage() {
  return (
    <>
      <Hero />
      <ShootTypes />
      <Gallery />

      {/* Closing call-to-action */}
      <section className="bg-mist">
        <div className="mx-auto max-w-6xl px-4 py-20 text-center sm:px-6 sm:py-28">
          <Reveal>
            <h2 className="mx-auto max-w-2xl font-display text-4xl font-medium sm:text-5xl">
              Ready when you are.
            </h2>
            <p className="mx-auto mt-4 max-w-md text-fog">
              Tell me about your date, your people, and your vision — I&apos;ll take care of
              the rest.
            </p>
            <Link
              href="/book"
              className="mt-10 inline-flex min-h-13 items-center rounded-full bg-sage-deep px-8 text-base font-medium text-white shadow-sm transition-all duration-200 hover:bg-ink hover:shadow-md active:scale-[0.97]"
            >
              Start your booking
            </Link>
          </Reveal>
        </div>
      </section>
    </>
  );
}
