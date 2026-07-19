"use client";

import { useMemo, useState } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { AnimatePresence, motion, useReducedMotion } from "framer-motion";
import { addOns, shootTypes, site } from "@/config/site";
import AnimatedCheck from "@/components/AnimatedCheck";

type FormData = {
  shootType: string;
  otherDescription: string;
  date: string;
  time: string;
  name: string;
  email: string;
  phone: string;
  location: string;
  people: string;
  notes: string;
  addOns: string[];
};

type Errors = Partial<Record<keyof FormData, string>>;

const inputClass =
  "w-full min-h-12 rounded-xl border border-line bg-paper px-4 py-3 text-base text-ink placeholder:text-fog/60 transition-colors duration-200 focus:border-sage aria-[invalid=true]:border-red-700";

const labelClass = "mb-1.5 block text-sm font-medium text-ink";

function validate(data: FormData): Errors {
  const errors: Errors = {};
  if (!data.shootType) errors.shootType = "Please choose a shoot type.";
  if (data.shootType === "other" && !data.otherDescription.trim())
    errors.otherDescription = "Please describe the shoot you have in mind.";
  if (!data.date) errors.date = "Please pick a date.";
  if (!data.name.trim()) errors.name = "Please enter your name.";
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email))
    errors.email = "Please enter a valid email address.";
  if (!data.phone.trim()) errors.phone = "Please enter a phone number.";
  return errors;
}

