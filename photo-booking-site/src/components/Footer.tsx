import { site } from "@/config/site";

export default function Footer() {
  return (
    <footer className="border-t border-line bg-mist">
      <div className="mx-auto flex max-w-6xl flex-col items-center gap-3 px-4 py-10 text-center sm:px-6">
        <p className="font-display text-2xl font-semibold">
          {site.name}{" "}
          <span className="text-base font-normal italic text-fog">{site.nameSuffix}</span>
        </p>
        <p className="text-sm text-fog">{site.serviceArea}</p>
        <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2 text-sm">
          <a
            href={`mailto:${site.contactEmail}`}
            className="inline-flex min-h-11 items-center text-sage-deep underline-offset-4 transition-colors duration-200 hover:underline"
          >
            {site.contactEmail}
          </a>
          {site.instagram && (
            <a
              href={`https://instagram.com/${site.instagram.replace(/^@/, "")}`}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex min-h-11 items-center text-sage-deep underline-offset-4 transition-colors duration-200 hover:underline"
            >
              {site.instagram}
            </a>
          )}
        </div>
        <p className="mt-2 text-xs text-fog">
          © {new Date().getFullYear()} {site.name} {site.nameSuffix}. All rights reserved.
        </p>
      </div>
    </footer>
  );
}
