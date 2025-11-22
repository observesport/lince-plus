import{O as k,Q as S,d as x,ak as O,J as e,R as r,n as i,cv as n,S as w,a1 as l,B as N,cw as T,Z as C,cx as z,cy as A,aP as R}from"./index-BM98bpvU.js";import{F as h}from"./FullscreenBox-BQzYGKhs.js";import{D as m}from"./index-B1eszRPN.js";const p={linceBasic:`print(linceData);
print(linceDataByCategory);
#This is a comment!
df <- data.frame(x = seq(5), y = runif(5));
print(df);
print(attributes(df));
print(is.list(df));`,rSquare:`Square <- function(x) {
  return(x^2);
}
print(Square(16));
print(match(5, c(2,7,5,3))); #  5 is in 3rd place
print(seq(from=1,to=3,by=.5) %in% 1:3);`,rMatrix:`#Para ver los datos con los criterios concatenados con "," utiliza:
print(lince);
#Para ver los datos del análisis y los criterios agrupados por categorías utiliza:
print(linceByCategory);
#Para ver los datos o recorrerlos puedes utilizar lo siguiente,fijate que el nombre de las columnas es case sensitive:
#x[i]
#x[i, j]
#x[[i]]
#x[[i, j]]
#x$a
#x$"a"
print(lince[1,1]);
print(lince[8,'SceneName']);
print(lince[1,'Categories']);`},B=({className:u,style:j})=>{const{t}=k(),c=S(),[s,o]=x.useState({script:p.linceBasic,result:null,loading:!1,activeTab:"body1",basicOptions:{lineNumbers:!0,readOnly:!1,tabSize:4,mode:"r",theme:"zenburn"},snippetOptions:{lineNumbers:!0,readOnly:!0,tabSize:4,mode:"r",theme:"solarized"}});x.useEffect(()=>{c(O())},[c]);const y=async a=>{try{return await(await fetch(z.executeRenjin,{method:"POST",...A(),body:JSON.stringify(a)})).text()}catch(d){return console.error("Error executing R code:",d),alert(d),""}},b=()=>{const a=s.script;o({...s,loading:!0}),y(a).then(d=>{C(t("notification.finished-execution.title"),t("notification.finished-execution.body")),o({...s,result:d,loading:!1})})},g=a=>{o({...s,script:a})},f=[{key:"body1",tab:t("tips-1-title")},{key:"body2",tab:t("tips-2-title")},{key:"body3",tab:t("tips-3-title")},{key:"body4",tab:t("tips-4-title")},{key:"body5",tab:t("tips-5-title")},{key:"body6",tab:t("tips-6-title")},{key:"body7",tab:t("tips-7-title")},{key:"body8",tab:t("tips-8-title")}],v={body1:e.jsxs(r,{gutter:[16,16],children:[e.jsxs(i,{md:12,children:[e.jsx("div",{children:t("tips-1-body")}),e.jsx("br",{}),e.jsx("div",{children:t("tips-1-body-2")})]}),e.jsx(i,{md:12,children:e.jsx(n,{value:p.rSquare,options:s.snippetOptions})})]}),body2:e.jsxs(r,{gutter:[16,16],children:[e.jsxs(i,{md:12,children:[e.jsx("p",{children:t("tips-2-body")}),e.jsx("a",{href:"https://cran.r-project.org/doc/manuals/r-release/R-lang.html#Indexing-matrices-and-arrays",target:"_blank",rel:"noopener noreferrer",children:t("link")})]}),e.jsx(i,{md:12,children:e.jsx(n,{value:p.rMatrix,options:s.snippetOptions})})]}),body3:e.jsx("div",{children:e.jsx("p",{children:t("tips-3-body")})}),body4:e.jsxs(r,{gutter:[16,16],children:[e.jsxs(i,{md:12,children:[t("tips-4-body"),e.jsx(m,{}),e.jsx("pre",{children:e.jsx("code",{children:'install.packages("devtools")'})})]}),e.jsx(i,{md:12,children:e.jsx(n,{value:`library(jsonlite)
hadley_orgs <- fromJSON("https://api.github.com/users/hadley/orgs")
print(hadley_orgs)`,options:s.snippetOptions})})]}),body5:e.jsxs(r,{gutter:[16,16],children:[e.jsx(i,{md:12,children:t("tips-5-body")}),e.jsx(i,{md:12,children:e.jsx(n,{value:`library(jsonlite)
lince_data <- fromJSON("http://localhost:64192/register/get")
print(lince_data)`,options:s.snippetOptions})})]}),body6:e.jsxs(r,{gutter:[16,16],children:[e.jsx(i,{md:12,children:t("tips-6-body")}),e.jsx(i,{md:12,children:e.jsx(n,{value:`df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))
print(df)`,options:s.snippetOptions})})]}),body7:e.jsx("div",{children:e.jsx("p",{children:t("tips-7-body")})}),body8:e.jsxs("div",{children:[e.jsxs(r,{gutter:[16,16],children:[e.jsxs(i,{md:24,children:[t("tips-8-body"),e.jsx(m,{})]}),e.jsx(i,{md:8,children:e.jsx("div",{style:{padding:"10px"},children:t("tips-8-body-1")})}),e.jsx(i,{md:8,children:e.jsx("div",{style:{padding:"10px"},children:t("tips-8-body-2")})}),e.jsx(i,{md:8,children:e.jsx("div",{style:{padding:"10px"},children:t("tips-8-body-3")})})]}),e.jsxs(r,{gutter:[16,16],children:[e.jsx(i,{md:8,children:e.jsx("div",{style:{padding:"10px"},children:e.jsx(n,{value:`## build a graph with 5 nodes
x <- matrix(NA, 5, 5);
diag(x) <- 0;
x[1,2] <- 30; x[1,3] <- 10;
x[2,4] <- 70; x[2,5] <- 40;
x[3,4] <- 50; x[3,5] <- 20;
x[4,5] <- 60;
x[5,4] <- 10;
print(x);
## compute all path lengths
z <- allShortestPaths(x);
print(z);
## the following should give 1 -> 3 -> 5 -> 4
print(extractPath(z, 1, 4));`,options:s.snippetOptions})})}),e.jsx(i,{md:8,children:e.jsx("div",{style:{padding:"10px"},children:e.jsx(n,{value:`A = matrix(c(2, 4, 3, 1, 5, 7),nrow=2,ncol=3,byrow = TRUE);
print(A);`,options:s.snippetOptions})})}),e.jsx(i,{md:8,children:e.jsx("div",{style:{padding:"10px"},children:e.jsx(n,{value:`# the "C" data for Krippendorff
nmm<-matrix(c(1,1,NA,1,2,2,3,2,3,3,3,3,3,3,3,3,2,2,2,2,1,2,3,4,4,4,4,4,1,1,2,1,2,2,2,2,NA,5,5,5,NA,NA,1,1,NA,NA,3,NA),nrow=4);
print(nmm);
# first assume the default nominal classification
print('-------  krippendorf nominal  -------');
aux <- kripp.alpha(nmm);
print(aux);
# now use the same data with the other three methods
print('-------  krippendorf ordinal  -------');
print(kripp.alpha(nmm,'ordinal'));
print('-------  krippendorf interval -------  ');
print(kripp.alpha(nmm,'interval'));
print('-------  krippendorf ratio -------  ');
print(kripp.alpha(nmm,'ratio'));`,options:s.snippetOptions})})})]})]})};return e.jsxs("div",{className:u,style:j,children:[e.jsx("h1",{style:{fontSize:"24px",fontWeight:"bold",marginBottom:"16px"},children:t("r-studio")}),e.jsxs(r,{gutter:[16,16],children:[e.jsx(i,{md:12,sm:12,xs:24,children:e.jsx(h,{title:t("program-your-r-code"),children:e.jsx(w,{spinning:s.loading,children:e.jsx("div",{style:{padding:"16px"},children:e.jsxs(r,{gutter:[16,16],children:[e.jsx(i,{md:20,sm:20,xs:20,children:e.jsxs("div",{children:[e.jsxs("span",{children:[t("r-scope"),": "]}),e.jsx(l,{color:"magenta",children:"ctx"}),e.jsx(l,{color:"magenta",children:"linceData"}),e.jsx(l,{color:"magenta",children:"linceDataByCategory"})]})}),e.jsx(i,{md:4,sm:4,xs:4,children:e.jsx(N,{type:"primary",shape:"round",size:"small",style:{marginBottom:5},onClick:b,children:t("execute")})}),e.jsx(i,{md:24,children:e.jsx(n,{value:s.script,onChange:g,options:s.basicOptions})})]})})})})}),e.jsx(i,{md:12,sm:12,xs:24,children:e.jsx(h,{title:t("your-result"),children:e.jsx("div",{style:{padding:"16px"},children:s.result?e.jsx(n,{value:s.result}):t("r-noResult")})})})]}),e.jsx(r,{style:{marginTop:"16px"},children:e.jsx(i,{md:24,children:e.jsx(T,{style:{width:"100%"},tabList:f,activeTabKey:s.activeTab,onTabChange:a=>{o({...s,activeTab:a})},children:v[s.activeTab]})})})]})},E=()=>e.jsxs(e.Fragment,{children:[e.jsx(R,{pageName:"RStudio"}),e.jsx("div",{className:"overflow-hidden rounded-sm border border-stroke bg-white shadow-default dark:border-strokedark dark:bg-boxdark",children:e.jsx(B,{})})]});export{E as default};
