(this["webpackJsonp@iso/boilerplate"]=this["webpackJsonp@iso/boilerplate"]||[]).push([[33],{1353:function(e,t,n){"use strict";n.r(t);n(353);var a=n(354),r=(n(347),n(348)),i=(n(440),n(491)),o=n(95),l=n(98),c=n(100),s=n(97),u=n(96),p=n(0),d=n.n(p),f=(n(626),n(620)),m=(n(360),n(357)),h=n(2),g=n(61),b=n(513),x=n.n(b),E=n(399),v=n(398),y=[{id:1,title:"Cutting Costs",by:"me",lastOpened:"Aug 7 9:52 AM"},{id:2,title:"Wedding Planner",by:"me",lastOpened:"Sept 14 2:52 PM"},{id:3,title:"Expense Tracker",by:"me",lastOpened:"Sept 12 2:41 PM"},{id:4,title:"Home Brew Water Calculator",by:"me",lastOpened:"Jube 3 5:45 PM"}],O=[{cell:function(){return d.a.createElement(v.a,{icon:E.k,style:{fill:"#43a047"}})},width:"56px",style:{borderBottom:"1px solid #FFFFFF",marginBottom:"-1px"}},{name:"Title",selector:"title",sortable:!0,grow:2,style:{color:"#202124",fontSize:"14px",fontWeight:500}},{name:"Owner",selector:"by",sortable:!0,style:{color:"rgba(0,0,0,.54)"}}],k={headRow:{style:{border:"none"}},headCells:{style:{color:"#202124",fontSize:"14px"}},rows:{highlightOnHoverStyle:{backgroundColor:"rgb(230, 244, 244)",borderBottomColor:"#FFFFFF",borderRadius:"25px",outline:"1px solid #FFFFFF"}},pagination:{style:{border:"none"}}};function j(){return{cell:function(){return d.a.createElement(v.a,{icon:E.k,style:{fill:"#43a047"}})},width:"56px",style:{borderBottom:"1px solid #FFFFFF",marginBottom:"-1px"}}}function w(e,t){return{name:e,selector:t,sortable:!0,grow:2,style:{color:"#202124",fontSize:"14px",fontWeight:500}}}function C(e,t){return{name:e,selector:t,sortable:!0,style:{color:"rgba(0,0,0,.54)"}}}function F(e){return d.a.createElement(x.a,{responsive:!0,dense:e.dense,title:e.title,columns:e.columns?e.columns:O,data:e.data?e.data:y,customStyles:k,highlightOnHover:!0,pointerOnHover:!0})}F.defaultProps={responsive:!0,dense:!0};var R=F,P=n(39);function I(){var e=Object(g.a)(["\n    .ant-card-body{\n        padding-left:0px;\n        padding-right:0px;\n    }\n"]);return I=function(){return e},e}var K=Object(P.default)(f.a)(I()),S=function(e){Object(s.a)(n,e);var t=Object(u.a)(n);function n(e){var a;return Object(o.a)(this,n),(a=t.call(this,e))._renderContingencyTable=a._renderContingencyTable.bind(Object(c.a)(a)),a}return Object(l.a)(n,[{key:"_renderContingencyTable",value:function(e){var t=e.contingencyMatrix[0],n=[j()];t.forEach((function(e,t){n.push(Object(h.a)(Object(h.a)({},C(e,"")),{},{sortable:!1,cell:function(e){return d.a.createElement("span",null,e[t])}}))}));var a=e.contingencyMatrix.slice(1,e.contingencyMatrix.length);return d.a.createElement(R,{title:e.criteria.name,columns:n,data:a})}},{key:"render",value:function(){var e=this;return d.a.createElement(K,{title:this.props.title},this.props.content.map((function(t){return e._renderContingencyTable(t)})))}}]),n}(p.Component);S.defaultProps={title:"Tablas de contingencia",content:[]};var N=S,A=n(1371),_=n(384);function M(){var e=Object(g.a)(["\n  width: 100%;\n  display: flex;\n  align-items: stretch;\n  overflow: hidden;\n  ",";\n\n  .isoIconWrapper {\n    display: flex;\n    align-items: center;\n    justify-content: center;\n    width: 80px;\n    flex-shrink: 0;\n    background-color: rgba(0, 0, 0, 0.1);\n\n    i {\n      font-size: 30px;\n    }\n  }\n\n  .isoContentWrapper {\n    width: 100%;\n    padding: 20px 15px 20px 20px;\n    display: flex;\n    flex-direction: column;\n\n    .isoStatNumber {\n      font-size: 20px;\n      font-weight: 500;\n      line-height: 1.1;\n      margin: 0 0 5px;\n    }\n\n    .isoLabel {\n      font-size: 16px;\n      font-weight: 400;\n      margin: 0;\n      line-height: 1.2;\n    }\n  }\n"]);return M=function(){return e},e}var z=P.default.div(M(),Object(_.a)("5px")),T=function(e){var t=e.fontColor,n=e.bgColor,a=e.width,r=e.icon,i=e.number,o=e.text,l={color:t},c={backgroundColor:n,width:a},s={color:t};return d.a.createElement(z,{className:"isoStickerWidget",style:c},d.a.createElement("div",{className:"isoIconWrapper"},d.a.createElement("i",{className:r,style:s})),d.a.createElement("div",{className:"isoContentWrapper"},d.a.createElement("h3",{className:"isoStatNumber",style:l},i),d.a.createElement("span",{className:"isoLabel",style:l},o)))},W=n(113),B=n(48);function V(){var e=Object(g.a)(["\n  align-content: ",";\n  display: flex;\n  flex-wrap: wrap;\n  flex-direction: row;\n  margin-top: ","px;\n  margin-right: ","px;\n  margin-bottom: ","px;\n  margin-left: ","px;\n  padding: ","px;\n  width: ","px;\n"]);return V=function(){return e},e}function L(){var e=Object(g.a)(["\n  width: 100%;\n  height: ",";\n  padding: ",";\n  background-color: #ffffff;\n  border: 1px solid ",";\n\n  canvas {\n    width: 100% !important;\n    height: 100% !important;\n  }\n"]);return L=function(){return e},e}function q(){var e=Object(g.a)(["\n  margin: 0 10px;\n  width: ","px;\n  margin-top: ","px;\n  margin-right: ","px;\n  margin-bottom: ","px;\n  margin-left: ","px;\n  padding: ",";\n  background-color: ","px;\n  @media only screen and (max-width: 767) {\n    margin-right: 0 !important;\n  }\n"]);return q=function(){return e},e}var D=P.default.div(q(),(function(e){return e.width}),(function(e){return e.gutterTop}),(function(e){return e.gutterRight}),(function(e){return e.gutterBottom}),(function(e){return e.gutterLeft}),(function(e){return e.padding}),(function(e){return e.bgColor})),H=(P.default.div(L(),(function(e){return e.height?"".concat(e.height,"px"):"100%"}),(function(e){return e.padding?e.padding:"30px"}),Object(B.palette)("border",2)),P.default.div(V(),(function(e){return function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:"flex-start";return"start"===e?"flex-start":"end"===e?"flex-end":e}(e.align)}),(function(e){return e.gutterTop}),(function(e){return e.gutterRight}),(function(e){return e.gutterBottom}),(function(e){return e.gutterLeft}),(function(e){return e.padding}),(function(e){return e.width})),function(e){var t=e.children,n=Object(W.a)(e,["children"]);return d.a.createElement(D,Object.assign({className:"isoWidgetsWrapper"},n),t)}),U=n(913),J=function(e){Object(s.a)(n,e);var t=Object(u.a)(n);function n(e){var a;return Object(o.a)(this,n),(a=t.call(this,e)).nonErrValue=a.nonErrValue.bind(Object(c.a)(a)),a}return Object(l.a)(n,[{key:"nonErrValue",value:function(e){return e&&!isNaN(e)?e.toPrecision(6):"-"}},{key:"render",value:function(){var e=this;if(this.props.showResults){if("kappa"===this.props.currentAction.key){var t=[j(),w("Criteria","key"),Object(h.a)(Object(h.a)({},C("Value","valor")),{},{cell:function(t){return d.a.createElement("span",null,e.nonErrValue(t.value))}})];return d.a.createElement(f.a,null,d.a.createElement(R,{title:d.a.createElement(A.a,{i18nKey:"results.kappaResult"},"Resultado de c\xe1lculo del \xedndice Kappa"),columns:t,data:this.props.kappaResult}))}if("kappa-pro"===this.props.currentAction.key){var n=[j(),Object(h.a)(Object(h.a)({},w("Criteria","key")),{},{cell:function(e){return d.a.createElement("span",null,e.criteria.code)}}),Object(h.a)(Object(h.a)({},C("Agreement","agreement")),{},{cell:function(t){return d.a.createElement("span",null,e.nonErrValue(t.agreement))}}),Object(h.a)(Object(h.a)({},C("Expected disagreement","expectedDisagreement")),{},{cell:function(t){return d.a.createElement("span",null,e.nonErrValue(t.expectedDisagreement))}}),Object(h.a)(Object(h.a)({},C("Observed disagreement","observedDisagreement")),{},{cell:function(t){return d.a.createElement("span",null,e.nonErrValue(t.observedDisagreement))}})];return d.a.createElement(f.a,null,d.a.createElement(R,{title:d.a.createElement(A.a,{i18nKey:"results.fleissKappa"},"Resultado de \xcdndice Fleiss\u2019s \u03ba (1971)"),columns:n,data:this.props.kappaProResult}))}return"matrix"===this.props.currentAction.key?d.a.createElement(N,{content:this.props.contingencyMatrix}):d.a.createElement("div",{className:"todo-tasks-container"},d.a.createElement("div",{className:"todo-head"},d.a.createElement("h3",null,d.a.createElement("i",{className:"icon-arrow-left-circle"}),d.a.createElement("span",null,d.a.createElement(A.a,{i18nKey:"results.pressExecute"},"Presiona ejecutar para ver un resultado")))))}var a=[];this.props.registerContent&&(a=this.props.registerContent);var r=null!==a?a.length:0,i=this.props.name;return r>0||""!==i?d.a.createElement(f.a,null,d.a.createElement("h3",null,d.a.createElement("span",{className:"todo-grey"},d.a.createElement(A.a,{i18nKey:"results.observationFrom"},"Observaci\xf3n de"),"\xa0"),i),d.a.createElement("p",{className:"todo-inline"},r," ",d.a.createElement(A.a,{i18nKey:"scenes"},"Escenas")),d.a.createElement(U.a,{register:a,fullMode:!1})):d.a.createElement(f.a,{title:d.a.createElement(A.a,{i18nKey:"results.selectRegister"},"Selecciona un registro a visualizar")},d.a.createElement(H,null,d.a.createElement(T,{text:"Selecciona en el panel izquierdo",number:0,icon:"ion-wand",fontColor:"#ffffff",bgColor:"#42A5F6"})))}}]),n}(p.Component);J.defaultProps={showResults:!1,content:[]};var Q=J,Y=n(49),G=n(368),X=n(414),Z=n(619),$=function(e){Object(s.a)(n,e);var t=Object(u.a)(n);function n(e){var a;return Object(o.a)(this,n),(a=t.call(this,e)).state={expanded:!1},a._onExpand=a._onExpand.bind(Object(c.a)(a)),a.onExpand=a.props.onExpand.bind(Object(c.a)(a)),a}return Object(l.a)(n,[{key:"_onExpand",value:function(){var e=!this.state.expanded;this.setState({expanded:e}),this.onExpand()}},{key:"render",value:function(){var e=this.state.expanded?"#ffffff":"#7ED320",t=this.state.expanded?"#7ED320":"#ffffff",n=this.props.user.userName,i=this.state.expanded?"ion-person":"ion-person-add",o=d.a.createElement(d.a.Fragment,null,d.a.createElement("span",null,n),d.a.createElement(m.a,{onClick:this._onExpand,type:"dashed",size:"small",style:{float:"right"}},"Select"));return d.a.createElement(a.a,{gutter:0,justify:"center"},d.a.createElement(r.a,{md:24,style:{paddingTop:"5px"}},d.a.createElement(H,{onClick:this._onExpand,style:{cursor:"pointer"}},d.a.createElement(T,{number:this.props.user.registerAmount+" scenes",text:o,icon:i,fontColor:e,bgColor:t}))))}}]),n}(p.Component),ee=function(e){Object(s.a)(n,e);var t=Object(u.a)(n);function n(e){var a;return Object(o.a)(this,n),(a=t.call(this,e)).state={users:[],currentUser:"",showResults:!1},a.fetchRegisterById=a.props.fetchRegisterById.bind(Object(c.a)(a)),a._onExpand=a._onExpand.bind(Object(c.a)(a)),a._onActionRequest=a._onActionRequest.bind(Object(c.a)(a)),a}return Object(l.a)(n,[{key:"componentWillMount",value:function(){this.props.fetchProfileInfo()}},{key:"_onActionRequest",value:function(){var e=this,t=[];this.props.profile.userProfiles.forEach((function(n){e.refs["item-"+n.key].state.expanded&&t.push(n.registerCode)})),2===t.length?(Object(X.a)("Observers","Se est\xe1 calculando el \xedndice Kappa de los registros seleccionados"),this.setState({showResults:!0}),"kappa"===this.props.currentAction.key&&this.props.fetchKappaIndex(t[0],t[1]),"kappa-pro"===this.props.currentAction.key&&this.props.fetchKappaProIndex(t[0],t[1]),"matrix"===this.props.currentAction.key&&this.props.fetchContingencyMatrix(t[0],t[1])):Object(X.b)("",d.a.createElement("p",null,"Debes seleccionar 2 registros para poder lanzar los \xedndices kappa.",d.a.createElement("br",null),"Los registros seleccionados tienen el bot\xf3n de color verde y el observador como destacado"))}},{key:"_onExpand",value:function(e){this.props.fetchRegisterById(e.registerCode),this.setState({currentUser:e.userName,showResults:!1})}},{key:"render",value:function(){var e=this,t=[];return this.props.profile.userProfiles&&(t=this.props.profile.userProfiles),console.log(this.props.kappaProIndex),d.a.createElement(a.a,null,d.a.createElement(r.a,{md:24,sm:24,xs:24},d.a.createElement("div",{style:{display:"flex",width:"100%",height:"100%"}},d.a.createElement(Z.a,{defaultSize:{width:"35%",height:"100%"},minWidth:"300",minHeight:"600"},d.a.createElement(f.a,{title:"Participants of the project"},d.a.createElement(f.a,{type:"inner"},d.a.createElement("div",null,d.a.createElement(a.a,null,d.a.createElement(r.a,{md:20},d.a.createElement("p",null,"Found ",t.length,"participants."),d.a.createElement("p",null,"You must select two participants to calculate your Kappa index or contingency matrix"),d.a.createElement("p",null,"Krippendorf allows you 2 or more observers")),d.a.createElement(r.a,{md:4},d.a.createElement(m.a,{onClick:this._onActionRequest},"Execute"))))),t.map((function(t){return d.a.createElement($,{key:t.key,ref:"item-"+t.key,user:t,isActive:!1,onExpand:function(){return e._onExpand(t)}})})))),d.a.createElement("div",{style:{width:"100%"}},d.a.createElement(Q,{ref:"resultsPanel",kappaResult:this.props.kappaIndex,kappaProResult:this.props.kappaProIndex,contingencyMatrix:this.props.contingencyMatrix,showResults:this.state.showResults,currentAction:this.props.currentAction,name:this.state.currentUser,registerContent:this.props.registerTemp})))))}}]),n}(p.Component),te=Object(Y.b)((function(e){return{profile:e.lince.profile,registerTemp:e.lince.registerTemp,kappaIndex:e.lince.kappaIndex,kappaProIndex:e.lince.kappaProIndex,contingencyMatrix:e.lince.contingencyMatrix}}),{fetchProfileInfo:G.h,fetchRegisterById:G.k,fetchKappaIndex:G.f,fetchKappaProIndex:G.g,fetchContingencyMatrix:G.c},null,{forwardRef:!0})(ee),ne=n(111),ae=(n(593),n(436)),re=n(412),ie=n(631),oe=n(377),le=n(410);le.a.initialize(oe.a.google.analyticsKey),le.a.pageview(window.location.pathname+window.location.search);var ce=function(e){Object(s.a)(n,e);var t=Object(u.a)(n);function n(e){var a;return Object(o.a)(this,n),(a=t.call(this,e)).handleClick=function(e){if(a.props.actions){var t={};a.props.actions.map((function(n){return n.key===e.key?t=n:null})),a.setState({active:t}),Object(X.a)(t.label,t.info)}},a.state={active:a.props.actions[0]},a.handleClick=a.handleClick.bind(Object(c.a)(a)),a}return Object(l.a)(n,[{key:"render",value:function(){var e=this.props.actions;return d.a.createElement(Y.a,{store:ne.a},d.a.createElement(ae.c,null,d.a.createElement(ae.b,null,d.a.createElement(re.a,null,"Observers: ",d.a.createElement(A.a,{i18nKey:"calculateResults"},"Calculate results")),d.a.createElement(a.a,null,d.a.createElement(r.a,{span:24},d.a.createElement(ie.b,{selectedKeys:[this.state.active.key],onClick:this.handleClick},e.map((function(e){return d.a.createElement(i.a.Item,{key:e.key}," ",e.label," ")}))))),d.a.createElement(a.a,null,d.a.createElement(r.a,{span:24},d.a.createElement(te,{currentAction:this.state.active}))))))}}]),n}(d.a.Component);ce.defaultProps={actions:[{key:"kappa",label:"Conventional Kappa",info:"El \xedndice Kappa te permite calcular los \xedndices kappa de forma id\xe9ntica a como se hace en la primera versi\xf3n de Lince (2012). \n Es tu responsabilidad que las escenas de observaci\xf3n sean id\xe9nticas."},{key:"kappa-pro",label:"Fleiss Kappa",info:"Kappa pro es el nuevo c\xe1lculo de Lince en base al agoritmo de Fleiss\u2019s Kappa \u03ba (1971). S\xf3lo apto para 2 observadores. \n Es tu responsabilidad que las escenas de observaci\xf3n sean id\xe9nticas."},{key:"matrix",label:"Contingency matrix",info:"Esta funcionalidad te permitir\xe1 generar la tabla de contingencia entre 2 observadores para todos los criterios."}]};t.default=ce},361:function(e,t,n){"use strict";n.d(t,"a",(function(){return i}));var a=n(61);function r(){var e=Object(a.a)(["\n  padding: 40px 20px;\n  display: flex;\n  flex-flow: row wrap;\n  overflow: hidden;\n\n  @media only screen and (max-width: 767px) {\n    padding: 50px 20px;\n  }\n\n  @media (max-width: 580px) {\n    padding: 15px;\n  }\n"]);return r=function(){return e},e}var i=n(39).default.div(r())},374:function(e,t,n){"use strict";var a=n(61),r=n(39),i=n(48);function o(){var e=Object(a.a)(["\n  width: 100%;\n  padding: 35px;\n  background-color: #ffffff;\n  border: 1px solid ",";\n  height: 100%;\n"]);return o=function(){return e},e}var l=r.default.div(o(),Object(i.palette)("border",0));t.a=l},412:function(e,t,n){"use strict";var a=n(0),r=n.n(a),i=n(61),o=n(39),l=n(48),c=n(351);function s(){var e=Object(i.a)(["\n  font-size: 19px;\n  font-weight: 500;\n  color: ",";\n  width: 100%;\n  margin-right: 10px;\n  margin-bottom: 10px;\n  display: flex;\n  align-items: center;\n  white-space: nowrap;\n\n  @media only screen and (max-width: 767px) {\n    margin: 0 10px;\n    margin-bottom: 30px;\n  }\n\n  &:before {\n    content: '';\n    width: 5px;\n    height: 40px;\n    background-color: ",";\n    display: flex;\n    margin: ",";\n  }\n\n  &:after {\n    content: '';\n    width: 100%;\n    height: 1px;\n    background-color: ",";\n    display: flex;\n    margin: ",";\n  }\n"]);return s=function(){return e},e}var u=o.default.h1(s(),Object(l.palette)("secondary",2),Object(l.palette)("secondary",3),(function(e){return"rtl"===e["data-rtl"]?"0 0 0 15px":"0 15px 0 0"}),Object(l.palette)("secondary",3),(function(e){return"rtl"===e["data-rtl"]?"0 15px 0 0":"0 0 0 15px"})),p=Object(c.a)(u);t.a=function(e){return r.a.createElement(p,{className:"isoComponentTitle"},e.children)}},436:function(e,t,n){"use strict";n.d(t,"b",(function(){return d})),n.d(t,"c",(function(){return f})),n.d(t,"a",(function(){return m}));n(347);var a=n(348),r=n(61),i=n(0),o=n.n(i),l=n(39),c=n(374),s=n(361);function u(){var e=Object(r.a)(["\n    text-align: center;\n"]);return u=function(){return e},e}function p(){var e=Object(r.a)(["\n    padding: 5px 5px!important;  \n    .mainContent{\n        padding: 5px 5px!important;\n    }\n"]);return p=function(){return e},e}function d(e){return o.a.createElement(c.a,Object.assign({},e,{className:"mainContent"}))}var f=Object(l.default)(s.a)(p()),m=Object(l.default)(a.a)(u())},593:function(e,t,n){"use strict";var a=n(901),r=n(867),i=n(868),o=n(1079);a.a.use(r.a).use(i.a).use(o.d).init({preload:["es","en"],initImmediate:!1,load:"languageOnly",detection:{lookupQuerystring:"lang"},fallbackLng:{"es-ES":["es"],"en-US":["en"],default:["es"]},debug:!0,react:{wait:!0,bindI18n:"languageChanged loaded"},interpolation:{escapeValue:!1}});a.a},619:function(e,t,n){"use strict";n.d(t,"a",(function(){return l}));var a=n(0),r=n.n(a),i=n(902),o={zIndex:"9999!important",borderRight:"solid 5px #ddd",margin:"5px 5 5 5"};function l(e){return r.a.createElement(i.a,Object.assign({},e,{style:o}))}}}]);
//# sourceMappingURL=33.ac67957f.chunk.js.map