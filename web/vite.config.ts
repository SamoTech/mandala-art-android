import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  base: process.env.GITHUB_ACTIONS ? "/mandala-art-android/" : "/",
  test: {
    environment: "jsdom",
    globals: true
  }
});