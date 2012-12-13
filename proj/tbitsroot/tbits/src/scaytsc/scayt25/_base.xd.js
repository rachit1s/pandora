/*
    Copyright (c) 2004-2009, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/

/*
    This is a compiled version of Dojo, built for deployment and not for
    development. To get an editable version, please visit:

        http://dojotoolkit.org

    for documentation and information on getting the source.
*/

sc_dojo._xdResourceLoaded(function(_1,_2,_3){
return {depends:[["provide","scayt._base"],["provide","dojox.collections._base"],["provide","dojox.collections.Dictionary"],["provide","dojo.regexp"],["provide","dojo.cookie"],["provide","dojo.date.stamp"],["provide","dojo.parser"],["provide","dojo.io.script"],["provide","dijit._base.focus"],["provide","dijit._base.manager"],["provide","dojo.AdapterRegistry"],["provide","dijit._base.place"],["provide","dijit._base.window"],["provide","dijit._base.popup"],["provide","dijit._base.scroll"],["provide","dijit._base.sniff"],["provide","dijit._base.typematic"],["provide","dijit._base.wai"],["provide","dijit._base"],["provide","dijit._Widget"],["require","dijit._base"],["provide","dojo.string"],["provide","dijit._Templated"],["provide","dijit._Container"],["provide","dijit._Contained"],["provide","dijit.layout._LayoutWidget"],["provide","dijit.form._FormWidget"],["provide","dijit.dijit"],["provide","dijit._editor.range"],["provide","scayt"],["provide","scayt.banner"]],defineResource:function(_4,_5,_6){
_4.provide("scayt._base");
if(!_4._hasResource["dojox.collections._base"]){
_4._hasResource["dojox.collections._base"]=true;
_4.provide("dojox.collections._base");
_6.collections.DictionaryEntry=function(k,v){
this.key=k;
this.value=v;
this.valueOf=function(){
return this.value;
};
this.toString=function(){
return String(this.value);
};
};
_6.collections.Iterator=function(_9){
var a=_9;
var _b=0;
this.element=a[_b]||null;
this.atEnd=function(){
return (_b>=a.length);
};
this.get=function(){
if(this.atEnd()){
return null;
}
this.element=a[_b++];
return this.element;
};
this.map=function(fn,_d){
return _4.map(a,fn,_d);
};
this.reset=function(){
_b=0;
this.element=a[_b];
};
};
_6.collections.DictionaryIterator=function(_e){
var a=[];
var _10={};
for(var p in _e){
if(!_10[p]){
a.push(_e[p]);
}
}
var _12=0;
this.element=a[_12]||null;
this.atEnd=function(){
return (_12>=a.length);
};
this.get=function(){
if(this.atEnd()){
return null;
}
this.element=a[_12++];
return this.element;
};
this.map=function(fn,_14){
return _4.map(a,fn,_14);
};
this.reset=function(){
_12=0;
this.element=a[_12];
};
};
}
if(!_4._hasResource["dojox.collections.Dictionary"]){
_4._hasResource["dojox.collections.Dictionary"]=true;
_4.provide("dojox.collections.Dictionary");
_6.collections.Dictionary=function(_15){
var _16={};
this.count=0;
var _17={};
this.add=function(k,v){
var b=(k in _16);
_16[k]=new _6.collections.DictionaryEntry(k,v);
if(!b){
this.count++;
}
};
this.clear=function(){
_16={};
this.count=0;
};
this.clone=function(){
return new _6.collections.Dictionary(this);
};
this.contains=this.containsKey=function(k){
if(_17[k]){
return false;
}
return (_16[k]!=null);
};
this.containsValue=function(v){
var e=this.getIterator();
while(e.get()){
if(e.element.value==v){
return true;
}
}
return false;
};
this.entry=function(k){
return _16[k];
};
this.forEach=function(fn,_20){
var a=[];
for(var p in _16){
if(!_17[p]){
a.push(_16[p]);
}
}
_4.forEach(a,fn,_20);
};
this.getKeyList=function(){
return (this.getIterator()).map(function(_23){
return _23.key;
});
};
this.getValueList=function(){
return (this.getIterator()).map(function(_24){
return _24.value;
});
};
this.item=function(k){
if(k in _16){
return _16[k].valueOf();
}
return undefined;
};
this.getIterator=function(){
return new _6.collections.DictionaryIterator(_16);
};
this.remove=function(k){
if(k in _16&&!_17[k]){
delete _16[k];
this.count--;
return true;
}
return false;
};
if(_15){
var e=_15.getIterator();
while(e.get()){
this.add(e.element.key,e.element.value);
}
}
};
}
if(!_4._hasResource["dojo.regexp"]){
_4._hasResource["dojo.regexp"]=true;
_4.provide("dojo.regexp");
_4.regexp.escapeString=function(str,_29){
return str.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g,function(ch){
if(_29&&_29.indexOf(ch)!=-1){
return ch;
}
return "\\"+ch;
});
};
_4.regexp.buildGroupRE=function(arr,re,_2d){
if(!(arr instanceof Array)){
return re(arr);
}
var b=[];
for(var i=0;i<arr.length;i++){
b.push(re(arr[i]));
}
return _4.regexp.group(b.join("|"),_2d);
};
_4.regexp.group=function(_30,_31){
return "("+(_31?"?:":"")+_30+")";
};
}
if(!_4._hasResource["dojo.cookie"]){
_4._hasResource["dojo.cookie"]=true;
_4.provide("dojo.cookie");
_4.cookie=function(_32,_33,_34){
var c=document.cookie;
if(arguments.length==1){
var _36=c.match(new RegExp("(?:^|; )"+_4.regexp.escapeString(_32)+"=([^;]*)"));
return _36?decodeURIComponent(_36[1]):undefined;
}else{
_34=_34||{};
var exp=_34.expires;
if(typeof exp=="number"){
var d=new Date();
d.setTime(d.getTime()+exp*24*60*60*1000);
exp=_34.expires=d;
}
if(exp&&exp.toUTCString){
_34.expires=exp.toUTCString();
}
_33=encodeURIComponent(_33);
var _39=_32+"="+_33,_3a;
for(_3a in _34){
_39+="; "+_3a;
var _3b=_34[_3a];
if(_3b!==true){
_39+="="+_3b;
}
}
document.cookie=_39;
}
};
_4.cookie.isSupported=function(){
if(!("cookieEnabled" in navigator)){
this("__djCookieTest__","CookiesAllowed");
navigator.cookieEnabled=this("__djCookieTest__")=="CookiesAllowed";
if(navigator.cookieEnabled){
this("__djCookieTest__","",{expires:-1});
}
}
return navigator.cookieEnabled;
};
}
if(!_4._hasResource["dojo.date.stamp"]){
_4._hasResource["dojo.date.stamp"]=true;
_4.provide("dojo.date.stamp");
_4.date.stamp.fromISOString=function(_3c,_3d){
if(!_4.date.stamp._isoRegExp){
_4.date.stamp._isoRegExp=/^(?:(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(.\d+)?)?((?:[+-](\d{2}):(\d{2}))|Z)?)?$/;
}
var _3e=_4.date.stamp._isoRegExp.exec(_3c);
var _3f=null;
if(_3e){
_3e.shift();
if(_3e[1]){
_3e[1]--;
}
if(_3e[6]){
_3e[6]*=1000;
}
if(_3d){
_3d=new Date(_3d);
_4.map(["FullYear","Month","Date","Hours","Minutes","Seconds","Milliseconds"],function(_40){
return _3d["get"+_40]();
}).forEach(function(_41,_42){
if(_3e[_42]===undefined){
_3e[_42]=_41;
}
});
}
_3f=new Date(_3e[0]||1970,_3e[1]||0,_3e[2]||1,_3e[3]||0,_3e[4]||0,_3e[5]||0,_3e[6]||0);
var _43=0;
var _44=_3e[7]&&_3e[7].charAt(0);
if(_44!="Z"){
_43=((_3e[8]||0)*60)+(Number(_3e[9])||0);
if(_44!="-"){
_43*=-1;
}
}
if(_44){
_43-=_3f.getTimezoneOffset();
}
if(_43){
_3f.setTime(_3f.getTime()+_43*60000);
}
}
return _3f;
};
_4.date.stamp.toISOString=function(_45,_46){
var _=function(n){
return (n<10)?"0"+n:n;
};
_46=_46||{};
var _49=[];
var _4a=_46.zulu?"getUTC":"get";
var _4b="";
if(_46.selector!="time"){
var _4c=_45[_4a+"FullYear"]();
_4b=["0000".substr((_4c+"").length)+_4c,_(_45[_4a+"Month"]()+1),_(_45[_4a+"Date"]())].join("-");
}
_49.push(_4b);
if(_46.selector!="date"){
var _4d=[_(_45[_4a+"Hours"]()),_(_45[_4a+"Minutes"]()),_(_45[_4a+"Seconds"]())].join(":");
var _4e=_45[_4a+"Milliseconds"]();
if(_46.milliseconds){
_4d+="."+(_4e<100?"0":"")+_(_4e);
}
if(_46.zulu){
_4d+="Z";
}else{
if(_46.selector!="time"){
var _4f=_45.getTimezoneOffset();
var _50=Math.abs(_4f);
_4d+=(_4f>0?"-":"+")+_(Math.floor(_50/60))+":"+_(_50%60);
}
}
_49.push(_4d);
}
return _49.join("T");
};
}
if(!_4._hasResource["dojo.parser"]){
_4._hasResource["dojo.parser"]=true;
_4.provide("dojo.parser");
_4.parser=new function(){
var d=_4;
var _52=d._scopeName+"Type";
var a=scaytConfig&&!scaytConfig.isDojoAlreadyLoaded&&d._scopeName!="dojo";
var qry="["+_52+"]"+(a?",[dojoType]":"");
var _55=0,_56={};
var _57=function(_58,_59){
var nso=_59||_56;
if(_4.isIE){
var cn=_58["__dojoNameCache"];
if(cn&&nso[cn]===_58){
return cn;
}
}
var _5c;
do{
_5c="__"+_55++;
}while(_5c in nso);
nso[_5c]=_58;
return _5c;
};
function _5d(_5e){
if(d.isString(_5e)){
return "string";
}
if(typeof _5e=="number"){
return "number";
}
if(typeof _5e=="boolean"){
return "boolean";
}
if(d.isFunction(_5e)){
return "function";
}
if(d.isArray(_5e)){
return "array";
}
if(_5e instanceof Date){
return "date";
}
if(_5e instanceof d._Url){
return "url";
}
return "object";
};
function _5f(_60,_61){
switch(_61){
case "string":
return _60;
case "number":
return _60.length?Number(_60):NaN;
case "boolean":
return typeof _60=="boolean"?_60:!(_60.toLowerCase()=="false");
case "function":
if(d.isFunction(_60)){
_60=_60.toString();
_60=d.trim(_60.substring(_60.indexOf("{")+1,_60.length-1));
}
try{
if(_60.search(/[^\w\.]+/i)!=-1){
_60=_57(new Function(_60),this);
}
return d.getObject(_60,false);
}
catch(e){
return new Function();
}
case "array":
return _60?_60.split(/\s*,\s*/):[];
case "date":
switch(_60){
case "":
return new Date("");
case "now":
return new Date();
default:
return d.date.stamp.fromISOString(_60);
}
case "url":
return d.baseUrl+_60;
default:
return d.fromJson(_60);
}
};
var _62={};
function _63(_64){
if(!_62[_64]){
var cls=d.getObject(_64);
if(!d.isFunction(cls)){
throw new Error("Could not load class '"+_64+"'. Did you spell the name correctly and use a full path, like 'dijit.form.Button'?");
}
var _66=cls.prototype;
var _67={},_68={};
for(var _69 in _66){
if(_69.charAt(0)=="_"){
continue;
}
if(_69 in _68){
continue;
}
var _6a=_66[_69];
_67[_69]=_5d(_6a);
}
_62[_64]={cls:cls,params:_67};
}
return _62[_64];
};
this._functionFromScript=function(_6b){
var _6c="";
var _6d="";
var _6e=_6b.getAttribute("args");
if(_6e){
d.forEach(_6e.split(/\s*,\s*/),function(_6f,idx){
_6c+="var "+_6f+" = arguments["+idx+"]; ";
});
}
var _71=_6b.getAttribute("with");
if(_71&&_71.length){
d.forEach(_71.split(/\s*,\s*/),function(_72){
_6c+="with("+_72+"){";
_6d+="}";
});
}
return new Function(_6c+_6b.innerHTML+_6d);
};
this.instantiate=function(_73,_74){
var _75=[];
_74=_74||{};
d.forEach(_73,function(_76){
if(!_76){
return;
}
var _77=_52 in _74?_74[_52]:_76.getAttribute(_52);
if(!_77||!_77.length){
return;
}
var _78=_63(_77),_79=_78.cls,ps=_79._noScript||_79.prototype._noScript;
var _7b={},_7c=_76.attributes;
for(var _7d in _78.params){
var _7e=_7d in _74?{value:_74[_7d],specified:true}:_7c.getNamedItem(_7d);
if(!_7e||(!_7e.specified&&(!_4.isIE||_7d.toLowerCase()!="value"))){
continue;
}
var _7f=_7e.value;
switch(_7d){
case "class":
_7f="className" in _74?_74.className:_76.className;
break;
case "style":
_7f="style" in _74?_74.style:(_76.style&&_76.style.cssText);
}
var _80=_78.params[_7d];
if(typeof _7f=="string"){
_7b[_7d]=_5f(_7f,_80);
}else{
_7b[_7d]=_7f;
}
}
if(!ps){
var _81=[],_82=[];
d.query("> script[type^='dojo/']",_76).orphan().forEach(function(_83){
var _84=_83.getAttribute("event"),_77=_83.getAttribute("type"),nf=d.parser._functionFromScript(_83);
if(_84){
if(_77=="dojo/connect"){
_81.push({event:_84,func:nf});
}else{
_7b[_84]=nf;
}
}else{
_82.push(nf);
}
});
}
var _86=_79["markupFactory"];
if(!_86&&_79["prototype"]){
_86=_79.prototype["markupFactory"];
}
var _87=_86?_86(_7b,_76,_79):new _79(_7b,_76);
_75.push(_87);
var _88=_76.getAttribute("jsId");
if(_88){
d.setObject(_88,_87);
}
if(!ps){
d.forEach(_81,function(_89){
d.connect(_87,_89.event,null,_89.func);
});
d.forEach(_82,function(_8a){
_8a.call(_87);
});
}
});
d.forEach(_75,function(_8b){
if(_8b&&_8b.startup&&!_8b._started&&(!_8b.getParent||!_8b.getParent())){
_8b.startup();
}
});
return _75;
};
this.parse=function(_8c){
var _8d=d.query(qry,_8c);
if(a){
_8d.forEach(function(_8e){
if(!_4.hasAttr(_8e,String(_52))){
var _8f=_4.attr(_8e,"dojoType");
var obj={};
obj[_52]=_8f;
_4.attr(_8e,obj);
}
});
}
var _91=this.instantiate(_8d);
return _91;
};
}();
(function(){
var _92=function(){
if(_4.config["parseOnLoad"]==true){
_4.parser.parse();
}
};
if(_4.exists("dijit.wai.onload")&&(_5.wai.onload===_4._loaders[0])){
_4._loaders.splice(1,0,_92);
}else{
_4._loaders.unshift(_92);
}
})();
}
if(!_4._hasResource["dojo.io.script"]){
_4._hasResource["dojo.io.script"]=true;
_4.provide("dojo.io.script");
_4.io.script={get:function(_93){
var dfd=this._makeScriptDeferred(_93);
var _95=dfd.ioArgs;
_4._ioAddQueryToUrl(_95);
if(this._canAttach(_95)){
this.attach(_95.id,_95.url,_93.frameDoc);
}
_4._ioWatch(dfd,this._validCheck,this._ioCheck,this._resHandle);
return dfd;
},attach:function(id,url,_98){
var doc=(_98||_4.doc);
var _9a=doc.createElement("script");
_9a.type="text/javascript";
_9a.src=url;
_9a.id=id;
_9a.charset="utf-8";
doc.getElementsByTagName("head")[0].appendChild(_9a);
},remove:function(id,_9c){
_4.destroy(_4.byId(id,_9c));
if(this["jsonp_"+id]){
delete this["jsonp_"+id];
}
},_makeScriptDeferred:function(_9d){
var dfd=_4._ioSetArgs(_9d,this._deferredCancel,this._deferredOk,this._deferredError);
var _9f=dfd.ioArgs;
_9f.id=_4._scopeName+"IoScript"+(this._counter++);
_9f.canDelete=false;
if(_9d.callbackParamName){
_9f.query=_9f.query||"";
if(_9f.query.length>0){
_9f.query+="&";
}
_9f.query+=_9d.callbackParamName+"="+(_9d.frameDoc?"parent.":"")+_4._scopeName+".io.script.jsonp_"+_9f.id+"._jsonpCallback";
_9f.frameDoc=_9d.frameDoc;
_9f.canDelete=true;
dfd._jsonpCallback=this._jsonpCallback;
this["jsonp_"+_9f.id]=dfd;
}
return dfd;
},_deferredCancel:function(dfd){
dfd.canceled=true;
if(dfd.ioArgs.canDelete){
_4.io.script._addDeadScript(dfd.ioArgs);
}
},_deferredOk:function(dfd){
if(dfd.ioArgs.canDelete){
_4.io.script._addDeadScript(dfd.ioArgs);
}
if(dfd.ioArgs.json){
return dfd.ioArgs.json;
}else{
return dfd.ioArgs;
}
},_deferredError:function(_a2,dfd){
if(dfd.ioArgs.canDelete){
if(_a2.dojoType=="timeout"){
_4.io.script.remove(dfd.ioArgs.id,dfd.ioArgs.frameDoc);
}else{
_4.io.script._addDeadScript(dfd.ioArgs);
}
}
return _a2;
},_deadScripts:[],_counter:1,_addDeadScript:function(_a4){
_4.io.script._deadScripts.push({id:_a4.id,frameDoc:_a4.frameDoc});
_a4.frameDoc=null;
},_validCheck:function(dfd){
var _a6=_4.io.script;
var _a7=_a6._deadScripts;
if(_a7&&_a7.length>0){
for(var i=0;i<_a7.length;i++){
_a6.remove(_a7[i].id,_a7[i].frameDoc);
_a7[i].frameDoc=null;
}
_4.io.script._deadScripts=[];
}
return true;
},_ioCheck:function(dfd){
if(dfd.ioArgs.json){
return true;
}
var _aa=dfd.ioArgs.args.checkString;
if(_aa&&eval("typeof("+_aa+") != 'undefined'")){
return true;
}
return false;
},_resHandle:function(dfd){
if(_4.io.script._ioCheck(dfd)){
dfd.callback(dfd);
}else{
dfd.errback(new Error("inconceivable dojo.io.script._resHandle error"));
}
},_canAttach:function(_ac){
return true;
},_jsonpCallback:function(_ad){
this.ioArgs.json=_ad;
}};
}
if(!_4._hasResource["dijit._base.focus"]){
_4._hasResource["dijit._base.focus"]=true;
_4.provide("dijit._base.focus");
_4.mixin(_5,{_curFocus:null,_prevFocus:null,isCollapsed:function(){
var _ae=_4.doc;
if(_ae.selection){
var s=_ae.selection;
if(s.type=="Text"){
return !s.createRange().htmlText.length;
}else{
return !s.createRange().length;
}
}else{
var _b0=_4.global;
var _b1=_b0.getSelection();
if(_4.isString(_b1)){
return !_b1;
}else{
return !_b1||_b1.isCollapsed||!_b1.toString();
}
}
},getBookmark:function(){
var _b2,_b3=_4.doc.selection;
if(_b3){
var _b4=_b3.createRange();
if(_b3.type.toUpperCase()=="CONTROL"){
if(_b4.length){
_b2=[];
var i=0,len=_b4.length;
while(i<len){
_b2.push(_b4.item(i++));
}
}else{
_b2=null;
}
}else{
_b2=_b4.getBookmark();
}
}else{
if(window.getSelection){
_b3=_4.global.getSelection();
if(_b3){
_b4=_b3.getRangeAt(0);
_b2=_b4.cloneRange();
}
}else{
console.warn("No idea how to store the current selection for this browser!");
}
}
return _b2;
},moveToBookmark:function(_b7){
var _b8=_4.doc;
if(_b8.selection){
var _b9;
if(_4.isArray(_b7)){
_b9=_b8.body.createControlRange();
_4.forEach(_b7,function(n){
_b9.addElement(n);
});
}else{
_b9=_b8.selection.createRange();
_b9.moveToBookmark(_b7);
}
_b9.select();
}else{
var _bb=_4.global.getSelection&&_4.global.getSelection();
if(_bb&&_bb.removeAllRanges){
_bb.removeAllRanges();
_bb.addRange(_b7);
}else{
console.warn("No idea how to restore selection for this browser!");
}
}
},getFocus:function(_bc,_bd){
return {node:_bc&&_4.isDescendant(_5._curFocus,_bc.domNode)?_5._prevFocus:_5._curFocus,bookmark:!_4.withGlobal(_bd||_4.global,_5.isCollapsed)?_4.withGlobal(_bd||_4.global,_5.getBookmark):null,openedForWindow:_bd};
},focus:function(_be){
if(!_be){
return;
}
var _bf="node" in _be?_be.node:_be,_c0=_be.bookmark,_c1=_be.openedForWindow;
if(_bf){
var _c2=(_bf.tagName.toLowerCase()=="iframe")?_bf.contentWindow:_bf;
if(_c2&&_c2.focus){
try{
_c2.focus();
}
catch(e){
}
}
_5._onFocusNode(_bf);
}
if(_c0&&_4.withGlobal(_c1||_4.global,_5.isCollapsed)){
if(_c1){
_c1.focus();
}
try{
_4.withGlobal(_c1||_4.global,_5.moveToBookmark,null,[_c0]);
}
catch(e){
}
}
},_activeStack:[],registerIframe:function(_c3){
_5.registerWin(_c3.contentWindow,_c3);
},registerWin:function(_c4,_c5){
_4.connect(_c4.document,"onmousedown",function(evt){
_5._justMouseDowned=true;
setTimeout(function(){
_5._justMouseDowned=false;
},0);
_5._onTouchNode(_c5||evt.target||evt.srcElement);
});
var doc=_c4.document;
if(doc){
if(_4.isIE){
doc.attachEvent("onactivate",function(evt){
if(evt.srcElement.tagName.toLowerCase()!="#document"){
_5._onFocusNode(_c5||evt.srcElement);
}
});
doc.attachEvent("ondeactivate",function(evt){
_5._onBlurNode(_c5||evt.srcElement);
});
}else{
doc.addEventListener("focus",function(evt){
_5._onFocusNode(_c5||evt.target);
},true);
doc.addEventListener("blur",function(evt){
_5._onBlurNode(_c5||evt.target);
},true);
}
}
doc=null;
},_onBlurNode:function(_cc){
_5._prevFocus=_5._curFocus;
_5._curFocus=null;
if(_5._justMouseDowned){
return;
}
if(_5._clearActiveWidgetsTimer){
clearTimeout(_5._clearActiveWidgetsTimer);
}
_5._clearActiveWidgetsTimer=setTimeout(function(){
delete _5._clearActiveWidgetsTimer;
_5._setStack([]);
_5._prevFocus=null;
},100);
},_onTouchNode:function(_cd){
if(_5._clearActiveWidgetsTimer){
clearTimeout(_5._clearActiveWidgetsTimer);
delete _5._clearActiveWidgetsTimer;
}
var _ce=[];
try{
while(_cd){
if(_cd.dijitPopupParent){
_cd=_5.byId(_cd.dijitPopupParent).domNode;
}else{
if(_cd.tagName&&_cd.tagName.toLowerCase()=="body"){
if(_cd===_4.body()){
break;
}
_cd=_5.getDocumentWindow(_cd.ownerDocument).frameElement;
}else{
var id=_cd.getAttribute&&_cd.getAttribute("widgetId");
if(id){
_ce.unshift(id);
}
_cd=_cd.parentNode;
}
}
}
}
catch(e){
}
_5._setStack(_ce);
},_onFocusNode:function(_d0){
if(!_d0){
return;
}
if(_d0.nodeType==9){
return;
}
_5._onTouchNode(_d0);
if(_d0==_5._curFocus){
return;
}
if(_5._curFocus){
_5._prevFocus=_5._curFocus;
}
_5._curFocus=_d0;
_4.publish("focusNode",[_d0]);
},_setStack:function(_d1){
var _d2=_5._activeStack;
_5._activeStack=_d1;
for(var _d3=0;_d3<Math.min(_d2.length,_d1.length);_d3++){
if(_d2[_d3]!=_d1[_d3]){
break;
}
}
for(var i=_d2.length-1;i>=_d3;i--){
var _d5=_5.byId(_d2[i]);
if(_d5){
_d5._focused=false;
_d5._hasBeenBlurred=true;
if(_d5._onBlur){
_d5._onBlur();
}
if(_d5._setStateClass){
_d5._setStateClass();
}
_4.publish("widgetBlur",[_d5]);
}
}
for(i=_d3;i<_d1.length;i++){
_d5=_5.byId(_d1[i]);
if(_d5){
_d5._focused=true;
if(_d5._onFocus){
_d5._onFocus();
}
if(_d5._setStateClass){
_d5._setStateClass();
}
_4.publish("widgetFocus",[_d5]);
}
}
}});
_4.addOnLoad(function(){
_5.registerWin(window);
});
}
if(!_4._hasResource["dijit._base.manager"]){
_4._hasResource["dijit._base.manager"]=true;
_4.provide("dijit._base.manager");
_4.declare("dijit.WidgetSet",null,{constructor:function(){
this._hash={};
},add:function(_d6){
if(this._hash[_d6.id]){
throw new Error("Tried to register widget with id=="+_d6.id+" but that id is already registered");
}
this._hash[_d6.id]=_d6;
},remove:function(id){
delete this._hash[id];
},forEach:function(_d8){
for(var id in this._hash){
_d8(this._hash[id]);
}
},filter:function(_da){
var res=new _5.WidgetSet();
this.forEach(function(_dc){
if(_da(_dc)){
res.add(_dc);
}
});
return res;
},byId:function(id){
return this._hash[id];
},byClass:function(cls){
return this.filter(function(_df){
return _df.declaredClass==cls;
});
}});
_5.registry=new _5.WidgetSet();
_5._widgetTypeCtr={};
_5.getUniqueId=function(_e0){
var id;
do{
id=_e0+"_"+(_e0 in _5._widgetTypeCtr?++_5._widgetTypeCtr[_e0]:_5._widgetTypeCtr[_e0]=0);
}while(_5.byId(id));
return id;
};
_5.findWidgets=function(_e2){
var _e3=[];
function _e4(_e5){
var _e6=_4.isIE?_e5.children:_e5.childNodes,i=0,_e8;
while(_e8=_e6[i++]){
if(_e8.nodeType!=1){
continue;
}
var _e9=_e8.getAttribute("widgetId");
if(_e9){
var _ea=_5.byId(_e9);
_e3.push(_ea);
}else{
_e4(_e8);
}
}
};
_e4(_e2);
return _e3;
};
if(_4.isIE){
_4.addOnWindowUnload(function(){
_4.forEach(_5.findWidgets(_4.body()),function(_eb){
if(_eb.destroyRecursive){
_eb.destroyRecursive();
}else{
if(_eb.destroy){
_eb.destroy();
}
}
});
});
}
_5.byId=function(id){
return (_4.isString(id))?_5.registry.byId(id):id;
};
_5.byNode=function(_ed){
return _5.registry.byId(_ed.getAttribute("widgetId"));
};
_5.getEnclosingWidget=function(_ee){
while(_ee){
if(_ee.getAttribute&&_ee.getAttribute("widgetId")){
return _5.registry.byId(_ee.getAttribute("widgetId"));
}
_ee=_ee.parentNode;
}
return null;
};
_5._tabElements={area:true,button:true,input:true,object:true,select:true,textarea:true};
_5._isElementShown=function(_ef){
var _f0=_4.style(_ef);
return (_f0.visibility!="hidden")&&(_f0.visibility!="collapsed")&&(_f0.display!="none")&&(_4.attr(_ef,"type")!="hidden");
};
_5.isTabNavigable=function(_f1){
if(_4.hasAttr(_f1,"disabled")){
return false;
}
var _f2=_4.hasAttr(_f1,"tabindex");
var _f3=_4.attr(_f1,"tabindex");
if(_f2&&_f3>=0){
return true;
}
var _f4=_f1.nodeName.toLowerCase();
if(((_f4=="a"&&_4.hasAttr(_f1,"href"))||_5._tabElements[_f4])&&(!_f2||_f3>=0)){
return true;
}
return false;
};
_5._getTabNavigable=function(_f5){
var _f6,_f7,_f8,_f9,_fa,_fb;
var _fc=function(_fd){
_4.query("> *",_fd).forEach(function(_fe){
var _ff=_5._isElementShown(_fe);
if(_ff&&_5.isTabNavigable(_fe)){
var _100=_4.attr(_fe,"tabindex");
if(!_4.hasAttr(_fe,"tabindex")||_100==0){
if(!_f6){
_f6=_fe;
}
_f7=_fe;
}else{
if(_100>0){
if(!_f8||_100<_f9){
_f9=_100;
_f8=_fe;
}
if(!_fa||_100>=_fb){
_fb=_100;
_fa=_fe;
}
}
}
}
if(_ff&&_fe.nodeName.toUpperCase()!="SELECT"){
_fc(_fe);
}
});
};
if(_5._isElementShown(_f5)){
_fc(_f5);
}
return {first:_f6,last:_f7,lowest:_f8,highest:_fa};
};
_5.getFirstInTabbingOrder=function(root){
var _102=_5._getTabNavigable(_4.byId(root));
return _102.lowest?_102.lowest:_102.first;
};
_5.getLastInTabbingOrder=function(root){
var _104=_5._getTabNavigable(_4.byId(root));
return _104.last?_104.last:_104.highest;
};
_5.defaultDuration=_4.config["defaultDuration"]||200;
}
if(!_4._hasResource["dojo.AdapterRegistry"]){
_4._hasResource["dojo.AdapterRegistry"]=true;
_4.provide("dojo.AdapterRegistry");
_4.AdapterRegistry=function(_105){
this.pairs=[];
this.returnWrappers=_105||false;
};
_4.extend(_4.AdapterRegistry,{register:function(name,_107,wrap,_109,_10a){
this.pairs[((_10a)?"unshift":"push")]([name,_107,wrap,_109]);
},match:function(){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[1].apply(this,arguments)){
if((pair[3])||(this.returnWrappers)){
return pair[2];
}else{
return pair[2].apply(this,arguments);
}
}
}
throw new Error("No match found");
},unregister:function(name){
for(var i=0;i<this.pairs.length;i++){
var pair=this.pairs[i];
if(pair[0]==name){
this.pairs.splice(i,1);
return true;
}
}
return false;
}});
}
if(!_4._hasResource["dijit._base.place"]){
_4._hasResource["dijit._base.place"]=true;
_4.provide("dijit._base.place");
_5.getViewport=function(){
var _110=(_4.doc.compatMode=="BackCompat")?_4.body():_4.doc.documentElement;
var _111=_4._docScroll();
return {w:_110.clientWidth,h:_110.clientHeight,l:_111.x,t:_111.y};
};
_5.placeOnScreen=function(node,pos,_114,_115){
var _116=_4.map(_114,function(_117){
var c={corner:_117,pos:{x:pos.x,y:pos.y}};
if(_115){
c.pos.x+=_117.charAt(1)=="L"?_115.x:-_115.x;
c.pos.y+=_117.charAt(0)=="T"?_115.y:-_115.y;
}
return c;
});
return _5._place(node,_116);
};
_5._place=function(node,_11a,_11b){
var view=_5.getViewport();
if(!node.parentNode||String(node.parentNode.tagName).toLowerCase()!="body"){
_4.body().appendChild(node);
}
var best=null;
_4.some(_11a,function(_11e){
var _11f=_11e.corner;
var pos=_11e.pos;
if(_11b){
_11b(node,_11e.aroundCorner,_11f);
}
var _121=node.style;
var _122=_121.display;
var _123=_121.visibility;
_121.visibility="hidden";
_121.display="";
var mb=_4.marginBox(node);
_121.display=_122;
_121.visibility=_123;
var _125=(_11f.charAt(1)=="L"?pos.x:Math.max(view.l,pos.x-mb.w)),_126=(_11f.charAt(0)=="T"?pos.y:Math.max(view.t,pos.y-mb.h)),endX=(_11f.charAt(1)=="L"?Math.min(view.l+view.w,_125+mb.w):pos.x),endY=(_11f.charAt(0)=="T"?Math.min(view.t+view.h,_126+mb.h):pos.y),_129=endX-_125,_12a=endY-_126,_12b=(mb.w-_129)+(mb.h-_12a);
if(best==null||_12b<best.overflow){
best={corner:_11f,aroundCorner:_11e.aroundCorner,x:_125,y:_126,w:_129,h:_12a,overflow:_12b};
}
return !_12b;
});
node.style.left=best.x+"px";
node.style.top=best.y+"px";
if(best.overflow&&_11b){
_11b(node,best.aroundCorner,best.corner);
}
return best;
};
_5.placeOnScreenAroundNode=function(node,_12d,_12e,_12f){
_12d=_4.byId(_12d);
var _130=_12d.style.display;
_12d.style.display="";
var _131=_12d.offsetWidth;
var _132=_12d.offsetHeight;
var _133=_4.coords(_12d,true);
_12d.style.display=_130;
return _5._placeOnScreenAroundRect(node,_133.x,_133.y,_131,_132,_12e,_12f);
};
_5.placeOnScreenAroundRectangle=function(node,_135,_136,_137){
return _5._placeOnScreenAroundRect(node,_135.x,_135.y,_135.width,_135.height,_136,_137);
};
_5._placeOnScreenAroundRect=function(node,x,y,_13b,_13c,_13d,_13e){
var _13f=[];
for(var _140 in _13d){
_13f.push({aroundCorner:_140,corner:_13d[_140],pos:{x:x+(_140.charAt(1)=="L"?0:_13b),y:y+(_140.charAt(0)=="T"?0:_13c)}});
}
return _5._place(node,_13f,_13e);
};
_5.placementRegistry=new _4.AdapterRegistry();
_5.placementRegistry.register("node",function(n,x){
return typeof x=="object"&&typeof x.offsetWidth!="undefined"&&typeof x.offsetHeight!="undefined";
},_5.placeOnScreenAroundNode);
_5.placementRegistry.register("rect",function(n,x){
return typeof x=="object"&&"x" in x&&"y" in x&&"width" in x&&"height" in x;
},_5.placeOnScreenAroundRectangle);
_5.placeOnScreenAroundElement=function(node,_146,_147,_148){
return _5.placementRegistry.match.apply(_5.placementRegistry,arguments);
};
}
if(!_4._hasResource["dijit._base.window"]){
_4._hasResource["dijit._base.window"]=true;
_4.provide("dijit._base.window");
_5.getDocumentWindow=function(doc){
if(_4.isIE&&window!==document.parentWindow&&!doc._parentWindow){
doc.parentWindow.execScript("document._parentWindow = window;","Javascript");
var win=doc._parentWindow;
doc._parentWindow=null;
return win;
}
return doc._parentWindow||doc.parentWindow||doc.defaultView;
};
}
if(!_4._hasResource["dijit._base.popup"]){
_4._hasResource["dijit._base.popup"]=true;
_4.provide("dijit._base.popup");
_5.popup=new function(){
var _14b=[],_14c=1000,_14d=1;
this.prepare=function(node){
var s=node.style;
s.visibility="hidden";
s.position="absolute";
s.top="-9999px";
if(s.display=="none"){
s.display="";
}
_4.body().appendChild(node);
};
this.open=function(args){
var _151=args.popup,_152=args.orient||{"BL":"TL","TL":"BL"},_153=args.around,id=(args.around&&args.around.id)?(args.around.id+"_dropdown"):("popup_"+_14d++);
var _155=_4.create("div",{id:id,"class":"dijitPopup",style:{zIndex:_14c+_14b.length,visibility:"hidden"}},_4.body());
_5.setWaiRole(_155,"presentation");
_155.style.left=_155.style.top="0px";
if(args.parent){
_155.dijitPopupParent=args.parent.id;
}
var s=_151.domNode.style;
s.display="";
s.visibility="";
s.position="";
s.top="0px";
_155.appendChild(_151.domNode);
var _157=new _5.BackgroundIframe(_155);
var best=_153?_5.placeOnScreenAroundElement(_155,_153,_152,_151.orient?_4.hitch(_151,"orient"):null):_5.placeOnScreen(_155,args,_152=="R"?["TR","BR","TL","BL"]:["TL","BL","TR","BR"],args.padding);
_155.style.visibility="visible";
var _159=[];
var _15a=function(){
for(var pi=_14b.length-1;pi>0&&_14b[pi].parent===_14b[pi-1].widget;pi--){
}
return _14b[pi];
};
_159.push(_4.connect(_155,"onkeypress",this,function(evt){
if(evt.charOrCode==_4.keys.ESCAPE&&args.onCancel){
_4.stopEvent(evt);
args.onCancel();
}else{
if(evt.charOrCode===_4.keys.TAB){
_4.stopEvent(evt);
var _15d=_15a();
if(_15d&&_15d.onCancel){
_15d.onCancel();
}
}
}
}));
if(_151.onCancel){
_159.push(_4.connect(_151,"onCancel",null,args.onCancel));
}
_159.push(_4.connect(_151,_151.onExecute?"onExecute":"onChange",null,function(){
var _15e=_15a();
if(_15e&&_15e.onExecute){
_15e.onExecute();
}
}));
_14b.push({wrapper:_155,iframe:_157,widget:_151,parent:args.parent,onExecute:args.onExecute,onCancel:args.onCancel,onClose:args.onClose,handlers:_159});
if(_151.onOpen){
_151.onOpen(best);
}
return best;
};
this.close=function(_15f){
while(_4.some(_14b,function(elem){
return elem.widget==_15f;
})){
var top=_14b.pop(),_162=top.wrapper,_163=top.iframe,_164=top.widget,_165=top.onClose;
if(_164.onClose){
_164.onClose();
}
_4.forEach(top.handlers,_4.disconnect);
if(!_164||!_164.domNode){
return;
}
this.prepare(_164.domNode);
_163.destroy();
_4.destroy(_162);
if(_165){
_165();
}
}
};
}();
_5._frames=new function(){
var _166=[];
this.pop=function(){
var _167;
if(_166.length){
_167=_166.pop();
_167.style.display="";
}else{
if(_4.isIE){
var burl=_4.config["dojoBlankHtmlUrl"]||(_4.moduleUrl("dojo","resources/blank.html")+"")||"javascript:\"\"";
var html="<iframe src='"+burl+"'"+" style='position: absolute; left: 0px; top: 0px;"+"z-index: -1; filter:Alpha(Opacity=\"0\");'>";
_167=_4.doc.createElement(html);
}else{
_167=_4.create("iframe");
_167.src="javascript:\"\"";
_167.className="dijitBackgroundIframe";
}
_167.tabIndex=-1;
_4.body().appendChild(_167);
}
return _167;
};
this.push=function(_16a){
_16a.style.display="none";
if(_4.isIE){
_16a.style.removeExpression("width");
_16a.style.removeExpression("height");
}
_166.push(_16a);
};
}();
_5.BackgroundIframe=function(node){
if(!node.id){
throw new Error("no id");
}
if(_4.isIE<7||(_4.isFF<3&&_4.hasClass(_4.body(),"dijit_a11y"))){
var _16c=_5._frames.pop();
node.appendChild(_16c);
if(_4.isIE){
_16c.style.setExpression("width",_4._scopeName+".doc.getElementById('"+node.id+"').offsetWidth");
_16c.style.setExpression("height",_4._scopeName+".doc.getElementById('"+node.id+"').offsetHeight");
}
this.iframe=_16c;
}
};
_4.extend(_5.BackgroundIframe,{destroy:function(){
if(this.iframe){
_5._frames.push(this.iframe);
delete this.iframe;
}
}});
}
if(!_4._hasResource["dijit._base.scroll"]){
_4._hasResource["dijit._base.scroll"]=true;
_4.provide("dijit._base.scroll");
_5.scrollIntoView=function(node){
try{
node=_4.byId(node);
var doc=_4.doc;
var body=_4.body();
var html=body.parentNode;
if((!(_4.isFF>=3||_4.isIE||_4.isWebKit)||node==body||node==html)&&(typeof node.scrollIntoView=="function")){
node.scrollIntoView(false);
return;
}
var ltr=_4._isBodyLtr();
var _172=_4.isIE>=8&&!_173;
var rtl=!ltr&&!_172;
var _175=body;
var _173=doc.compatMode=="BackCompat";
if(_173){
html._offsetWidth=html._clientWidth=body._offsetWidth=body.clientWidth;
html._offsetHeight=html._clientHeight=body._offsetHeight=body.clientHeight;
}else{
if(_4.isWebKit){
body._offsetWidth=body._clientWidth=html.clientWidth;
body._offsetHeight=body._clientHeight=html.clientHeight;
}else{
_175=html;
}
html._offsetHeight=html.clientHeight;
html._offsetWidth=html.clientWidth;
}
function _176(_177){
var ie=_4.isIE;
return ((ie<=6||(ie>=7&&_173))?false:(_4.style(_177,"position").toLowerCase()=="fixed"));
};
function _179(_17a){
var _17b=_17a.parentNode;
var _17c=_17a.offsetParent;
if(_17c==null||_176(_17a)){
_17c=html;
_17b=(_17a==body)?html:null;
}
_17a._offsetParent=_17c;
_17a._parent=_17b;
var bp=_4._getBorderExtents(_17a);
_17a._borderStart={H:(_172&&!ltr)?(bp.w-bp.l):bp.l,V:bp.t};
_17a._borderSize={H:bp.w,V:bp.h};
_17a._scrolledAmount={H:_17a.scrollLeft,V:_17a.scrollTop};
_17a._offsetSize={H:_17a._offsetWidth||_17a.offsetWidth,V:_17a._offsetHeight||_17a.offsetHeight};
_17a._offsetStart={H:(_172&&!ltr)?_17c.clientWidth-_17a.offsetLeft-_17a._offsetSize.H:_17a.offsetLeft,V:_17a.offsetTop};
_17a._clientSize={H:_17a._clientWidth||_17a.clientWidth,V:_17a._clientHeight||_17a.clientHeight};
if(_17a!=body&&_17a!=html&&_17a!=node){
for(var dir in _17a._offsetSize){
var _17f=_17a._offsetSize[dir]-_17a._clientSize[dir]-_17a._borderSize[dir];
var _180=_17a._clientSize[dir]>0&&_17f>0;
if(_180){
_17a._offsetSize[dir]-=_17f;
if(_4.isIE&&rtl&&dir=="H"){
_17a._offsetStart[dir]+=_17f;
}
}
}
}
};
var _181=node;
while(_181!=null){
if(_176(_181)){
node.scrollIntoView(false);
return;
}
_179(_181);
_181=_181._parent;
}
if(_4.isIE&&node._parent){
var _182=node._offsetParent;
node._offsetStart.H+=_182._borderStart.H;
node._offsetStart.V+=_182._borderStart.V;
}
if(_4.isIE>=7&&_175==html&&rtl&&body._offsetStart&&body._offsetStart.H==0){
var _183=html.scrollWidth-html._offsetSize.H;
if(_183>0){
body._offsetStart.H=-_183;
}
}
if(_4.isIE<=6&&!_173){
html._offsetSize.H+=html._borderSize.H;
html._offsetSize.V+=html._borderSize.V;
}
if(rtl&&body._offsetStart&&_175==html&&html._scrolledAmount){
var ofs=body._offsetStart.H;
if(ofs<0){
html._scrolledAmount.H+=ofs;
body._offsetStart.H=0;
}
}
_181=node;
while(_181){
var _185=_181._parent;
if(!_185){
break;
}
if(_185.tagName=="TD"){
var _186=_185._parent._parent._parent;
if(_185!=_181._offsetParent&&_185._offsetParent!=_181._offsetParent){
_185=_186;
}
}
var _187=_181._offsetParent==_185;
for(var dir in _181._offsetStart){
var _189=dir=="H"?"V":"H";
if(rtl&&dir=="H"&&(_185!=html)&&(_185!=body)&&(_4.isIE||_4.isWebKit)&&_185._clientSize.H>0&&_185.scrollWidth>_185._clientSize.H){
var _18a=_185.scrollWidth-_185._clientSize.H;
if(_18a>0){
_185._scrolledAmount.H-=_18a;
}
}
if(_185._offsetParent.tagName=="TABLE"){
if(_4.isIE){
_185._offsetStart[dir]-=_185._offsetParent._borderStart[dir];
_185._borderStart[dir]=_185._borderSize[dir]=0;
}else{
_185._offsetStart[dir]+=_185._offsetParent._borderStart[dir];
}
}
if(_4.isIE){
_185._offsetStart[dir]+=_185._offsetParent._borderStart[dir];
}
var _18b=_181._offsetStart[dir]-_185._scrolledAmount[dir]-(_187?0:_185._offsetStart[dir])-_185._borderStart[dir];
var _18c=_18b+_181._offsetSize[dir]-_185._offsetSize[dir]+_185._borderSize[dir];
var _18d=(dir=="H")?"scrollLeft":"scrollTop";
var _18e=dir=="H"&&rtl;
var _18f=_18e?-_18c:_18b;
var _190=_18e?-_18b:_18c;
var _191=(_18f*_190<=0)?0:Math[(_18f<0)?"max":"min"](_18f,_190);
if(_191!=0){
var _192=_185[_18d];
_185[_18d]+=(_18e)?-_191:_191;
var _193=_185[_18d]-_192;
}
if(_187){
_181._offsetStart[dir]+=_185._offsetStart[dir];
}
_181._offsetStart[dir]-=_185[_18d];
}
_181._parent=_185._parent;
_181._offsetParent=_185._offsetParent;
}
_185=node;
var next;
while(_185&&_185.removeAttribute){
next=_185.parentNode;
_185.removeAttribute("_offsetParent");
_185.removeAttribute("_parent");
_185=next;
}
}
catch(error){
console.error("scrollIntoView: "+error);
node.scrollIntoView(false);
}
};
}
if(!_4._hasResource["dijit._base.sniff"]){
_4._hasResource["dijit._base.sniff"]=true;
_4.provide("dijit._base.sniff");
(function(){
var d=_4,html=d.doc.documentElement,ie=d.isIE,_198=d.isOpera,maj=Math.floor,ff=d.isFF,_19b=d.boxModel.replace(/-/,""),_19c={dj_ie:ie,dj_ie6:maj(ie)==6,dj_ie7:maj(ie)==7,dj_iequirks:ie&&d.isQuirks,dj_opera:_198,dj_opera8:maj(_198)==8,dj_opera9:maj(_198)==9,dj_khtml:d.isKhtml,dj_webkit:d.isWebKit,dj_safari:d.isSafari,dj_gecko:d.isMozilla,dj_ff2:maj(ff)==2,dj_ff3:maj(ff)==3};
_19c["dj_"+_19b]=true;
for(var p in _19c){
if(_19c[p]){
if(html.className){
html.className+=" "+p;
}else{
html.className=p;
}
}
}
_4._loaders.unshift(function(){
if(!_4._isBodyLtr()){
html.className+=" dijitRtl";
for(var p in _19c){
if(_19c[p]){
html.className+=" "+p+"-rtl";
}
}
}
});
})();
}
if(!_4._hasResource["dijit._base.typematic"]){
_4._hasResource["dijit._base.typematic"]=true;
_4.provide("dijit._base.typematic");
_5.typematic={_fireEventAndReload:function(){
this._timer=null;
this._callback(++this._count,this._node,this._evt);
this._currentTimeout=(this._currentTimeout<0)?this._initialDelay:((this._subsequentDelay>1)?this._subsequentDelay:Math.round(this._currentTimeout*this._subsequentDelay));
this._timer=setTimeout(_4.hitch(this,"_fireEventAndReload"),this._currentTimeout);
},trigger:function(evt,_1a0,node,_1a2,obj,_1a4,_1a5){
if(obj!=this._obj){
this.stop();
this._initialDelay=_1a5||500;
this._subsequentDelay=_1a4||0.9;
this._obj=obj;
this._evt=evt;
this._node=node;
this._currentTimeout=-1;
this._count=-1;
this._callback=_4.hitch(_1a0,_1a2);
this._fireEventAndReload();
}
},stop:function(){
if(this._timer){
clearTimeout(this._timer);
this._timer=null;
}
if(this._obj){
this._callback(-1,this._node,this._evt);
this._obj=null;
}
},addKeyListener:function(node,_1a7,_1a8,_1a9,_1aa,_1ab){
if(_1a7.keyCode){
_1a7.charOrCode=_1a7.keyCode;
_4.deprecated("keyCode attribute parameter for dijit.typematic.addKeyListener is deprecated. Use charOrCode instead.","","2.0");
}else{
if(_1a7.charCode){
_1a7.charOrCode=String.fromCharCode(_1a7.charCode);
_4.deprecated("charCode attribute parameter for dijit.typematic.addKeyListener is deprecated. Use charOrCode instead.","","2.0");
}
}
return [_4.connect(node,"onkeypress",this,function(evt){
if(evt.charOrCode==_1a7.charOrCode&&(_1a7.ctrlKey===undefined||_1a7.ctrlKey==evt.ctrlKey)&&(_1a7.altKey===undefined||_1a7.altKey==evt.ctrlKey)&&(_1a7.shiftKey===undefined||_1a7.shiftKey==evt.ctrlKey)){
_4.stopEvent(evt);
_5.typematic.trigger(_1a7,_1a8,node,_1a9,_1a7,_1aa,_1ab);
}else{
if(_5.typematic._obj==_1a7){
_5.typematic.stop();
}
}
}),_4.connect(node,"onkeyup",this,function(evt){
if(_5.typematic._obj==_1a7){
_5.typematic.stop();
}
})];
},addMouseListener:function(node,_1af,_1b0,_1b1,_1b2){
var dc=_4.connect;
return [dc(node,"mousedown",this,function(evt){
_4.stopEvent(evt);
_5.typematic.trigger(evt,_1af,node,_1b0,node,_1b1,_1b2);
}),dc(node,"mouseup",this,function(evt){
_4.stopEvent(evt);
_5.typematic.stop();
}),dc(node,"mouseout",this,function(evt){
_4.stopEvent(evt);
_5.typematic.stop();
}),dc(node,"mousemove",this,function(evt){
_4.stopEvent(evt);
}),dc(node,"dblclick",this,function(evt){
_4.stopEvent(evt);
if(_4.isIE){
_5.typematic.trigger(evt,_1af,node,_1b0,node,_1b1,_1b2);
setTimeout(_4.hitch(this,_5.typematic.stop),50);
}
})];
},addListener:function(_1b9,_1ba,_1bb,_1bc,_1bd,_1be,_1bf){
return this.addKeyListener(_1ba,_1bb,_1bc,_1bd,_1be,_1bf).concat(this.addMouseListener(_1b9,_1bc,_1bd,_1be,_1bf));
}};
}
if(!_4._hasResource["dijit._base.wai"]){
_4._hasResource["dijit._base.wai"]=true;
_4.provide("dijit._base.wai");
_5.wai={onload:function(){
var div=_4.create("div",{id:"a11yTestNode",style:{cssText:"border: 1px solid;"+"border-color:red green;"+"position: absolute;"+"height: 5px;"+"top: -999px;"+"background-image: url(\""+(_4.config.blankGif||_4.moduleUrl("dojo","resources/blank.gif"))+"\");"}},_4.body());
var cs=_4.getComputedStyle(div);
if(cs){
var _1c2=cs.backgroundImage;
var _1c3=(cs.borderTopColor==cs.borderRightColor)||(_1c2!=null&&(_1c2=="none"||_1c2=="url(invalid-url:)"));
_4[_1c3?"addClass":"removeClass"](_4.body(),"dijit_a11y");
if(_4.isIE){
div.outerHTML="";
}else{
_4.body().removeChild(div);
}
}
}};
if(_4.isIE||_4.isMoz){
_4._loaders.unshift(_5.wai.onload);
}
_4.mixin(_5,{_XhtmlRoles:/banner|contentinfo|definition|main|navigation|search|note|secondary|seealso/,hasWaiRole:function(elem,role){
var _1c6=this.getWaiRole(elem);
return role?(_1c6.indexOf(role)>-1):(_1c6.length>0);
},getWaiRole:function(elem){
return _4.trim((_4.attr(elem,"role")||"").replace(this._XhtmlRoles,"").replace("wairole:",""));
},setWaiRole:function(elem,role){
var _1ca=_4.attr(elem,"role")||"";
if(_4.isFF<3||!this._XhtmlRoles.test(_1ca)){
_4.attr(elem,"role",_4.isFF<3?"wairole:"+role:role);
}else{
if((" "+_1ca+" ").indexOf(" "+role+" ")<0){
var _1cb=_4.trim(_1ca.replace(this._XhtmlRoles,""));
var _1cc=_4.trim(_1ca.replace(_1cb,""));
_4.attr(elem,"role",_1cc+(_1cc?" ":"")+role);
}
}
},removeWaiRole:function(elem,role){
var _1cf=_4.attr(elem,"role");
if(!_1cf){
return;
}
if(role){
var _1d0=_4.isFF<3?"wairole:"+role:role;
var t=_4.trim((" "+_1cf+" ").replace(" "+_1d0+" "," "));
_4.attr(elem,"role",t);
}else{
elem.removeAttribute("role");
}
},hasWaiState:function(elem,_1d3){
if(_4.isFF<3){
return elem.hasAttributeNS("http://www.w3.org/2005/07/aaa",_1d3);
}
return elem.hasAttribute?elem.hasAttribute("aria-"+_1d3):!!elem.getAttribute("aria-"+_1d3);
},getWaiState:function(elem,_1d5){
if(_4.isFF<3){
return elem.getAttributeNS("http://www.w3.org/2005/07/aaa",_1d5);
}
return elem.getAttribute("aria-"+_1d5)||"";
},setWaiState:function(elem,_1d7,_1d8){
if(_4.isFF<3){
elem.setAttributeNS("http://www.w3.org/2005/07/aaa","aaa:"+_1d7,_1d8);
}else{
elem.setAttribute("aria-"+_1d7,_1d8);
}
},removeWaiState:function(elem,_1da){
if(_4.isFF<3){
elem.removeAttributeNS("http://www.w3.org/2005/07/aaa",_1da);
}else{
elem.removeAttribute("aria-"+_1da);
}
}});
}
if(!_4._hasResource["dijit._base"]){
_4._hasResource["dijit._base"]=true;
_4.provide("dijit._base");
}
if(!_4._hasResource["dijit._Widget"]){
_4._hasResource["dijit._Widget"]=true;
_4.provide("dijit._Widget");
_4.require("dijit._base");
_4.connect(_4,"connect",function(_1db,_1dc){
if(_1db&&_4.isFunction(_1db._onConnect)){
_1db._onConnect(_1dc);
}
});
_5._connectOnUseEventHandler=function(_1dd){
};
(function(){
var _1de={};
var _1df=function(dc){
if(!_1de[dc]){
var r=[];
var _1e2;
var _1e3=_4.getObject(dc).prototype;
for(var _1e4 in _1e3){
if(_4.isFunction(_1e3[_1e4])&&(_1e2=_1e4.match(/^_set([a-zA-Z]*)Attr$/))&&_1e2[1]){
r.push(_1e2[1].charAt(0).toLowerCase()+_1e2[1].substr(1));
}
}
_1de[dc]=r;
}
return _1de[dc]||[];
};
_4.declare("dijit._Widget",null,{id:"",lang:"",dir:"","class":"",style:"",title:"",srcNodeRef:null,domNode:null,containerNode:null,attributeMap:{id:"",dir:"",lang:"","class":"",style:"",title:""},_deferredConnects:{onClick:"",onDblClick:"",onKeyDown:"",onKeyPress:"",onKeyUp:"",onMouseMove:"",onMouseDown:"",onMouseOut:"",onMouseOver:"",onMouseLeave:"",onMouseEnter:"",onMouseUp:""},onClick:_5._connectOnUseEventHandler,onDblClick:_5._connectOnUseEventHandler,onKeyDown:_5._connectOnUseEventHandler,onKeyPress:_5._connectOnUseEventHandler,onKeyUp:_5._connectOnUseEventHandler,onMouseDown:_5._connectOnUseEventHandler,onMouseMove:_5._connectOnUseEventHandler,onMouseOut:_5._connectOnUseEventHandler,onMouseOver:_5._connectOnUseEventHandler,onMouseLeave:_5._connectOnUseEventHandler,onMouseEnter:_5._connectOnUseEventHandler,onMouseUp:_5._connectOnUseEventHandler,_blankGif:(_4.config.blankGif||_4.moduleUrl("dojo","resources/blank.gif")),postscript:function(_1e5,_1e6){
this.create(_1e5,_1e6);
},create:function(_1e7,_1e8){
this.srcNodeRef=_4.byId(_1e8);
this._connects=[];
this._deferredConnects=_4.clone(this._deferredConnects);
for(var attr in this.attributeMap){
delete this._deferredConnects[attr];
}
for(attr in this._deferredConnects){
if(this[attr]!==_5._connectOnUseEventHandler){
delete this._deferredConnects[attr];
}
}
if(this.srcNodeRef&&(typeof this.srcNodeRef.id=="string")){
this.id=this.srcNodeRef.id;
}
if(_1e7){
this.params=_1e7;
_4.mixin(this,_1e7);
}
this.postMixInProperties();
if(!this.id){
this.id=_5.getUniqueId(this.declaredClass.replace(/\./g,"_"));
}
_5.registry.add(this);
this.buildRendering();
if(this.domNode){
this._applyAttributes();
var _1ea=this.srcNodeRef;
if(_1ea&&_1ea.parentNode){
_1ea.parentNode.replaceChild(this.domNode,_1ea);
}
for(attr in this.params){
this._onConnect(attr);
}
}
if(this.domNode){
this.domNode.setAttribute("widgetId",this.id);
}
this.postCreate();
if(this.srcNodeRef&&!this.srcNodeRef.parentNode){
delete this.srcNodeRef;
}
this._created=true;
},_applyAttributes:function(){
var _1eb=function(attr,_1ed){
if((_1ed.params&&attr in _1ed.params)||_1ed[attr]){
_1ed.attr(attr,_1ed[attr]);
}
};
for(var attr in this.attributeMap){
_1eb(attr,this);
}
_4.forEach(_1df(this.declaredClass),function(a){
if(!(a in this.attributeMap)){
_1eb(a,this);
}
},this);
},postMixInProperties:function(){
},buildRendering:function(){
this.domNode=this.srcNodeRef||_4.create("div");
},postCreate:function(){
},startup:function(){
this._started=true;
},destroyRecursive:function(_1f0){
this.destroyDescendants(_1f0);
this.destroy(_1f0);
},destroy:function(_1f1){
this.uninitialize();
_4.forEach(this._connects,function(_1f2){
_4.forEach(_1f2,_4.disconnect);
});
this.destroyRendering(_1f1);
_5.registry.remove(this.id);
},destroyRendering:function(_1f3){
if(this.bgIframe){
this.bgIframe.destroy(_1f3);
delete this.bgIframe;
}
if(this.domNode){
if(_1f3){
_4.removeAttr(this.domNode,"widgetId");
}else{
_4.destroy(this.domNode);
}
delete this.domNode;
}
if(this.srcNodeRef){
if(!_1f3){
_4.destroy(this.srcNodeRef);
}
delete this.srcNodeRef;
}
},destroyDescendants:function(_1f4){
_4.forEach(this.getChildren(),function(_1f5){
if(_1f5.destroyRecursive){
_1f5.destroyRecursive(_1f4);
}
});
},uninitialize:function(){
return false;
},onFocus:function(){
},onBlur:function(){
},_onFocus:function(e){
this.onFocus();
},_onBlur:function(){
this.onBlur();
},_onConnect:function(_1f7){
if(_1f7 in this._deferredConnects){
var _1f8=this[this._deferredConnects[_1f7]||"domNode"];
this.connect(_1f8,_1f7.toLowerCase(),_1f7);
delete this._deferredConnects[_1f7];
}
},_setClassAttr:function(_1f9){
var _1fa=this[this.attributeMap["class"]||"domNode"];
_4.removeClass(_1fa,this["class"]);
this["class"]=_1f9;
_4.addClass(_1fa,_1f9);
},_setStyleAttr:function(_1fb){
var _1fc=this[this.attributeMap["style"]||"domNode"];
if(_4.isObject(_1fb)){
_4.style(_1fc,_1fb);
}else{
if(_1fc.style.cssText){
_1fc.style.cssText+="; "+_1fb;
}else{
_1fc.style.cssText=_1fb;
}
}
this["style"]=_1fb;
},setAttribute:function(attr,_1fe){
_4.deprecated(this.declaredClass+"::setAttribute() is deprecated. Use attr() instead.","","2.0");
this.attr(attr,_1fe);
},_attrToDom:function(attr,_200){
var _201=this.attributeMap[attr];
_4.forEach(_4.isArray(_201)?_201:[_201],function(_202){
var _203=this[_202.node||_202||"domNode"];
var type=_202.type||"attribute";
switch(type){
case "attribute":
if(_4.isFunction(_200)){
_200=_4.hitch(this,_200);
}
if(/^on[A-Z][a-zA-Z]*$/.test(attr)){
attr=attr.toLowerCase();
}
_4.attr(_203,attr,_200);
break;
case "innerHTML":
_203.innerHTML=_200;
break;
case "class":
_4.removeClass(_203,this[attr]);
_4.addClass(_203,_200);
break;
}
},this);
this[attr]=_200;
},attr:function(name,_206){
var args=arguments.length;
if(args==1&&!_4.isString(name)){
for(var x in name){
this.attr(x,name[x]);
}
return this;
}
var _209=this._getAttrNames(name);
if(args==2){
if(this[_209.s]){
return this[_209.s](_206)||this;
}else{
if(name in this.attributeMap){
this._attrToDom(name,_206);
}
this[name]=_206;
}
return this;
}else{
if(this[_209.g]){
return this[_209.g]();
}else{
return this[name];
}
}
},_attrPairNames:{},_getAttrNames:function(name){
var apn=this._attrPairNames;
if(apn[name]){
return apn[name];
}
var uc=name.charAt(0).toUpperCase()+name.substr(1);
return apn[name]={n:name+"Node",s:"_set"+uc+"Attr",g:"_get"+uc+"Attr"};
},toString:function(){
return "[Widget "+this.declaredClass+", "+(this.id||"NO ID")+"]";
},getDescendants:function(){
if(this.containerNode){
var list=_4.query("[widgetId]",this.containerNode);
return list.map(_5.byNode);
}else{
return [];
}
},getChildren:function(){
if(this.containerNode){
return _5.findWidgets(this.containerNode);
}else{
return [];
}
},nodesWithKeyClick:["input","button"],connect:function(obj,_20f,_210){
var d=_4;
var dc=_4.connect;
var _213=[];
if(_20f=="ondijitclick"){
if(!this.nodesWithKeyClick[obj.nodeName]){
var m=d.hitch(this,_210);
_213.push(dc(obj,"onkeydown",this,function(e){
if(!d.isFF&&e.keyCode==d.keys.ENTER&&!e.ctrlKey&&!e.shiftKey&&!e.altKey&&!e.metaKey){
return m(e);
}else{
if(e.keyCode==d.keys.SPACE){
d.stopEvent(e);
}
}
}),dc(obj,"onkeyup",this,function(e){
if(e.keyCode==d.keys.SPACE&&!e.ctrlKey&&!e.shiftKey&&!e.altKey&&!e.metaKey){
return m(e);
}
}));
if(d.isFF){
_213.push(dc(obj,"onkeypress",this,function(e){
if(e.keyCode==d.keys.ENTER&&!e.ctrlKey&&!e.shiftKey&&!e.altKey&&!e.metaKey){
return m(e);
}
}));
}
}
_20f="onclick";
}
_213.push(dc(obj,_20f,this,_210));
this._connects.push(_213);
return _213;
},disconnect:function(_218){
for(var i=0;i<this._connects.length;i++){
if(this._connects[i]==_218){
_4.forEach(_218,_4.disconnect);
this._connects.splice(i,1);
return;
}
}
},isLeftToRight:function(){
return _4._isBodyLtr();
},isFocusable:function(){
return this.focus&&(_4.style(this.domNode,"display")!="none");
},placeAt:function(_21a,_21b){
if(_21a["declaredClass"]&&_21a["addChild"]){
_21a.addChild(this,_21b);
}else{
_4.place(this.domNode,_21a,_21b);
}
return this;
}});
})();
}
if(!_4._hasResource["dojo.string"]){
_4._hasResource["dojo.string"]=true;
_4.provide("dojo.string");
_4.string.rep=function(str,num){
if(num<=0||!str){
return "";
}
var buf=[];
for(;;){
if(num&1){
buf.push(str);
}
if(!(num>>=1)){
break;
}
str+=str;
}
return buf.join("");
};
_4.string.pad=function(text,size,ch,end){
if(!ch){
ch="0";
}
var out=String(text),pad=_4.string.rep(ch,Math.ceil((size-out.length)/ch.length));
return end?out+pad:pad+out;
};
_4.string.substitute=function(_225,map,_227,_228){
_228=_228||_4.global;
_227=(!_227)?function(v){
return v;
}:_4.hitch(_228,_227);
return _225.replace(/\$\{([^\s\:\}]+)(?:\:([^\s\:\}]+))?\}/g,function(_22a,key,_22c){
var _22d=_4.getObject(key,false,map);
if(_22c){
_22d=_4.getObject(_22c,false,_228).call(_228,_22d,key);
}
return _227(_22d,key).toString();
});
};
_4.string.trim=String.prototype.trim?_4.trim:function(str){
str=str.replace(/^\s+/,"");
for(var i=str.length-1;i>=0;i--){
if(/\S/.test(str.charAt(i))){
str=str.substring(0,i+1);
break;
}
}
return str;
};
}
if(!_4._hasResource["dijit._Templated"]){
_4._hasResource["dijit._Templated"]=true;
_4.provide("dijit._Templated");
_4.declare("dijit._Templated",null,{templateString:null,templatePath:null,widgetsInTemplate:false,_skipNodeCache:false,_stringRepl:function(tmpl){
var _231=this.declaredClass,_232=this;
return _4.string.substitute(tmpl,this,function(_233,key){
if(key.charAt(0)=="!"){
_233=_4.getObject(key.substr(1),false,_232);
}
if(typeof _233=="undefined"){
throw new Error(_231+" template:"+key);
}
if(_233==null){
return "";
}
return key.charAt(0)=="!"?_233:_233.toString().replace(/"/g,"&quot;");
},this);
},buildRendering:function(){
var _235=_5._Templated.getCachedTemplate(this.templatePath,this.templateString,this._skipNodeCache);
var node;
if(_4.isString(_235)){
node=_4._toDom(this._stringRepl(_235));
}else{
node=_235.cloneNode(true);
}
this.domNode=node;
this._attachTemplateNodes(node);
if(this.widgetsInTemplate){
var cw=(this._supportingWidgets=_4.parser.parse(node));
this._attachTemplateNodes(cw,function(n,p){
return n[p];
});
}
this._fillContent(this.srcNodeRef);
},_fillContent:function(_23a){
var dest=this.containerNode;
if(_23a&&dest){
while(_23a.hasChildNodes()){
dest.appendChild(_23a.firstChild);
}
}
},_attachTemplateNodes:function(_23c,_23d){
_23d=_23d||function(n,p){
return n.getAttribute(p);
};
var _240=_4.isArray(_23c)?_23c:(_23c.all||_23c.getElementsByTagName("*"));
var x=_4.isArray(_23c)?0:-1;
for(;x<_240.length;x++){
var _242=(x==-1)?_23c:_240[x];
if(this.widgetsInTemplate&&_23d(_242,"dojoType")){
continue;
}
var _243=_23d(_242,"dojoAttachPoint");
if(_243){
var _244,_245=_243.split(/\s*,\s*/);
while((_244=_245.shift())){
if(_4.isArray(this[_244])){
this[_244].push(_242);
}else{
this[_244]=_242;
}
}
}
var _246=_23d(_242,"dojoAttachEvent");
if(_246){
var _247,_248=_246.split(/\s*,\s*/);
var trim=_4.trim;
while((_247=_248.shift())){
if(_247){
var _24a=null;
if(_247.indexOf(":")!=-1){
var _24b=_247.split(":");
_247=trim(_24b[0]);
_24a=trim(_24b[1]);
}else{
_247=trim(_247);
}
if(!_24a){
_24a=_247;
}
this.connect(_242,_247,_24a);
}
}
}
var role=_23d(_242,"waiRole");
if(role){
_5.setWaiRole(_242,role);
}
var _24d=_23d(_242,"waiState");
if(_24d){
_4.forEach(_24d.split(/\s*,\s*/),function(_24e){
if(_24e.indexOf("-")!=-1){
var pair=_24e.split("-");
_5.setWaiState(_242,pair[0],pair[1]);
}
});
}
}
}});
_5._Templated._templateCache={};
_5._Templated.getCachedTemplate=function(_250,_251,_252){
var _253=_5._Templated._templateCache;
var key=_251||_250;
var _255=_253[key];
if(_255){
if(!_255.ownerDocument||_255.ownerDocument==_4.doc){
return _255;
}
_4.destroy(_255);
}
if(!_251){
_251=_5._Templated._sanitizeTemplateString(_4.trim(_4._getText(_250)));
}
_251=_4.string.trim(_251);
if(_252||_251.match(/\$\{([^\}]+)\}/g)){
return (_253[key]=_251);
}else{
return (_253[key]=_4._toDom(_251));
}
};
_5._Templated._sanitizeTemplateString=function(_256){
if(_256){
_256=_256.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im,"");
var _257=_256.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
if(_257){
_256=_257[1];
}
}else{
_256="";
}
return _256;
};
if(_4.isIE){
_4.addOnWindowUnload(function(){
var _258=_5._Templated._templateCache;
for(var key in _258){
var _25a=_258[key];
if(!isNaN(_25a.nodeType)){
_4.destroy(_25a);
}
delete _258[key];
}
});
}
_4.extend(_5._Widget,{dojoAttachEvent:"",dojoAttachPoint:"",waiRole:"",waiState:""});
}
if(!_4._hasResource["dijit._Container"]){
_4._hasResource["dijit._Container"]=true;
_4.provide("dijit._Container");
_4.declare("dijit._Container",null,{isContainer:true,buildRendering:function(){
this.inherited(arguments);
if(!this.containerNode){
this.containerNode=this.domNode;
}
},addChild:function(_25b,_25c){
var _25d=this.containerNode;
if(_25c&&typeof _25c=="number"){
var _25e=this.getChildren();
if(_25e&&_25e.length>=_25c){
_25d=_25e[_25c-1].domNode;
_25c="after";
}
}
_4.place(_25b.domNode,_25d,_25c);
if(this._started&&!_25b._started){
_25b.startup();
}
},removeChild:function(_25f){
if(typeof _25f=="number"&&_25f>0){
_25f=this.getChildren()[_25f];
}
if(!_25f||!_25f.domNode){
return;
}
var node=_25f.domNode;
node.parentNode.removeChild(node);
},_nextElement:function(node){
do{
node=node.nextSibling;
}while(node&&node.nodeType!=1);
return node;
},_firstElement:function(node){
node=node.firstChild;
if(node&&node.nodeType!=1){
node=this._nextElement(node);
}
return node;
},getChildren:function(){
return _4.query("> [widgetId]",this.containerNode).map(_5.byNode);
},hasChildren:function(){
return !!this._firstElement(this.containerNode);
},destroyDescendants:function(_263){
_4.forEach(this.getChildren(),function(_264){
_264.destroyRecursive(_263);
});
},_getSiblingOfChild:function(_265,dir){
var node=_265.domNode;
var _268=(dir>0?"nextSibling":"previousSibling");
do{
node=node[_268];
}while(node&&(node.nodeType!=1||!_5.byNode(node)));
return node?_5.byNode(node):null;
},getIndexOfChild:function(_269){
var _26a=this.getChildren();
for(var i=0,c;c=_26a[i];i++){
if(c==_269){
return i;
}
}
return -1;
}});
}
if(!_4._hasResource["dijit._Contained"]){
_4._hasResource["dijit._Contained"]=true;
_4.provide("dijit._Contained");
_4.declare("dijit._Contained",null,{getParent:function(){
for(var p=this.domNode.parentNode;p;p=p.parentNode){
var id=p.getAttribute&&p.getAttribute("widgetId");
if(id){
var _26f=_5.byId(id);
return _26f.isContainer?_26f:null;
}
}
return null;
},_getSibling:function(_270){
var node=this.domNode;
do{
node=node[_270+"Sibling"];
}while(node&&node.nodeType!=1);
if(!node){
return null;
}
var id=node.getAttribute("widgetId");
return _5.byId(id);
},getPreviousSibling:function(){
return this._getSibling("previous");
},getNextSibling:function(){
return this._getSibling("next");
},getIndexInParent:function(){
var p=this.getParent();
if(!p||!p.getIndexOfChild){
return -1;
}
return p.getIndexOfChild(this);
}});
}
if(!_4._hasResource["dijit.layout._LayoutWidget"]){
_4._hasResource["dijit.layout._LayoutWidget"]=true;
_4.provide("dijit.layout._LayoutWidget");
_4.declare("dijit.layout._LayoutWidget",[_5._Widget,_5._Container,_5._Contained],{baseClass:"dijitLayoutContainer",isLayoutContainer:true,postCreate:function(){
_4.addClass(this.domNode,"dijitContainer");
_4.addClass(this.domNode,this.baseClass);
},startup:function(){
if(this._started){
return;
}
_4.forEach(this.getChildren(),function(_274){
_274.startup();
});
if(!this.getParent||!this.getParent()){
this.resize();
this._viewport=_5.getViewport();
this.connect(_4.global,"onresize",function(){
var _275=_5.getViewport();
if(_275.w!=this._viewport.w||_275.h!=this._viewport.h){
this._viewport=_275;
this.resize();
}
});
}
this.inherited(arguments);
},resize:function(_276,_277){
var node=this.domNode;
if(_276){
_4.marginBox(node,_276);
if(_276.t){
node.style.top=_276.t+"px";
}
if(_276.l){
node.style.left=_276.l+"px";
}
}
var mb=_277||{};
_4.mixin(mb,_276||{});
if(!("h" in mb)||!("w" in mb)){
mb=_4.mixin(_4.marginBox(node),mb);
}
var cs=_4.getComputedStyle(node);
var me=_4._getMarginExtents(node,cs);
var be=_4._getBorderExtents(node,cs);
var bb=(this._borderBox={w:mb.w-(me.w+be.w),h:mb.h-(me.h+be.h)});
var pe=_4._getPadExtents(node,cs);
this._contentBox={l:_4._toPixelValue(node,cs.paddingLeft),t:_4._toPixelValue(node,cs.paddingTop),w:bb.w-pe.w,h:bb.h-pe.h};
this.layout();
},layout:function(){
},_setupChild:function(_27f){
_4.addClass(_27f.domNode,this.baseClass+"-child");
if(_27f.baseClass){
_4.addClass(_27f.domNode,this.baseClass+"-"+_27f.baseClass);
}
},addChild:function(_280,_281){
this.inherited(arguments);
if(this._started){
this._setupChild(_280);
}
},removeChild:function(_282){
_4.removeClass(_282.domNode,this.baseClass+"-child");
if(_282.baseClass){
_4.removeClass(_282.domNode,this.baseClass+"-"+_282.baseClass);
}
this.inherited(arguments);
}});
_5.layout.marginBox2contentBox=function(node,mb){
var cs=_4.getComputedStyle(node);
var me=_4._getMarginExtents(node,cs);
var pb=_4._getPadBorderExtents(node,cs);
return {l:_4._toPixelValue(node,cs.paddingLeft),t:_4._toPixelValue(node,cs.paddingTop),w:mb.w-(me.w+pb.w),h:mb.h-(me.h+pb.h)};
};
(function(){
var _288=function(word){
return word.substring(0,1).toUpperCase()+word.substring(1);
};
var size=function(_28b,dim){
_28b.resize?_28b.resize(dim):_4.marginBox(_28b.domNode,dim);
_4.mixin(_28b,_4.marginBox(_28b.domNode));
_4.mixin(_28b,dim);
};
_5.layout.layoutChildren=function(_28d,dim,_28f){
dim=_4.mixin({},dim);
_4.addClass(_28d,"dijitLayoutContainer");
_28f=_4.filter(_28f,function(item){
return item.layoutAlign!="client";
}).concat(_4.filter(_28f,function(item){
return item.layoutAlign=="client";
}));
_4.forEach(_28f,function(_292){
var elm=_292.domNode,pos=_292.layoutAlign;
var _295=elm.style;
_295.left=dim.l+"px";
_295.top=dim.t+"px";
_295.bottom=_295.right="auto";
_4.addClass(elm,"dijitAlign"+_288(pos));
if(pos=="top"||pos=="bottom"){
size(_292,{w:dim.w});
dim.h-=_292.h;
if(pos=="top"){
dim.t+=_292.h;
}else{
_295.top=dim.t+dim.h+"px";
}
}else{
if(pos=="left"||pos=="right"){
size(_292,{h:dim.h});
dim.w-=_292.w;
if(pos=="left"){
dim.l+=_292.w;
}else{
_295.left=dim.l+dim.w+"px";
}
}else{
if(pos=="client"){
size(_292,dim);
}
}
}
});
};
})();
}
if(!_4._hasResource["dijit.form._FormWidget"]){
_4._hasResource["dijit.form._FormWidget"]=true;
_4.provide("dijit.form._FormWidget");
_4.declare("dijit.form._FormWidget",[_5._Widget,_5._Templated],{baseClass:"",name:"",alt:"",value:"",type:"text",tabIndex:"0",disabled:false,readOnly:false,intermediateChanges:false,scrollOnFocus:true,attributeMap:_4.delegate(_5._Widget.prototype.attributeMap,{value:"focusNode",disabled:"focusNode",readOnly:"focusNode",id:"focusNode",tabIndex:"focusNode",alt:"focusNode"}),postMixInProperties:function(){
this.nameAttrSetting=this.name?("name='"+this.name+"'"):"";
this.inherited(arguments);
},_setDisabledAttr:function(_296){
this.disabled=_296;
_4.attr(this.focusNode,"disabled",_296);
_5.setWaiState(this.focusNode,"disabled",_296);
if(_296){
this._hovering=false;
this._active=false;
this.focusNode.removeAttribute("tabIndex");
}else{
this.focusNode.setAttribute("tabIndex",this.tabIndex);
}
this._setStateClass();
},setDisabled:function(_297){
_4.deprecated("setDisabled("+_297+") is deprecated. Use attr('disabled',"+_297+") instead.","","2.0");
this.attr("disabled",_297);
},_onFocus:function(e){
if(this.scrollOnFocus){
_5.scrollIntoView(this.domNode);
}
this.inherited(arguments);
},_onMouse:function(_299){
var _29a=_299.currentTarget;
if(_29a&&_29a.getAttribute){
this.stateModifier=_29a.getAttribute("stateModifier")||"";
}
if(!this.disabled){
switch(_299.type){
case "mouseenter":
case "mouseover":
this._hovering=true;
this._active=this._mouseDown;
break;
case "mouseout":
case "mouseleave":
this._hovering=false;
this._active=false;
break;
case "mousedown":
this._active=true;
this._mouseDown=true;
var _29b=this.connect(_4.body(),"onmouseup",function(){
if(this._mouseDown&&this.isFocusable()){
this.focus();
}
this._active=false;
this._mouseDown=false;
this._setStateClass();
this.disconnect(_29b);
});
break;
}
this._setStateClass();
}
},isFocusable:function(){
return !this.disabled&&!this.readOnly&&this.focusNode&&(_4.style(this.domNode,"display")!="none");
},focus:function(){
_5.focus(this.focusNode);
},_setStateClass:function(){
var _29c=this.baseClass.split(" ");
function _29d(_29e){
_29c=_29c.concat(_4.map(_29c,function(c){
return c+_29e;
}),"dijit"+_29e);
};
if(this.checked){
_29d("Checked");
}
if(this.state){
_29d(this.state);
}
if(this.selected){
_29d("Selected");
}
if(this.disabled){
_29d("Disabled");
}else{
if(this.readOnly){
_29d("ReadOnly");
}else{
if(this._active){
_29d(this.stateModifier+"Active");
}else{
if(this._focused){
_29d("Focused");
}
if(this._hovering){
_29d(this.stateModifier+"Hover");
}
}
}
}
var tn=this.stateNode||this.domNode,_2a1={};
_4.forEach(tn.className.split(" "),function(c){
_2a1[c]=true;
});
if("_stateClasses" in this){
_4.forEach(this._stateClasses,function(c){
delete _2a1[c];
});
}
_4.forEach(_29c,function(c){
_2a1[c]=true;
});
var _2a5=[];
for(var c in _2a1){
_2a5.push(c);
}
tn.className=_2a5.join(" ");
this._stateClasses=_29c;
},compare:function(val1,val2){
if((typeof val1=="number")&&(typeof val2=="number")){
return (isNaN(val1)&&isNaN(val2))?0:(val1-val2);
}else{
if(val1>val2){
return 1;
}else{
if(val1<val2){
return -1;
}else{
return 0;
}
}
}
},onChange:function(_2a9){
},_onChangeActive:false,_handleOnChange:function(_2aa,_2ab){
this._lastValue=_2aa;
if(this._lastValueReported==undefined&&(_2ab===null||!this._onChangeActive)){
this._resetValue=this._lastValueReported=_2aa;
}
if((this.intermediateChanges||_2ab||_2ab===undefined)&&((typeof _2aa!=typeof this._lastValueReported)||this.compare(_2aa,this._lastValueReported)!=0)){
this._lastValueReported=_2aa;
if(this._onChangeActive){
this.onChange(_2aa);
}
}
},create:function(){
this.inherited(arguments);
this._onChangeActive=true;
this._setStateClass();
},destroy:function(){
if(this._layoutHackHandle){
clearTimeout(this._layoutHackHandle);
}
this.inherited(arguments);
},setValue:function(_2ac){
_4.deprecated("dijit.form._FormWidget:setValue("+_2ac+") is deprecated.  Use attr('value',"+_2ac+") instead.","","2.0");
this.attr("value",_2ac);
},getValue:function(){
_4.deprecated(this.declaredClass+"::getValue() is deprecated. Use attr('value') instead.","","2.0");
return this.attr("value");
},_layoutHack:function(){
if(_4.isFF==2&&!this._layoutHackHandle){
var node=this.domNode;
var old=node.style.opacity;
node.style.opacity="0.999";
this._layoutHackHandle=setTimeout(_4.hitch(this,function(){
this._layoutHackHandle=null;
node.style.opacity=old;
}),0);
}
}});
_4.declare("dijit.form._FormValueWidget",_5.form._FormWidget,{attributeMap:_4.delegate(_5.form._FormWidget.prototype.attributeMap,{value:""}),postCreate:function(){
if(_4.isIE||_4.isWebKit){
this.connect(this.focusNode||this.domNode,"onkeydown",this._onKeyDown);
}
if(this._resetValue===undefined){
this._resetValue=this.value;
}
},_setValueAttr:function(_2af,_2b0){
this.value=_2af;
this._handleOnChange(_2af,_2b0);
},_getValueAttr:function(_2b1){
return this._lastValue;
},undo:function(){
this._setValueAttr(this._lastValueReported,false);
},reset:function(){
this._hasBeenBlurred=false;
this._setValueAttr(this._resetValue,true);
},_onKeyDown:function(e){
if(e.keyCode==_4.keys.ESCAPE&&!e.ctrlKey&&!e.altKey){
var te;
if(_4.isIE){
e.preventDefault();
te=document.createEventObject();
te.keyCode=_4.keys.ESCAPE;
te.shiftKey=e.shiftKey;
e.srcElement.fireEvent("onkeypress",te);
}else{
if(_4.isWebKit){
te=document.createEvent("Events");
te.initEvent("keypress",true,true);
te.keyCode=_4.keys.ESCAPE;
te.shiftKey=e.shiftKey;
e.target.dispatchEvent(te);
}
}
}
}});
}
if(!_4._hasResource["dijit.dijit"]){
_4._hasResource["dijit.dijit"]=true;
_4.provide("dijit.dijit");
}
if(!_4._hasResource["dijit._editor.range"]){
_4._hasResource["dijit._editor.range"]=true;
_4.provide("dijit._editor.range");
_5.range={};
_5.range.getIndex=function(node,_2b5){
var ret=[],retR=[];
var stop=_2b5;
var _2b9=node;
var _2ba,n;
while(node!=stop){
var i=0;
_2ba=node.parentNode;
while((n=_2ba.childNodes[i++])){
if(n===node){
--i;
break;
}
}
if(i>=_2ba.childNodes.length){
_4.debug("Error finding index of a node in dijit.range.getIndex");
}
ret.unshift(i);
retR.unshift(i-_2ba.childNodes.length);
node=_2ba;
}
if(ret.length>0&&_2b9.nodeType==3){
n=_2b9.previousSibling;
while(n&&n.nodeType==3){
ret[ret.length-1]--;
n=n.previousSibling;
}
n=_2b9.nextSibling;
while(n&&n.nodeType==3){
retR[retR.length-1]++;
n=n.nextSibling;
}
}
return {o:ret,r:retR};
};
_5.range.getNode=function(_2bd,_2be){
if(!_4.isArray(_2bd)||_2bd.length==0){
return _2be;
}
var node=_2be;
_4.every(_2bd,function(i){
if(i>=0&&i<node.childNodes.length){
node=node.childNodes[i];
}else{
node=null;
return false;
}
return true;
});
return node;
};
_5.range.getCommonAncestor=function(n1,n2){
var _2c3=function(n){
var as=[];
while(n){
as.unshift(n);
if(n.nodeName!="BODY"){
n=n.parentNode;
}else{
break;
}
}
return as;
};
var n1as=_2c3(n1);
var n2as=_2c3(n2);
var m=Math.min(n1as.length,n2as.length);
var com=n1as[0];
for(var i=1;i<m;i++){
if(n1as[i]===n2as[i]){
com=n1as[i];
}else{
break;
}
}
return com;
};
_5.range.getAncestor=function(node,_2cc,root){
root=root||node.ownerDocument.body;
while(node&&node!==root){
var name=node.nodeName.toUpperCase();
if(_2cc.test(name)){
return node;
}
node=node.parentNode;
}
return null;
};
_5.range.BlockTagNames=/^(?:P|DIV|H1|H2|H3|H4|H5|H6|ADDRESS|PRE|OL|UL|LI|DT|DE)$/;
_5.range.getBlockAncestor=function(node,_2d0,root){
root=root||node.ownerDocument.body;
_2d0=_2d0||_5.range.BlockTagNames;
var _2d2=null,_2d3;
while(node&&node!==root){
var name=node.nodeName.toUpperCase();
if(!_2d2&&_2d0.test(name)){
_2d2=node;
}
if(!_2d3&&(/^(?:BODY|TD|TH|CAPTION)$/).test(name)){
_2d3=node;
}
node=node.parentNode;
}
return {blockNode:_2d2,blockContainer:_2d3||node.ownerDocument.body};
};
_5.range.atBeginningOfContainer=function(_2d5,node,_2d7){
var _2d8=false;
var _2d9=(_2d7==0);
if(!_2d9&&node.nodeType==3){
if(_4.trim(node.nodeValue.substr(0,_2d7))==0){
_2d9=true;
}
}
if(_2d9){
var _2da=node;
_2d8=true;
while(_2da&&_2da!==_2d5){
if(_2da.previousSibling){
_2d8=false;
break;
}
_2da=_2da.parentNode;
}
}
return _2d8;
};
_5.range.atEndOfContainer=function(_2db,node,_2dd){
var _2de=false;
var _2df=(_2dd==(node.length||node.childNodes.length));
if(!_2df&&node.nodeType==3){
if(_4.trim(node.nodeValue.substr(_2dd))==0){
_2df=true;
}
}
if(_2df){
var _2e0=node;
_2de=true;
while(_2e0&&_2e0!==_2db){
if(_2e0.nextSibling){
_2de=false;
break;
}
_2e0=_2e0.parentNode;
}
}
return _2de;
};
_5.range.adjacentNoneTextNode=function(_2e1,next){
var node=_2e1;
var len=(0-_2e1.length)||0;
var prop=next?"nextSibling":"previousSibling";
while(node){
if(node.nodeType!=3){
break;
}
len+=node.length;
node=node[prop];
}
return [node,len];
};
_5.range._w3c=Boolean(window["getSelection"]);
_5.range.create=function(){
if(_5.range._w3c){
return _4.doc.createRange();
}else{
return new _5.range.W3CRange;
}
};
_5.range.getSelection=function(win,_2e7){
if(_5.range._w3c){
return win.getSelection();
}else{
var s=new _5.range.ie.selection(win);
if(!_2e7){
s._getCurrentSelection();
}
return s;
}
};
if(!_5.range._w3c){
_5.range.ie={cachedSelection:{},selection:function(win){
this._ranges=[];
this.addRange=function(r,_2eb){
this._ranges.push(r);
if(!_2eb){
r._select();
}
this.rangeCount=this._ranges.length;
};
this.removeAllRanges=function(){
this._ranges=[];
this.rangeCount=0;
};
var _2ec=function(){
var r=win.document.selection.createRange();
var type=win.document.selection.type.toUpperCase();
if(type=="CONTROL"){
return new _5.range.W3CRange(_5.range.ie.decomposeControlRange(r));
}else{
return new _5.range.W3CRange(_5.range.ie.decomposeTextRange(r));
}
};
this.getRangeAt=function(i){
return this._ranges[i];
};
this._getCurrentSelection=function(){
this.removeAllRanges();
var r=_2ec();
if(r){
this.addRange(r,true);
}
};
},decomposeControlRange:function(_2f1){
var _2f2=_2f1.item(0),_2f3=_2f1.item(_2f1.length-1);
var _2f4=_2f2.parentNode,_2f5=_2f3.parentNode;
var _2f6=_5.range.getIndex(_2f2,_2f4).o;
var _2f7=_5.range.getIndex(_2f3,_2f5).o+1;
return [_2f4,_2f6,_2f5,_2f7];
},getEndPoint:function(_2f8,end){
var _2fa=_2f8.duplicate();
_2fa.collapse(!end);
var _2fb="EndTo"+(end?"End":"Start");
var _2fc=_2fa.parentElement();
var _2fd,_2fe,_2ff;
if(_2fc.childNodes.length>0){
_4.every(_2fc.childNodes,function(node,i){
var _302;
if(node.nodeType!=3){
_2fa.moveToElementText(node);
if(_2fa.compareEndPoints(_2fb,_2f8)>0){
_2fd=node.previousSibling;
if(_2ff&&_2ff.nodeType==3){
_2fd=_2ff;
_302=true;
}else{
_2fd=_2fc;
_2fe=i;
return false;
}
}else{
if(i==_2fc.childNodes.length-1){
_2fd=_2fc;
_2fe=_2fc.childNodes.length;
return false;
}
}
}else{
if(i==_2fc.childNodes.length-1){
_2fd=node;
_302=true;
}
}
if(_302&&_2fd){
var _303=_5.range.adjacentNoneTextNode(_2fd)[0];
if(_303){
_2fd=_303.nextSibling;
}else{
_2fd=_2fc.firstChild;
}
var _304=_5.range.adjacentNoneTextNode(_2fd);
_303=_304[0];
var _305=_304[1];
if(_303){
_2fa.moveToElementText(_303);
_2fa.collapse(false);
}else{
_2fa.moveToElementText(_2fc);
}
_2fa.setEndPoint(_2fb,_2f8);
_2fe=_2fa.text.length-_305;
return false;
}
_2ff=node;
return true;
});
}else{
_2fd=_2fc;
_2fe=0;
}
if(!end&&_2fd.nodeType!=3&&_2fe==_2fd.childNodes.length){
if(_2fd.nextSibling&&_2fd.nextSibling.nodeType==3){
_2fd=_2fd.nextSibling;
_2fe=0;
}
}
return [_2fd,_2fe];
},setEndPoint:function(_306,_307,_308){
var _309=_306.duplicate(),node,len;
if(_307.nodeType!=3){
if(_308>0){
node=_307.childNodes[_308-1];
if(node.nodeType==3){
_307=node;
_308=node.length;
}else{
if(node.nextSibling&&node.nextSibling.nodeType==3){
_307=node.nextSibling;
_308=0;
}else{
_309.moveToElementText(node.nextSibling?node:_307);
var _30c=node.parentNode.insertBefore(document.createTextNode(" "),node.nextSibling);
_309.collapse(false);
_30c.parentNode.removeChild(_30c);
}
}
}else{
_309.moveToElementText(_307);
_309.collapse(true);
}
}
if(_307.nodeType==3){
var _30d=_5.range.adjacentNoneTextNode(_307);
var _30e=_30d[0];
len=_30d[1];
if(_30e){
_309.moveToElementText(_30e);
_309.collapse(false);
if(_30e.contentEditable!="inherit"){
len++;
}
}else{
_309.moveToElementText(_307.parentNode);
_309.collapse(true);
}
_308+=len;
if(_308>0){
if(_309.move("character",_308)!=_308){
console.error("Error when moving!");
}
}
}
return _309;
},decomposeTextRange:function(_30f){
var _310=_5.range.ie.getEndPoint(_30f);
var _311=_310[0],_312=_310[1];
var _313=_310[0],_314=_310[1];
if(_30f.htmlText.length){
if(_30f.htmlText==_30f.text){
_314=_312+_30f.text.length;
}else{
_310=_5.range.ie.getEndPoint(_30f,true);
_313=_310[0],_314=_310[1];
}
}
return [_311,_312,_313,_314];
},setRange:function(_315,_316,_317,_318,_319,_31a){
var _31b=_5.range.ie.setEndPoint(_315,_316,_317);
_315.setEndPoint("StartToStart",_31b);
if(!_31a){
var end=_5.range.ie.setEndPoint(_315,_318,_319);
}
_315.setEndPoint("EndToEnd",end||_31b);
return _315;
}};
_4.declare("dijit.range.W3CRange",null,{constructor:function(){
if(arguments.length>0){
this.setStart(arguments[0][0],arguments[0][1]);
this.setEnd(arguments[0][2],arguments[0][3]);
}else{
this.commonAncestorContainer=null;
this.startContainer=null;
this.startOffset=0;
this.endContainer=null;
this.endOffset=0;
this.collapsed=true;
}
},_updateInternal:function(){
if(this.startContainer!==this.endContainer){
this.commonAncestorContainer=_5.range.getCommonAncestor(this.startContainer,this.endContainer);
}else{
this.commonAncestorContainer=this.startContainer;
}
this.collapsed=(this.startContainer===this.endContainer)&&(this.startOffset==this.endOffset);
},setStart:function(node,_31e){
_31e=parseInt(_31e);
if(this.startContainer===node&&this.startOffset==_31e){
return;
}
delete this._cachedBookmark;
this.startContainer=node;
this.startOffset=_31e;
if(!this.endContainer){
this.setEnd(node,_31e);
}else{
this._updateInternal();
}
},setEnd:function(node,_320){
_320=parseInt(_320);
if(this.endContainer===node&&this.endOffset==_320){
return;
}
delete this._cachedBookmark;
this.endContainer=node;
this.endOffset=_320;
if(!this.startContainer){
this.setStart(node,_320);
}else{
this._updateInternal();
}
},setStartAfter:function(node,_322){
this._setPoint("setStart",node,_322,1);
},setStartBefore:function(node,_324){
this._setPoint("setStart",node,_324,0);
},setEndAfter:function(node,_326){
this._setPoint("setEnd",node,_326,1);
},setEndBefore:function(node,_328){
this._setPoint("setEnd",node,_328,0);
},_setPoint:function(what,node,_32b,ext){
var _32d=_5.range.getIndex(node,node.parentNode).o;
this[what](node.parentNode,_32d.pop()+ext);
},_getIERange:function(){
var r=(this._body||this.endContainer.ownerDocument.body).createTextRange();
_5.range.ie.setRange(r,this.startContainer,this.startOffset,this.endContainer,this.endOffset,this.collapsed);
return r;
},getBookmark:function(body){
this._getIERange();
return this._cachedBookmark;
},_select:function(){
var r=this._getIERange();
r.select();
},deleteContents:function(){
var r=this._getIERange();
r.pasteHTML("");
this.endContainer=this.startContainer;
this.endOffset=this.startOffset;
this.collapsed=true;
},cloneRange:function(){
var r=new _5.range.W3CRange([this.startContainer,this.startOffset,this.endContainer,this.endOffset]);
r._body=this._body;
return r;
},detach:function(){
this._body=null;
this.commonAncestorContainer=null;
this.startContainer=null;
this.startOffset=0;
this.endContainer=null;
this.endOffset=0;
this.collapsed=true;
}});
}
}
if(!_4._hasResource["scayt"]){
_4._hasResource["scayt"]=true;
_4.provide("scayt");
_4.declare("scayt",_5._Widget,{baseClass:"scayt",time:1500,destroyCalled:false,_errorTime:5000,_afterErrorTime:100,_focused:false,_content:"",_options:"",assocApp:"vasya",sLang:"",_paused:false,disabled:false,_ignored:false,init:false,alwaysSend:true,block:"P|BUTTON|TEXTAREA|SELECT|DIV|H[1-6]|ADDRESS|PRE|OL|UL|LI|TABLE|TBODY|DT|DE|T[DHR]|CAPTION|SUB|SUP",form:"SELECT|BUTTON|TEXTAREA",_blankGif:"",_optionNames:{allCaps:"",mixedCase:"",mixedWithDigits:"",ignoreDomainNames:""},allCaps:false,mixedCase:false,mixedWithDigits:false,ignoreDomainNames:true,domainNameList:"http|https|ftp",_userDictionaryName:"",_customDictionaryIds:[],afterMarkupInsert:[],afterMarkupRemove:[],isNormalizeEmptyParagraph:false,constructor:function(o){
var t=this;
if(typeof o.customerid!="undefined"&&o.customerid!="1:11111111111111111111111111111111111111"){
scayt.customerid=o.customerid;
}
if(o&&o.window&&o.window.frameElement&&o.window.frameElement.nodeName.toUpperCase()=="IFRAME"){
this.iframe=o.window.frameElement;
}
if(o.customDictionaryName&&o.customDictionaryName.length){
this._customDictionaryIds=_4.isString(o.customDictionaryName)?o.customDictionaryName.split(","):o.customDictionaryName;
}
if(o.customDictionaryIds&&o.customDictionaryIds.length){
this._customDictionaryIds=_4.isString(o.customDictionaryIds)?o.customDictionaryIds.split(","):o.customDictionaryIds;
}
if(o.userDictionaryName){
this._userDictionaryName=o.userDictionaryName;
}
o.sLang=o.sLang||o.defLang||scayt.defaultLang;
_4.mixin(t.attributeMap,t._optionNames,{sLang:"",userDictionaryName:"",customDictionaryIds:""});
},buildRendering:function(){
this.domNode=this.srcNodeRef;
this._applyAttributes();
for(var attr in this.params){
this._onConnect(attr);
}
this.srcNodeRef.setAttribute("widgetId",this.id);
this.domNode=null;
},postCreate:function(){
var t=this;
t.domNode=t.srcNodeRef;
if(this.domNode.nodeName=="IFRAME"){
this.iframe=this.domNode;
t.open();
}else{
if(this.iframe){
this.domNode=this.iframe;
t.open();
}else{
this.document=this.domNode.ownerDocument;
this.window=_5.getDocumentWindow(this.document);
this.window=window;
this.containerNode=this.containerNode||this.domNode;
if(_4.isIE){
this.containerNode.style.zoom=1;
}
this._onLoad();
}
}
},open:function(){
this.destroyCalled=false;
var h=null,ifr=this.domNode,_339=_4.hitch(this,function(){
if(this._loaded){
return;
}
if(h){
_4.disconnect(h);
h=null;
}
this.window=ifr.contentWindow;
var d=this.document=this.window.document;
this.containerNode=this.document.body;
this._onLoad();
});
if(_4.isIE&&_4.isIE<=7){
var c=setInterval(function(){
if(ifr.contentWindow.document.body){
clearInterval(c);
_339();
}
},100);
}else{
if(!(/CKEDITOR/i.test(this.assocApp))&&((_4.isOpera&&!ifr.contentWindow)||(ifr.contentDocument&&ifr.contentDocument.URL=="about:blank")||(_4.isIE&&!ifr.contentWindow.document.body))){
h=_4.connect(((_4.isIE)?ifr.contentWindow:ifr),"onload",_339);
}else{
_339();
}
}
},_onLoad:function(e){
var t=this;
if(!t.window.__registeredWindow){
t.window.__registeredWindow=true;
_5.registerWin(t.window);
}
t.focusNode=t.containerNode;
t.sLang=t.sLang||scayt.defaultLang;
t.state=scayt.STATE_ENABLED;
if(scayt.persist){
scayt._restoreState(t,["_options","sLang"]);
var tmp=_4.clone(t._optionNames),str=String(t._options),i=0;
t._options=t._serializeOptions();
for(var x in tmp){
tmp[x]=Number(str.charAt(i));
i++;
}
t._option(tmp);
}
if(!t.is(scayt.dictionary[t.sLang])){
t.dictionary=scayt.dictionary[t.sLang]=new _6.collections.Dictionary();
}
_4.subscribe("scayt::call",t,"call");
_4.subscribe("scayt::actionUserDictionary",t,"actionUserDictionary");
_4.subscribe("scayt::error",t,"setDisabled");
_4.subscribe("scayt::afterError",t,"refresh");
_4.subscribe("scayt::errorDisabled",t,function(_342){
if(_4.indexOf(_342,t.sLang)!=-1){
t.setDisabled(true);
}
});
t.regexp=t._getRegExp();
t.s=t.s();
t.s.add(t.containerNode,{correct:0});
t.span=this.document.createElement("span");
if(t.addClass){
_4.addClass(t.span,t.baseClass+"-misspell");
}
t.defaultSpellcheck=_4.attr(t.containerNode,"spellcheck");
_4.attr(t.containerNode,{"spellcheck":false});
if(_4.isIE){
t.addDataUrl=false;
}
if(!scayt.cssStyleNode.item(t.document)){
scayt.cssStyleNode.push({"doc":t.document,"css":[t.addCss(".scayt-enabled  "+t.selectorCss(),t.declarationCss()),t.addCss(".scayt-enabled .scayt-ignore","background: inherit !important"),t.addCss(".scayt-enabled pre "+t.selectorCss(),"white-space: pre !important")]});
}
scayt._hash.push(t);
_4.mixin(t,scayt._mixin);
if(_4.isFF<3){
t.disabled=true;
}
if(!t.disabled){
t.dispatchEvent();
}
if(!t.disabled){
_4.addClass(t.containerNode,"scayt-enabled");
if(t._getText(t.containerNode)){
t.reset();
t._m();
}
}else{
_4.addClass(t.containerNode,"scayt-disabled");
}
t.postLoad();
t.onLoad();
_4.publish(this.id+"::onLoad",[this]);
this.isLoaded=true;
},cssStyleNode:[],postLoad:function(){
},onLoad:function(){
},focus:function(){
this[!_4.isIE&&this.iframe?"window":"containerNode"][_4.isIE?"setActive":"focus"]();
this._focused++;
},dispatchEvent:function(){
var t=this,ap=t.iframe?t.document:t.containerNode,evt={"onkeypress":"onBeforeEvt","onkeyup":"onAfterEvt","onmousedown":"onMouseDown"},x;
if(_4.isMoz||_4.isWebKit||_4.isIE){
evt.onpaste="onAfterEvt";
evt.oncut="onAfterEvt";
}
if(_4.isIE){
for(x in evt){
t.connect(t.containerNode,x,t[evt[x]]);
}
}else{
for(x in evt){
t.connect(ap,x,t[evt[x]]);
}
}
},_serializeUnknowWord:[],deffMarkup:function(res,_348){
var t=this,ap=0,arr;
if(t.markupWord.length){
if(t._clearActiveTimer){
clearTimeout(t._clearActiveTimer);
delete (t._clearActiveTimer);
if(_348){
ap=3;
}
}
t.inFlight=true;
if(t.markupWord.length>21){
arr=t.markupWord.splice(0,20);
}else{
arr=_4.clone(t.markupWord);
t.markupWord=[];
}
t._m(arr,ap);
t.inFlight=false;
if(t.markupWord.length){
t.inFlight=true;
t._clearDeffMarkupTimer=setTimeout(function(){
t.deffMarkup(res);
},4000);
}else{
if(_4.isIE&&!t.is(t._clearActiveTimer1)){
t._clearActiveTimer1=setTimeout(function(){
if(t._clearActiveTimer1){
clearTimeout(t._clearActiveTimer1);
delete (t._clearActiveTimer1);
}
t._setActive=true;
t._m(null,3);
if(t.stackUnknowWord.length){
scayt.send({words:t.filterWords(t.stackUnknowWord),slang:t.sLang},null,t);
}
t.stackUnknowWord=[];
t._setActive=false;
},t.time);
}
}
}
return res;
},filterWords:function(arr){
var a="";
for(var i=0;i<arr.length;i++){
if(_4.isString(arr[i])){
a=(i==0?"":",")+arr[i];
}
}
return a;
},sendSerializeWord:function(res){
var t=this;
t.inFlightSendSerialize=true;
if(t._serializeUnknowWord.length){
scayt.send({words:t._serializeUnknowWord.shift(),slang:t.sLang},null,t);
if(t._serializeUnknowWord.length){
t._clearSendSerializeTimer=setTimeout(function(){
t.sendSerializeWord(res);
},4000);
}else{
t.inFlightSendSerialize=false;
}
return res;
}
},_option:function(_351,_352){
var args=arguments.length,t=this,tmp=_4.clone(t._optionNames),opt=false,x;
if(args==2&&t.is(t._optionNames[_351])){
opt=t[_351]=Number(_352);
}
if(args==1){
if(!_4.isString(_351)){
opt={};
for(x in _351){
if(t.is(_351[x])){
opt[x]=t[x]=Number(_351[x]);
}
opt[x]=t[x];
}
}else{
if(_4.isString(_351)){
if(!t.is(t._optionNames[_351])){
throw new Error("This option not found: "+_351);
}
opt=t[_351];
}
}
}
if(!args){
opt={};
for(x in tmp){
opt[x]=t[x];
}
}
var ser=t._serializeOptions();
if(t._options!=ser){
t._options=ser;
if(scayt.persist){
scayt._saveState(t,["_options"]);
}
scayt._io.options=t._options;
t._onOptionChange(_351,_352);
}
return opt;
},option:function(_359,_35a){
var t=this,tmp,args=arguments;
if(typeof _35a!="undefined"||_4.isObject(_359)){
tmp=t._option.apply(t,args);
if(t._paused){
t.afterPausedCall.push(function(){
t._setContent(function(){
t.refresh(null,true);
});
});
}else{
t._setContent(function(){
t.refresh(null,true);
});
}
return tmp;
}else{
return t._option.apply(t,args);
}
},setPaused:function(_35e){
this._paused=_35e;
var n=(_35e?"before":"after")+"PausedCall";
for(var i=0;i<this[n].length;i++){
this[n][i].apply(this);
}
this[n]=[];
this.onSetPaused(_35e);
},afterPausedCall:[],beforePausedCall:[],onSetPaused:function(_361){
},setDisabled:function(_362,_363){
var t=this;
if(_4.isFF<3){
return;
}
if(t.disabled==_362){
return;
}
t.disabled=_362;
_4.toggleClass(t.containerNode,"scayt-disabled");
_4.toggleClass(t.containerNode,"scayt-enabled");
if(_362){
if(t._clearSendSerializeTimer){
clearTimeout(t._clearSendSerializeTimer);
delete (t._clearSendSerializeTimer);
}
if(t._clearDeffMarkupSerializeTimer){
clearTimeout(t._clearDeffMarkupSerializeTimer);
delete (t._clearDeffMarkupSerializeTimer);
}
if(t._clearActiveTimer){
clearTimeout(t._clearActiveTimer);
delete (t._clearActiveTimer);
}
t.isTextSizeBig=false;
t.inFlight=false;
t.inFlightSendSerialize=false;
t.markupWord=[];
t.unknowWord=[];
t._serializeUnknowWord=[];
t.s.clear();
t.reset();
_4.forEach(t._connects,function(_365){
_4.forEach(_365,_4.disconnect);
});
}else{
t.sizeTextBig();
t.checkMisspellings();
t.reset();
t.dispatchEvent();
t.refresh();
}
t.state=_362?scayt.STATE_DISABLED:scayt.STATE_ENABLED;
t._onSetDisabled(_362);
_4.publish(this.id+"::setDisabled",[this]);
},pause:function(){
var t=this;
t._paused=!t._paused;
t.setDisabled(t._paused,true);
_4.publish(this.id+"::pause",[this]);
},setLang:function(lang,_368){
var t=this;
if(t.disabled||(t.sLang==lang&&!_368)){
return;
}
t.attr("sLang",lang);
if(scayt.persist){
scayt._saveState(t,["sLang"]);
}
var f=function(){
t._setContent(function(){
t.reset();
if(!t.is(scayt.dictionary[t.sLang])){
t.dictionary=scayt.dictionary[t.sLang]=new _6.collections.Dictionary();
}
t.refresh();
_4.publish(t.id+"::setLang",[t]);
});
};
if(t._paused){
t.afterPausedCall.unshift(function(){
f.apply(t);
});
}else{
setTimeout(f,1);
}
t._onLangChange(lang);
},reset:function(str){
var t=this;
if(str&&_4.isString(str)){
var node=_4.withGlobal(t.window,"create",_4,["div"]);
if(_4.isWebKit){
_4.style(node,"display","none");
t.document.body.appendChild(node);
}
node.innerHTML=str;
t._containerNode=t.containerNode;
t.containerNode=node;
t.remove(t.query(),t.containerNode,true);
str=t.containerNode.innerHTML;
t.containerNode=t._containerNode;
if(_4.isWebKit){
_4.destroy(node);
}
return str;
}
if(!t.destroyCalled){
t._setContent(function(){
t.remove(t.query());
});
}else{
t.remove(t.query());
}
t._innerHTML="";
_4.publish(t.id+"::reset",[t]);
},getScaytNode:function(node){
var t=this;
if(!arguments.length){
try{
node=_4.isIE?t.getRng().parentElement():t.getSel().anchorNode;
}
catch(e){
return null;
}
}
node=t.getParent(node,null,{"scayt_word":null});
return _4.hasClass(node,"scayt-ignore")?null:node;
},getWord:function(node){
if(!node){
return null;
}
return _4.hasClass(node,"scayt-ignore")?null:_4.attr(node,"scayt_word");
},getLang:function(){
return this.sLang;
},refresh:function(_371,_372){
var t=this;
if(t.disabled||t._paused){
return;
}
_371=_371||t.containerNode;
if(!_371){
return;
}
if(!_372&&_371==t.containerNode&&t._innerHTML==t.containerNode.innerHTML){
return;
}
t.s.add(_371);
t._m();
},ignore:function(node){
_4.removeClass(node,"scayt-misspell");
_4.addClass(node,"scayt-ignore");
this._onIgnore(node);
},ignoreAll:function(word){
if(!_4.isString(word)){
try{
word=_4.attr(word,"scayt_word");
}
catch(e){
}
}
if(!word){
throw new Error("Incorrect node");
return;
}
scayt.ignoreDictionary.add(word);
var nl=this.query(word);
nl.removeClass("scayt-misspell");
nl.addClass("scayt-ignore");
this._onIgnoreAll(word);
},addClass:false,addDataUrl:true,declarationCss:function(){
return "background: url("+(this.addDataUrl?scayt.imageDataURL:scayt.imageURL)+") transparent bottom repeat-x !important; padding-bottom: 0 !important; text-decoration: none !important; white-space: nowrap !important;";
},selectorCss:function(){
return _4.isIE||this.addClass?"."+this.baseClass+"-misspell":"["+this.baseClass+"_word]";
},addStyle:function(_377,_378){
var t=this;
scayt.cssStyleNode.item(t.document)["css"].push(t.addCss(_377,_378));
},addCss:function(_37a,_37b){
var t=this,ss=scayt.dynamicStyleMap[name],_37e=_37a+" {"+_37b+"}";
if(!ss){
if(this.document.createStyleSheet){
ss=t.document.createStyleSheet();
}else{
ss=t.document.createElement("style");
_4.attr(ss,"cke_temp","2");
ss.setAttribute("type","text/css");
t.document.getElementsByTagName("head")[0].appendChild(ss);
}
ss._indicies=[];
}
if(_4.isIE){
ss.cssText+=_37e;
_4.attr(ss.owningElement,"cke_temp","2");
}else{
if(ss.sheet){
ss.sheet.insertRule(_37e,ss._indicies.length);
}else{
ss.appendChild(t.document.createTextNode(_37e));
}
}
ss._indicies.push(_37a+" "+_37b);
ss.scayt=true;
return ss;
},getBookmark:function(si){
var t=this,d=_4,ch=-16777215,_383=t.getRng(),c=t.containerNode,vp=(t.containerNode.nodeName=="BODY"?_4.withGlobal(t.window,"getViewport",_5,[]):{l:c.scrollLeft,t:c.scrollTop}),o={scrollX:vp.l,scrollY:vp.t,collapse:0,start:0},sp,nv,sel=t.getSel();
t.asd=null;
t.asd1=null;
if(si=="simple"){
return d.mixin(o,{rng:_383});
}
if(_4.isIE){
if(t.isNormalizeEmptyParagraph){
_4.withGlobal(t.window,"query",_4,["p:empty",t.containerNode]).forEach(function(node){
node.innerHTML="&nbsp;";
});
}
if(this.document.selection&&this.document.selection.type&&this.document.selection.createRange().item){
_383=this.document.selection.createRange();
var e=_383.item(0);
d.query(e.nodeName,c).forEach(function(node,i){
if(e==node){
return !(sp=i);
}
});
return d.mixin(o,{tag:e.nodeName,index:sp});
}
var tr,bp,tr1;
tr=_383.duplicate();
try{
tr.moveToElementText(c);
}
catch(e){
tr=t.document.body.createTextRange();
}
tr.collapse(true);
bp=Math.abs(tr.move("character",ch));
tr=_383.duplicate();
tr.collapse(true);
sp=Math.abs(tr.move("character",ch));
tr=_383.duplicate();
tr.collapse(false);
var _391=0;
tr1=tr.duplicate();
tr1.moveEnd("character",1);
tr1.collapse(false);
var parN=tr1.parentElement();
tr1.moveEnd("character",2);
if((new RegExp("^(?:"+t.block+")$")).test(parN.nodeName)){
if(/^\r\n<TR>/i.test(tr1.htmlText)){
t.asd1=1;
}
var _393=t.getParent(tr.parentElement(),t.block,null,t.containerNode);
}
var _394=Math.abs(tr.move("character",ch))-sp;
return d.mixin(o,{start:sp-bp-_391,length:_394,collapse:!_394});
}
var p=_4.withGlobal(t.window,"getParentElement",this,[]);
if(p&&p.nodeName=="IMG"){
return o;
}
if(!sel){
return null;
}
var sc=_383.startContainer,an=sel.anchorNode,n,w;
sp=t.getScaytNode(an);
if(sel.isCollapsed){
o.collapse=1;
var _39a=t.getParent(an,t.block)||c;
if(an.nodeType==3){
w=t.document.createTreeWalker(_39a,NodeFilter.SHOW_TEXT,null,false);
while((n=w.nextNode())){
if(n==an){
o.start=o.start+sel.anchorOffset;
break;
}
o.start+=t._trimNl(n.nodeValue||"").length;
}
}else{
if(an!=_39a){
w=t.document.createTreeWalker(_39a,NodeFilter.SHOW_ALL,null,false);
while((n=w.nextNode())){
if(n==an){
break;
}
o.start+=t._trimNl(n.nodeValue||"").length;
}
}
if(sc.nodeType!=8){
for(var i=0;i<_383.startOffset;i++){
o.start+=parseInt(String(sc.childNodes[i].textContent).length,10);
}
}
}
o.end=o.start;
var tc=sc.textContent!="undefined"?sc.textContent:sc.innerHTML;
if(!sp){
o.content=tc;
try{
if(_383.startOffset==0&&sc.previousSibling&&(/IMG|BR|INPUT/.test(sc.previousSibling.nodeName))){
o.br=sc.previousSibling;
}
if(sc.childNodes[_383.startOffset-1]&&(/IMG|BR|INPUT/.test(sc.childNodes[_383.startOffset-1].nodeName))){
o.br=sc.childNodes[_383.startOffset-1];
}
}
catch(e){
}
}else{
if(tc==""){
_4.attr(sc,"scaytbookmark","true");
o.empty=true;
}
}
if(!_4.isIE&&sp&&_383.startContainer==sp&&_383.startOffset>0&&(n=sp.childNodes[_383.startOffset-1])&&/IMG|BR|INPUT/.test(n.nodeName)){
_4.attr(n,"scaytbookmark","true");
o.img=true;
}
if(sp&&_383.startOffset==0){
n=sp.previousSibling;
while(n&&((n.nodeType==3&&n.textContent=="")||(n.nodeType!=3&&n.innerHTML==""))){
if(n&&(/IMG|BR|INPUT/.test(n.nodeName))){
o.br=n;
o.br2=n.nextSibling;
break;
}
n=n.previousSibling;
}
}
d.mixin(o,{block:_39a,node:sc,offset:_383.startOffset});
return o;
}
var s=[];
p=0;
w=t.document.createTreeWalker(c,NodeFilter.SHOW_TEXT,null,false);
while((n=w.nextNode())!=null){
if(n==sc){
s[0]=p;
}
if(n==_383.endContainer){
s[1]=p;
break;
}
p+=t._trimNl(n.nodeValue||"").length;
}
d.mixin(o,{start:s[0]+_383.startOffset,end:typeof s[1]!="undefined"?s[1]+_383.endOffset:s[0]+t._trimNl(_383.startContainer.nodeValue||"").length,block:c});
return o;
},moveToBookmark:function(b){
var t=this,sel=t.getSel(),c=t.containerNode,rng=t.getRng(),_3a3;
function _3a4(sp,ep){
var n,p=0,d={},o,par,k=-1,nv,nvl,w=t.document.createTreeWalker(b.block,NodeFilter.SHOW_TEXT,null,false);
while((n=w.nextNode())){
nv=n.nodeValue||"";
nvl=t._trimNl(nv).length;
p+=nvl;
if(b.collapse){
if(p>=sp){
par=t.getParent(n,t.block)||c;
}
if(p==sp){
k=par==b.block?1:0;
}
if(k==-1&&p>sp||k==1){
d.endNode=d.startNode=n;
d.endOffset=d.startOffset=sp-(p-nvl);
return d;
}
}else{
if(p>=sp&&!d.startNode){
o=sp-(p-nvl);
d.startNode=n;
d.startOffset=sp-(p-nvl);
}
if(p>=ep){
d.endNode=n;
d.endOffset=ep-(p-nvl);
return d;
}
}
}
return null;
};
if(!b){
return false;
}
if(_4.isIE){
if((_3a3=b.rng)){
try{
_3a3.select();
}
catch(ex){
}
return true;
}
if(b.tag){
_3a3=t.containerNode.createControlRange();
_4.query(b.tag,c).forEach(function(n,i){
if(i==b.index){
_3a3.addElement(n);
}
});
}else{
if(b.start<0){
return true;
}
_3a3=t.getRng();
try{
if(t.asd){
_3a3.moveToElementText(t.asd);
_3a3.moveStart("character",-2);
_3a3.expand("word");
_3a3.collapse(true);
_3a3.move("word",1);
}else{
if(!t.asd1){
_3a3.moveToElementText(t.containerNode);
_3a3.collapse(true);
_3a3.moveStart("character",b.start);
_3a3.moveEnd("character",b.length);
}
}
}
catch(e){
_3a3=t.getRng();
}
}
if(t.iframe){
var vp=_5.getViewport(),_3b3=_4.coords(t.iframe);
if(t.ignoreFormElement||_3a3.boundingTop<0||_3a3.boundingTop>0&&_3b3.y<0&&_3a3.boundingTop<_3b3.y*-1||_3a3.boundingTop>(_3b3.h-_3a3.boundingHeight)||_3a3.boundingTop>(vp.h-_3b3.y)||_3a3.boundingLeft<0||_3a3.boundingLeft>0&&_3b3.x<0&&_3a3.boundingLeft<_3b3.x*-1||_3a3.boundingLeft>(_3b3.w-_3a3.boundingWidth)||_3a3.boundingLeft>(vp.w-_3b3.x)){
}else{
if(typeof _3a3.boundingHeight=="undefined"||(_3a3.boundingHeight+_3a3.boundingWidth)){
_3a3.select();
}
}
}else{
if(!t.ignoreFormElement){
_3a3.select();
}
}
if(!t.iframe){
setTimeout(function(){
t.containerNode.scrollLeft=b.scrollX;
t.containerNode.scrollTop=b.scrollY;
},0);
}
try{
if(_4.isIE){
if(scayt.activateEditableBlockOnMouseClick){
this.getParentEditBlock(_3a3.parentElement()).setActive();
}
t._focused=true;
}else{
t.containerNode.focus();
}
}
catch(ex){
}
return true;
}
if(!sel){
return false;
}
_3a3=rng.cloneRange();
if(b.rng){
sel.removeAllRanges();
sel.addRange(b.rng);
}else{
if(t.is(b.node)&&!(_4.isWebKit&&b.node.nodeType==3)){
var a=false;
try{
if((b.node.nodeType==3&&b.node.parentNode!=null&&b.node.textContent==b.content)||(b.node.nodeType!=3&&b.node.innerHTML==b.content)){
_3a3.setStart(b.node,b.offset);
if(_3a3.toString()){
_3a3.collapse(true);
a=true;
}
}
if(b.empty||b.img){
var nn=_4.withGlobal(t.window,"query",_4,["[scaytbookmark]",t.containerNode]);
_4.removeAttr(nn[0],"scaytbookmark");
if(b.img){
_3a3.selectNode(nn[0]);
_3a3.collapse(false);
}else{
_3a3.setStart(nn[0],0);
_3a3.collapse(true);
}
a=true;
}
if(t.is(b.br)&&(/IMG|BR|INPUT/.test(b.br.nodeName))){
if(b.br.nextSibling){
_3a3.selectNode(b.br.nextSibling);
_3a3.collapse(true);
}else{
_3a3.selectNode(b.br);
_3a3.collapse(false);
}
a=true;
}
}
catch(ex){
}
if(a){
if(!_4.isOpera){
sel.removeAllRanges();
}
sel.addRange(_3a3);
t.containerNode.focus();
return;
}
}
if(t.is(b.start)&&t.is(b.end)){
try{
var sd=_3a4(b.start,b.end);
if(sd){
_3a3.setStart(sd.startNode,sd.startOffset);
_3a3.setEnd(sd.endNode,sd.endOffset);
if(!_4.isOpera){
sel.removeAllRanges();
}
sel.addRange(_3a3);
}
}
catch(ex){
}
}
return;
}
},getKeyMoving:function(){
var dk=_4.keys;
return [dk.HOME,dk.UP_ARROW,dk.DOWN_ARROW,dk.LEFT_ARROW,dk.RIGHT_ARROW,dk.END,dk.PAGE_UP,dk.PAGE_DOWN,93];
},_c:{},onMouseDown:function(evt){
if(!_4.isWebKit&&!_4.isIE){
this.containerNode.focus();
}
if(_4.isIE){
evt=_4.fixEvent(evt);
if(scayt.activateEditableBlockOnMouseClick||this.assocApp=="vasya"){
this.getParentEditBlock(evt.target).setActive();
}
}
},ignoreFormElement:false,onBeforeEvt:function(evt){
var t=this,sel;
t.ignoreFormElement=false;
t._focused=true;
t._currNode={};
evt=_4.fixEvent(evt);
if(_4.isIE){
var peb=this.getParentEditBlock(evt.target);
peb.setActive();
t._focused=true;
if(peb.nodeName=="INPUT"){
t.ignoreFormElement=true;
}
}else{
if((/CKEDITOR/i.test(this.assocApp))&&evt.ctrlKey&&evt.charCode==118){
setTimeout(function(){
t.containerNode.focus();
},0);
}else{
t.containerNode.focus();
}
}
var _3bd=evt.keyCode||evt.charCode,_3be=t.getKeyMoving();
if(evt.ctrlKey||evt.altKey||(!_4.isMoz&&evt.shiftKey&&!evt.charCode)){
return;
}
if((!_4.IsMoz&&evt.altKey&&_3bd==18)||_4.indexOf(_3be,evt.keyCode)!=-1||t.disabled||evt.keyCode==9){
return;
}
if(_4.isOpera){
sel=t.getRng();
if(!sel.isCollapsed){
sel.deleteContents();
}
}
t._c=t.getParentEx();
if(t._c.isIgnore||t.ignoreFormElement){
return;
}
if(!t.s.item(t.containerNode,1)){
t.s.add(t._c.block,{full:0});
}
if(0&&_4.isFF&&t._c.isScayt){
var rr=t.getRng(),rr2=rr.cloneRange();
sel=t.getSel();
rr2.setStart(t._c.scayt[0],0);
if(rr2.toString()==_4.attr(t._c.scayt[0],"scayt_word")){
rr2.selectNode(t._c.scayt[0]);
rr2.collapse(false);
sel.removeAllRanges();
sel.addRange(rr2);
return;
}
}
var _3c1=false;
var _3c2=true;
if(_4.isIE){
try{
var r=t.getRng();
if(!(r.parentElement().innerText)){
_3c2=false;
}else{
r.moveStart("character",-1);
r.collapse(true);
_3c1=t.getScaytNode(r.parentElement());
}
}
catch(e){
}
}
var span=t._c.scayt||_3c1;
if(_3c2&&t._c.isScayt){
t._setContent(function(){
t.remove(span);
t.s.item(t._c.block,0).html=(_4.isFF&&evt.keyCode==13)?null:t._c.block.innerHTML;
});
}
},onAfterEvt:function(evt){
evt=_4.fixEvent(evt);
var t=this,_3c7=evt.keyCode||evt.charCode,_3c8=t.getKeyMoving();
if(t.ignoreFormElement&&_4.isIE){
return;
}
if(_4.isIE&&t._c.block&&!t._c.block.innerHTML){
var ieB=t.getParentEx().block;
t.s.add(ieB,{full:0});
}
if(evt.type=="cut"||evt.type=="paste"){
setTimeout(function(){
_4.query("script,link,style,meta",t.containerNode).forEach(_4.destroy);
},0);
t._cc=t.getParentEx();
if(!_4.isIE&&t._cc.isScayt){
t._setContent(function(){
t.remove(t._cc.scayt);
t.s.item(t._cc.block,0).html=t._cc.block.innerHTML;
});
}
if(t._cc.isIgnore){
return;
}
if(!t.s.item(t.containerNode,1)){
if(evt.type=="paste"){
t.s.clear();
setTimeout(function(){
t.refresh(null,true);
},10);
}else{
t.s.add(t._cc.block,{full:0,correct:1});
}
}
}
if(evt.keyCode==17||evt.ctrlKey||evt.altKey||((_4.isIE||_4.Chrome)&&evt.shiftKey)){
return;
}
if((!_4.IsMoz&&evt.altKey&&_3c7==18)||_4.indexOf(_3c8,evt.keyCode)!=-1){
return;
}
if(evt.keyCode==13){
var sel=t.getRng(),_3cb=new RegExp("^(?:"+t.block+")$","i"),node=t.getSelectionNode();
if(!_4.isIE&&!_4.isWebKit){
t.containerNode.normalize();
}
while(node&&node!=_3cb.test(node.nodeName)&&!node.previousSibling){
node=node.parentNode;
}
if(node&&node.nodeName!="BODY"){
_4.forEach([node,node.previousSibling],function(node){
if(node.nodeType==3||node.nodeName=="BR"){
return;
}
if(!t.s.item(t.containerNode,1)&&!t.s.item(node,0)){
t.s.add(node,{full:0});
}
});
}
}
if(evt.keyCode!=13&&(!t.is(t._c)||evt.keyCode==9||(t.is(t._c)&&t.is(t._c.block)&&t._c.block.innerHTML==t.s.item(t._c.block,0).html)||t._paused||t.disabled||t._c.isIgnore)){
return;
}
if(t._clearActiveTimer){
return;
}
if(t._clearActiveTimer1){
clearTimeout(t._clearActiveTimer1);
delete (t._clearActiveTimer1);
}
t._clearActiveTimer=setTimeout(function(){
if(t.disabled){
return;
}
t._clearActiveTimer1=setTimeout(function(){
if(t._clearActiveTimer1){
clearTimeout(t._clearActiveTimer1);
delete (t._clearActiveTimer1);
}
t._setActive=true;
t._m(null,3);
if(t.stackUnknowWord.length){
scayt.send({words:t.filterWords(t.stackUnknowWord),slang:t.sLang},null,t);
}
t.stackUnknowWord=[];
t._setActive=false;
},scayt.time);
try{
t._m(null,_4.isIE?1:0);
}
catch(e){
}
if(t._clearActiveTimer){
clearTimeout(t._clearActiveTimer);
delete (t._clearActiveTimer);
}
},scayt.time);
},stackUnknowWord:[],_setContent:function(func){
var t=this;
if(t._focused){
try{
if(0&&_4.isIE){
_4.withGlobal(t.window,"query",_4,["p:empty",t.containerNode]).forEach(function(node){
_4.destroy(node);
});
}
t.bookmark=t.getBookmark();
t._markup=true;
}
catch(ex){
}
}
func.apply(t,[]);
try{
t._innerHTML=t.containerNode.innerHTML;
}
catch(e){
}
if(t._markup){
t.moveToBookmark(t.bookmark);
t._markup=false;
}
},actionUserDictionary:function(_3d1,o){
var t=this;
if(_3d1=="addword"){
this._setContent(function(){
t.remove(t.query(o));
});
}
for(var k in scayt.dictionary){
var dic=scayt.dictionary[k];
if(dic.contains(o)){
dic.item(o).status=1;
}
}
if(_3d1=="delete"||_3d1=="restore"){
t.setLang(t.sLang,true);
}
},inFlight:false,call:function(o){
var t=this;
if(o.errorLang&&o.errorLang==t.sLang){
t.sLang=o.sLang;
}
if(o.sLang&&o.sLang!=t.sLang){
return;
}
_4.filter(o.words,function(word){
var hh;
if((hh=_4.indexOf(t.unknowWord,word))!=-1){
t.unknowWord.splice(hh,1);
return true;
}
return false;
});
_4.forEach(o.correct,function(word){
var hh;
if((hh=_4.indexOf(t.unknowWord,word))!=-1){
t.unknowWord.splice(hh,1);
}
});
t.markupWord=t.markupWord.concat(o.words);
if(!t.inFlight){
t.deffMarkup(true,true);
}
return;
},unknowWord:[],markupWord:[],onInputText:function(){
},sizeTextBig:function(){
var t=this,_3dd=String(this.containerNode[_4.isIE?"innerText":"textContent"]).length>scayt.maxSizeMarkup;
if(_3dd&&!t.isTextSizeBig){
t.isTextSizeBig=!t.isTextSizeBig;
t._onSizeTextBig();
}
if(!_3dd&&t.isTextSizeBig){
t.isTextSizeBig=!t.isTextSizeBig;
t._onSizeTextNormal();
}
},isTextSizeBig:false,checkMisspellings:function(all,_3df){
var t=this,b=(all>scayt.minCheckIncorrectWord&&_3df/all>scayt.maxPercentIncorrectWord);
if(b&&!t.isTooManyMisspellings){
t.isTooManyMisspellings=!t.isTooManyMisspellings;
t._onTooManyMisspellings();
}
if(!b&&t.isTooManyMisspellings){
t.isTooManyMisspellings=!t.isTooManyMisspellings;
t._onNormalMisspellings();
}
},isTooManyMisspellings:false,_m:function(_3e2,_3e3){
var t=this,dic=scayt.dictionary[t.sLang],_3e6=false,_3e7=false,_3e8=false,_3e9=false,_3ea=false,_3eb={},n=[],arr=[],trr,_3ef=[];
if(t.disabled||t._paused){
return;
}
t.sizeTextBig();
if(t.disabled){
return;
}
t.onInputText();
t.s.forEach(function(item,i){
var text="",r={},nr=Number(item.correct),arr=[],rem=[];
item.n={all:0,correct:0,incorrect:0};
if(item.word.length&&_3e2){
var _3f6=true;
_4.forEach(_3e2,function(word){
if(_4.indexOf(item.word,word)!=-1){
_3f6=false;
}
});
if(_3f6){
return;
}
}
_4.forEach(_3e2,function(word){
if(item.unknow[word]){
_3e7=true;
item.wrap[word]=item.unknow[word];
item.word.splice(_4.indexOf(item.word,word),1);
delete (item.unknow[word]);
}
});
arr=_4.clone(item.word);
_4.forEach(arr,function(word){
if(dic.contains(word)&&dic.item(word).status==1){
item.word.splice(_4.indexOf(item.word,word),1);
delete (item.unknow[word]);
}
});
if(_3e3!=3&&item.node.innerHTML==item.html&&(!_4.isIE||_3e3!=3)){
return;
}
item.wrap={};
if(_4.isIE){
var _3fa=t.getRng(),par2=_3fa.parentElement(),bl2=t.getParentEx(_3fa.parentElement(),t.containerNode).blockList;
}
if(_4.isIE&&(par2==item.node||(bl2&&bl2[0]==item.node))&&_3e3==1){
_3fa.moveStart("word",-7);
_3fa.moveEnd("word",2);
text=_3fa.htmlText;
}else{
text=item.full?item.node:t._textContent(item.node);
}
if(!item.full&&_4.isIE){
text=text.replace(/<(?:UL|OL)[^>]*>(((?!<\/?(?:UL|OL)[^>]*>).)*)<\/(?:UL|OL)>/gi,"<+> </+>");
}
text=t._process(text,"0-13");
text=t._clean(text,item.full||_4.isIE?"-":"+-");
while(t.regexp[18][0].exec(text)){
r={$1:RegExp.$1,$2:RegExp.$2,$3:RegExp.$3,$4:RegExp.$4,$5:RegExp.$5,leftContext:RegExp.leftContext,rightContext:RegExp.rightContext,lastMatch:RegExp.lastMatch};
text=r.leftContext+r.lastMatch.replace(/<[^>]+>/g,"")+r.rightContext;
for(var y=1;y<=5;y++){
if(!r["$"+y]){
continue;
}
_3e8=true;
rem.push(r["$"+y]);
}
}
text=" "+text+" ";
while(t.regexp[19][nr].exec(text)){
r={$1:RegExp.$1,$2:RegExp.$2,$3:RegExp.$3,$4:RegExp.$4,$5:RegExp.$5,leftContext:RegExp.leftContext};
var ttui=r.$3.match(/^([']*)?(.*)/),w1=ttui[2];
if(!w1){
continue;
}
var ttuy=w1.match(/([.'\-]*)?$/),_401=ttui[1]?String(ttui[1]).length:0,_402=ttuy[1]?String(ttuy[1]).length:0,w=_402?w1.slice(0,-_402):w1;
if(!w){
continue;
}
var ign=t.getProp(w),ig=t.isIgnore(ign),w2=r.$3;
item.n.all++;
if(r.$2&&/\d+/.test(r.$2)){
var sl=scayt.scaytList[r.$2];
if(typeof sl!="undefined"&&(w!=sl.word||sl.lang!=t.sLang)){
if(dic.contains(w)&&dic.item(w).status==0&&!ig){
item.n.incorrect++;
_4.withGlobal(t.window,"query",_4,["[scaytid="+r.$2+"]",item.node]).forEach(function(n){
if(t._getText(n)==w){
_4.attr(n,"scaytid",++scayt.i);
_4.attr(n,"scayt_word",w);
scayt.scaytList[scayt.i]=t.getProp(w);
}
});
if(item.wrap[w]){
item.wrap[w].amount++;
}else{
_3e7=true;
item.wrap[w]={amount:1,ig:ign,pos:[],ie:[],offs:[]};
}
if(item.wrap[w].ie&&_4.indexOf(item.wrap[w].ie,w2)==-1){
item.wrap[w].ie.push(w2);
item.wrap[w].offs.push({l:_401,r:_402});
}
continue;
}else{
_3e8=true;
rem.push(r.$2);
}
}else{
if(ig){
_3e8=true;
rem.push(r.$2);
}else{
}
}
}
if(_3eb[w]){
continue;
}
if(/^[\d\-.]+$/.test(w)||(new RegExp("^"+String.fromCharCode(8211)+"+$","")).test(w)||ig){
_3eb[w]=1;
continue;
}
var _409=_4.isIE?0:_401+t._process(t._trimNl(r.leftContext+r.$1),[14]).length-1;
if(dic.contains(w)){
switch(dic.item(w).status){
case -1:
if(_4.indexOf(item.word,w)==-1){
item.word.push(w);
}
if(item.unknow[w]&&item.unknow[w].pos){
item.unknow[w].amount++;
item.unknow[w].pos.push(_409);
}else{
item.unknow[w]={amount:1,ig:ign,pos:[_409],ie:[],offs:[]};
}
if(item.unknow[w].ie&&_4.indexOf(item.unknow[w].ie,w2)==-1){
item.unknow[w].ie.push(w2);
item.unknow[w].offs.push({l:_401,r:_402});
}
break;
case 0:
item.n.incorrect++;
if(item.wrap[w]&&item.wrap[w].pos){
item.wrap[w].amount++;
item.wrap[w].pos.push(_409);
}else{
_3e7=true;
item.wrap[w]={amount:1,ig:ign,pos:[_409],ie:[],offs:[]};
}
if(item.wrap[w].ie&&_4.indexOf(item.wrap[w].ie,w2)==-1){
item.wrap[w].ie.push(w2);
item.wrap[w].offs.push({l:_401,r:_402});
}
break;
}
continue;
}
if(_4.indexOf(n,w)==-1){
if(/[^.]+\.+/.test(w)){
_3e7=true;
item.n.incorrect++;
item.wrap[w]={amount:1,ig:ign,pos:[_409],ie:[],offs:[]};
if(item.wrap[w].ie&&_4.indexOf(item.wrap[w].ie,w2)==-1){
item.wrap[w].ie.push(w2);
item.wrap[w].offs.push({l:_401,r:_402});
}
var gh=_4.indexOf(item.word,w);
if(gh!=-1){
item.word.splice(gh,1);
}
item.unknow[w]={};
delete (item.unknow[w]);
dic.add(w,{sugg:[w.replace(/(\.+)/g,". ")],status:0});
continue;
}else{
if(scayt._errorPaused){
continue;
}
if(_4.indexOf(t.unknowWord,w)==-1){
t.unknowWord.push(w);
}
n.push(w);
if(!scayt.off){
dic.add(w,{sugg:[],status:-1});
}
}
}
if(scayt._errorPaused){
continue;
}
if(_4.indexOf(item.word,w)==-1){
item.word.push(w);
}
if(item.unknow[w]&&item.unknow[w].pos){
item.unknow[w].amount++;
item.unknow[w].pos.push(_409);
}else{
item.unknow[w]={amount:1,ig:ign,pos:[_409],ie:[],offs:[]};
}
if(item.unknow[w].ie&&_4.indexOf(item.unknow[w].ie,w2)==-1){
item.unknow[w].ie.push(w2);
item.unknow[w].offs.push({l:_401,r:_402});
}
}
t.checkMisspellings(item.n.all,item.n.incorrect);
_4.forEach(rem,function(_40b){
_4.withGlobal(t.window,"query",_4,["[scaytid="+_40b+"]",item.node]).forEach(function(n){
item.remove.push(n);
});
});
});
if(!scayt.off&&!t._errorPaused){
if(n.length&&(n.length>scayt.minSendWords||t.alwaysSend)){
var _40d="",_40e="";
for(var k=0;k<n.length;k++){
if(!_4.isString(n[k])){
break;
}
_40e=_40e+(_40e?",":"")+encodeURIComponent(n[k]);
if(_40e.length<scayt["sizeRequest"+(_4.isIE?"IE":"")]){
_40d=_40d+(_40d?",":"")+n[k];
}else{
arr.push(_40d);
_40d=n[k];
_40e="";
}
}
arr.push(_40d);
var str=arr[0];
if(t._serializeUnknowWord.length){
trr=t._serializeUnknowWord[t._serializeUnknowWord.length-1]+","+arr[arr.length-1];
if(trr.length<1200){
t._serializeUnknowWord[t._serializeUnknowWord.length-1]=trr;
arr.pop();
}
t._serializeUnknowWord=t._serializeUnknowWord.concat(arr);
}else{
t._serializeUnknowWord=arr;
}
t.sendSerializeWord(true);
}else{
if(n.length&&n.length<=scayt.minSendWords){
if(t._setActive){
_3ea=true;
if(t._serializeUnknowWord.length){
trr=t._serializeUnknowWord[t._serializeUnknowWord.length-1]+","+t.filterWords(n);
if(trr.length<1200){
t._serializeUnknowWord[t._serializeUnknowWord.length-1]=trr;
}else{
t._serializeUnknowWord.push(t.filterWords(n));
}
}else{
_4.forEach(n,function(word){
var num;
if((num=_4.indexOf(t.stackUnknowWord,word))!=-1){
t.stackUnknowWord.splice(num,1);
}
});
scayt.send({words:t.filterWords(n),slang:t.sLang},null,t);
}
}else{
if(t._serializeUnknowWord.length){
trr=t._serializeUnknowWord[t._serializeUnknowWord.length-1]+","+t.filterWords(n);
if(trr.length<1200){
t._serializeUnknowWord[t._serializeUnknowWord.length-1]=trr;
}else{
t._serializeUnknowWord.push(t.filterWords(n));
}
}else{
_3e9=true;
t.stackUnknowWord=t.stackUnknowWord.concat(n);
_4.forEach(n,function(word){
dic.remove(word);
});
}
}
}
}
}
arr=[];
t._setContent(function(){
t.s.forEach(function(item,i){
if(item.remove.length){
t.remove(item.remove);
item.remove=[];
}
if(_3e9){
_4.forEach(n,function(word){
if(item.unknow[word]){
item.word.splice(_4.indexOf(item.word,word),1);
delete (item.unknow[word]);
}
});
}
var _417=[],_418={},_419=false,g1=0;
for(var g in item.wrap){
if(g1<22){
_418[g]=item.wrap[g];
delete (item.wrap[g]);
_419=true;
}else{
_417.push(g);
}
g1++;
}
if(_419){
try{
t.wrap(_418,item.node,item.full);
}
catch(e){
t.wrap(_418,item.node,item.full,true);
}
if(!_417.length){
item.wrap={};
}else{
_4.forEach(_417,function(word){
if(_4.indexOf(t.markupWord,word)==-1){
t.markupWord.push(word);
}
});
if(!t.inFlight){
setTimeout(function(){
t.deffMarkup(true);
},4000);
}
}
}
var bb=_4.isIE?_3e3:0;
if(item.full==0&&bb==1){
item.full=0;
item.correct=1;
item.html=item.node.innerHTML;
arr.push(item);
return;
}
if(_3e9||_3ea||(item.word.length||_417.length)){
item.html=item.node.innerHTML;
arr.push(item);
}
});
});
if(!_3e9){
t.s._s=arr;
}
},_setActive:false,isIgnore:function(word){
var prop=_4.isString(word)?this.getProp(word):word;
for(var k in prop.attr){
if(this[k]&&prop.attr[k]){
return true;
}
}
return false;
},getProp:function(word){
var t=this,l=!(String(word).length==1),uc=word.toUpperCase(),lc=word.toLowerCase(),obj={word:word,lang:t.sLang,ignoreDictionary:scayt.ignoreDictionary.contains(word),attr:{}},tmp={allCaps:uc==word,mixedCase:(uc.slice(1)!=word.slice(1)&&lc.slice(1)!=word.slice(1)),mixedWithDigits:/\d+/.test(word),ignoreDomainNames:(/\.+/.test(word)||(new RegExp("^(?:"+t.domainNameList+")$")).test(word))};
for(var k in tmp){
obj.attr[k]=tmp[k]&&l?1:0;
}
return obj;
},wrap:function(_429,_42a,full,_42c){
var t=this,rng=t.getRng(),_42f=false,_430,par,x,_433;
function _434(node,word){
_4.attr(node,"scaytid",++scayt.i);
_4.attr(node,"scayt_word",word);
if(_4.isIE){
_4.addClass(node,"scayt-misspell");
}
};
if(_4.isIE){
if(t.document.body.setActive){
if(_42c){
t.document.body.setActive();
}
}
for(x in _429){
_430=rng.duplicate();
try{
_430.moveToElementText(_42a);
}
catch(e){
_430=t.document.body.createTextRange();
}
var rd=_430.duplicate();
for(var j=0;j<_429[x].ie.length;j++){
var xx=_429[x].ie[j],offs=_429[x].offs[j];
for(var i=0;_430.findText(xx,1000000,2)&&_430.compareEndPoints("StartToStart",rd)>-1&&_430.compareEndPoints("EndToEnd",rd)<1;i++){
_430.moveStart("character",offs.l);
_430.moveEnd("character",-offs.r);
var rss=_430.duplicate();
rss.moveEnd("character",1);
var _43d=String(rss.text).charAt(String(rss.text).length-1);
var tx=rss.text;
rss.expand("word");
var rss1=rss.duplicate();
rss1.moveEnd("word",1);
if(/[.-]/.test(_43d)&&!(rss.text!=tx)&&rss.text!=rss1.text){
_430.collapse(false);
continue;
}
rss=_430.duplicate();
rss.moveStart("character",-2);
var _440=String(rss.text).charAt(0);
var _441=String(rss.text).charAt(1);
tx=rss.text;
rss.expand("word");
if(/[.-]/.test(_441)&&(!(rss.text!=tx)||!/[.-]/.test(_440))){
_430.collapse(false);
continue;
}
if(_430.text!=x||/.*<(SUP|SUB)>/.test(_430.htmlText)||/<\/?(SUP|SUB)>.*/.test(_430.htmlText)){
_430.collapse(false);
continue;
}
par=t.getParentEx(_430.parentElement(),_42a);
if(full==1){
par.block=null;
}else{
par.block=_42a!=par.block?par.block:null;
}
var ig=par.ignore[0]||par.block||par.scayt[0];
if(ig){
_430.moveToElementText(ig);
_430.collapse(false);
continue;
}else{
var sp=_4.clone(t.span);
_434(sp,x);
scayt.scaytList[scayt.i]=_429[x].ig;
var html=_430.htmlText,r=_430.duplicate(),rp=r.parentElement(),rw=_430.htmlText.replace(/<[^A].*>/gi,"");
if(!_42f){
t._onBeforeMark();
_42f=true;
}
if(rp.nodeName=="P"&&r.htmlText==rp.outerHTML){
sp.innerHTML=rp.innerHTML;
rp.appendChild(sp);
_430.text="";
_430.moveToElementText(sp);
}else{
if(/<\/A>((<[^>]*>)*)?$/gi.test(_430.htmlText)&&/^((<[^>]*>)*)?<A/gi.test(_430.htmlText)){
_433=String(_430.text).length;
while(rp.nodeName!="A"&&rp.nodeName!="BODY"){
rp=rp.parentNode;
}
try{
sp.innerHTML=_430.htmlText.match(/<A[^>]*>(.*)<\/A>/i)[1];
}
catch(e){
sp.innerHTML=_430.htmlText;
}
_430.text="";
if(rp.firstChild){
_4.withGlobal(t.window,"place",_4,[sp,rp.firstChild,"before"]);
}else{
rp.appendChild(sp);
}
_430.moveToElementText(sp);
_430.collapse(false);
_430.moveEnd("character",_433);
}else{
r.collapse(true);
var rl;
rl=r.parentElement();
sp.innerHTML=html;
var _449=new RegExp("^(?:"+t.block+")$","i");
if(!(_449.test(rl.nodeName))&&rl!=rp){
while(!(_449.test(rl.nodeName))&&rl!=t.containerNode&&rl.parentNode.lastChild==rl){
rl=rl.parentNode;
}
_430.text="";
_4.withGlobal(t.window,"place",_4,[sp,rl,"after"]);
_430.moveToElementText(sp);
_430.collapse(false);
}else{
var div=_4.create("div",{innerHTML:html});
while(div.childNodes.length==1&&div.firstChild.nodeType!=3){
div=div.firstChild;
}
html=div.innerHTML;
_430.pasteHTML("<span class=\"scayt-misspell\" scaytid=\""+(scayt.i++)+"\" scayt_word=\""+x+"\">"+html+"</span>");
}
}
}
}
_430.collapse(false);
}
}
}
}else{
for(x in _429){
var pos=_429[x].pos,span=_4.clone(t.span),_44d=t.getProp(x);
_433=String(x).length;
_4.forEach(pos,function(pos){
var sp=_4.clone(span),sd=t._getPos(pos,pos+_433,_42a);
if(!sd){
return;
}
_430=rng.cloneRange();
_434(sp,x);
scayt.scaytList[scayt.i]=_429[x].ig;
var sn,en;
if((sn=sd.startNode.parentNode)!=(en=sd.endNode.parentNode)){
var par=_5.range.getCommonAncestor(sd.startNode.parentNode,sd.endNode.parentNode);
var node=sd.startNode;
if(sd.startOffset===0){
while(node!=par&&node.nodeName!="A"&&node.parentNode.firstChild==node){
node=node.parentNode;
}
}
sd.startNode=node;
node=sd.endNode;
if(node.length==sd.endOffset){
while(node.parentNode!=par&&node.nodeName!="A"&&node.parentNode.lastChild==node){
node=node.parentNode;
}
sd.endAfterNode=node;
}
}
par=t.getParentEx(sd.startNode,_42a);
if(par.isIgnore||par.isScayt){
return;
}
try{
_430.setStart(sd.startNode,sd.startOffset);
_430.setEnd(sd.endNode,sd.endOffset);
if(sd.endAfterNode){
_430.setEndAfter(sd.endAfterNode);
}
if(_430.toString()!=x){
return;
}
if(!_42f){
t._onBeforeMark();
_42f=true;
}
sp.appendChild(_430.extractContents());
_430.insertNode(sp);
}
catch(e){
}
});
}
}
if(t.containerNode.normalize&&!_4.isIE){
t.containerNode.normalize();
}
if(_42f){
t._onMark();
}
},remove:function(node,_456,_457){
if(!arguments.length){
return;
}
if(!_4.isArray(node)){
node=[node];
}
var t=this,rng=t.getRng(),_45a=new _4.NodeList(),_45b,_45c,_45d=[];
if(!_457){
t._onBeforeUnMark();
}
_4.forEach(node,function(node){
if(_4.isIE){
_45b=t.document.createDocumentFragment();
for(var i=0;i<node.childNodes.length;i++){
_45b.appendChild(_4.clone(node.childNodes[i]));
}
var n1=t.document.createElement("div");
if(_45b.firstChild&&_45b.firstChild.nodeType!=3){
_45d.push(_45b.firstChild);
}
if(_45b.firstChild&&_45b.lastChild.nodeType!=3&&_45b.lastChild!=_45b.firstChild){
_45d.push(_45b.lastChild);
}
node.parentNode.replaceChild(_45b,node);
}else{
_45c=rng.cloneRange();
try{
_45c.selectNode(node);
_45b=_45c.createContextualFragment(node.innerHTML);
if(_45b.firstChild&&_45b.firstChild.nodeType!=3){
_45d.push(_45b.firstChild);
}
if(_45b.firstChild&&_45b.lastChild.nodeType!=3&&_45b.lastChild!=_45b.firstChild){
_45d.push(_45b.lastChild);
}
_45c.deleteContents();
_45c.insertNode(_45b);
}
catch(ex){
}
}
for(var i=0;i<t.afterMarkupRemove.length;i++){
for(var j=0;j<_45d.length;j++){
t.afterMarkupRemove[i](_45d[j]);
}
}
});
if(t.containerNode.normalize&&!_4.isIE){
t.containerNode.normalize();
}
if(!_457){
t._onUnMark();
}
},_onBeforeUnMark:function(){
this._onBeforeChange();
this.onBeforeUnMark();
},_onBeforeMark:function(){
this._onBeforeChange();
this.onBeforeMark();
},_onBeforeChange:function(){
this.onBeforeChange();
},_onBeforeReplace:function(node,word){
this.onBeforeReplace();
this._onBeforeChange();
},onBeforeUnMark:function(){
},onBeforeMark:function(){
},onBeforeChange:function(){
},onBeforeReplace:function(node,word){
},_onUnMark:function(){
this._onChange();
this.onUnMark();
},_onMark:function(){
this._onChange();
this.onMark();
},_onChange:function(){
this.onChange();
},onUnMark:function(){
},onMark:function(){
},onChange:function(){
},_onReplace:function(_466,word){
this.onReplace();
this._onChange();
},onReplace:function(_468,word){
},_onIgnore:function(node){
this.onIgnore();
this._onChange();
},_onIgnoreAll:function(word){
this.onIgnoreAll();
this._onChange();
},onIgnore:function(node){
},onIgnoreAll:function(word){
},_onSetDisabled:function(_46e){
this.onSetDisabled(_46e);
},onSetDisabled:function(_46f){
},_onLangChange:function(lang){
this.onLangChange(lang);
},onLangChange:function(lang){
},_onOptionChange:function(_472,_473){
this.onOptionChange(_472,_473);
},onOptionChange:function(_474,_475){
},_onSizeTextBig:function(){
this.onSizeTextBig();
},_onSizeTextNormal:function(){
this.onSizeTextNormal();
},onSizeTextBig:function(){
},_onTooManyMisspellings:function(){
this.onTooManyMisspellings();
},_onNormalMisspellings:function(){
this.onNormalMisspellings();
},onNormalMisspellings:function(){
},onTooManyMisspellings:function(){
},onSizeTextNormal:function(){
},onBeforeStateChange:function(_476){
},onStateChange:function(_477){
},stateChange:function(_478){
var t=this,_47a=t.state;
if(_47a==_478){
return;
}
t.state=_478;
if(_478=t.onBeforeStateChange(_478)){
t.state=_478;
}
if(_47a==t.state){
return;
}
if(t.state==scayt.STATE_DISABLED){
t.setDisabled(true);
}
if(t.state==scayt.STATE_ENABLED){
t.setDisabled(false);
}
if(_47a!=t.state){
t.onStateChange(t.state);
}
},replace:function(node,word){
var t=this;
if(t._paused){
return;
}
if(!arguments.length){
return null;
}
if(!_4.isArray(node)){
node=new _4.NodeList(node);
}
var _47e=t.getWord(node[0]);
var _47f=word.length;
var b=(new RegExp(". ","g")).test(word);
word=this.document.createTextNode(word);
var _481=[];
t._onBeforeReplace(node,word);
node.forEach(function(node){
if(b){
var _483=t.getParentEx(node).block;
if(t.s.contains(_483)==-1){
t.s.add(_483,{full:0});
}
}
var w=_4.clone(word);
_481.push(w);
_4.withGlobal(t.window,"place",_4,[w,node,"after"]);
_4.withGlobal(t.window,"_destroyElement",_4,[node]);
});
if(!_4.isIE){
var sel=t.getSel(),_486=t.getRng();
_486.setStart(_481[0],_47f);
_486.collapse(true);
if(!_4.isOpera){
sel.removeAllRanges();
}
sel.addRange(_486);
t.containerNode.focus();
}else{
t.containerNode.setActive();
var _486=t.getRng();
_486.move("character",_47f);
_486.select();
}
if(b){
t._m();
}
t._onReplace(_47e,word);
},getSel:function(){
return _4.isIE?this.document.selection:this.window.getSelection();
},getRng:function(){
var t=this,_488,sel=t.getSel();
try{
if(sel){
_488=sel.rangeCount>0?sel.getRangeAt(0):(sel.createRange?sel.createRange():t.document.createRange());
}
}
catch(ex){
}
if(!_488||(_4.isIE&&typeof _488.text=="undefined")){
if(_4.isIE){
try{
_488=t.containerNode.createTextRange();
}
catch(e){
_488=t.document.body.createTextRange();
}
}else{
_488=t.document.createRange();
}
}
return _488;
},getParentElement:function(){
var _48a=_4.global.getSelection();
if(_48a){
var node=_48a.anchorNode;
while(node&&(node.nodeType!=1)){
node=node.parentNode;
}
return node;
}
},getParent:function(node,_48d,attr,root){
var t=this;
root=root||this.containerNode;
_48d=_48d?new RegExp("^(?:"+_48d+")$"):null;
while(node&&node!=root){
if(_48d&&_48d.test(node.nodeName.toUpperCase())){
return node;
}
if(attr){
for(var k in attr){
if(_4.attr(node,k)!==null&&attr[k]===null){
return node;
}
if((attr[k]!==null&&_4.attr(node,k)!==null&&!(_4.attr(node,k)===false))?!attr[k]:attr[k]){
return node;
}
}
}
node=node.parentNode;
}
return false;
},getSelectionNode:function(){
var node;
try{
if(_4.isIE){
var rng=this.getRng();
if(rng.parentElement){
node=rng.parentElement();
}else{
node=null;
}
}else{
node=this.getSel().anchorNode;
}
}
catch(e){
node=null;
}
return node;
},getParentEx:function(node,root){
var t=this,o={isBlock:false,block:null,blockList:[],isIgnore:false,ignore:[],isScayt:false,scayt:[]},_498="",_499=new RegExp("^(?:"+t.block+")$","i"),form=new RegExp("^(?:"+t.form+")$","i");
root=root||t.containerNode;
node=node||t.getSelectionNode();
while(node&&node!=root){
_498=node.nodeName.toUpperCase();
if(_498=="SPAN"&&_4.attr(node,"scayt_word")){
o.scayt.push(node);
o.isScayt=true;
}
if(_498.toUpperCase()=="STYLE"||form.test(_498)||_4.attr(node,"scayt")=="false"||_4.attr(node,"widgetId")||_4.attr(node,"contenteditable")=="false"){
o.ignore.push(node);
o.isIgnore=true;
}else{
if(_499.test(_498)){
o.blockList.push(node);
o.isBlock=true;
}
}
node=node.parentNode;
}
o.blockList.push(root);
o.block=o.blockList[0];
return o;
},getParentEditBlock:function(node,root){
var t=this,_49e="";
root=root||t.containerNode;
node=node||t.getSelectionNode();
while(node&&node!=root){
_49e=node.nodeName.toUpperCase();
if(_4.style(node,"zoom")!="normal"||_4.style(node,"position")=="absolute"||node.nodeName=="INPUT"||node.nodeName=="TEXTAREA"){
return node;
}
node=node.parentNode;
}
return node;
},_getPos:function(sp,ep,root){
var t=this,nv,nvl,p=0,n,d={},r=root||t.containerNode,w=t.document.createTreeWalker(r,NodeFilter.SHOW_TEXT,null,false);
while(n=w.nextNode()){
nv=n.nodeValue||"";
nvl=t._trimNl(nv).length;
p+=nvl;
if(p>sp&&!d.startNode){
d.startNode=n;
d.startOffset=sp-(p-nvl);
}
if(p>=ep){
d.endNode=n;
d.endOffset=ep-(p-nvl);
return d;
}
}
return null;
},_trimNl:function(str){
return (_4.isOpera||_4.isIE)?str:str.replace(/\r\n/g," ");
},_getText:function(node){
return String(_4.isIE?node.nodeType==3?node.nodeValue:node.innerText:node.textContent).replace(/[<>]/g," ");
},_getHtml:function(node){
return String(node.nodeType==3?String(_4.isIE?node.nodeValue:node.textContent).replace(/[<>]/g," "):node.innerHTML);
},_inline:"BR|IMG|HR|INPUT",_textContent:function(node,o){
var t=this,_4b0=new RegExp("^(?:"+t.block+")$","i"),_4b1=new RegExp("^(?:"+t.form+")$","i"),_4b2=new RegExp("^(?:"+t._inline+")$","i"),text="",cn,sp=0,str="";
for(var i=0;i<node.childNodes.length;i++){
cn=node.childNodes[i];
if(_4b1.test(cn.nodeName)){
str+="<->"+(_4.isIE?"":t._trimNl(t._getText(cn)).replace(/./gi," "))+"</->";
}else{
if(_4b0.test(cn.nodeName)){
str+="<+>"+(_4.isIE?"":t._trimNl(t._getText(cn)).replace(/./gi," "))+"</+>";
}else{
if(_4b2.test(cn.nodeName)){
str+="<>";
}else{
if(cn.nodeName=="SPAN"&&(sp=_4.attr(cn,"scaytid"))){
str+="<"+sp+">"+t._trimNl(t._getHtml(cn))+"</>";
}else{
str+=t._trimNl(t._getHtml(cn));
}
}
}
}
}
return str;
},regexp:[],separator:"\\s!\"#$&%()*+,/`~:;=?[]^{|} "+(function(){
var arr=["8212","8364","0167","0169","0171","0174","0177","0182","0183","0184","0187","0191","0215","0247","0164","FFFD","2013","201d","201e","201c","00a0","3000","3001","3002","3003"];
var s="";
for(var i=0;i<arr.length;i++){
s+=String.fromCharCode(arr[i]);
}
return s;
})(),_getRegExp:function(str){
var t=this,re="",sep=t.separator;
for(var i=0;i<sep.length;i++){
re+=(sep.charAt(i)!="-"?"\\":"")+sep.charAt(i);
}
return [[/&[^;]+;/g," "],[/<(IMG|BR|HR|INPUT)[^>]*>/gi,"<>"],[/<SPAN[^>]*scaytid=[\'"]{0,1}(\d+)[\'"]{0,1}[^>]*>/gi,"<$1>"],[/<(\/?)SPAN[^>]*>/gi,"<$1=>"],[new RegExp("<(/?)(?:"+t.form+")[^>]*>","gi"),"<$1->"],[new RegExp("<(/?)(?:"+t.block+")[^>]*>","gi"),"<$1+>"],[new RegExp("</?(?!(?:[+-=]|\\d+))[^>]+>","gi"),""],[[/<(\d+)>(((?!<\/?=>).)*)<\/=>/gi,"<$1>$2</>"],[/<=>(((?!<\/?=>).)*)<\/=>/gi,"$1"]],[new RegExp("(?!/)["+re+"](?!>)","gi")," "],[/[ ]\//g,"  "],[],[],[],[],[/<[^>]*>/gi,""],[/(?:<(\d+|=)>|<\/=?>)/gi,""],[/<[^>]*>/gi," "],[/[ ]+/g," "],[new RegExp("(?:[^ <>.']<(\\d*)>[^<]*</>|<(\\d*)>[^<]*</>[^ <>.']|<(\\d*)>[^<]*</><(\\d*)>[^<]*</>|<(\\d*)>[^<]*<>[^<]*</>)","gi")],[new RegExp("(?:([ ])|<([^>\\d]*)>)([^<> ]+)(?=(?:([ ])|<([^>\\d]*)>))","g"),new RegExp("(?:([ ])|<([^>]*)>)([^<> ]+)(?=(?:([ ])|<([^>]*)>))","g")]];
},_process:function(str,arr){
var t=this,str1="";
str=t.is(str)?str:t.containerNode;
if(!_4.isString(str)){
str=str.innerHTML;
}
if(_4.isString(arr)){
var tmp=arr.split("-");
arr=[];
for(var i=tmp[0];i<=tmp[1];i++){
arr.push(i*1);
}
}
function _4c6(r,str){
_4.forEach(r,function(k){
str=str.replace(k[0],k[1]);
});
return str;
};
_4.forEach(arr,function(item,j){
if(_4.isArray(t.regexp[item][0])){
var i=0,k=0,text=str,r={},bb=false;
str="";
var str1=text;
while((new RegExp("(<([\\d]+)>|<(/?)=>)","g")).exec(text)){
bb=true;
r={$1:RegExp.$1,$2:RegExp.$2,$3:RegExp.$3,$4:RegExp.$4,$5:RegExp.$5,leftContext:RegExp.leftContext,rightContext:RegExp.rightContext};
if(r.$2){
str+=r.leftContext+"<"+r.$2+">";
k++;
}else{
if(k){
if(r.$3){
if(!i){
str+=r.leftContext+"</>";
k--;
}else{
str+=r.leftContext;
i--;
}
}else{
str+=r.leftContext;
i++;
}
}else{
str+=r.leftContext;
}
}
text=r.rightContext;
}
str+=bb?r.rightContext:str1;
str=str.replace(/([^<])\//g,"$1 ");
}else{
str=_4c6([t.regexp[item]],str);
}
});
return str;
},s:function(b){
var t=this;
return {_s:[],count:0,contains:function(node,full){
var b=-1,tmp=false;
full=full||0;
_4.forEach(this._s,function(item,i){
if(item.node==node&&item.full==full){
b=i;
}
});
return b;
},add:function(node,o){
o=o||{};
if(this.contains(node,o.full)==-1){
this.count++;
return this._s.push(_4.mixin({node:node,correct:1,full:1,html:null,s:null,wrap:{},unknow:{},remove:[],word:[],n:{}},o));
}
return false;
},clear:function(){
this._s=[];
this.count=0;
},getAttr:function(o){
if(!_4.isObject(o)){
throw ("incorrect arguments");
}
return _4.filter(this._s,function(item,i){
for(var k in o){
if(item[k]===null||Boolean(o[k])!=Boolean(item[k])){
return false;
}
}
return true;
});
},forEach:function(_4e0,_4e1,o){
_4.forEach((o?this.getAttr(o):this._s),_4e0,_4e1);
},item:function(_4e3,full){
if(typeof _4e3!="number"){
_4e3=this.contains(_4e3,full);
}
if(_4e3==-1){
return false;
}
return this._s[_4e3];
},remove:function(_4e5,full){
if(typeof _4e5!="number"){
_4e5=this.contains(_4e5,full);
}
if(_4e5==-1){
return false;
}
this._s.splice(_4e5,1);
this.count--;
}};
},_clean:function(str,c){
var text="",tag1,tag2,s=str,_4ed="",_4ee="",_4ef=new RegExp("(</?["+c+"]>)(((?!</?["+c+"]>).)*)(</?["+c+"]>)","gi"),r,i=0,_4f2,_4f3;
c=c||"+-";
while(_4ef.exec(str)){
tag1=String(RegExp.$1);
_4f2=String(RegExp.$2);
tag2=String(RegExp.$4);
_4ed=RegExp.leftContext;
_4ee=RegExp.rightContext;
if(tag1.length==3){
i++;
}else{
i--;
}
if(i==1&&tag2.length==4){
str=_4ee;
i--;
_4f3=tag2;
}else{
str=tag2+_4ee;
_4f3="";
}
_4ef.lastIndex=0;
text+=_4ed+tag1+_4f2.replace(/<[^>]*>/gi,"").replace(/./gi," ")+_4f3;
}
text+=_4ee;
return text||s;
},is:function(o,t){
var n=typeof (o);
if(!t){
return n!="undefined";
}
if(t=="array"&&(o instanceof Array)){
return true;
}
return n==t;
},query:function(word){
if(_4.isString(word)){
word=[word];
}
if(!word){
word="*";
}
var t=this,nl=new _4.NodeList();
_4.forEach(word,function(word){
_4.withGlobal(t.window,"query",_4,["[scayt_word"+(word=="*"?"":"=\""+word+"\"")+"]",t.containerNode]).forEach(function(item){
nl.push(item);
});
});
return nl;
},_serializeOptions:function(){
var str="";
for(var x in this._optionNames){
str+=String(this[x]*1);
}
return str;
},destroy:function(){
this.destroyCalled=true;
this.uninitialize();
_4.forEach(this._connects,function(_4fe){
_4.forEach(_4fe,_4.disconnect);
});
if(this.iframe){
scayt.cssStyleNode.remove(this.document);
}
_4.forEach(this._supportingWidgets||[],function(w){
if(w.destroy){
w.destroy();
}
});
this.destroyRendering(true);
_5.registry.remove(this.id);
},destroyRecursive:function(_500){
},uninitialize:function(_501){
this.setDisabled(true);
_4.attr(this.containerNode,{"spellcheck":this.defaultSpellcheck});
_4.removeClass(this.containerNode,"scayt-disabled");
_4.removeClass(this.containerNode,"scayt-enabled");
var num;
if((num=_4.indexOf(scayt._hash,this))!=-1){
scayt._hash.splice(num,1);
}
}});
_4.mixin(scayt,{STATE_DISABLED:0,STATE_ENABLED:1,STATE_OFFLINE:2,STATE_BIG_TEXT:3,maxSizeMarkup:6000,persist:true,off:false,time:3000,minTime:3000,minCheckIncorrectWord:10,maxPercentIncorrectWord:0.8,sizeRequest:4200,sizeRequestIE:1200,disabled:false,i:0,dictionary:{},ignoreDictionary:new _6.collections.Dictionary(),dynamicStyleMap:{},defaultLang:"en_US",_defaultLang:"en_US",verLang:0,timeout:30000,manager:500,langURL:_4.moduleUrl("langURL","lang_list.js").toString(),langList:[],scaytURL:_4.moduleUrl("scaytURL","script/ssrv.cgi").toString(),imageDataURL:"data:image/gif;base64,R0lGODlhBAADAIABAP8NDQAAACH5BAEAAAEALAAAAAAEAAMAAAIFRB5mGQUAOw==",imageURL:_4.moduleUrl("imageURL","i/waveline.gif").toString(),siteURL:"http://www.spellchecker.net/",logoURL:_4.moduleUrl("imageURL","i/logo.gif").toString(),scaytList:[],_hash:[],sessionId:"",userDictionaryName:"",customDictionaryIds:[],uiTags:[1,1,1,1],_io:{},amountError:4,_currError:0,version:{scaytMajor:2,scaytMinor:5,scaytPatch:5,scaytFlag:"",scaytRevision:"$Revision: 215 $".match(/\d+/),toString:function(){
with(scayt.version){
return scaytMajor+"."+scaytMinor+"."+scaytPatch+scaytFlag+" ("+scaytRevision+")";
}
}},getAboutInfo:function(){
return {version:scayt.version,siteURL:scayt.siteURL,logoURL:scayt.logoURL};
},_getCookieName:function(name,i){
return (_4.isIE?("["+(window.location.pathname.match(/([^\/]*)$/g)[0]||"")+"]"):"")+(name||"scayt")+"_"+i;
},_restoreState:function(obj,key){
_4.forEach(key,function(item){
var _508=scayt._getCookieName(obj.id,item);
var _509=_4.cookie(_508);
if(_509){
obj[item]=_4.fromJson(_509);
}
});
},_saveState:function(obj,key){
_4.forEach(key,function(item){
_4.cookie(this._getCookieName(obj.id,item),_4.toJson(obj[item]),{expires:365,path:(_4.isIE?window.location.pathname.replace(/\/[^\/]*$/,"/"):window.location.pathname)});
},this);
},_errorTime:10000,_countAfterError:0,_maxAfterError:3,_maxDicWords:100,minSendWords:3,maxCritSendWords:600,_responseTimeList:[],customerid:"1:ncttD3-fIoSf2-huzwE4-Y5muI2-mD0Tt-kG9Wz-UEDFC-tYu243-1Uq474-d9Z2l3",init:false,versionProtocol:"2.1",_errorPaused:false,_errorCallbackName:false,activateEditableBlockOnMouseClick:false,send:function(_50d,_50e,obj){
var t=this;
if(scayt.isIframeLoaded){
return t._send(_50d,_50e,obj);
}else{
scayt._iframeLoaders.push(function(){
t._send(_50d,_50e,obj);
});
}
},_send:function(_511,_512,obj){
var _514={cmd:"sc",v:scayt.versionProtocol,format:"json",verLang:scayt.verLang,customerid:scayt.customerid,sid:scayt.sessionId};
_4.mixin(_514,_4.clone(scayt._io));
scayt._io={};
_4.mixin(_514,_511);
_512=_512||{};
var tmp={callbackParamName:"c",timeout:scayt.timeout,url:(scayt.scaytURL),content:_514,handleAs:"json",frameDoc:scayt.frameDoc,handle:function(_516,_517){
if(_516.error){
scayt.err(_516);
return _4.mixin(new Error("Error"),_516);
}
if(_517.args.content&&_517.args.content.words&&_517.args.content.words.split(",").length<13){
scayt._responseTimeList.push((new Date).getTime()-td.startTime);
var s=0,k=0;
for(var i=0;i<scayt._responseTimeList.length;i++){
s+=scayt._responseTimeList[i];
}
s=Math.round(s/scayt._responseTimeList.length);
s=s>scayt.maxCritSendWords?s-scayt.maxCritSendWords:0;
scayt.time=scayt.minTime+s;
}
return _516;
}};
_4.mixin(tmp,_512);
if(!scayt.init){
_4.mixin(tmp.content,{"init":1});
if(scayt.customDictionaryIds.length){
_4.mixin(tmp.content,{"cust_dic_ids":scayt.customDictionaryIds.join(",")});
}
if(scayt.userDictionaryName){
_4.mixin(tmp.content,{"udn":scayt.userDictionaryName});
}
scayt.init=true;
}
if(obj&&!obj.init){
if(obj._userDictionaryName){
_4.mixin(tmp.content,{"udn":obj._userDictionaryName});
}
if(obj._customDictionaryIds&&obj._customDictionaryIds.length){
_4.mixin(tmp.content,{"cust_dic_ids":obj._customDictionaryIds.join(",")});
}
if(scayt.ads&&scayt.ads.params&&!scayt.ads.params[obj.assocApp]){
_4.mixin(tmp.content,{"product":obj.assocApp});
}
obj.init=true;
}
var td=_4.io.script.get(tmp);
td.addCallbacks(function(_51c){
scayt.call(_51c);
if(scayt._errorPaused){
scayt._errorCallbackName=false;
scayt._errorPaused=false;
_4.forEach(scayt._hash,function(_51d){
_51d._setContent(function(){
_51d.s.add(_51d.containerNode);
_51d._m(null,3);
});
});
}else{
}
return _51c;
},function(_51e){
if(!scayt._errorPaused){
var args=td.ioArgs.args,dic=scayt.dictionary;
scayt._errorPaused=true;
td.ioArgs.args.scaytErr=true;
for(var i in dic){
dic[i].forEach(function(item){
if(item.value.status==-1){
dic[i].remove(item.key);
}
});
}
scayt._errorCallbackName=td.ioArgs.id;
}
if(td.ioArgs.id==scayt._errorCallbackName){
setTimeout(function(){
var _523=scayt._send(td.ioArgs.args.content);
scayt._errorCallbackName=_523.ioArgs.id;
},_51e.error?scayt.timeout:0);
}
if(_51e.dname){
return;
}
return _51e;
});
return td;
},_sendUserDictionary:function(name,_525,_526){
if(!_4.isString(name)){
console.error("Incorrect name");
return false;
}
return function(type){
scayt._io={cmd:"dictionary",action:type};
if(name&&type!="addword"){
_4.mixin(scayt._io,{dname:name});
}else{
if(type=="addword"){
_4.mixin(scayt._io,{word:name});
}
}
var td=scayt._send(scayt._io,{timeout:scayt.timeout,handleAs:"json",callbackParamName:"callback",handle:function(_529,_52a){
scayt.opt({dictionary:_529});
if(_529.error){
return _4.mixin(new Error("Error User Dictionary"),_529);
}else{
return _529;
}
}});
_526=_526||null;
td.addCallbacks(_525,function(_52b){
return _526(_52b,td);
});
return td;
};
},byId:function(id){
if(!_4.isString(id)){
return;
}
var _52d=_5.registry.byId(id);
return _52d;
},byNode:function(node){
if(!node){
return null;
}
return _5.registry.byId(node.getAttribute("widgetId"));
},getNameUserDictionary:function(){
return scayt._sendUserDictionary.apply(this,[""].concat(_4._toArray(arguments)))("getname");
},getUserDictionaryName:function(){
return scayt._sendUserDictionary.apply(this,[""].concat(_4._toArray(arguments)))("getname");
},addWordToUserDictionary:function(word,_530,_531){
if(!_4.isString(word)){
var pre=[_4.attr(word,"scayt_word")].concat(_4._toArray(arguments,1));
}
return scayt._sendUserDictionary.apply(this,pre||arguments)("addword");
},deleteWordFromUserDictionary:function(word,_534,_535){
},createUserDictionary:function(name,_537,_538){
return scayt._sendUserDictionary.apply(this,arguments)("create");
},deleteUserDictionary:function(_539,_53a){
return scayt._sendUserDictionary.apply(this,[""].concat(_4._toArray(arguments)))("delete");
},renameUserDictionary:function(name,_53c,_53d){
return scayt._sendUserDictionary.apply(this,arguments)("rename");
},restoreUserDictionary:function(name,_53f,_540){
return scayt._sendUserDictionary.apply(this,arguments)("restore");
},getUserDictionaryContent:function(){
},setUserDictionaryContent:function(){
},_mixin:null,props:function(o){
_4.mixin(scayt._mixin,o);
if(o["ads"]&&!scayt.ads){
scayt.ads={};
}
if(o["ads"]&&(typeof (o["ads"].active)!="undefined")&&!(o["ads"].active)){
scayt.ads.active=o["ads"].active;
}
if(o["ads"]&&o["ads"].params){
for(var x in o["ads"].params){
if(!scayt.ads.inputParams){
scayt.ads.inputParams={};
}
scayt.ads.inputParams[x]=_4.mixin(o["ads"].params[x],scayt.ads.inputParams["default"]);
}
}
if(o["uiTags"]){
scayt.uiTags=o["uiTags"];
}
if(o["userDictionaryName"]){
scayt.userDictionaryName=o["userDictionaryName"];
}
if(o["customDictionaryIds"]){
scayt.customDictionaryIds=o["customDictionaryIds"];
}
_4.extend(scayt,o);
},err:function(_543){
if(_543&&_543.disabled&&!scayt.disabled){
scayt.disabled=true;
_4.forEach(scayt._hash,function(_544){
_544.destroy();
});
}
},getLangList:function(){
return _4.clone(scayt.langList);
},opt:function(o){
for(var x in o){
switch(x){
case "verLang":
if(o[x]!=scayt[x]&&!o.langList){
_4.io.script.get({url:this.langURL,content:{verLang:o[x]}});
scayt[x]=_4.clone(o[x]);
}else{
if(o.langList){
scayt[x]=_4.clone(o[x]);
if(scayt.persist){
scayt._saveState(scayt,["verLang"]);
}
}
}
break;
case "dictionary":
if(o[x].action=="delete"||o[x].action=="restore"){
scayt.dictionary={};
}
_4.publish("scayt::actionUserDictionary",[o[x].action,o[x].word]);
break;
default:
scayt[x]=_4.clone(o[x]);
}
}
},nls:{},manager:[],getCaption:function(_547,_548){
var mn="scayt",bn="ui",str=[];
_4.forEach(["ltr","rtl"],function(item){
var arr=scayt.langList[item];
for(var k in arr){
str.push(k);
}
});
var _54f=str.join(",").replace(/_/g,"-").toLowerCase();
_547="ROOT";
var l=_547.replace(/-/g,"_");
if(scayt.nls&&scayt.nls[bn]&&scayt.nls[bn][l]){
_548(scayt.nls[bn][l]);
}else{
_4._xdReset();
_4._loadNotifying=false;
_4["require"+"Localization"](mn,bn,_547,_54f);
_4.addOnLoad(function(){
if(scayt.clearTimeoutLocalization){
clearTimeout(scayt.clearTimeoutLocalization);
delete (scayt.clearTimeoutLocalization);
}
_548(_4.i18n["get"+"Localization"](mn,bn,_547));
});
scayt.clearTimeoutLocalization=setTimeout(function(){
try{
_548(_4.i18n["get"+"Localization"](mn,bn,"root"));
_4._loaders=[];
scayt.nls[bn][_547]=_4.i18n["get"+"Localization"](mn,bn,"root");
}
catch(e){
_548(scayt.nls[bn]["ROOT"]);
}
_4._xdReset();
},scayt.timeout/2);
}
},call:function(o){
var lang=scayt._defaultLang,_553=o.incorrect,_554=o.correct;
o=o.o;
if(o&&_4.isObject(o)&&o.errorLang){
delete (scayt.dictionary[o.errorLang]);
if(typeof scayt.dictionary[o.lang]=="undefined"){
scayt.dictionary[o.lang]=new _6.collections.Dictionary();
}
lang=o.lang;
}else{
if(o&&o.lang){
lang=o.lang;
}
}
var dic=scayt.dictionary[lang],tmp=[],corr=[],d;
_4.forEach(_553,function(item){
if(typeof item=="undefined"){
return;
}
d=dic.item(item[0]);
if(typeof d=="undefined"){
dic.add(item[0],{sugg:item[1],amount:0,status:0});
d=dic.item(item[0]);
}else{
d.sugg=item[1];
d.status=0;
}
corr=corr.concat(d.sugg);
tmp.push(item[0]);
});
_4.forEach(_554,function(item){
d=dic.item(item);
if(typeof d=="undefined"){
dic.add(item,{status:1});
}else{
d.status=1;
}
});
_4.forEach(corr,function(item){
dic.add(item,{amount:0,status:1});
});
_4.publish("scayt::call",[{words:tmp,"sLang":lang,errorLang:((o&&o.errorLang)||null),status:0,correct:_554}]);
},getSuggestion:function(word,lang){
return scayt.dictionary[(lang||scayt._defaultLang)].item(word).sugg;
},cssStyleNode:{arr:[],item:function(doc){
var b=false;
for(var i=0;i<scayt.cssStyleNode.arr.length;i++){
if(scayt.cssStyleNode.arr[i]["doc"]==doc){
b=scayt.cssStyleNode.arr[i];
}
}
return b;
},push:function(_561){
if(!scayt.cssStyleNode.item(_561.doc)){
scayt.cssStyleNode.arr.push(_561);
}
},remove:function(doc){
var b=false,_564=-1;
for(var i=0;i<scayt.cssStyleNode.arr.length;i++){
if(scayt.cssStyleNode.arr[i]["doc"]==doc){
b=scayt.cssStyleNode.arr[i]["css"];
_564=i;
}
}
if(_564==-1){
return false;
}
if(!_4.isIE){
for(i=0;i<b.length;i++){
_4.destroy(b[i]);
}
}
scayt.cssStyleNode.arr.splice(_564,1);
}}});
(function(){
if(typeof scaytConfig!="undefined"){
for(var opt in scaytConfig){
scayt[opt]=scaytConfig[opt];
}
}
scayt._restoreState(scayt,["verLang"]);
scayt.sessionId=document.domain+"|"+(new Date()).getTime()+Math.random();
_4.subscribe("widgetBlur",this,function(w){
if(w.baseClass==="scayt"){
w._focused==true;
}
});
_4.subscribe("widgetFocus",this,function(w){
if(w.baseClass==="scayt"){
w._focused==false;
}
});
if(scayt.node){
function func(){
var sn=scayt.node;
if(_4.isString(sn)){
var node=_4.byId(sn);
sn=node?[node]:[];
}
if(!_4.isArray(sn)){
sn=[sn];
}
_4.forEach(sn,function(node,i){
scayt.manager[i]=new scayt({srcNodeRef:node,id:node.id});
});
};
func();
}
scayt.isIframeLoaded=false;
scayt._iframeLoaders=[];
scayt._started=true;
if(_4.isOpera){
var _56e={};
if(scayt.verLang){
_4.mixin(_56e,{verLang:scayt.verLang});
}
_4.io.script.get({url:scayt.langURL,content:_56e});
scayt.frameDoc=document;
scayt.isIframeLoaded=true;
}
})();
if(!_4.isOpera){
scayt.addLoad=function(){
scayt.iframeOnLoad=function(){
var _56f={};
if(scayt.verLang){
_4.mixin(_56f,{verLang:scayt.verLang});
}
_4.io.script.get({url:scayt.langURL,content:_56f,frameDoc:scayt.frameDoc});
scayt.isIframeLoaded=true;
for(var x=0;x<scayt._iframeLoaders.length;x++){
scayt._iframeLoaders[x]();
}
};
if(!scayt.frameDoc){
var html="javascript:void(0)";
var _572=_4.doc.createElement("iframe");
if(!_4.isIE){
_4.connect(_572.contentWindow,"onload",scayt.iframeOnLoad);
}
_4.attr(_572,{hidefocus:"true",frameborder:"0",src:html});
_4.style(_572,{position:"absolute",left:"-10000px",top:"-10000px",width:"0",height:"0",display:"none"});
_4.body().appendChild(_572);
_572.contentWindow.document.open();
_572.contentWindow.document.write("<html><head></head><body><script type=\"text/javascript\">scayt=parent.scayt;scayt.iframeOnLoad()</script></body></html>");
_572.contentWindow.document.close();
_572.contentWindow.scayt=scayt;
scayt.frameDoc=_572.contentWindow.document;
}
if(_4.isSafari&&_4.isSafari<5){
scayt.iframeOnLoad();
}
};
if(_4._loaders.length){
_4._loaders.unshift(scayt.addLoad);
}else{
scayt.addLoad();
}
}
}
({tab_options:"Options",tab_langs:"Languages",tab_dictionary:"User Dictionary",tab_about:"About Us",label_allCaps:"Ignore All-Caps Words",label_ignoreDomainNames:"Ignore Domain Names",label_mixedCase:"Ignore Words with Mixed Case",label_mixedWithDigits:"Ignore Words with Numbers",label_more_suggestion:"More suggestions",label_no_suggestion:"No suggestions",label_ignore:"Ignore",label_ignore_all:"Ignore all",label_add_word:"Add word",label_disabled:"Disabled",label_dname:"Dictionary Name:",button_dic_create:"Create",button_dic_delete:"Delete",button_dic_rename:"Rename",button_dic_restore:"Restore",dic_info:"Initially the User Dictionary is stored in a Cookie. However, Cookies are limited in size. When the User Dictionary grows to a point where it cannot be stored in a Cookie, then the dictionary may be stored on our server. To store your personal dictionary on our server you should specify a name for your dictionary. If you already have a stored dictionary, please type it's name and click the Restore button.",err_dic_create:"Dictionary %s cannot be created.",err_dic_delete:"Dictionary %s cannot be deleted.",err_dic_rename:"Dictionary %s cannot be renamed.",err_dic_restore:"Dictionary %s cannot be restored.",succ_dic_create:"Dictionary %s successfuly created.",succ_dic_delete:"Dictionary %s successfuly deleted.",succ_dic_rename:"Dictionary %s successfuly renamed.",succ_dic_restore:"Dictionary %s successful restored.",err_dic_emptyname:"Dictionary name should not be empty.",curr_dic:"Current dictionary",version:"Version",about_throwt_image:"<img src='http://www.spellchecker.net/v3/images/logo.gif' />",about_throwt_copy:" &copy;1999-2010, SpellChecker.net, Inc.<br /> &copy;2005-2009, The Dojo Foundation<br />All Rights Reserved.",button_ok:"Ok",button_cancel:"Cancel",buttonSave:"Save",itemClose:"Close",loadingState:"Loading...",errorState:"Sorry, an error occurred",copy:"Copy",cut:"Cut",paste:"Paste","delete":"Delete",select_all:"Select All",redo:"Redo",undo:"Undo",systemShortcut:"The \"${0}\" action is only available in your browser using a keyboard shortcut. Use ${1}.",ctrlKey:"ctrl+${0}",appleKey:"⌘${0}"});
if(!_4._hasResource["scayt.banner"]){
_4._hasResource["scayt.banner"]=false;
_4.provide("scayt.banner");
_4.mixin(scayt,{ads:{inputParams:{"default":{width:232,height:72,pleft:3,vscroll:0,adsurl:(document.location.protocol.search(/https?:/)!=-1?document.location.protocol:"http:")+"//www.spellchecker.net/images/fck/ckscayt3.html",nshow:3}},params:{width:232,height:72,pleft:3,vscroll:0,adsurl:(document.location.protocol.search(/https?:/)!=-1?document.location.protocol:"http:")+"//www.spellchecker.net/images/fck/ckscayt3.html",nshow:3},active:true,clickOverClAds:false,iframeId:"spellads",adsbuttonId:"scayt_banner_close",getAbsPosition:function(_573){
var x=y=0;
var elem=typeof _573=="string"?document.getElementById(_573):_573;
var w=elem.offsetWidth;
var h=elem.offsetHeight;
while(elem){
x+=elem.offsetLeft;
y+=elem.offsetTop;
elem=elem.offsetParent;
}
return {x:x,y:y,w:w,h:h};
},createIframe:function(_578,url,adsp,xy){
var _57c=window[_578]||window.frames[_578];
var _57d=document.getElementById(this.adsbuttonId);
var _57e=function(){
_57c.style.position="absolute";
_57c.style.width=adsp.width+"px";
_57c.style.height=adsp.height+"px";
_57c.style.overflow="hidden";
_57c.style.top=xy.y+"px";
_57c.style.left=xy.x+"px";
_57c.style.zIndex="100001111";
_57d.style.position="absolute";
_57d.style.width="17"+"px";
_57d.style.height="17"+"px";
_57d.style.top=xy.y+4+"px";
_57d.style.left=xy.x+adsp.width-17+"px";
_57d.style.zIndex="100002222";
_57d.style.cursor="pointer";
_57d.style.background="transparent url('"+(document.location.protocol.search(/https?:/)!=-1?document.location.protocol:"http:")+"//www.spellchecker.net/images/fck/close.jpg') no-repeat scroll 0 0";
_57d.title="close advertisement";
};
if(_57c){
_57e();
return [document.getElementById(_578),document.getElementById(this.adsbuttonId)];
}
var _57f=document.body;
_57c=document.createElement("iframe")||_57c;
var _57d=document.createElement("a");
_57d.id=this.adsbuttonId;
_57d.style.display="none";
_57c.name=_578;
_57c.id=_578;
_57c.style.display="none";
_57e();
window.frames[_578]=_57c;
_57c.setAttribute("src",url);
_57f.appendChild(_57c);
_57f.appendChild(_57d);
var cl=document.getElementById(this.adsbuttonId);
cl.onmouseover=function(ev){
scayt.ads.clickOverClAds=true;
};
cl.onmouseout=function(ev){
scayt.ads.clickOverClAds=false;
};
cl.onclick=function(){
scayt.hideBanner();
return false;
};
return [document.getElementById(_578),document.getElementById(this.adsbuttonId)];
},getRMBinfo:function(env,args){
var _585=this.get_rmb[env]||null;
if(!_585){
return null;
}
return _585.apply(this,[args]);
},get_rmb:{scayt_ui:function(){
return null;
},ck_editor:function(_586){
try{
if(!_586){
return null;
}
var _587=_586;
var el=null;
var xy={};
var adsp=this.params;
var d=_587.contextMenu._.menu.id.split("_");
var num=new Number(d[1]);
var _58d=d[0]+"_"+(num+3);
el=document.getElementById(_58d);
if(!el){
_58d=d[0]+"_"+(num+1);
el=document.getElementById(_58d);
}
if(!el){
el=_4.query(".cke_contextmenu");
if(el&&el.length>0){
el=el[0];
}else{
el=null;
}
}
if(!el){
return null;
}
var _58e=_4.coords(el,true);
if(_58e.x<=0||_58e.y<=0){
return null;
}
xy.x=_58e.x+_58e.w+adsp.pleft;
xy.y=_58e.y+(_58e.h*adsp.vscroll);
return xy;
}
catch(err){
return null;
}
},fck_menu:function(_58f){
try{
if(!_58f||!_58f._Panel){
return null;
}
var xy={};
var adsp=this.params;
if(_58f._Panel._Popup){
var _592={x:_58f._Panel._PopupArgs[0],y:_58f._Panel._PopupArgs[1],w:_58f._Panel._PopupArgs[2],h:_58f._Panel._PopupArgs[3]};
var px=new Number((document.documentElement&&document.documentElement.scrollLeft)||(document.body.parentNode&&document.body.parentNode.scrollLeft))||0;
var py=new Number((document.documentElement&&document.documentElement.scrollTop)||(document.body.parentNode&&document.body.parentNode.scrollTop))||0;
xy.x=_592.x-window.screenLeft+px+_592.w+adsp.pleft;
xy.y=_592.y-window.screenTop+py+_592.h*adsp.vscroll;
}else{
if(_58f._Panel._IFrame){
var _592=_4.coords(el,true);
xy.x=_592.x+_592.w+adsp.pleft;
xy.y=_592.y+(_592.h*adsp.vscroll);
}
}
if(_592.x<=0||_592.y<=0){
return null;
}
return xy;
}
catch(err){
return null;
}
},tiny_mce:function(_595){
try{
var xy={};
var adsp=this.params;
var el=document.getElementById("menu_"+_595.id)||null;
if(!el){
return null;
}
var _599=_4.coords(el,true);
xy.x=_599.x+_599.w+adsp.pleft;
xy.y=_599.y+(_599.h*adsp.vscroll);
return xy;
}
catch(err){
return null;
}
}},_hideAdsInDOM:function(_59a,cl,time,_59d){
var that=this;
_59a=_59a||document.getElementById(that.iframeId);
cl=cl||document.getElementById(that.adsbuttonId);
time=(that.clickOverClAds==false&&_59d==false)?0:time>0?time:500;
_59d=_59d||false;
window.setTimeout(function(){
_59a.style.display="none";
cl.style.display="none";
},time);
},hide_rmb:{scayt_ui:function(){
return null;
},ck_editor:function(_59f,_5a0,cl){
try{
var that=this;
var _5a3=scayt.ads.clickOverClAds;
var _5a4=false;
_5a0.onmouseover=function(ev){
_5a4=true;
};
_5a0.onmouseout=function(ev){
_5a4=false;
};
var _5a7=_59f.contextMenu._.menu.onHide;
_59f.contextMenu._.menu.onHide=function(){
scayt.ads._hideAdsInDOM(_5a0,cl,500,_5a4);
if(_5a7){
_5a7.apply(_59f.contextMenu._.menu,[_59f]);
}
};
}
catch(err){
return null;
}
},fck_menu:function(_5a8,_5a9,cl){
try{
var that=this;
var _5ac=false;
var _5ad;
_5a9.onmouseover=function(ev){
_5ac=true;
};
_5a9.onmouseout=function(ev){
_5ac=false;
};
if(_5a8._Panel._Popup){
_5ad=window.setInterval(function(){
if(!_5a8._Panel._Popup.isOpen){
scayt.ads._hideAdsInDOM(_5a9,cl,500,_5ac);
window.clearInterval(_5ad);
}
},100);
}else{
if(_5a8._Panel._IFrame){
_5ad=window.setInterval(function(){
if(new String(_5a8._Panel._IFrame.style.height).indexOf("0")==0){
scayt.ads._hideAdsInDOM(_5a9,cl,200,_5ac);
window.clearInterval(_5ad);
}
},100);
}
}
}
catch(err){
return null;
}
},tiny_mce:function(_5b0,_5b1,cl){
try{
var that=this;
var _5b4=false;
_5b1.onmouseover=function(ev){
_5b4=true;
};
_5b1.onmouseout=function(ev){
_5b4=false;
};
var hide=function(){
scayt.ads._hideAdsInDOM(iframe,cl,500,_5b4);
};
_5b0.ed.onMouseDown.add(hide);
_5b0.ed.onKeyDown.add(hide);
}
catch(err){
return null;
}
}}},_adsHidden:false,_adsShown:0,rmbLayer:"",showads:function(){

arguments=Array.prototype.slice.call(arguments);
var that=scayt;
var adsp=scayt.ads.inputParams[arguments.shift()]||scayt.ads.inputParams["default"];
//Added by Sandeep Giri
return;
if(scayt._adsHidden||!scayt.ads.active){
return;
}
that._adsShown++;
adsp.nshow=adsp.nshow||1;
if((that._adsShown==1?adsp.nshow:that._adsShown-1)%adsp.nshow!=0){
return;
}
that.ads.params=adsp;
var ads=that.ads;
var args={};
args.ck_editor=null;
args.fck_menu=null;
args.tiny_mce=null;
that.rmbLayer="scayt_ui";
if(arguments[0]!==null&&arguments[0]){
args.ck_editor=arguments[0];
that.rmbLayer="ck_editor";
}else{
if(arguments[1]!==null&&arguments[1]){
args.fck_menu=arguments[1];
that.rmbLayer="fck_menu";
}else{
if(arguments[2]!==null&&arguments[2]){
args.tiny_mce=arguments[2];
that.rmbLayer="tiny_mce";
}
}
}
var _5bc=window.setInterval(function(){
var _5bd=ads.getRMBinfo(that.rmbLayer,args[that.rmbLayer]);
if(_5bd){
var sq=that.ads.createIframe(that.ads.iframeId,adsp.adsurl,adsp,_5bd);
sq[0].style.display="block";
sq[1].style.display="block";
that.runHideBannerListener(that.rmbLayer,args[that.rmbLayer]);
window.clearInterval(_5bc);
}
},10);
},runHideBannerListener:function(_5bf,_5c0){
var that=scayt;
var _5c2=that.ads.hide_rmb[_5bf]||null;
var _5c3=document.getElementById(scayt.ads.iframeId);
var _5c4=document.getElementById(scayt.ads.adsbuttonId);
if(!_5c2){
return null;
}
scayt._hideFunc={foo:_5c2,args:[_5c0,_5c3]};
return _5c2.apply(scayt,[_5c0,_5c3,_5c4]);
},hideBanner:function(){
try{
scayt._adsHidden=true;
scayt._hideFunc.foo.apply(scayt,scayt._hideFunc.args);
}
catch(err){
}
return;
},hidetinBanner:function(){
if(!scayt.ads.active){
return null;
}
try{
var _5c5=document.getElementById(scayt.ads.iframeId);
var _5c6=document.getElementById(scayt.ads.adsbuttonId);
_5c5.style.display="none";
_5c6.style.display="none";
}
catch(err){
}
}});
_4.extend(scayt,{showads:function(){
return scayt.showads.apply(scayt,[this.assocApp].concat(_4._toArray(arguments)));
},hidetinBanner:function(){
return scayt.hidetinBanner.apply(scayt,arguments);
},fireOnContextMenu:function(){
return scayt.showads.apply(scayt,[this.assocApp].concat(_4._toArray(arguments)));
}});
}
}};
});