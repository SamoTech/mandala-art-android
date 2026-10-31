export type Artwork = { colors: Record<string, string>; updatedAt: number };
const KEY = "mandala-art-web:v1:artworks";
export function readArtworks(): Record<string, Artwork> {
  try {
    const raw = localStorage.getItem(KEY);
    if (!raw) return {};
    const value: unknown = JSON.parse(raw);
    return value && typeof value === "object" && !Array.isArray(value) ? value as Record<string, Artwork> : {};
  } catch { return {}; }
}
export function writeArtwork(id: string, colors: Record<string, string>): void {
  const all = readArtworks();
  all[id] = { colors, updatedAt: Date.now() };
  localStorage.setItem(KEY, JSON.stringify(all));
}
export function clearArtwork(id: string): void {
  const all = readArtworks();
  delete all[id];
  localStorage.setItem(KEY, JSON.stringify(all));
}