export default function BookingForm() {
  const params = useSearchParams();
  const preselected = params.get("type") ?? "";
  const reduce = useReducedMotion();

  const [data, setData] = useState<FormData>({
    shootType: shootTypes.some((t) => t.slug === preselected) ? preselected : "",
    otherDescription: "",
    date: "",
    time: "",
    name: "",
    email: "",
    phone: "",
    location: "",
    people: "",
    notes: "",
    addOns: [],
  });
  const [errors, setErrors] = useState<Errors>({});
  const [status, setStatus] = useState<"idle" | "sending" | "done" | "error">("idle");
  const [demoMode, setDemoMode] = useState(false);

  const today = useMemo(() => new Date().toISOString().split("T")[0], []);
  const selectedType = shootTypes.find((t) => t.slug === data.shootType);

  const set = <K extends keyof FormData>(key: K, value: FormData[K]) => {
    setData((d) => ({ ...d, [key]: value }));
    // Clear a field's error as soon as the visitor fixes it.
    setErrors((e) => (e[key] ? { ...e, [key]: undefined } : e));
  };

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const nextErrors = validate(data);
    setErrors(nextErrors);
    const firstError = Object.keys(nextErrors).find((k) => nextErrors[k as keyof FormData]);
    if (firstError) {
      document.getElementById(`field-${firstError}`)?.scrollIntoView({
        behavior: reduce ? "auto" : "smooth",
        block: "center",
      });
      return;
    }

    setStatus("sending");

    const payload = {
      _subject: `New booking request: ${selectedType?.name ?? "shoot"} — ${data.name}`,
      shootType:
        data.shootType === "other"
          ? `Other: ${data.otherDescription}`
          : selectedType?.name ?? data.shootType,
      requestedDate: data.date,
      requestedTime: data.time || "Flexible",
      name: data.name,
      email: data.email,
      phone: data.phone,
      location: data.location || "To be decided",
      estimatedPeople: data.people || "Not specified",
      addOns:
        data.addOns.length > 0
          ? data.addOns
              .map((id) => addOns.find((a) => a.id === id)?.name ?? id)
              .join(", ")
          : "None",
      notes: data.notes || "None",
      submittedAt: new Date().toISOString(),
    };

    // Always keep a local copy so no request is ever lost.
    try {
      const existing = JSON.parse(localStorage.getItem("booking-submissions") ?? "[]");
      localStorage.setItem("booking-submissions", JSON.stringify([...existing, payload]));
    } catch {
      // localStorage unavailable (private mode etc.) — not fatal.
    }

    if (site.formspreeId === "YOUR_FORM_ID") {
      // Demo mode: no Formspree ID configured yet.
      console.info("[demo mode] Booking submission:", payload);
      setDemoMode(true);
      setStatus("done");
      window.scrollTo({ top: 0, behavior: reduce ? "auto" : "smooth" });
      return;
    }

    try {
      const res = await fetch(`https://formspree.io/f/${site.formspreeId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error(`Formspree responded ${res.status}`);
      setStatus("done");
      window.scrollTo({ top: 0, behavior: reduce ? "auto" : "smooth" });
    } catch (err) {
      console.error("Booking submission failed:", err);
      setStatus("error");
    }
  }

  /* ── Confirmation screen ─────────────────────────────────── */
  if (status === "done") {
    const summary: [string, string][] = [
      [
        "Shoot type",
        data.shootType === "other"
          ? `Other — ${data.otherDescription}`
          : selectedType?.name ?? "",
      ],
      ["Date", data.date],
      ["Time", data.time || "Flexible"],
      ["Name", data.name],
      ["Email", data.email],
      ["Phone", data.phone],
      ["Location", data.location || "To be decided"],
      ["People", data.people || "Not specified"],
      [
        "Add-ons",
        data.addOns.length
          ? data.addOns.map((id) => addOns.find((a) => a.id === id)?.name ?? id).join(", ")
          : "None",
      ],
      ["Notes", data.notes || "None"],
    ];

    return (
      <motion.div
        initial={reduce ? false : { opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.45, ease: [0.22, 1, 0.36, 1] }}
        className="text-center"
      >
        <div className="flex justify-center">
          <AnimatedCheck />
        </div>
        <h1 className="mt-6 font-display text-4xl font-medium sm:text-5xl">
          Request received!
        </h1>
        <p className="mx-auto mt-4 max-w-md text-fog">
          Thank you, {data.name.split(" ")[0]}. I&apos;ll review your request and get back to
          you within 1–2 business days to confirm details and availability.
        </p>
        {demoMode && (
          <p className="mx-auto mt-4 max-w-md rounded-xl bg-mist px-4 py-3 text-sm text-fog">
            Demo mode: this site isn&apos;t connected to an inbox yet, so the request was
            saved in this browser only. (Site owner: set your Formspree ID in{" "}
            <code>src/config/site.ts</code>.)
          </p>
        )}

        <dl className="mx-auto mt-10 max-w-md divide-y divide-line rounded-2xl border border-line bg-mist/50 text-left">
          {summary.map(([label, value]) => (
            <div key={label} className="flex justify-between gap-4 px-5 py-3">
              <dt className="shrink-0 text-sm font-medium text-fog">{label}</dt>
              <dd className="break-words text-right text-sm">{value}</dd>
            </div>
          ))}
        </dl>

        <div className="mt-10 flex flex-wrap justify-center gap-4">
          <Link
            href="/"
            className="inline-flex min-h-12 items-center rounded-full bg-sage-deep px-7 text-sm font-medium text-white transition-all duration-200 hover:bg-ink active:scale-[0.97]"
          >
            Back to home
          </Link>
          <button
            type="button"
            onClick={() => {
              setStatus("idle");
              setDemoMode(false);
              setData((d) => ({ ...d, shootType: "", otherDescription: "", notes: "" }));
            }}
            className="inline-flex min-h-12 cursor-pointer items-center rounded-full border border-line px-7 text-sm font-medium text-ink transition-colors duration-200 hover:bg-mist"
          >
            Book another shoot
          </button>
        </div>
      </motion.div>
    );
  }

  /* ── The form ────────────────────────────────────────────── */
  return (
    <div>
      <p className="text-xs font-medium uppercase tracking-[0.35em] text-sage-deep">Booking</p>
      <h1 className="mt-3 font-display text-4xl font-medium sm:text-5xl">
        Let&apos;s plan your shoot
      </h1>
      <p className="mt-4 text-fog">
        Fill in what you can — anything you&apos;re unsure about, we&apos;ll figure out
        together.
      </p>

      <form onSubmit={handleSubmit} noValidate className="mt-10 space-y-10">
        {/* Shoot type */}
        <fieldset id="field-shootType">
          <legend className={labelClass}>
            What kind of shoot? <span aria-hidden="true">*</span>
          </legend>
          {errors.shootType && (
            <p role="alert" className="mb-2 text-sm text-red-700">
              {errors.shootType}
            </p>
          )}
          <div className="grid gap-3 sm:grid-cols-2">
            {shootTypes.map((type) => {
              const selected = data.shootType === type.slug;
              return (
                <label
                  key={type.slug}
                  className={`flex min-h-12 cursor-pointer flex-col rounded-xl border p-4 transition-all duration-200 active:scale-[0.99] ${
                    selected
                      ? "border-sage-deep bg-mist shadow-sm"
                      : "border-line bg-paper hover:border-sage"
                  }`}
                >
                  <input
                    type="radio"
                    name="shootType"
                    value={type.slug}
                    checked={selected}
                    onChange={() => set("shootType", type.slug)}
                    className="sr-only"
                  />
                  <span className="flex items-baseline justify-between gap-2">
                    <span className="font-medium">{type.name}</span>
                    <span className="shrink-0 text-xs font-medium text-sage-deep">
                      {type.startingPrice > 0 ? `From $${type.startingPrice}` : "Custom"}
                    </span>
                  </span>
                  <span className="mt-1 text-xs leading-relaxed text-fog">
                    {type.description}
                  </span>
                </label>
              );
            })}
          </div>

          <AnimatePresence initial={false}>
            {data.shootType === "other" && (
              <motion.div
                initial={reduce ? false : { opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: "auto" }}
                exit={reduce ? undefined : { opacity: 0, height: 0 }}
                transition={{ duration: 0.25, ease: "easeOut" }}
                className="overflow-hidden"
              >
                <div className="pt-4" id="field-otherDescription">
                  <label htmlFor="otherDescription" className={labelClass}>
                    Tell me about it <span aria-hidden="true">*</span>
                  </label>
                  <input
                    id="otherDescription"
                    type="text"
                    value={data.otherDescription}
                    onChange={(e) => set("otherDescription", e.target.value)}
                    placeholder="e.g. Pet portraits, product photos, a proposal…"
                    aria-invalid={!!errors.otherDescription}
                    aria-describedby={
                      errors.otherDescription ? "otherDescription-error" : undefined
                    }
                    className={inputClass}
                  />
                  {errors.otherDescription && (
                    <p id="otherDescription-error" role="alert" className="mt-1.5 text-sm text-red-700">
                      {errors.otherDescription}
                    </p>
                  )}
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </fieldset>

        {/* Date & time */}
        <div className="grid gap-5 sm:grid-cols-2">
          <div id="field-date">
            <label htmlFor="date" className={labelClass}>
              Requested date <span aria-hidden="true">*</span>
            </label>
            <input
              id="date"
              type="date"
              min={today}
              value={data.date}
              onChange={(e) => set("date", e.target.value)}
              aria-invalid={!!errors.date}
              aria-describedby={errors.date ? "date-error" : undefined}
              className={inputClass}
            />
            {errors.date && (
              <p id="date-error" role="alert" className="mt-1.5 text-sm text-red-700">
                {errors.date}
              </p>
            )}
          </div>
          <div>
            <label htmlFor="time" className={labelClass}>
              Preferred time <span className="font-normal text-fog">(optional)</span>
            </label>
            <input
              id="time"
              type="time"
              value={data.time}
              onChange={(e) => set("time", e.target.value)}
              className={inputClass}
            />
          </div>
        </div>

        {/* Contact details */}
        <div className="space-y-5">
          <div id="field-name">
            <label htmlFor="name" className={labelClass}>
              Your name <span aria-hidden="true">*</span>
            </label>
            <input
              id="name"
              type="text"
              autoComplete="name"
              value={data.name}
              onChange={(e) => set("name", e.target.value)}
              aria-invalid={!!errors.name}
              aria-describedby={errors.name ? "name-error" : undefined}
              className={inputClass}
            />
            {errors.name && (
              <p id="name-error" role="alert" className="mt-1.5 text-sm text-red-700">
                {errors.name}
              </p>
            )}
          </div>

          <div className="grid gap-5 sm:grid-cols-2">
            <div id="field-email">
              <label htmlFor="email" className={labelClass}>
                Email <span aria-hidden="true">*</span>
              </label>
              <input
                id="email"
                type="email"
                autoComplete="email"
                inputMode="email"
                value={data.email}
                onChange={(e) => set("email", e.target.value)}
                aria-invalid={!!errors.email}
                aria-describedby={errors.email ? "email-error" : undefined}
                className={inputClass}
              />
              {errors.email && (
                <p id="email-error" role="alert" className="mt-1.5 text-sm text-red-700">
                  {errors.email}
                </p>
              )}
            </div>
            <div id="field-phone">
              <label htmlFor="phone" className={labelClass}>
                Phone <span aria-hidden="true">*</span>
              </label>
              <input
                id="phone"
                type="tel"
                autoComplete="tel"
                inputMode="tel"
                value={data.phone}
                onChange={(e) => set("phone", e.target.value)}
                aria-invalid={!!errors.phone}
                aria-describedby={errors.phone ? "phone-error" : undefined}
                className={inputClass}
              />
              {errors.phone && (
                <p id="phone-error" role="alert" className="mt-1.5 text-sm text-red-700">
                  {errors.phone}
                </p>
              )}
            </div>
          </div>

          <div className="grid gap-5 sm:grid-cols-2">
            <div>
              <label htmlFor="location" className={labelClass}>
                Location / venue <span className="font-normal text-fog">(optional)</span>
              </label>
              <input
                id="location"
                type="text"
                value={data.location}
                onChange={(e) => set("location", e.target.value)}
                placeholder="Park, venue, your home…"
                className={inputClass}
              />
            </div>
            <div>
              <label htmlFor="people" className={labelClass}>
                Estimated number of people{" "}
                <span className="font-normal text-fog">(optional)</span>
              </label>
              <input
                id="people"
                type="number"
                min={1}
                inputMode="numeric"
                value={data.people}
                onChange={(e) => set("people", e.target.value)}
                className={inputClass}
              />
            </div>
          </div>

          <div>
            <label htmlFor="notes" className={labelClass}>
              Notes &amp; special requests{" "}
              <span className="font-normal text-fog">(optional)</span>
            </label>
            <textarea
              id="notes"
              rows={4}
              value={data.notes}
              onChange={(e) => set("notes", e.target.value)}
              placeholder="Vision, must-have shots, accessibility needs, anything else…"
              className={inputClass}
            />
          </div>
        </div>

        {/* Add-ons */}
        <fieldset>
          <legend className={labelClass}>Add-ons</legend>
          <div className="space-y-3">
            {addOns.map((addOn) => {
              const checked = data.addOns.includes(addOn.id);
              return (
                <label
                  key={addOn.id}
                  className={`flex min-h-12 cursor-pointer items-start gap-3 rounded-xl border p-4 transition-all duration-200 active:scale-[0.99] ${
                    checked
                      ? "border-sage-deep bg-mist shadow-sm"
                      : "border-line bg-paper hover:border-sage"
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={checked}
                    onChange={(e) =>
                      set(
                        "addOns",
                        e.target.checked
                          ? [...data.addOns, addOn.id]
                          : data.addOns.filter((id) => id !== addOn.id),
                      )
                    }
                    className="mt-1 h-5 w-5 shrink-0 cursor-pointer accent-[#40503c]"
                  />
                  <span>
                    <span className="flex flex-wrap items-baseline gap-x-2">
                      <span className="font-medium">{addOn.name}</span>
                      <span className="text-xs font-medium text-sage-deep">
                        +${addOn.price}
                      </span>
                    </span>
                    <span className="mt-0.5 block text-xs leading-relaxed text-fog">
                      {addOn.description}
                    </span>
                  </span>
                </label>
              );
            })}
          </div>
        </fieldset>

        {/* Submit */}
        <div>
          {status === "error" && (
            <p role="alert" className="mb-4 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-800">
              Something went wrong sending your request. Please try again, or email me
              directly at{" "}
              <a href={`mailto:${site.contactEmail}`} className="font-medium underline">
                {site.contactEmail}
              </a>
              .
            </p>
          )}
          <motion.button
            type="submit"
            disabled={status === "sending"}
            whileTap={reduce ? undefined : { scale: 0.97 }}
            className="inline-flex min-h-14 w-full cursor-pointer items-center justify-center rounded-full bg-sage-deep px-8 text-base font-medium text-white shadow-sm transition-colors duration-200 hover:bg-ink disabled:cursor-wait disabled:opacity-70 sm:w-auto"
          >
            {status === "sending" ? "Sending…" : "Send booking request"}
          </motion.button>
          <p className="mt-3 text-xs text-fog">
            No payment now — this is a request. I&apos;ll confirm availability and details by
            email or phone.
          </p>
        </div>
      </form>
    </div>
  );
}
