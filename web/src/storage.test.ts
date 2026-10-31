import { beforeEach, describe, expect, it } from "vitest";
import { clearArtwork, readArtworks, writeArtwork } from "./storage";

describe("local artwork storage", () => {
  beforeEach(() => localStorage.clear());
  it("persists colors by design", () => {
    writeArtwork("lotus", { petal: "#ff0000" });
    expect(readArtworks().lotus.colors.petal).toBe("#ff0000");
  });
  it("handles malformed stored data safely", () => {
    localStorage.setItem("mandala-art-web:v1:artworks", "{broken");
    expect(readArtworks()).toEqual({});
  });
  it("clears a selected design without affecting others", () => {
    writeArtwork("one", { a: "#000000" });
    writeArtwork("two", { b: "#ffffff" });
    clearArtwork("one");
    expect(readArtworks().one).toBeUndefined();
    expect(readArtworks().two.colors.b).toBe("#ffffff");
  });
});