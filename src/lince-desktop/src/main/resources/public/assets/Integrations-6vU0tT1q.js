import{r as d,T as A,U as B,X as u,j as e,aA as T,aB as p,aC as I,aD as g,aE as S,aF as V,aG as L,aH as $,aI as q,aJ as P,aK as U,D,a as F,b as G,q as H,t as M,a1 as z,aL as Y,P as J}from"./index-D20h2lUZ.js";import"./jspdf-D6gHRtnR.js";import"./highcharts-DzzBXQTV.js";import"./codemirror-_NMEFjJJ.js";import"./reactplayer-DC1yCZAv.js";import"./tensorflow-6OrZI9nG.js";import"./index-DkFWLdT-.js";var K={icon:{tag:"svg",attrs:{viewBox:"64 64 896 896",focusable:"false"},children:[{tag:"path",attrs:{d:"M516 673c0 4.4 3.4 8 7.5 8h185c4.1 0 7.5-3.6 7.5-8v-48c0-4.4-3.4-8-7.5-8h-185c-4.1 0-7.5 3.6-7.5 8v48zm-194.9 6.1l192-161c3.8-3.2 3.8-9.1 0-12.3l-192-160.9A7.95 7.95 0 00308 351v62.7c0 2.4 1 4.6 2.9 6.1L420.7 512l-109.8 92.2a8.1 8.1 0 00-2.9 6.1V673c0 6.8 7.9 10.5 13.1 6.1zM880 112H144c-17.7 0-32 14.3-32 32v736c0 17.7 14.3 32 32 32h736c17.7 0 32-14.3 32-32V144c0-17.7-14.3-32-32-32zm-40 728H184V184h656v656z"}}]},name:"code",theme:"outlined"},W=function(t,r){return d.createElement(A,B({},t,{ref:r,icon:K}))},X=d.forwardRef(W);const w=["r","python","http"],Q=["install","fetch","chart","compare"],E={uuid1:"<UUID_1>",uuid2:"<UUID_2>"},Z={r:"r",python:"python",http:"shell"},x=`# Lince's rule (/register/kappa): rows are paired by position, the
# comparison stops at the shorter register, and a criterion with no category
# in a row counts as its own empty value.`,ee={install:()=>'install.packages(c("jsonlite", "irr"))  # irr only for the optional comparison',fetch:({baseUrl:s,uuid1:t,uuid2:r,second:a})=>`library(jsonlite)

# Constants from the Integrations page: the address changes on every launch
base       <- "${s}"
observer_1 <- "${t}"
${a(`observer_2 <- "${r}"`)}

get_json <- function(path) fromJSON(paste0(base, path), simplifyVector = FALSE)

profile <- get_json("/profile/get")$data[[1]]
tool    <- get_json("/rest/categories/getAll")$data
# Unnamed observers come back with a null userName
field <- function(items, key) sapply(items, function(x) if (is.null(x[[key]])) "" else x[[key]])
observers <- data.frame(name = field(profile$userProfiles, "userName"),
                        uuid = field(profile$userProfiles, "registerCode"))
print(observers)

register_1 <- get_json(paste0("/register/get/", observer_1))
cat(length(register_1), "observations\\n")
${a('register_2 <- get_json(paste0("/register/get/", observer_2))  # for the comparison')}`,chart:()=>`# One row per observed category: video time, criterion, category
criteria <- Filter(function(x) !isTRUE(x$category), tool)
criterion_name <- setNames(sapply(criteria, \`[[\`, "name"), sapply(criteria, \`[[\`, "id"))
events <- do.call(rbind, lapply(Filter(function(row) length(row$register) > 0, register_1), function(row) {
  data.frame(time = row$videoTime,
             criterion = criterion_name[as.character(field(row$register, "parent"))],
             category = field(row$register, "name"))
}))
head(events)

# Observations per category
par(mar = c(4, 10, 2, 1))
barplot(sort(table(events$category)), horiz = TRUE, las = 1, main = "Observations per category")

# Observations along the video, one line per category
categories <- factor(events$category)
plot(events$time, as.integer(categories), pch = 16, xaxt = "n", yaxt = "n", xlab = "video time", ylab = "",
     main = "Observations along the video")
ticks <- pretty(events$time)
axis(1, at = ticks, labels = sprintf("%02d:%02d", ticks %/% 60, round(ticks %% 60)))
axis(2, at = seq_along(levels(categories)), labels = levels(categories), las = 1)`,compare:()=>`# Needs two observers: register_2 from the fetch stage
library(irr)

criteria <- Filter(function(x) !isTRUE(x$category), tool)

${x}
column <- function(register, criterion) {
  vapply(register, function(row) {
    hit <- Filter(function(c) identical(c$parent, criterion$id), row$register)
    if (length(hit) == 0) "" else hit[[1]]$name
  }, character(1))
}
n <- min(length(register_1), length(register_2))

local <- t(sapply(criteria, function(criterion) {
  a <- column(register_1, criterion)[seq_len(n)]
  b <- column(register_2, criterion)[seq_len(n)]
  c(kappa = kappa2(cbind(a, b))$value,
    alpha = kripp.alpha(rbind(a, b), method = "nominal")$value)
}))
rownames(local) <- sapply(criteria, \`[[\`, "name")

lince <- get_json(paste0("/register/kappa/", observer_1, "/", observer_2))
local <- cbind(local, lince_kappa = sapply(rownames(local), function(name) {
  hit <- Filter(function(k) k$key == name, lince)
  if (length(hit) == 0) NA else hit[[1]]$value
}))
print(round(local, 3))`},te={install:()=>`python -m pip install requests pandas matplotlib numpy scikit-learn krippendorff
# scikit-learn and krippendorff only for the optional comparison`,fetch:({baseUrl:s,uuid1:t,uuid2:r,second:a})=>`import requests
import pandas as pd

# Constants from the Integrations page: the address changes on every launch
BASE = "${s}"
OBSERVER_1 = "${t}"
${a(`OBSERVER_2 = "${r}"`)}

get = lambda path: requests.get(BASE + path, timeout=30).json()

profile = get("/profile/get")["data"][0]
tool = get("/rest/categories/getAll")["data"]
observers = pd.DataFrame(
    [{"name": o["userName"], "uuid": o["registerCode"]} for o in profile["userProfiles"]]
)
print(observers)

register_1 = get(f"/register/get/{OBSERVER_1}")
print(len(register_1), "observations")
${a('register_2 = get(f"/register/get/{OBSERVER_2}")  # for the comparison')}`,chart:()=>`import matplotlib.pyplot as plt

# One row per observed category: video time, criterion, category
criterion_name = {c["id"]: c["name"] for c in tool if not c["category"]}
events = pd.DataFrame([
    {"time": row["videoTime"], "criterion": criterion_name.get(c["parent"]), "category": c["name"]}
    for row in register_1 for c in row["register"]
])
print(events.head())

events["category"].value_counts().sort_values().plot.barh(title="Observations per category")
plt.tight_layout()
plt.show()

from matplotlib.ticker import FuncFormatter

plt.scatter(events["time"], events["category"])
plt.gca().xaxis.set_major_formatter(FuncFormatter(lambda s, _: f"{int(s // 60):02d}:{int(s % 60):02d}"))
plt.xlabel("video time")
plt.title("Observations along the video")
plt.tight_layout()
plt.show()`,compare:()=>`# Needs two observers: register_2 from the fetch stage
import krippendorff
import numpy as np
from sklearn.metrics import cohen_kappa_score

criteria = [c for c in tool if not c["category"]]


${x}
def column(register, criterion):
    return [
        next((c["name"] for c in row["register"] if c["parent"] == criterion["id"]), "")
        for row in register
    ]


n = min(len(register_1), len(register_2))
rows = []
for criterion in criteria:
    a, b = column(register_1, criterion)[:n], column(register_2, criterion)[:n]
    codes = {value: i for i, value in enumerate(sorted(set(a) | set(b)))}
    coded = np.array([[codes[v] for v in a], [codes[v] for v in b]])
    rows.append({
        "criterion": criterion["name"],
        "kappa": cohen_kappa_score(a, b),
        "alpha": krippendorff.alpha(reliability_data=coded, level_of_measurement="nominal"),
    })
local = pd.DataFrame(rows).set_index("criterion")

lince = pd.Series(
    {k["key"]: k["value"] for k in get(f"/register/kappa/{OBSERVER_1}/{OBSERVER_2}")}, name="lince_kappa"
)
print(local.join(lince).round(3))`},re={install:()=>`# Any HTTP client works; these examples use curl and jq.
# macOS:          brew install curl jq
# Debian/Ubuntu:  sudo apt install curl jq`,fetch:({baseUrl:s,uuid1:t,uuid2:r,second:a})=>`# Constants from the Integrations page: the address changes on every launch
BASE="${s}"
OBSERVER_1="${t}"
${a(`OBSERVER_2="${r}"`)}

# Observers and their register UUIDs
curl -s "$BASE/profile/get" | jq '.data[0].userProfiles[] | {name: .userName, uuid: .registerCode}'
# Observation tool: criteria (category == false) and their categories (parent = criterion id)
curl -s "$BASE/rest/categories/getAll" | jq '.data[] | select(.category == false) | .name'
# One observer's register: rows with videoTime and the categories marked per criterion
curl -s "$BASE/register/get/$OBSERVER_1" | jq '.[0]'
${a(`curl -s "$BASE/register/get/$OBSERVER_2" | jq 'length'  # for the comparison`)}`,chart:()=>`# Observations per category, most frequent first
curl -s "$BASE/register/get/$OBSERVER_1" \\
  | jq -r '[.[].register[].name] | group_by(.) | map({category: .[0], n: length}) | sort_by(-.n) | .[] | "\\(.n)\\t\\(.category)"'

# Video time (hh:mm:ss) of every observation of one category
curl -s "$BASE/register/get/$OBSERVER_1" | jq -r '.[] | select(any(.register[]; .name == "<CATEGORY>")) | .videoTimeTxt'`,compare:()=>`# Needs two observers' register codes (OBSERVER_2 from the fetch stage)
# Cohen's kappa per criterion as Lince computes it; "Total" is the mean.
${x}
curl -s "$BASE/register/kappa/$OBSERVER_1/$OBSERVER_2" | jq -r '.[] | "\\(.key)\\t\\(.value)"'

# Contingency matrix per criterion
curl -s "$BASE/register/matrix/$OBSERVER_1/$OBSERVER_2" | jq '.'`},se={r:ee,python:te,http:re},ie=(s,t,r)=>se[s][t]({baseUrl:r.baseUrl,uuid1:r.uuid1||E.uuid1,uuid2:r.uuid2||E.uuid2,second:a=>r.uuid2?a:`# ${a}`}),j="rounded-field px-2 py-0.5 font-mono text-[12px] text-ink bg-surface-chip",f=({text:s,label:t})=>{const{t:r}=u(),[a,l]=d.useState(!1);return e.jsxs("button",{className:$({tone:"neutralOutline"}),"aria-label":t,onClick:()=>{navigator.clipboard?.writeText(s),l(!0),window.setTimeout(()=>l(!1),1500)},children:[e.jsx(L,{"aria-hidden":!0}),a?r("integrations.code.copied"):t]})},ae=({index:s,stage:t,language:r,code:a})=>{const{t:l}=u(),[n,m]=d.useState(!1);return e.jsxs("li",{className:p(S,"p-4"),children:[e.jsxs("div",{className:"flex cursor-pointer flex-wrap items-start gap-3",onClick:()=>m(o=>!o),children:[e.jsx("span",{className:p(P,"font-semibold"),"aria-hidden":!0,children:s+1}),e.jsxs("div",{className:"min-w-0 flex-1",children:[e.jsx("h3",{className:U,children:l(`integrations.stages.${t}.title`)}),e.jsx("p",{className:p(g,"mt-0.5"),children:l(`integrations.stages.${t}.description`)})]}),e.jsxs("div",{className:"flex shrink-0 items-center gap-2",onClick:o=>o.stopPropagation(),children:[e.jsxs("button",{className:$({tone:"tealOutline"}),"aria-expanded":n,onClick:()=>m(o=>!o),children:[e.jsx(X,{"aria-hidden":!0}),l(n?"integrations.code.hide":"integrations.code.show")]}),e.jsx(f,{text:a,label:l("integrations.code.copy")})]})]}),n&&e.jsx("div",{className:"mt-3 overflow-hidden rounded-field [&_.CodeMirror]:h-auto [&_.CodeMirror]:text-[12px] [&_.CodeMirror]:leading-[1.5]",children:e.jsx(D,{value:a,options:{readOnly:!0,lineNumbers:!0,lineWrapping:!0,mode:Z[r],theme:"zenburn"}},`${r}-${t}-${a.length}`)})]})},ne=({baseUrl:s,observers:t,language:r,onLanguageChange:a,className:l})=>{const{t:n}=u(),[m,o]=d.useState([]),h=i=>t.find(c=>c.uuid===m[i])??t[i],O=h(0)?.uuid,R=h(1)?.uuid,k={baseUrl:s,uuid1:O,uuid2:R},b=i=>i.name||n("integrations.observers.unnamed",{n:t.indexOf(i)+1}),C=t.map(i=>({value:i.uuid,label:b(i)})),v=i=>{const c=h(i);return c?e.jsxs("li",{className:"flex flex-wrap items-center gap-2 text-ui-sm text-ink",children:[e.jsx("span",{className:"font-semibold",children:n("integrations.observers.slot",{n:i+1})}),t.length>1?e.jsx(q,{size:"small","aria-label":n("integrations.observers.slot",{n:i+1}),value:c.uuid,options:C,onChange:_=>o(y=>i===0?[_,y[1]]:[y[0],_]),className:"min-w-40"}):e.jsx("span",{children:b(c)}),e.jsx("code",{className:j,children:c.uuid}),e.jsx(f,{text:c.uuid,label:n("integrations.observers.copy")})]}):null};return e.jsxs("div",{className:p(V,"overflow-hidden",l),children:[e.jsxs("header",{className:p("flex flex-wrap items-start justify-between gap-3 border-0 border-b border-solid px-4 py-4 md:px-6",I),children:[e.jsxs("div",{className:"min-w-0",children:[e.jsx("h2",{className:"m-0 text-title-xsm font-semibold text-ink",children:n("integrations.page.title")}),e.jsx("p",{className:"m-0 mt-1 text-ui-sm text-ink-3",children:n("integrations.page.description")})]}),e.jsx(T,{"aria-label":n("integrations.language"),value:r,onChange:i=>a(i),options:w.map(i=>({value:i,label:n(`integrations.tabs.${i}`)}))})]}),e.jsxs("div",{className:"flex flex-col gap-4 px-4 py-4 md:px-6",children:[e.jsxs("div",{className:p(S,"grid gap-4 p-4 md:grid-cols-2"),children:[e.jsxs("div",{className:"flex flex-col gap-2",children:[e.jsxs("div",{className:"flex flex-wrap items-center gap-2",children:[e.jsx("span",{className:"text-ui-sm font-semibold text-ink",children:n("integrations.base.title")}),e.jsx("code",{className:j,children:s}),e.jsx(f,{text:s,label:n("integrations.base.copy")})]}),e.jsx("p",{className:g,children:n("integrations.base.description")})]}),e.jsxs("div",{children:[e.jsx("span",{className:"text-ui-sm font-semibold text-ink",children:n("integrations.observers.title")}),t.length===0?e.jsx("p",{className:p(g,"mt-1"),children:n("integrations.observers.empty")}):e.jsxs("ul",{className:"m-0 mt-1 flex list-none flex-col gap-1 p-0",children:[v(0),v(1)]}),t.length===1&&e.jsx("p",{className:p(g,"mt-1"),children:n("integrations.observers.few")})]})]}),e.jsx("ol",{className:"m-0 flex list-none flex-col gap-3 p-0",children:Q.map((i,c)=>e.jsx(ae,{index:c,stage:i,language:r,code:ie(r,i,k)},`${r}-${i}`))})]})]})},N="lince-integrations-tab",oe=()=>{try{const s=window.localStorage.getItem(N);return w.includes(s)?s:"r"}catch{return"r"}},le=({className:s})=>{const t=F(),r=G(o=>o.lince?.profile?.userProfiles),[a,l]=d.useState(oe);d.useEffect(()=>{t(H())},[t]);const n=o=>{l(o);try{window.localStorage.setItem(N,o)}catch{}},m=(r??[]).filter(o=>M(o?.registerCode)).map(o=>({name:(o.userName??"").trim(),uuid:o.registerCode}));return e.jsx(ne,{className:s,baseUrl:z.url.API_BASE_URL||window.location.origin,observers:m,language:a,onLanguageChange:n})},fe=()=>{const{t:s}=u();return Y()?e.jsxs(e.Fragment,{children:[e.jsx(J,{crumbs:[{label:s("menu.integrations")}]}),e.jsx(le,{})]}):null};export{fe as default};
