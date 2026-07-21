"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { site } from "@/config/site";

export default function Header() {
  const pathname = usePathname();
  const onBookingPage = pathname === "/book";

  return (
    <header className="sticky top-0 z-40 border-b border-line bg-paper/85 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
        <Link
          href="/"
          className="flex min-h-11 flex-col justify-center leading-tight"
          aria-label={`${site.name} ${site.nameSuffix} — home`}
        >
          <span className="font-display text-xl font-semibold tracking-wide sm:text-2xl">
            {site.name}
          </span>
          <span className="text-[0.6rem] font-medium uppercase tracking-[0.35em] text-fog">
            {site.nameSuffix}
          </span>
        </Link>

        <nav aria-label="Main" className="flex items-center gap-1 sm:gap-6">
          <Link
            href="/#shoots"
            className="hidden min-h-11 items-center px-2 text-sm text-fog transition-colors duration-200 hover:text-ink sm:flex"
          >
            Shoots
          </Link>
          <Link
            href="/#gallery"
            className="hidden min-h-11 items-center px-2 text-sm text-fog transition-colors duration-200 hover:text-ink sm:flex"
          >
            Gallery
          </Link>
          <Link
            href="/book"
            aria-current={onBookingPage ? "page" : undefined}
            className="inline-flex min-h-11 items-center rounded-full bg-sage-deep px-5 text-sm font-medium text-white transition-all duration-200 hover:bg-ink active:scale-[0.97]"
          >
            Book a shoot
          </Link>
        </nav>
      </div>
    </header>
  );
}
