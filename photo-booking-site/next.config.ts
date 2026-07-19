import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Static export: the whole site builds to plain HTML/CSS/JS in `out/`,
  // so it can be hosted free on Vercel, Netlify, Cloudflare Pages, or GitHub Pages.
  output: "export",
  images: {
    // next/image optimization needs a server; static export uses plain <img>-style
    // loading with native lazy-loading instead.
    unoptimized: true,
  },
};

export default nextConfig;
