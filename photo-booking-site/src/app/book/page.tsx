import type { Metadata } from "next";
import { Suspense } from "react";
import BookingForm from "@/components/BookingForm";

export const metadata: Metadata = {
  title: "Book a Shoot",
};

export default function BookPage() {
  return (
    <div className="mx-auto max-w-2xl px-4 py-12 sm:px-6 sm:py-16">
      {/* Suspense is required because BookingForm reads ?type= from the URL */}
      <Suspense>
        <BookingForm />
      </Suspense>
    </div>
  );
}
