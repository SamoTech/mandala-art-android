export type Region = { id: string; d: string };
export type Design = { id: string; title: string; category: "Floral" | "Geometric" | "Intricate"; difficulty: "Easy" | "Medium" | "Hard"; regions: Region[] };

const point = (r: number, a: number) => ({ x: 500 + r * Math.cos(a), y: 500 + r * Math.sin(a) });
const polar = (r: number, degrees: number) => point(r, degrees * Math.PI / 180);
const nfmt = (v: number) => Number(v.toFixed(2));
function petal(id: string, inner: number, outer: number, angle: number, half: number): Region {
  const a = angle * Math.PI / 180, p0 = point(inner, a), p1 = point(outer, a);
  const c1 = point((inner + outer) / 2, a - half), c2 = point((inner + outer) / 2, a + half);
  return { id, d: `M ${nfmt(p0.x)} ${nfmt(p0.y)} Q ${nfmt(c1.x)} ${nfmt(c1.y)} ${nfmt(p1.x)} ${nfmt(p1.y)} Q ${nfmt(c2.x)} ${nfmt(c2.y)} ${nfmt(p0.x)} ${nfmt(p0.y)} Z` };
}
function sector(id: string, inner: number, outer: number, start: number, sweep: number): Region {
  const a0 = polar(inner, start), b0 = polar(outer, start), b1 = polar(outer, start + sweep), a1 = polar(inner, start + sweep);
  const large = sweep > 180 ? 1 : 0;
  return { id, d: `M ${nfmt(a0.x)} ${nfmt(a0.y)} L ${nfmt(b0.x)} ${nfmt(b0.y)} A ${outer} ${outer} 0 ${large} 1 ${nfmt(b1.x)} ${nfmt(b1.y)} L ${nfmt(a1.x)} ${nfmt(a1.y)} A ${inner} ${inner} 0 ${large} 0 ${nfmt(a0.x)} ${nfmt(a0.y)} Z` };
}
function circle(id: string, r: number, angle = 0): Region {
  const p = polar(r, angle);
  return { id, d: `M ${nfmt(p.x-r/2)} ${nfmt(p.y)} a ${r/2} ${r/2} 0 1 0 ${r} 0 a ${r/2} ${r/2} 0 1 0 -${r} 0` };
}
export function createDesign(id: string, title: string, symmetry: number, category: Design["category"], difficulty: Design["difficulty"], style: number): Design {
  const step = 360 / symmetry, regions: Region[] = [];
  regions.push({ id: id + "-center", d: "M 500 462 A 38 38 0 1 0 500 538 A 38 38 0 1 0 500 462 Z" });
  for (let i=0;i<symmetry;i++) {
    const a=i*step;
    if (style % 3 === 0) regions.push(petal(`${id}-inner-${i}`, 48, 148, a, Math.PI/symmetry*.82));
    else regions.push(sector(`${id}-inner-${i}`, 48, 145, a-step*.36, step*.72));
    regions.push(petal(`${id}-outer-${i}`, 160, 330, a+step/2, Math.PI/symmetry*.78));
    regions.push(sector(`${id}-rim-${i}`, 345, 405, a, step));
    regions.push(circle(`${id}-bead-${i}`, 22, a));
  }
  regions.push({id:id+"-outer-ring",d:"M 500 70 A 430 430 0 1 0 500 930 A 430 430 0 1 0 500 70 Z M 500 92 A 408 408 0 1 1 500 908 A 408 408 0 1 1 500 92 Z"});
  return { id, title, category, difficulty, regions };
}
export const designs: Design[] = [
  createDesign("lotus-bloom","Lotus Bloom",12,"Floral","Easy",0),
  createDesign("sunburst-harmony","Sunburst Harmony",16,"Geometric","Medium",1),
  createDesign("mystic-daisy","Mystic Daisy",10,"Floral","Easy",2),
  createDesign("sacred-geometry","Sacred Geometry",18,"Geometric","Hard",1),
  createDesign("celestial-dahlia","Celestial Dahlia",14,"Floral","Medium",0),
  createDesign("arabesque-rosette","Arabesque Rosette",20,"Intricate","Hard",2),
  createDesign("zen-wheel","Zen Wheel",12,"Geometric","Easy",1),
  createDesign("golden-peacock","Golden Peacock",16,"Intricate","Hard",0)
];