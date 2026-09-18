import{aM as j,z as l,y as w,d,ae as y,af as v,aN as c,a2 as N,t as e,D as s,aO as r,aP as $,aQ as C,aR as F,aS as I,aT as S,aU as k,aV as A,aK as P}from"./index-D2VTYkoi.js";import{A as D,S as L}from"./AutoForm-CjpJkJge.js";import{H as T}from"./HiddenField-CwupHmQK.js";import{D as p}from"./index-B1iltsGr.js";import"./jspdf-ZESPQhM3.js";import"./highcharts-DzzBXQTV.js";import"./codemirror-DysWCSd_.js";import"./videojs-ClQFrXUN.js";import"./tensorflow-BwidVqjN.js";import"./index--L20Qd-n.js";const R={type:"object",properties:{key:{title:"Id",type:"string",description:"ID"},researchGroup:{title:"Nombre del grupo",type:"string",description:"Research group name",uniforms:{labelCol:{span:8},label:""}},researchGroupCode:{title:"Código del grupo",type:"string",description:"Research group code",uniforms:{labelCol:{span:8},label:""}},researchName:{title:"Nombre de investigación",type:"string",description:"Research name",uniforms:{labelCol:{span:8},label:""}},researchNameCode:{title:"Código de investigación",type:"string",description:"Research name code",uniforms:{labelCol:{span:8},label:""}},fps:{title:"FPS",type:"integer",description:"Video fps",nullable:!0,minimum:1,maximum:300,uniforms:{labelCol:{span:8},label:""}},userProfiles:{type:"array",items:{title:"Perfil",type:"object",properties:{key:{title:"Id",type:"string",description:"ID",uniforms:{}},registerCode:{title:"Código",type:"string",description:"Código para el registro observacional",uniforms:{labelCol:{span:8}}},userName:{title:"Nombre completo",type:"string",description:"User name",uniforms:{labelCol:{span:8}}},registerAmount:{title:"Observaciones",type:"integer",default:0,uniforms:{disabled:!0}}},required:["userName","registerAmount"]}}},required:["researchGroup","researchGroupCode","researchName","researchNameCode","userProfiles"]},z=()=>j(R),i="lince-project-info",G=25,E=`
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
/* label | value | "code" | code — the two belong on one line. */
.${i} .lince-pi-field--pair {
  grid-template-columns:
    minmax(120px, 200px)
    minmax(180px, 380px)
    auto
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

/* The list's own chrome is noise inside an already-bounded section, and its
   per-item padding insets every row 10px past the header above it. */
.${i} .ant-list-bordered {
  border: 0;
  margin: 0;
  /* !important: antd's own .ant-list-bordered rule sets a 10px inset that
     pushed every observer row 10px right of its column header. */
  padding: 0 !important;
  background: transparent;
}
.${i} .ant-list-item,
.${i} .ant-list-items > li {
  padding: 0;
  border: 0;
}
/* uniforms' ListItemField ALWAYS renders its own ListDelField ahead of the
   children (see uniforms-antd/cjs/ListItemField.js), so the explicit one in
   the row grid below is a second copy — two delete buttons per observer, the
   built-in one floating above its row, plus one more from the list itself.
   The row keeps the button that sits in its grid column.

   Every delete button inside the list is hidden, then the one inside
   .lince-pi-observer-row is brought back. Position selectors cannot separate
   them: the list renders bare divs, and both buttons carry identical markup. */
.${i} .ant-list .ant-btn {
  display: none;
}
/* The row's own delete, and the add control — a DIRECT child of .ant-list
   (uniforms renders no footer element), which is how it is told apart from
   the per-item deletes nested inside the list. */
.${i} .lince-pi-observer-row .ant-btn,
.${i} .ant-list > .ant-btn {
  display: inline-flex;
}
/* That add control renders as a full-width teal slab; the design wants a
   compact button under the rows. */
.${i} .ant-list > .ant-btn {
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
`,O=({onSave:m,title:h,showSubmit:x=!0,enableAnalytics:o=!0}={})=>{const n=l(t=>t.lince.profile),u=l(t=>t.lince.status),a=w(),b=d.useMemo(()=>n.fps==null?{...n,fps:G}:n,[n]);d.useEffect(()=>{if(a(y()),a(v()),o)try{c.initialize(N.google.analyticsKey),c.send({hitType:"pageview",page:window.location.pathname+window.location.search,title:document.title})}catch(t){console.warn("GA4 initialization failed:",t)}},[a,o]);const g=t=>{a(A(t)),m?.(t)},f=z();return e.jsxs(D,{schema:f,model:b,onSubmit:g,disabled:u==="loading",children:[e.jsx("style",{children:E}),e.jsxs("div",{className:i,children:[e.jsxs("div",{style:{display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:16},children:[e.jsx("h2",{style:{margin:0,fontSize:16},children:h||e.jsx(s,{id:"project-info"})}),x&&e.jsx(L,{value:e.jsx(s,{id:"update"})})]}),e.jsx(T,{name:"key"}),e.jsxs("section",{className:"lince-pi-section",children:[e.jsx("h3",{className:"lince-pi-section-title",children:e.jsx(s,{id:"project"})}),e.jsxs("div",{className:"lince-pi-field lince-pi-field--pair",children:[e.jsx("strong",{children:e.jsx(s,{id:"project-group"})}),e.jsx(r,{name:"researchGroupCode",style:{width:"100%"}}),e.jsx("span",{className:"lince-pi-sublabel",children:e.jsx(s,{id:"code"})}),e.jsx(r,{name:"researchGroup",style:{width:"100%"}})]}),e.jsxs("div",{className:"lince-pi-field lince-pi-field--pair",children:[e.jsx("strong",{children:e.jsx(s,{id:"project-name"})}),e.jsx(r,{name:"researchNameCode",style:{width:"100%"}}),e.jsx("span",{className:"lince-pi-sublabel",children:e.jsx(s,{id:"code"})}),e.jsx(r,{name:"researchName",style:{width:"100%"}})]})]}),e.jsx(p,{style:{margin:"16px 0"}}),e.jsxs("section",{className:"lince-pi-section",children:[e.jsx("h3",{className:"lince-pi-section-title",children:e.jsx(s,{id:"project-observers"})}),e.jsxs("div",{className:"lince-pi-observers-head",children:[e.jsx("span",{children:e.jsx(s,{id:"username"})}),e.jsx("span",{children:e.jsx(s,{id:"userObservations"})}),e.jsx("span",{})]}),e.jsx($,{name:"userProfiles",label:"",children:e.jsx(C,{name:"$",children:e.jsx(F,{name:"",label:"",children:e.jsxs("div",{className:"lince-pi-observer-row",children:[e.jsx(r,{name:"userName",label:""}),e.jsx(I,{name:"registerAmount",label:"",disabled:!0}),e.jsx(S,{name:"",type:"primary"})]})})})})]}),e.jsx(p,{style:{margin:"16px 0"}}),e.jsxs("section",{className:"lince-pi-section",children:[e.jsx("h3",{className:"lince-pi-section-title",children:e.jsx(s,{id:"project-settings"})}),e.jsxs("div",{className:"lince-pi-field",children:[e.jsx("span",{children:e.jsx(s,{id:"project-settings-fps"})}),e.jsx(k,{name:"fps",style:{width:"100%"}})]}),e.jsx("small",{className:"text-[12px] text-ink-3",children:e.jsx(s,{id:"project-settings-subtitle"})})]})]})]})},Y=()=>e.jsxs(e.Fragment,{children:[e.jsx(P,{crumbs:[{label:e.jsx(s,{id:"project"})}]}),e.jsx("div",{className:"overflow-hidden rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark",children:e.jsx("div",{style:{padding:"20px"},children:e.jsx(O,{})})})]});export{Y as default};
