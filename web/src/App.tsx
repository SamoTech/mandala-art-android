import { useEffect, useMemo, useState } from "react";
import { designs, type Design } from "./mandala";
import { readArtworks, writeArtwork } from "./storage";

const palettes = ["#E76F51","#F4A261","#E9C46A","#2A9D8F","#264653","#8E6CBB","#D95D9B","#6C8EAD","#A7C957","#F2E9E4","#343A40","#FFFFFF"];
type Locale = "en" | "ar";
type Change = { region: string; before?: string; after?: string };
const copy = {
  en: { tagline:"Find your colors. Follow the pattern. Make it yours.", gallery:"Explore patterns", saved:"Your creations", start:"Start coloring", back:"Back to gallery", undo:"Undo", redo:"Redo", reset:"Reset", export:"Export PNG", colors:"Your palette", all:"All designs", floral:"Floral", geometric:"Geometric", intricate:"Intricate", hint:"Choose a region, then pick a color.", local:"Saved on this device", language:"العربية", confirm:"Clear all colors for this design?" },
  ar: { tagline:"اختار ألوانك، لوّن التصميم، وخليه يعبر عنك.", gallery:"اكتشف التصميمات", saved:"إبداعاتك", start:"ابدأ التلوين", back:"العودة للمعرض", undo:"تراجع", redo:"إعادة", reset:"مسح الألوان", export:"تصدير PNG", colors:"لوحة الألوان", all:"كل التصميمات", floral:"زهري", geometric:"هندسي", intricate:"زخرفي", hint:"اختار جزء من التصميم ثم اختار اللون.", local:"محفوظ على هذا الجهاز", language:"English", confirm:"تمسح كل الألوان من التصميم ده؟" }
};
function downloadBlob(blob: Blob, name: string) {
  const url = URL.createObjectURL(blob), a = document.createElement("a");
  a.href = url; a.download = name; a.click(); URL.revokeObjectURL(url);
}
export default function App() {
  const [locale,setLocale] = useState<Locale>("en");
  const [filter,setFilter] = useState("All");
  const [active,setActive] = useState<Design|null>(null);
  const [colors,setColors] = useState<Record<string,string>>({});
  const [selected,setSelected] = useState(palettes[0]);
  const [history,setHistory] = useState<Change[]>([]);
  const [future,setFuture] = useState<Change[]>([]);
  const [artworks,setArtworks] = useState(readArtworks);
  const t = copy[locale];
  const visible = useMemo(()=> designs.filter(d=>filter==="All" || d.category===filter),[filter]);
  useEffect(()=>{document.documentElement.lang=locale;document.documentElement.dir=locale==="ar"?"rtl":"ltr";},[locale]);
  function openDesign(d: Design) { setActive(d); setColors(artworks[d.id]?.colors ?? {}); setHistory([]); setFuture([]); }
  function paint(region: string) {
    const before=colors[region], after=selected;
    if(before===after) return;
    setColors(old=>({...old,[region]:after}));
    setHistory(old=>[...old,{region,before,after}]); setFuture([]);
  }
  function undo() {
    const change=history.at(-1); if(!change)return;
    setColors(old=>{const next={...old}; if(change.before)next[change.region]=change.before;else delete next[change.region];return next;});
    setHistory(old=>old.slice(0,-1)); setFuture(old=>[...old,change]);
  }
  function redo() {
    const change=future.at(-1); if(!change)return;
    setColors(old=>({...old,[change.region]:change.after}));
    setFuture(old=>old.slice(0,-1)); setHistory(old=>[...old,change]);
  }
  function save() { if(!active)return; writeArtwork(active.id,colors);setArtworks(readArtworks()); }
  function reset() { if(!active || !window.confirm(t.confirm))return; setColors({});setHistory([]);setFuture([]); }
  async function exportPng() {
    const svg=document.querySelector(".mandala-svg") as SVGSVGElement|null; if(!svg)return;
    const clone=svg.cloneNode(true) as SVGSVGElement;
    clone.setAttribute("xmlns","http://www.w3.org/2000/svg");
    const source=new XMLSerializer().serializeToString(clone);
    const url=URL.createObjectURL(new Blob([source],{type:"image/svg+xml;charset=utf-8"}));
    const image=new Image();
    image.onload=()=>{const canvas=document.createElement("canvas");canvas.width=1600;canvas.height=1600;const ctx=canvas.getContext("2d");if(!ctx)return;ctx.fillStyle="#fff";ctx.fillRect(0,0,1600,1600);ctx.drawImage(image,0,0,1600,1600);canvas.toBlob(blob=>{if(blob)downloadBlob(blob,`${active?.id ?? "mandala"}.png`);URL.revokeObjectURL(url);},"image/png");};
    image.onerror=()=>URL.revokeObjectURL(url); image.src=url;
  }
  return <main className="app-shell">
    <header className="topbar"><a className="brand" href="#" onClick={e=>{e.preventDefault();setActive(null);}}><span className="brand-mark">✺</span><span>MANDALA <b>ART</b></span></a><div className="top-actions"><span className="save-note">{t.local}</span><button className="language" onClick={()=>setLocale(locale==="en"?"ar":"en")}>{t.language}</button></div></header>
    {!active ? <section className="gallery-view">
      <div className="hero"><div className="hero-copy"><span className="eyebrow">COLOR · RELAX · CREATE</span><h1>{t.gallery}<br/><em>Make a little space for calm.</em></h1><p>{t.tagline}</p></div><div className="hero-art" aria-hidden="true"><span>✺</span><span>✿</span><span>✧</span></div></div>
      <div className="section-heading"><div><span className="eyebrow">THE COLLECTION</span><h2>{t.gallery}</h2></div><div className="filters">{["All","Floral","Geometric","Intricate"].map(f=><button key={f} className={filter===f?"filter active":"filter"} onClick={()=>setFilter(f)}>{f==="All"?t.all:f==="Floral"?t.floral:f==="Geometric"?t.geometric:t.intricate}</button>)}</div></div>
      <div className="design-grid">{visible.map((d,i)=><button className="design-card" key={d.id} onClick={()=>openDesign(d)}><div className="preview"><MiniMandala design={d} /><span className="card-index">{String(i+1).padStart(2,"0")}</span></div><div className="card-meta"><span><b>{d.title}</b><small>{d.category} · {d.difficulty}</small></span><span className="arrow">↗</span></div></button>)}</div>
      <footer className="footer">MANDALA ART <span>·</span> {t.tagline}</footer>
    </section> : <section className="editor-view">
      <div className="editor-heading"><button className="back-button" onClick={()=>{save();setActive(null);}}>{locale==="ar"?"→":"←"} {t.back}</button><div><span className="eyebrow">COLORING STUDIO</span><h1>{active.title}</h1></div><span className="save-note">{t.local}</span></div>
      <div className="workspace"><div className="canvas-wrap"><Mandala design={active} colors={colors} onPaint={paint}/><p className="canvas-hint">{t.hint}</p></div><aside className="tool-panel"><div className="tool-actions"><button disabled={!history.length} onClick={undo}>{t.undo}</button><button disabled={!future.length} onClick={redo}>{t.redo}</button><button onClick={reset}>{t.reset}</button></div><h3>{t.colors}</h3><div className="palette">{palettes.map(color=><button key={color} aria-label={color} title={color} className={selected===color?"swatch selected":"swatch"} style={{background:color}} onClick={()=>setSelected(color)}/>)}</div><label className="custom-color">Custom color <input type="color" value={selected} onChange={e=>setSelected(e.target.value)}/></label><button className="primary-button" onClick={()=>{save();void exportPng();}}>{t.export}</button><button className="secondary-button" onClick={save}>Save artwork</button><p className="privacy-note">Your artwork stays in this browser on this device. Cloud sync is not part of this preview.</p></aside></div>
    </section>}
  </main>;
}
function Mandala({design,colors,onPaint}:{design:Design;colors:Record<string,string>;onPaint:(id:string)=>void}) {
  return <svg className="mandala-svg" viewBox="0 0 1000 1000" role="img" aria-label={design.title}>{design.regions.map(r=><path key={r.id} d={r.d} fill={colors[r.id]??"#fff"} stroke="#36322f" strokeWidth="3.5" strokeLinejoin="round" onClick={()=>onPaint(r.id)} style={{cursor:"pointer"}}/> )}</svg>;
}
function MiniMandala({design}:{design:Design}) { return <svg viewBox="0 0 1000 1000" aria-hidden="true">{design.regions.map(r=><path key={r.id} d={r.d} fill="#fff" stroke="#77716b" strokeWidth="4" strokeLinejoin="round"/>)}</svg>; }