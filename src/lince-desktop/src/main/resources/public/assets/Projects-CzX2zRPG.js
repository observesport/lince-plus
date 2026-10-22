import{bW as w,o as c,n as y,r as p,aH as v,aI as $,ct as m,a1 as N,j as e,a6 as s,cu as n,cv as C,cw as S,cx as k,c6 as F,cy as I,cz as A,cA as L,cB as P,cC as T,cs as D}from"./index-CzurJIzM.js";import{A as E,S as R}from"./AutoForm-2alMts-t.js";import{H as z}from"./HiddenField-XIxlAVYr.js";import{D as l}from"./index-C72raQa3.js";import"./jspdf-CL8MyXL4.js";import"./highcharts-DzzBXQTV.js";import"./codemirror-B-bxm7Or.js";import"./reactplayer-DryCZyrv.js";import"./tensorflow-CTxBjaN0.js";import"./index-DPDMdlKJ.js";const G={type:"object",properties:{key:{title:"Id",type:"string",description:"ID"},researchGroup:{title:"Nombre del grupo",type:"string",description:"Research group name",uniforms:{labelCol:{span:8},label:""}},researchGroupCode:{title:"Código del grupo",type:"string",description:"Research group code",uniforms:{labelCol:{span:8},label:""}},researchName:{title:"Nombre de investigación",type:"string",description:"Research name",uniforms:{labelCol:{span:8},label:""}},researchNameCode:{title:"Código de investigación",type:"string",description:"Research name code",uniforms:{labelCol:{span:8},label:""}},fps:{title:"FPS",type:"integer",description:"Video fps",nullable:!0,minimum:1,maximum:300,uniforms:{labelCol:{span:8},label:""}},userProfiles:{type:"array",items:{title:"Perfil",type:"object",properties:{key:{title:"Id",type:"string",description:"ID",uniforms:{}},registerCode:{title:"Código",type:"string",description:"Código para el registro observacional",uniforms:{labelCol:{span:8}}},userName:{title:"Nombre completo",type:"string",description:"User name",uniforms:{labelCol:{span:8}}},registerAmount:{title:"Observaciones",type:"integer",default:0,uniforms:{disabled:!0}}},required:["userName","registerAmount"]}}},required:["researchGroup","researchGroupCode","researchName","researchNameCode","userProfiles"]},O=()=>w(G),i="lince-project-info",t="lince-pi-observers",V=25,B=`
.${i} .ant-form-item { margin-bottom: 0 !important; }

/* Label + control on one line, at a fixed gutter. */
.${i} .lince-pi-field {
  display: grid;
  grid-template-columns: minmax(120px, 200px) minmax(200px, 460px);
  gap: 12px;
  align-items: center;
  margin-bottom: 8px;
}
.${i} .lince-pi-field > label,
.${i} .lince-pi-field > strong {
  font-weight: 600;
  font-size: 13px;
}
/* label | value | "code" | code — the two belong on one line.
   The "code" track is max-content, not auto: an auto track soaks up all the
   free width of a wide page (the other tracks are capped), which parked the
   code input at the far right edge, a screen away from its label. */
.${i} .lince-pi-field--pair {
  grid-template-columns:
    minmax(120px, 200px)
    minmax(180px, 380px)
    max-content
    minmax(90px, 160px);
}
.${i} .lince-pi-sublabel {
  font-size: 12px;
  color: var(--lince-ink-3);
  white-space: nowrap;
}

/* Observer rows: name | count | delete, aligned under their headers. */
.${i} .lince-pi-observers-head,
.${i} .lince-pi-observer-row {
  display: grid;
  grid-template-columns: minmax(160px, 1fr) minmax(120px, 220px) 40px;
  gap: 12px;
  align-items: center;
}
.${i} .lince-pi-observers-head {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 6px;
}
.${i} .lince-pi-observer-row { margin-bottom: 6px; }

/* The observers ListField's own chrome is noise inside an already-bounded
   section, and its per-item padding insets every row 10px past the header
   above it. Every list rule is scoped to .${t}: other antd Lists
   in this form (the video sources list) keep their own chrome and buttons. */
.${i} .${t} .ant-list-bordered {
  border: 0;
  margin: 0;
  /* !important: antd's own .ant-list-bordered rule sets a 10px inset that
     pushed every observer row 10px right of its column header. */
  padding: 0 !important;
  background: transparent;
}
.${i} .${t} .ant-list-item,
.${i} .${t} .ant-list-items > li {
  padding: 0;
  border: 0;
}
/* uniforms' ListItemField ALWAYS renders its own ListDelField ahead of the
   children (see uniforms-antd/cjs/ListItemField.js), so the explicit one in
   the row grid below is a second copy — two delete buttons per observer, the
   built-in one floating above its row, plus one more from the list itself.
   The row keeps the button that sits in its grid column.

   Every delete button inside the observers list is hidden, then the one
   inside .lince-pi-observer-row is brought back. Position selectors cannot
   separate them: the list renders bare divs, and both buttons carry identical
   markup. Scoped to .${t} so it cannot reach any other list. */
.${i} .${t} .ant-list .ant-btn {
  display: none;
}
/* The row's own delete, and the add control — a DIRECT child of .ant-list
   (uniforms renders no footer element), which is how it is told apart from
   the per-item deletes nested inside the list. */
.${i} .${t} .lince-pi-observer-row .ant-btn,
.${i} .${t} .ant-list > .ant-btn {
  display: inline-flex;
}
/* That add control renders as a full-width teal slab; the design wants a
   compact button under the rows. */
.${i} .${t} .ant-list > .ant-btn {
  width: auto !important;
  min-width: 36px;
  margin-top: 4px;
}
.${i} .lince-pi-section { margin-bottom: 20px; }
.${i} .lince-pi-section:last-child { margin-bottom: 0; }
.${i} .lince-pi-section-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 10px;
}

@media (max-width: 720px) {
  .${i} .lince-pi-field,
  .${i} .lince-pi-field--pair {
    grid-template-columns: 1fr;
    gap: 4px;
  }
  .${i} .lince-pi-observers-head,
  .${i} .lince-pi-observer-row {
    grid-template-columns: 1fr 1fr 40px;
  }
}
`,_=({onSave:h,title:x,showSubmit:u=!0,enableAnalytics:d=!0}={})=>{const a=c(r=>r.lince.profile),b=c(r=>r.lince.status),o=y(),g=p.useMemo(()=>a.fps==null?{...a,fps:V}:a,[a]);p.useEffect(()=>{if(o(v()),o($()),d)try{m.initialize(N.google.analyticsKey),m.send({hitType:"pageview",page:window.location.pathname+window.location.search,title:document.title})}catch(r){console.warn("GA4 initialization failed:",r)}},[o,d]);const f=r=>{o(T(r)),h?.(r)},j=O();return e.jsxs(E,{schema:j,model:g,onSubmit:f,disabled:b==="loading",children:[e.jsx("style",{children:B}),e.jsxs("div",{className:i,children:[e.jsxs("div",{style:{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:16},children:[e.jsx("h2",{style:{margin:0,fontSize:16},children:x||e.jsx(s,{id:"project-info"})}),u&&e.jsx(R,{value:e.jsx(s,{id:"update"})})]}),e.jsx(z,{name:"key"}),e.jsxs("section",{className:"lince-pi-section",children:[e.jsx("h3",{className:"lince-pi-section-title",children:e.jsx(s,{id:"project"})}),e.jsxs("div",{className:"lince-pi-field lince-pi-field--pair",children:[e.jsx("strong",{children:e.jsx(s,{id:"project-group"})}),e.jsx(n,{name:"researchGroupCode",style:{width:"100%"}}),e.jsx("span",{className:"lince-pi-sublabel",children:e.jsx(s,{id:"code"})}),e.jsx(n,{name:"researchGroup",style:{width:"100%"}})]}),e.jsxs("div",{className:"lince-pi-field lince-pi-field--pair",children:[e.jsx("strong",{children:e.jsx(s,{id:"project-name"})}),e.jsx(n,{name:"researchNameCode",style:{width:"100%"}}),e.jsx("span",{className:"lince-pi-sublabel",children:e.jsx(s,{id:"code"})}),e.jsx(n,{name:"researchName",style:{width:"100%"}})]})]}),e.jsx(l,{style:{margin:"16px 0"}}),e.jsxs("section",{className:`lince-pi-section ${t}`,children:[e.jsx("h3",{className:"lince-pi-section-title",children:e.jsx(s,{id:"project-observers"})}),e.jsxs("div",{className:"lince-pi-observers-head",children:[e.jsx("span",{children:e.jsx(s,{id:"username"})}),e.jsx("span",{children:e.jsx(s,{id:"userObservations"})}),e.jsx("span",{})]}),e.jsx(C,{name:"userProfiles",label:"",children:e.jsx(S,{name:"$",children:e.jsx(k,{name:"",label:"",children:e.jsxs("div",{className:"lince-pi-observer-row",children:[e.jsx(n,{name:"userName",label:""}),e.jsx(F,{name:"registerAmount",label:"",disabled:!0}),e.jsx(I,{name:"",type:"primary"})]})})})})]}),e.jsx(l,{style:{margin:"16px 0"}}),e.jsxs("section",{className:"lince-pi-section",children:[e.jsx("h3",{className:"lince-pi-section-title",children:e.jsx(s,{id:"project-settings"})}),e.jsxs("div",{className:"lince-pi-field",children:[e.jsx("span",{children:e.jsx(s,{id:"project-settings-fps"})}),e.jsx(A,{name:"fps",style:{width:160}})]}),e.jsx("small",{className:"text-[12px] text-ink-3",children:e.jsx(s,{id:"project-settings-subtitle"})})]}),L().available&&e.jsxs(e.Fragment,{children:[e.jsx(l,{style:{margin:"16px 0"}}),e.jsx(P,{})]})]})]})},Z=()=>e.jsxs(e.Fragment,{children:[e.jsx(D,{crumbs:[{label:e.jsx(s,{id:"project"})}]}),e.jsx("div",{className:"overflow-hidden rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark",children:e.jsx("div",{style:{padding:"20px"},children:e.jsx(_,{})})})]});export{Z as default};
