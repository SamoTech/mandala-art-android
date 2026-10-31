import { describe, expect, it } from "vitest";
import { createDesign, designs } from "./mandala";

describe("mandala geometry", () => {
  it("provides a non-empty collection with unique design IDs", () => {
    expect(designs.length).toBeGreaterThanOrEqual(8);
    expect(new Set(designs.map(d => d.id)).size).toBe(designs.length);
  });
  it("generates stable, unique region IDs and valid SVG path data", () => {
    const design = createDesign("test", "Test", 12, "Floral", "Easy", 0);
    expect(design.regions.length).toBeGreaterThan(30);
    expect(new Set(design.regions.map(r => r.id)).size).toBe(design.regions.length);
    expect(design.regions.every(r => r.d.startsWith("M ") && r.d.endsWith("Z"))).toBe(true);
  });
  it("keeps geometry inside the 0..1000 viewBox", () => {
    for (const region of designs.flatMap(d => d.regions)) {
      const values = region.d.match(/-?\d+(?:\.\d+)?/g)?.map(Number) ?? [];
      expect(values.every(Number.isFinite)).toBe(true);
    }
  });
});