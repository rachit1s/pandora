// User/Assignee auto complete code..
// This code needs to be refactored.

var agt = navigator.userAgent.toLowerCase();
var is_ie = (agt.indexOf("msie") !=-1 && document.all);
var is_ie5=(agt.indexOf("msie 5")!=-1 && document.all);
var is_safari=(agt.indexOf("safari")!=-1);
var is_nav= !is_ie && (agt.indexOf("mozilla") != -1);
var FIRST_BUTTON=(is_safari||is_ie)?1:0;

// CONSTANTS

var TOOLTIP_ID="tip";
var USER_HINT_ID_PREFIX="uh_";
var ASSIGNEE_HINT_ID_PREFIX="ah_";
var HINT_MENU_ID="auto_hm";

var C_ADDRESS=0;
var C_NAME=1;

var AUTOCOMPLETE_CLASS="auto";

var BACKSPACE_KEYCODE=8;
var COMMA_KEYCODE=188;
var SEMICOLON_KEYCODE = 59;
var DEBUG_KEYCODE=68;
var DELETE_KEYCODE=46;
var DOWN_KEYCODE=40;
var ENTER_KEYCODE=13;
var ESC_KEYCODE=27;
var SPACE_KEYCODE=32;
var TAB_KEYCODE=9;
var UP_KEYCODE=38;
var SHIFT_KEYCODE=16;


function AssertTrue(expr){
    if(!expr){
        DumpError("Assertion failed.\n");
        throw"Assertion failed.";
        
    }
}

function AssertEquals(val1, val2){
    if(val1!=val2){
        DumpError("AssertEquals failed: <"+val1+"> != <"+val2+">");
        throw"Assertion failed.";
        
    }
}

function AssertNumArgs(num){
    var caller=AssertNumArgs.caller;
    if(caller && caller.arguments.length!=num){
        DumpError("Wrong number of arguments!");
        
    }
}

function SetCookie(name, value, expires_ms){
    var date=new Date();
    var now=date.getTime();
    date.setTime(now+expires_ms);
    document.cookie=name+"="+value+";path=/;expires="+date.toGMTString();
}

function GetCookie(name){
    var cookie=String(document.cookie);
    var pos=cookie.indexOf(name+"=");
    if(pos!=-1){
        var end=cookie.indexOf(";",pos);
        return cookie.substring(pos+name.length+1,end==-1?cookie.length:end);
    }
    return "";
}

function Now(){
    return (new Date()).getTime();
}

function GetElement(win, id){
    return win.document.getElementById(id);
}

function SetInnerHTML(win, id, html){
    try{
        GetElement(win,id).innerHTML=html;
    }
    catch(ex){
        DumpException(ex,"Cannot set inner HTML: "+id);
    }
}

function GetInnerHTML(win, id){
    try{
        return GetElement(win,id).innerHTML;
    }
    catch(ex){
        DumpException(ex,"Cannot get inner HTML: "+id);
        return "";
    }
}

function ClearInnerHTML(win, id){
    try{
        GetElement(win,id).innerHTML="";
    }
    catch(ex){
        DumpException(ex,"Cannot set inner HTML: "+id);
    }
}

function SetCssStyle(win, id, name, value){
    try{
        var elem=GetElement(win,id);
        if(elem){
            elem.style[name]=value;
        }
        
    }
    catch(ex){
        DumpException(ex);
    }
}

function ShowElement(el, show){
    el.style.display=show?"":"none";
}

function ShowBlockElement(el, show){
    el.style.display=show?"block":"none";
    
}

function SetButtonText(button, text){
    button.childNodes[0].nodeValue=text;
}

function AppendNewElement(win, parent, tag){
    var e=win.document.createElement(tag);
    parent.appendChild(e);
    return e;
}

function CreateDIV(win, id){
    var div=GetElement(win,id);
    if(!div){
        var div=AppendNewElement(win,win.document.body,"div");
        div.id=id;
    }
    return div;
}

function CreateIFRAME(win, id, url){
    var iframe=GetElement(win,id);
    if(!iframe){
        var div=AppendNewElement(win,win.document.body,"div");
        div.innerHTML="<iframe id="+id+" name="+id+" src="+url+"></iframe>";
        iframe=GetElement(win,id);
    }
    return iframe;
}

function HasClass(el, cl){
    if(el.className==null){
        return false;
    }
    var classes=el.className.split(" ");
    for(var i=0; i<classes.length; i++){
        if(classes[i]==cl){
            return true;
        }
    }
    return false;
}

function AddClass(el, cl){
    if(HasClass(el,cl)){
        return;
    }
    el.className+=" "+cl;
}

function RemoveClass(el, cl){
    if(el.className==null){
        return;
    }
    var classes=el.className.split(" ");
    var result=[];
    for(var i=0; i<classes.length; i++){
        if(classes[i]!=cl){
            result[result.length]=classes[i];
        }
    }
    el.className=result.join(" ");
}

function GetPageOffsetLeft(el){
    var x=el.offsetLeft;
    if(el.offsetParent!=null){
        x+=GetPageOffsetLeft(el.offsetParent);
    }
    return x;
}

function GetPageOffsetTop(el){
    var y=el.offsetTop;
    if(el.offsetParent!=null){
        y+=GetPageOffsetTop(el.offsetParent);
    }
    return y;
}

function GetPageOffset(el){
    var x=el.offsetLeft;
    var y=el.offsetTop;
    if(el.offsetParent!=null){
        var pos=GetPageOffset(el.offsetParent);
        x+=pos.x;
        y+=pos.y;
    }
    return {x:x,y:y};
}

function GetPageOffsetRight(el){
    return GetPageOffsetLeft(el)+el.offsetWidth;
}

function GetPageOffsetBottom(el){
    return GetPageOffsetTop(el)+el.offsetHeight;
}

function GetScrollTop(win){
    return is_ie?win.document.body.scrollTop:win.pageYOffset;
}

function ScrollTo(win, el, position){
    var y=GetPageOffsetTop(el);
    y-=GetWindowHeight(win)*position;
    win.scrollTo(0,y);
}

// Scroll functions

var ALIGN_BOTTOM='b';
var ALIGN_MIDDLE='m';
var ALIGN_TOP='t';

function ScrollIntoView(win, el, alignment){
    var el_top=GetPageOffsetTop(el);
    var el_bottom=el_top+el.offsetHeight;
    var win_top=GetScrollTop(win);
    var win_height=GetWindowHeight(win);
    var win_bottom=win_top+win_height;
    if(el_top<win_top||el_bottom>win_bottom){
        var scrollto_y;
        if(alignment==ALIGN_BOTTOM){
            scrollto_y=el_bottom-win_height+5;
        }
        else {
            if(alignment==ALIGN_MIDDLE){
                scrollto_y=(el_top+el_bottom) / 2-win_height / 2;
            }
            else {
                scrollto_y=el_top-5;
            }
        }
        Debug("Scrolling to "+scrollto_y);
        win.scrollTo(0,scrollto_y);
    }
}

function GetWindowWidth(win){
    return is_ie?win.document.body.clientWidth:win.innerWidth;
}

function GetWindowHeight(win){
    return is_ie?win.document.body.clientHeight:win.innerHeight;
}

function GetAvailScreenWidth(){
    return screen.availWidth;
}

function GetAvailScreenHeight(){
    return screen.availHeight;
}

function GetNiceWindowHeight(win){
    return Math.floor(0.88*GetAvailScreenHeight());
}

function GetCenteringLeft(width){
    return Math.floor((screen.availWidth-width) / 2);
}

function GetCenteringTop(height){
    return Math.floor((screen.availHeight-height) / 2);
}

function OpenWindow(win, url, name, features){
    url=U_MakeUnique(url);
    var newwin;
    if(features){
        newwin=win.open(url,name,features);
    }
    else {
        newwin=win.open(url,name);
    }
    if(!newwin){
        Debug("Cannot open window. Is there a popup blocker?");
    }
    else {
        newwin.focus();
    }
    return newwin;
}

function CloseWindow(win){
    win.top.close();
}

function Popup(win, url, name, width, height, do_center){
    if(!height){
        height=Math.floor(GetWindowHeight(win.top)*0.88);
    }
    if(!width){
        width=Math.min(GetAvailScreenWidth(),height);
    }
    var features="resizable=yes,scrollbars=yes,"+"width="+width+",height="+height;
    if(do_center){
        features+=",left="+GetCenteringLeft(width)+","+"top="+GetCenteringTop(height);
    }
    return OpenWindow(win,url,name,features);
}

// Window data 

var windata=[];

function GetWindowData(win){
    var data=windata[win.name];
    if(!data){
        windata[win.name]=data=[];
    }
    return data;
}

function ClearWindowData(win_name){
    if(windata[win_name]){
        windata[win_name]=null;
    }
}


// Event Handler

var ONCLICK=1;
var ONMOUSEDOWN=2;
var ONKEYPRESS=3;
var ONKEYDOWN=4;
var ONSCROLL=5;
var ONMOUSEOVER=6;
var ONMOUSEOUT=7;
var ONTOOLTIP=8;
var ONCMD=9;
var EventTypeNames=["null","onclick","onmousedown","onkeypress","onkeydown","onscroll","onmouseover","onmouseout","ontooltip","oncmd"];

function InitEventHandlers(win, handlers, mouse_movement_handlers){
    GetWindowData(win).event_handlers=handlers;
    GetWindowData(win).mouse_movement_handlers=mouse_movement_handlers;
    win.document.onclick=CreateEventHandler(win,ONCLICK);
    win.document.onmousedown=CreateEventHandler(win,ONMOUSEDOWN);
    win.document.onkeydown=CreateEventHandler(win,ONKEYDOWN);
    win.document.onkeypress=CreateEventHandler(win,ONKEYPRESS);
    win.document.onmouseover=CreateEventHandler(win,ONMOUSEOVER);
    win.document.onmouseout=CreateEventHandler(win,ONMOUSEOUT);
    win.onscroll=CreateEventHandler(win,ONSCROLL);
}

function CreateEventHandler(win, type){
    if(is_ie){
        return win.eval("tmp=function(e){return HandleEvent(window,e,"+type+");}");
    }
    else {
        return function (e){return HandleEvent(win,e,type);};
    }
}

var last_event_time_=Now();
var key_buffer_="";
var last_key_time_=0;

function HandleEvent(win, e, type){
    try{
        var now=Now();
        last_event_time_=now;
        if(!e){
            e=win.event;
        }
        var keycode=GetKeyCode(e);
        var src=GetEventTarget(e);
        var id="";
        var is_keyboard_event=(type==ONKEYPRESS||type==ONKEYDOWN);
/*
        if(win.name!=Main_visible_view){
            win=top.main[Main_visible_view];
            win.focus();
            if(!is_keyboard_event){
                Debug("Ignoring event on hidden window");
                return false;
            }
        }
*/       
        if(type==ONSCROLL){}
        else {
            if(type==ONMOUSEOVER||type==ONMOUSEOUT){
                HandleMouseMovement(win,e,type,src);
                if(!GetWindowData(win).mouse_movement_handlers){
                    return;
                }
            }
            try{
                for(var el=src; el!=null; el=el.parentNode){
                    var nodename=el.nodeName;
                    if(nodename=="TEXTAREA"||(nodename=="INPUT" && el.type!="checkbox")||(nodename=="BUTTON" && is_keyboard_event)||(nodename=="A" && !el.id && (!is_keyboard_event||keycode==ENTER_KEYCODE))){
                        {
                            if(keycode==ESC_KEYCODE && src.blur){
                                src.blur();
                                win.focus();
                                return false;
                            }
                            return;
                        }
                    }
                    if(id=="" && el.id && el.id.indexOf('_')!=0){
                        id=el.id;
                    }
                }
            }
            catch(e){
                Debug("Exception: "+e);
            }
            if(is_keyboard_event){
                if(type==ONKEYPRESS){
                    if(keycode==DEBUG_KEYCODE){
                        DB_ToggleDebugMode();
                        return false;
                    }
                    if(now-last_key_time_>1000){
                        key_buffer_="";
                    }
                    last_key_time_=now;
                    key_buffer_+=GetKeyChar(e);
                    for(var n=0;
                    n<key_buffer_.length;
                    n++){
                        var cmds=null;//KeyMap[key_buffer_.substr(n)];
                        if(cmds){
                            var cmdlist=cmds.split("#");
                            for(var i=0;
                            i<cmdlist.length;
                            i++){
                                DispatchEvent(win,CreateEventInfo(e,ONCMD,src,id,cmdlist[i]));
                            }
                            key_buffer_="";
                            return false;
                        }
                    }
                    if(is_nav && keycode>=48 && ((keycode<=57)||(keycode>=65 && keycode<=90)||(keycode>=97 && keycode<=122))){
                        {
                            Debug("Ignoring key: "+keycode);
                            return false;
                        }
                    }
                }
                return;
            }
            if((type==ONMOUSEDOWN||(is_nav && type==ONCLICK)) && e.button!=FIRST_BUTTON){
                return;
            }
        }
        return DispatchEvent(win,CreateEventInfo(e,type,src,id,null));
    }
    catch(ex){
        DumpException(ex,"View: "+win.name+" Event: "+EventTypeNames[type]);
    }
    
}

function DispatchEvent(win, info){
    var handlers;
    if(info.type==ONMOUSEOVER||info.type==ONMOUSEOUT){
        handlers=GetWindowData(win).mouse_movement_handlers;
    }
    else {
        handlers=GetWindowData(win).event_handlers;
        Debug("Event: "+EventTypeNames[info.type]+" id = "+info.id+" keycode = "+info.keycode);
    }
    for(var i in handlers){
        var result=handlers[i](win,info);
        if(result==false||result==true){
            return result;
        }
    }
}

function GetEventTarget(e){
    var src=e.srcElement?e.srcElement:e.target;
    if(is_safari && src && src.nodeType==3){
        src=src.parentNode;
    }
    return src;
}

function GetKeyCode(e){
    return is_ie?e.keyCode:e.which;
}

function IsPrintableKeyCode(keycode){
    switch(keycode){
        case SPACE_KEYCODE:return true;
        default:return ((keycode>=65 && keycode<=90)||(keycode>=186 && keycode<=192)||(keycode>=48 && keycode<=57)||(keycode>=96 && keycode<=111)||(keycode>=219 && keycode<=222));
    }
}

function GetKeyChar(e){
    var keycode=GetKeyCode(e);
    if(is_ie && e.ctrlKey){
        keycode+=64;
    }
    return String.fromCharCode(keycode).toLowerCase();
}

function GetMouseXPos(win, e){
    return is_ie?(e.x+win.document.body.scrollLeft):e.pageX;
}

function GetMouseYPos(win, e){
    return is_ie?(e.y+win.document.body.scrollTop):e.pageY;
}

function CreateEventInfo(e, type, src, id, cmd){
    var u=id.indexOf("_",1);
    u++;
    return {event:e,type:type,src:src,id:id,id_prefix:id.substr(0,u),id_n:id.substr(u),cmd:cmd};
}

function GetEventElementByNodeName(win, info, type){
    try{
        var parent=GetElement(win,info.id);
        var el=info.src;
        while(el!=null && el!=parent && el.nodeName!=type){
            el=el.parentNode;
        }
        return el;
    }
    catch(e){
        Debug("Cannot get parent node");
        return null;
    }
}

function AddListener(publisher, handler, param){
    publisher[publisher.length]=[handler,param];
}

function RemoveListener(publisher, handler){
    for(var i=0; i<publisher.length; i++){
        if(publisher[i][0]==handler){
            publisher.splice(i,1);
        }
    }
}

function NotifyListeners(publisher, event){
    for(var i=0; i<publisher.length; i++){
        var handler=publisher[i][0];
        var param=publisher[i][1];
        try{
            handler(event,param);
        }
        catch(e){
            DumpException(e);
        }
    }
}

// Tooltip stuff...

var TOOLTIP_INTERVAL=800;

function HandleMouseMovement(win, e, type, src){
    var windata=GetWindowData(win);
    switch(type){
        case ONMOUSEOVER:
			while(true){
				if(src==null){
					return;
				}
				if(src.id){
					break;
				}
				src=src.parentNode;
			}
			win.clearTimeout(windata.mouseover_timeout);
			windata.mouseover_id=src.id;
			windata.mouseover_x=GetMouseXPos(win,e)+16;
			windata.mouseover_y=GetMouseYPos(win,e);
			windata.mouseover_timeout=win.setTimeout("HandleToolTip(window)",TOOLTIP_INTERVAL);
			break;
        case ONMOUSEOUT:
			HideToolTip(win);
			break;
    }
    
}

function HandleToolTip(win){
    try{
        var id=GetWindowData(win).mouseover_id;
        if(id){
            Debug("Tool tip: "+id);
            DispatchEvent(win,CreateEventInfo(null,ONTOOLTIP,null,id,null));
        }
    }
    catch(e){
        DumpException(e);
    }
}

function ShowToolTip(win, html){
    var windata=GetWindowData(win);
    var div=CreateDIV(win,TOOLTIP_ID);
    div.innerHTML=html;
    div.style.position="absolute";
    div.style.background="#ffd";
    div.style.borderWidth="1";
    div.style.borderStyle="outset";
    div.style.padding="2px";
    div.style.left=windata.mouseover_x;
    div.style.top=windata.mouseover_y;
    ShowElement(div,true);
}

function HideToolTip(win){
    var windata=GetWindowData(win);
    windata.mouseover_id=null;
    win.clearTimeout(windata.mouseover_timeout);
    var div=GetElement(win,TOOLTIP_ID);
    if(div){
        ShowElement(div,false);
    }
}

function GetIdleMillis(){
    return Now()-last_event_time_;
}

// HTML 

var amp_re_=/&/g;
var lt_re_=/</g;
var gt_re_=/>/g;

function HtmlEscape(str){
    return str.replace(amp_re_,"&amp;").replace(lt_re_,"&lt;").replace(gt_re_,"&gt;");
}

var quote_re_=/\"/g;

function QuoteEscape(str){
    return HtmlEscape(str).replace(quote_re_,"&quot;");
}

var spc_re_=/\s+/g;
var beg_spc_re_=/^ /;
var end_spc_re_=/ $/;

function CollapseWhitespace(str){
    return str.replace(spc_re_," ").replace(beg_spc_re_,"").replace(end_spc_re_,"");
}

function UrlEncode(str){
    return encodeURIComponent(str);
}

function Trim(str){
    return str.replace(/^\s+/,"").replace(/\s+$/,"");
}

function EndsWith(str, suffix){
    return (str.lastIndexOf(suffix)==(str.length-suffix.length));
}

function IsEmpty(str){
    return CollapseWhitespace(str)=="";
}

function IsLetterOrDigit(ch){
    return ((ch>="a" && ch<="z")||(ch>="A" && ch<="Z")||(ch>='0' && ch<='9'));
}


function IsSpace(ch){
    return (" \t\r\n".indexOf(ch)>=0);
}

var eol_re_=/\r\n?/g;

function ConvertEOLToLF(str){
    return str.replace(eol_re_,"\n");
}

function GetCursorPos(win, textfield){
    if(IsDefined(textfield.selectionEnd)){
        return textfield.selectionEnd;
    }
    else {
        if(win.document.selection && win.document.selection.createRange){
            var tr=win.document.selection.createRange();
            var tr2=tr.duplicate();
            tr2.moveToElementText(textfield);
            tr2.setEndPoint("EndToStart",tr);
            var cursor=tr2.text.length;
            if(cursor>textfield.value.length){
                return -1;
            }
            return cursor;
        }
        else {
            Debug("Unable to get cursor position for: "+navigator.userAgent);
            return textfield.value.length;
        }
    }
}

function SetCursorPos(win, textfield, pos){
    if(IsDefined(textfield.selectionEnd) && IsDefined(textfield.selectionStart)){
        textfield.selectionStart=pos;
        textfield.selectionEnd=pos;
    }
    else {
        if(win.document.selection && textfield.createTextRange){
            var sel=textfield.createTextRange();
            sel.collapse(true);
            sel.move("character",pos);
            sel.select();
        }
    }
}

// Map

function Map(){}

Map.prototype.get = function (key){ return this[':'+key]; };
Map.prototype.put = function (key, value){ this[':'+key]=value; };
Map.prototype.remove = function (key){ delete this[':'+key]; };

function FindInArray(array, x){
    for(var i=0; i<array.length; i++){
        if(array[i]==x){
            return i;
        }
    }
    return -1;
}

function InsertArray(array, x){
    if(FindInArray(array,x)==-1){
        array[array.length]=x;
    }
}

function DeleteArrayElement(array, x){
    var i=0;
    while(i<array.length && array[i]!=x)i++;
    array.splice(i,1);
}

function CloneObject(x){
    if((typeof x)=="object"){
        var y=[];
        for(var i in x){
            y[i]=CloneObject(x[i]);
        }
        return y;
    }
    return x;
}

function PrintArray(array){
    AssertEquals(array.length,PrintArray.arguments.length*2-1);
    var idx=1;
    for(var i=1; i<PrintArray.arguments.length; i++){
        array[idx]=PrintArray.arguments[i];
        idx+=2;
    }
    return array.join("");
}

function ImageHtml(url, attributes){
    return "<img "+attributes+" src="+url+">";
}

function FormatJSLink(desc, js, classname){
    return '<span class="'+classname+'" onclick="'+js+'">'+desc+'</span>';
}

function FormatIDLink(desc, id, underline){
    return "<span class="+(underline?ULINK_CLASS:LINK_CLASS)+' id="'+id+'">'+desc+"</span>";
}

function FormatURLLink(desc, url, extra){
    return '<a href="'+url+'" class='+ULINK_CLASS+' onclick="return Main_OnLink(this,event)" '+(extra?extra:'')+'>'+desc+"</a>";
}

function MakeId3(idprefix, m, n){
    return idprefix+m+"_"+n;
}

// XmlHttp

function XmlHttpCreate(){
    var xmlhttp=null;
    if(is_ie){
        var control=(is_ie5)?"Microsoft.XMLHTTP":"Msxml2.XMLHTTP";
        try{
            xmlhttp=new ActiveXObject(control);
        }
        catch(e){
            DumpException(e);
            alert("You need to enable active scripting and activeX controls.");
        }
    }
    else {
        xmlhttp=new XMLHttpRequest();
        if(!xmlhttp){
            alert("XMLHttpRequest is not supported on this browser.");
        }
    }
    return xmlhttp;
}

function XmlHttpGET(xmlhttp, url, handler){
    AssertNumArgs(3);
    url=U_MakeUnique(url);
    Debug("Server request: GET "+url);
    xmlhttp.onreadystatechange=handler;
    xmlhttp.open("GET",url,true);
    xmlhttp.send(null);
}

function XmlHttpPOST(xmlhttp, url, data, handler){
    AssertNumArgs(4);
    Debug("Server request: POST "+url);
    xmlhttp.onreadystatechange=handler;
    xmlhttp.open("POST",url,true);
    xmlhttp.send(data);
}

function ParseAddress(addr){
    var name="";
    var address="";
    for(var i=0;i<addr.length;){
        var token=GetEmailToken(addr,i);
        if(token.charAt(0)=='<'){
            var end=token.indexOf(">");
            address=token.substring(1,(end!=-1)?end:token.length);
        }
        else {
            if(address==""){
                name+=token;
            }
        }
        i+=token.length;
    }
    if(address=="" && name.indexOf("@")!=-1){
        address=name;
        name="";
    }
    name=CollapseWhitespace(name);
    name=StripQuotes(name,"'");
    name=StripQuotes(name,"\"");
    address=CollapseWhitespace(address);
    return [name,address];
}

function GetAddress(address){
    return ParseAddress(address)[1];
}

function GetAddressUsername(address){
    var address=GetAddress(address);
    var at=address.indexOf("@");
    return (at==-1)?address:address.substr(0,at);
}

function GetPersonal(address){
    return ParseAddress(address)[0];
}

function GetPersonalElseUsername(address){
    var personal=GetPersonal(address);
    if(personal!=""){
        return personal;
    }
    else {
        return GetAddressUsername(address);
    }
}

function StripQuotes(str, quotechar){
    var len=str.length;
    if(str.charAt(0)==quotechar && str.charAt(len-1)==quotechar){
        return str.substring(1,len-1);
    }
    return str;
}

function EmailsToArray(str){
    var result=[];
    var email="";
    var token;
    for(var i=0; i<str.length;){
        token=GetEmailToken(str,i);
        if(token==","){
            AddEmailAddress(result,email);
            email="";
            i++;
            continue;
        }
        email+=token;
        i+=token.length;
    }
    if(email!=""||token==","){
        AddEmailAddress(result,email);
    }
    return result;
}

var openers_="\"<([";
var closers_="\">)]";

function GetEmailToken(str, pos){
    var ch=str.charAt(pos);
    var p=openers_.indexOf(ch);
    if(p==-1){
        return ch;
    }
    var end_pos=str.indexOf(closers_.charAt(p),pos+1);
    var token=(end_pos>=0)?str.substring(pos,end_pos+1):str.substr(pos);
    return token;
}

function AddEmailAddress(result, email){
    email=CleanEmailAddress(email);
    result[result.length]=email;
}

var specialchars_re_=/[()<>@,;:\\\".\[\]]/;

function CleanEmailAddress(str){
    var name_address=ParseAddress(str);
    var name=name_address[0];
    var address=name_address[1];
    if(name.indexOf("\"")==-1){
        var quote_needed=specialchars_re_.test(name);
        if(quote_needed){
            name="\""+name+"\"";
        }
    }
    if(name==""){
        return address;
    }
    else {
        if(address==""){
            return name;
        }
        else {
            return name+" <"+address+">";
        }
    }
}

function CompareID(a, b){
    if(a.length!=b.length){
        return (a.length-b.length);
    }
    else {
        return (a<b)?-1:(a>b)?1:0;
    }
}

function IsDefined(value){
    return (typeof value)!='undefined';
}

// Debug Procedures

var DB_mode=false;

function Debug(str){
	if(DB_mode)
		DumpError(str);
}

function DumpError(str){
    var stacktrace=DB_GetStackTrace(DumpError.caller);
    var error="Javascript error: "+str+"\n"+"Stack trace:\n"+stacktrace;
    DB_SendJSReport(error);
}

function DumpException(e){
    DumpError(e);
}

function DB_ToggleDebugMode(){}

function DB_ShowDebugFooter(){}

function DB_ShowTimingFooter(){}

function DB_Event(){}

function DB_StartJS(){}

function DB_EndJS(){}

function DB_ClearTimings(){}

function DB_ExportTimings(){}

function DB_WriteDebugLinks(){}

function DB_GetStackTrace(fn){
    try{
        if(is_nav){
            return Error().stack;
        }
        if(!fn){
            return "";
        }
        var x="\- "+DB_GetFunctionName(fn)+"(";
        for(var i=0; i<fn.arguments.length; i++){
            if(i>0){
                x+=", ";
            }
            var arg=String(fn.arguments[i]);
            if(arg.length>40){
                arg=arg.substr(0,40)+"...";
            }
            x+=arg;
        }
        x+=")\n";
        x+=DB_GetStackTrace(fn.caller);
        return x;
    }
    catch(ex){
        return "[Cannot get stack trace]: "+ex+"\n";
    }
}
var function_name_re_=/function (\w+)/;

function DB_GetFunctionName(fn){
    var m=function_name_re_.exec(String(fn));
    if(m){
        return m[1];
    }
    return "";
}

var DB_last_report_time=0;
var DB_unsent_reports=0;
var REPORT_INTERVAL=60*1000;

function DB_SendJSReport(msg){
	alert(msg);
	return;
    try{
        var now=Now();
        if((now-DB_last_report_time)<REPORT_INTERVAL){
            DB_unsent_reports++;
            return;
        }
        DB_last_report_time=now;
        var unsent=(DB_unsent_reports>0)?(DB_unsent_reports+" other errors"):"";
        DB_unsent_reports=0;
        var data="Browser: "+agt+"\n"+unsent+"\n\n"+msg;
        var xmlhttp=XmlHttpCreate();
        XmlHttpPOST(xmlhttp,U_JSReportURL(),data,function (){});
    }
    catch(e){
    }
}

function DB_ReportBadRelatedInfo(){
    return false;
}


// Auto complete stuff

var USER_MAX_HINTS=25;
var USER_lookup_table=new Map();
var USER_cancel_keystroke;
var USER_states=new Map();

function USER_state(){
    this.clear();
};

USER_state.prototype.clear=
	function (){
		this.match_info=null;
		this.selected_match=-1;
		this.cursor_pos=0;
	};

function USER_GetState(win, id){
    var state=USER_states.get(win.name+id);
    AssertTrue(state);
    return state;
}

function USER_GetActiveTextboxId(win){
    return GetWindowData(win).USER_active;
}

function USER_SetActiveTextboxId(win, id){
    GetWindowData(win).USER_active=id;
}

function USER_IsActiveTextboxId(win, id){
    return id==GetWindowData(win).USER_active;
}

function USER_InitTextbox(win, box, id){
    box.id=id;
    box.onkeydown=
		function (e){
			return USER_OnKeyDown(win,this,e);
		};
    box.onkeypress=
		function (e){
			return USER_OnKeyPress(win,this,e);
		};
    box.onblur=
		function (){
			USER_OnBlur(win,this);
		};
    box.onfocus=
		function (){
			USER_UpdateHintsNow(win,this.id);
		};
    box.onclick=
		function (){
			USER_UpdateHintsNow(win,this.id);
		};
    USER_states.put(win.name+box.id,new USER_state());
}

function USER_ContactsChanged(){
    USER_lookup_table=new Map();
}

function USER_GetContactInfo(email){
    var index=USER_FindContactIndex(_users_,email);
    if(index>=0){
        return _users_[index];
    }
    return null;
}

function USER_FindContactIndex(contacts, email){
    for(var i=0; i<contacts.length; i++){
        if(contacts[i][C_ADDRESS]==email){
            return i;
        }
    }
    return -1;
}

function USER_GetContactAddress(contact){
    var name=contact[C_NAME];
    return ((name!="")?('"'+name+'" '):"")+"<"+contact[C_ADDRESS]+">";
}

function USER_GetContactAddressLogin(contact){
    var name=contact[C_ADDRESS];
    return name;
}

function USER_OnKeyDown(win, textbox, e){
    try{
        e=e?e:win.event;
        var keycode=GetKeyCode(e);
        USER_cancel_keystroke=false;
        switch(keycode){
            case TAB_KEYCODE:if(e.shiftKey){
                break;
            }
            case ENTER_KEYCODE:if(USER_Complete(win,textbox)){
                USER_HideHints(win);
                USER_cancel_keystroke=true;
            }
            break;
            case UP_KEYCODE:case DOWN_KEYCODE:if(USER_IsActiveTextboxId(win,textbox.id)){
                USER_MoveSelection(win,textbox,keycode==UP_KEYCODE);
                USER_cancel_keystroke=true;
            }
            break;
            case ESC_KEYCODE:break;
            default:USER_UpdateHints(win,textbox);
        }
        return (!USER_cancel_keystroke);
    }
    catch(ex){
        DumpException(ex);
    }
}

function USER_OnKeyPress(win, textbox, e){
    return (!USER_cancel_keystroke);
}

function USER_OnBlur(win, textbox){
    try{
        win.setTimeout('USER_OnBlur2(window,"'+textbox.id+'")',200);
    }
    catch(ex){
        DumpException(ex);
    }
}

function USER_OnBlur2(win, id){
    try{
        if(USER_IsActiveTextboxId(win,id)){
            USER_HideHints(win);
        }
    }
    catch(ex){
        DumpException(ex);
    }
}

function USER_EventHandler(win, info){
    if(info.type==ONCLICK && info.id_prefix==USER_HINT_ID_PREFIX){
        var textbox=GetElement(win,USER_GetActiveTextboxId(win));
        USER_Complete(win,textbox);
        USER_HideHints(win);
        textbox.focus();
        return false;
    }
}

function USER_MouseMovementHandler(win, info){
    if(info.id_prefix==USER_HINT_ID_PREFIX && info.type==ONMOUSEOVER){
        var textbox=GetElement(win,USER_GetActiveTextboxId(win));
        USER_SetSelection(win,textbox,parseInt(info.id_n));
    }
}

function USER_UpdateHints(win, textbox){
    var windata=GetWindowData(win);
    if(windata.USER_timeout){
        win.clearTimeout(windata.USER_timeout);
    }
    windata.USER_timeout=win.setTimeout('USER_UpdateHintsNow(window,"'+textbox.id+'")',50);
}

function USER_UpdateHintsNow(win, id){
    try{
/*
        if(win.name!=Main_visible_view){
            return;
        }
*/       
        GetWindowData(win).timeout=null;
        var textbox=GetElement(win,id);
        var cursor=GetCursorPos(win,textbox);
        if(cursor>=0){
            var partial=USER_GetPartialEmail(textbox.value,cursor);
            if(partial.length>0){
                var state=USER_GetState(win,textbox.id);
                state.cursor_pos=cursor;
                USER_ShowHints(win,textbox,state,CleanEmailAddress(partial));
                return;
            }
        }
        USER_HideHints(win);
    }
    catch(e){
        DumpException(e);
    }
}

function USER_GetPartialEmail(str, cursor){
    var segment=USER_GetEditSegment(str,cursor);
    var partial=str.substring(segment[0],segment[1]);
    return partial.replace(/\"/g,"");
}

function USER_GetEditSegment(str, cursor){
    return [USER_GetEditStart(str,cursor),USER_GetEditEnd(str,cursor)];
}

function USER_GetEditStart(str, cursor){
    AssertTrue(cursor<=str.length);
    var good_char=cursor-1;
    for(var i=good_char; i>=0; i--){
        var ch=str.charAt(i);
        if(ch==','||ch==';'){
            break;
        }
        else {
            if(!IsSpace(ch)){
                good_char=i;
            }
        }
    }
    return good_char;
}

function USER_GetEditEnd(str, cursor){
    AssertTrue(cursor<=str.length);
    var i;
    for(i=cursor; i<str.length; i++){
        var ch=str.charAt(i);
        if(ch==','||ch=='"'){
            break;
        }
    }
    return i;
}

function USER_Complete(win, textbox){
    var state=USER_GetState(win,textbox.id);
    if(state.match_info && state.selected_match>=0 && state.selected_match<state.match_info.contacts.length && state.cursor_pos>=0){
        var contacts=state.match_info.contacts;
        var selected=state.selected_match;
        USER_CompleteTo(win,textbox,state,USER_GetContactAddressLogin(contacts[selected]));
        return true;
    }
    return false;
}

function USER_CompleteTo(win, textbox, state, email){
    var cursor=state.cursor_pos;
    if(cursor>=0){
        var textstr=textbox.value;
        var segment=USER_GetEditSegment(textstr,cursor);
        var comma=(segment[1]==textstr.length||textstr.charAt(segment[1])!=',')?", ":"";
        var result=textstr.substring(0,segment[0])+email+comma+textstr.substring(segment[1]);
        if(is_safari){
            win.setTimeout(
				function (){
					textbox.value=result;
				},0);
        }
        else {
            textbox.value=result;
        }
        SetCursorPos(win,textbox,segment[0]+email.length+comma.length);
        state.clear();
        return true;
    }
    return false;
}

function USER_Matches(str){
    str=str.toLowerCase();
    var name_address=ParseAddress(str);
    var name=name_address[0];
    var address=name_address[1];
    if(!name && !address){
        return null;
    }
    var match_info=[];
    match_info.name=name;
    match_info.address=address;
    var at=match_info.address.indexOf("@");
    if(at!=-1 && address.indexOf(".",at)!=-1 && USER_GetContactInfo(address)!=null){
        return null;
    }
    var result;
    var cached=USER_lookup_table.get(str);
    if(cached){
        result=cached;
    }
    else {
        for(var i=str.length-1; i>0; i--){
            var prefix=str.substr(0,i);
            cached=USER_lookup_table.get(prefix);
            if(cached){
                break;
            }
        }
        var contacts=(cached!=null)?cached:_users_;
        result=USER_MatchContacts(contacts,match_info.name,match_info.address);
        USER_lookup_table.put(str,result);
    }
    match_info.contacts=result;
    return match_info;
}

function USER_MatchContacts(contacts, name, address){
    var result=[];
    for(var i=0; i<contacts.length; i++){
        var c=contacts[i];
        var c_address=c[C_ADDRESS];
        if(address){
            if(c_address.indexOf(address)==0){
                result[result.length]=c;
            }
        }
        else {
            if(name){
                var c_name=c[C_NAME];
                if((c_name && USER_MatchSubstring(c_name.toLowerCase(),name)>=0)||c_address.indexOf(name)==0){
                    result[result.length]=c;
                }
            }
        }
    }
    return result;
}

function USER_MatchSubstring(str, substr){
    for(var pos=0;;pos++){
        pos=str.indexOf(substr,pos);
        if(pos<0){
            return -1;
        }
        if(pos==0||!IsLetterOrDigit(str.charAt(pos-1))){
            return pos;
        }
    }
}

function USER_MoveSelection(win, textbox, up){
    var dir=(up)?-1:1;
    var state=USER_GetState(win,textbox.id);
    USER_SetSelection(win,textbox,state.selected_match+dir);
}

function USER_SetSelection(win, textbox, pos){
    var state=USER_GetState(win,textbox.id);
    AssertTrue(textbox && state);
    if(pos!=state.selected_match && state.match_info!=null && pos>=0 && pos<state.match_info.contacts.length && pos<USER_MAX_HINTS){
        state.selected_match=pos;
        USER_RenderHintsWindow(win,textbox,state);
    }
}

function USER_ShowHints(win, textbox, state, str){
    var curr_address=null;
    if(state.selected_match>0){
        curr_address=state.match_info.contacts[state.selected_match][C_ADDRESS];
    }
    state.match_info=USER_Matches(str);
    if(state.match_info && state.match_info.contacts.length>0){
        state.selected_match=0;
        if(curr_address){
            var new_index=USER_FindContactIndex(state.match_info.contacts,curr_address);
            if(new_index>=0){
                state.selected_match=new_index;
            }
        }
        USER_RenderHintsWindow(win,textbox,state);
    }
    else {
        USER_HideHints(win);
    }
}

function USER_RenderHintsWindow(win, textbox, state){
    AssertTrue(state.match_info && state.match_info.contacts.length>0);
    if(!USER_IsActiveTextboxId(win,textbox.id)){
        USER_HideHints(win);
    }
    USER_SetActiveTextboxId(win,textbox.id);
    var html=["<div class="+AUTOCOMPLETE_CLASS+">"];
    var spanLengh = 0;
    for(var i=0; i<state.match_info.contacts.length && i<USER_MAX_HINTS; i++){
        var contact=state.match_info.contacts[i];
        Debug("Contact: "+contact);
        html[html.length]="<span class='l"+((i==state.selected_match)?" sel' ":"' ")+"id='"+USER_HINT_ID_PREFIX+i+"'>"+USER_FormatContact(contact,state.match_info)+"</span>";
    }
    html[html.length]="</div>";
    var h=CreateDIV(win,HINT_MENU_ID);
    h.innerHTML=html.join("");
    var s=h.style;
    //s.width='300px';
    if(s.display!="block"){
        s.display="block";
        s.top=GetPageOffsetTop(textbox)+textbox.offsetHeight;
        s.left=GetPageOffsetLeft(textbox);
    }
}

function USER_HideHints(win){
    var h=GetElement(win,HINT_MENU_ID);
    if(h){
        ShowElement(h,false);
    }
    var active_id=USER_GetActiveTextboxId(win);
    if(active_id){
        USER_GetState(win,active_id).clear();
    }
    USER_SetActiveTextboxId(win,null);
    if(windata.USER_timeout){
        win.clearTimeout(win.USER_timeout);
        windata.USER_timeout=null;
    }
}

function USER_FormatContact(contact, match_info){
    var name=(contact[C_NAME]!="")?('"'+contact[C_NAME]+'"'):"";
    var addr=" <"+contact[C_ADDRESS]+">";
    return BoldSubstring(name,match_info.name)+BoldSubstring(addr,(match_info.address)?match_info.address:match_info.name);
}

function BoldSubstring(str, prefix){
    if(prefix!=""){
        var pos=USER_MatchSubstring(str.toLowerCase(),prefix);
        if(pos!=-1){
            return (HtmlEscape(str.substr(0,pos))+"<b>"+HtmlEscape(str.substr(pos,prefix.length))+"</b>"+HtmlEscape(str.substr(pos+prefix.length)));
        }
    }
    return HtmlEscape(str);
}


// ASSIGNEE

var ASSIGNEE_MAX_HINTS=25;
var ASSIGNEE_lookup_table=new Map();
var ASSIGNEE_cancel_keystroke;
var ASSIGNEE_states=new Map();

function ASSIGNEE_state(){
    this.clear();
};

ASSIGNEE_state.prototype.clear=
	function (){
		this.match_info=null;
		this.selected_match=-1;
		this.cursor_pos=0;
	};

function ASSIGNEE_GetState(win, id){
    var state=ASSIGNEE_states.get(win.name+id);
    AssertTrue(state);
    return state;
}

function ASSIGNEE_GetActiveTextboxId(win){
    return GetWindowData(win).ASSIGNEE_active;
}

function ASSIGNEE_SetActiveTextboxId(win, id){
    GetWindowData(win).ASSIGNEE_active=id;
}

function ASSIGNEE_IsActiveTextboxId(win, id){
    return id==GetWindowData(win).ASSIGNEE_active;
}

function ASSIGNEE_InitTextbox(win, box, id){
    box.id=id;
    box.onkeydown=
		function (e){
			return ASSIGNEE_OnKeyDown(win,this,e);
		};
    box.onkeypress=
		function (e){
			return ASSIGNEE_OnKeyPress(win,this,e);
		};
    box.onblur=
		function (){
			ASSIGNEE_OnBlur(win,this);
		};
    box.onfocus=
		function (){
			ASSIGNEE_UpdateHintsNow(win,this.id);
		};
    box.onclick=
		function (){
			ASSIGNEE_UpdateHintsNow(win,this.id);
		};
    ASSIGNEE_states.put(win.name+box.id,new ASSIGNEE_state());
}

function ASSIGNEE_ContactsChanged(){
    ASSIGNEE_lookup_table=new Map();
}

function ASSIGNEE_GetContactInfo(email){
    var index=ASSIGNEE_FindContactIndex(_assignees_,email);
    if(index>=0){
        return _assignees_[index];
    }
    return null;
}

function ASSIGNEE_FindContactIndex(contacts, email){
    for(var i=0; i<contacts.length; i++){
        if(contacts[i][C_ADDRESS]==email){
            return i;
        }
    }
    return -1;
}

function ASSIGNEE_GetContactAddress(contact){
    var name=contact[C_NAME];
    return ((name!="")?('"'+name+'" '):"")+"<"+contact[C_ADDRESS]+">";
}

function ASSIGNEE_GetContactAddressLogin(contact){
    var name=contact[C_ADDRESS];
    return name;
}

function ASSIGNEE_OnKeyDown(win, textbox, e){
    try{
        e=e?e:win.event;
        var keycode=GetKeyCode(e);
        ASSIGNEE_cancel_keystroke=false;
        switch(keycode){
            case TAB_KEYCODE:if(e.shiftKey){
                break;
            }
            case ENTER_KEYCODE:if(ASSIGNEE_Complete(win,textbox)){
                ASSIGNEE_HideHints(win);
                ASSIGNEE_cancel_keystroke=true;
            }
            break;
            case UP_KEYCODE:case DOWN_KEYCODE:if(ASSIGNEE_IsActiveTextboxId(win,textbox.id)){
                ASSIGNEE_MoveSelection(win,textbox,keycode==UP_KEYCODE);
                ASSIGNEE_cancel_keystroke=true;
            }
            break;
            case ESC_KEYCODE:break;
            default:ASSIGNEE_UpdateHints(win,textbox);
        }
        return (!ASSIGNEE_cancel_keystroke);
    }
    catch(ex){
        DumpException(ex);
    }
}

function ASSIGNEE_OnKeyPress(win, textbox, e){
    return (!ASSIGNEE_cancel_keystroke);
}

function ASSIGNEE_OnBlur(win, textbox){
    try{
        win.setTimeout('ASSIGNEE_OnBlur2(window,"'+textbox.id+'")',200);
    }
    catch(ex){
        DumpException(ex);
    }
}

function ASSIGNEE_OnBlur2(win, id){
    try{
        if(ASSIGNEE_IsActiveTextboxId(win,id)){
            ASSIGNEE_HideHints(win);
        }
    }
    catch(ex){
        DumpException(ex);
    }
}

function ASSIGNEE_EventHandler(win, info){
    if(info.type==ONCLICK && info.id_prefix==ASSIGNEE_HINT_ID_PREFIX){
        var textbox=GetElement(win,ASSIGNEE_GetActiveTextboxId(win));
        ASSIGNEE_Complete(win,textbox);
        ASSIGNEE_HideHints(win);
        textbox.focus();
        return false;
    }
}

function ASSIGNEE_MouseMovementHandler(win, info){
    if(info.id_prefix==ASSIGNEE_HINT_ID_PREFIX && info.type==ONMOUSEOVER){
        var textbox=GetElement(win,ASSIGNEE_GetActiveTextboxId(win));
        ASSIGNEE_SetSelection(win,textbox,parseInt(info.id_n));
    }
}

function ASSIGNEE_UpdateHints(win, textbox){
    var windata=GetWindowData(win);
    if(windata.ASSIGNEE_timeout){
        win.clearTimeout(windata.ASSIGNEE_timeout);
    }
    windata.ASSIGNEE_timeout=win.setTimeout('ASSIGNEE_UpdateHintsNow(window,"'+textbox.id+'")',50);
}

function ASSIGNEE_UpdateHintsNow(win, id){
    try{
/*
        if(win.name!=Main_visible_view){
            return;
        }
*/       
        GetWindowData(win).timeout=null;
        var textbox=GetElement(win,id);
        var cursor=GetCursorPos(win,textbox);
        if(cursor>=0){
            var partial=ASSIGNEE_GetPartialEmail(textbox.value,cursor);
            if(partial.length>0){
                var state=ASSIGNEE_GetState(win,textbox.id);
                state.cursor_pos=cursor;
                ASSIGNEE_ShowHints(win,textbox,state,CleanEmailAddress(partial));
                return;
            }
        }
        ASSIGNEE_HideHints(win);
    }
    catch(e){
        DumpException(e);
    }
}

function ASSIGNEE_GetPartialEmail(str, cursor){
    var segment=ASSIGNEE_GetEditSegment(str,cursor);
    var partial=str.substring(segment[0],segment[1]);
    return partial.replace(/\"/g,"");
}

function ASSIGNEE_GetEditSegment(str, cursor){
    return [ASSIGNEE_GetEditStart(str,cursor),ASSIGNEE_GetEditEnd(str,cursor)];
}

function ASSIGNEE_GetEditStart(str, cursor){
    AssertTrue(cursor<=str.length);
    var good_char=cursor-1;
    for(var i=good_char; i>=0; i--){
        var ch=str.charAt(i);
        if(ch==','||ch==';'){
            break;
        }
        else {
            if(!IsSpace(ch)){
                good_char=i;
            }
        }
    }
    return good_char;
}

function ASSIGNEE_GetEditEnd(str, cursor){
    AssertTrue(cursor<=str.length);
    var i;
    for(i=cursor; i<str.length; i++){
        var ch=str.charAt(i);
        if(ch==','||ch=='"'){
            break;
        }
    }
    return i;
}

function ASSIGNEE_Complete(win, textbox){
    var state=ASSIGNEE_GetState(win,textbox.id);
    if(state.match_info && state.selected_match>=0 && state.selected_match<state.match_info.contacts.length && state.cursor_pos>=0){
        var contacts=state.match_info.contacts;
        var selected=state.selected_match;
        ASSIGNEE_CompleteTo(win,textbox,state,ASSIGNEE_GetContactAddressLogin(contacts[selected]));
        return true;
    }
    return false;
}

function ASSIGNEE_CompleteTo(win, textbox, state, email){
    var cursor=state.cursor_pos;
    if(cursor>=0){
        var textstr=textbox.value;
        var segment=ASSIGNEE_GetEditSegment(textstr,cursor);
        var comma=(segment[1]==textstr.length||textstr.charAt(segment[1])!=',')?", ":"";
        var result=textstr.substring(0,segment[0])+email+comma+textstr.substring(segment[1]);
        if(is_safari){
            win.setTimeout(
				function (){
					textbox.value=result;
				},0);
        }
        else {
            textbox.value=result;
        }
        SetCursorPos(win,textbox,segment[0]+email.length+comma.length);
        state.clear();
        return true;
    }
    return false;
}

function ASSIGNEE_Matches(str){
    str=str.toLowerCase();
    var name_address=ParseAddress(str);
    var name=name_address[0];
    var address=name_address[1];
    if(!name && !address){
        return null;
    }
    var match_info=[];
    match_info.name=name;
    match_info.address=address;
    var at=match_info.address.indexOf("@");
    if(at!=-1 && address.indexOf(".",at)!=-1 && ASSIGNEE_GetContactInfo(address)!=null){
        return null;
    }
    var result;
    var cached=ASSIGNEE_lookup_table.get(str);
    if(cached){
        result=cached;
    }
    else {
        for(var i=str.length-1; i>0; i--){
            var prefix=str.substr(0,i);
            cached=ASSIGNEE_lookup_table.get(prefix);
            if(cached){
                break;
            }
        }
        var contacts=(cached!=null)?cached:_assignees_;
        result=ASSIGNEE_MatchContacts(contacts,match_info.name,match_info.address);
        ASSIGNEE_lookup_table.put(str,result);
    }
    match_info.contacts=result;
    return match_info;
}

function ASSIGNEE_MatchContacts(contacts, name, address){
    var result=[];
    for(var i=0; i<contacts.length; i++){
        var c=contacts[i];
        var c_address=c[C_ADDRESS];
        if(address){
            if(c_address.indexOf(address)==0){
                result[result.length]=c;
            }
        }
        else {
            if(name){
                var c_name=c[C_NAME];
                if((c_name && ASSIGNEE_MatchSubstring(c_name.toLowerCase(),name)>=0)||c_address.indexOf(name)==0){
                    result[result.length]=c;
                }
            }
        }
    }
    return result;
}

function ASSIGNEE_MatchSubstring(str, substr){
    for(var pos=0;;pos++){
        pos=str.indexOf(substr,pos);
        if(pos<0){
            return -1;
        }
        if(pos==0||!IsLetterOrDigit(str.charAt(pos-1))){
            return pos;
        }
    }
}

function ASSIGNEE_MoveSelection(win, textbox, up){
    var dir=(up)?-1:1;
    var state=ASSIGNEE_GetState(win,textbox.id);
    ASSIGNEE_SetSelection(win,textbox,state.selected_match+dir);
}

function ASSIGNEE_SetSelection(win, textbox, pos){
    var state=ASSIGNEE_GetState(win,textbox.id);
    AssertTrue(textbox && state);
    if(pos!=state.selected_match && state.match_info!=null && pos>=0 && pos<state.match_info.contacts.length && pos<ASSIGNEE_MAX_HINTS){
        state.selected_match=pos;
        ASSIGNEE_RenderHintsWindow(win,textbox,state);
    }
}

function ASSIGNEE_ShowHints(win, textbox, state, str){
    var curr_address=null;
    if(state.selected_match>0){
        curr_address=state.match_info.contacts[state.selected_match][C_ADDRESS];
    }
    state.match_info=ASSIGNEE_Matches(str);
    if(state.match_info && state.match_info.contacts.length>0){
        state.selected_match=0;
        if(curr_address){
            var new_index=ASSIGNEE_FindContactIndex(state.match_info.contacts,curr_address);
            if(new_index>=0){
                state.selected_match=new_index;
            }
        }
        ASSIGNEE_RenderHintsWindow(win,textbox,state);
    }
    else {
        ASSIGNEE_HideHints(win);
    }
}

function ASSIGNEE_RenderHintsWindow(win, textbox, state){
    AssertTrue(state.match_info && state.match_info.contacts.length>0);
    if(!ASSIGNEE_IsActiveTextboxId(win,textbox.id)){
        ASSIGNEE_HideHints(win);
    }
    ASSIGNEE_SetActiveTextboxId(win,textbox.id);
    var html=["<div class="+AUTOCOMPLETE_CLASS+">"];
    var spanLengh = 0;
    for(var i=0; i<state.match_info.contacts.length && i<ASSIGNEE_MAX_HINTS; i++){
        var contact=state.match_info.contacts[i];
        Debug("Contact: "+contact);
        html[html.length]="<span class='l"+((i==state.selected_match)?" sel' ":"' ")+"id='"+ASSIGNEE_HINT_ID_PREFIX+i+"'>"+ASSIGNEE_FormatContact(contact,state.match_info)+"</span>";
    }
    html[html.length]="</div>";
    var h=CreateDIV(win,HINT_MENU_ID);
    h.innerHTML=html.join("");
    var s=h.style;
    //s.width='300px';
    if(s.display!="block"){
        s.display="block";
        s.top=GetPageOffsetTop(textbox)+textbox.offsetHeight;
        s.left=GetPageOffsetLeft(textbox);
    }
}

function ASSIGNEE_HideHints(win){
    var h=GetElement(win,HINT_MENU_ID);
    if(h){
        ShowElement(h,false);
    }
    var active_id=ASSIGNEE_GetActiveTextboxId(win);
    if(active_id){
        ASSIGNEE_GetState(win,active_id).clear();
    }
    ASSIGNEE_SetActiveTextboxId(win,null);
    if(windata.ASSIGNEE_timeout){
        win.clearTimeout(win.ASSIGNEE_timeout);
        windata.ASSIGNEE_timeout=null;
    }
}

function ASSIGNEE_FormatContact(contact, match_info){
    var name=(contact[C_NAME]!="")?('"'+contact[C_NAME]+'"'):"";
    var addr=" <"+contact[C_ADDRESS]+">";
    return BoldSubstring(name,match_info.name)+BoldSubstring(addr,(match_info.address)?match_info.address:match_info.name);
}

function BoldSubstring(str, prefix){
    if(prefix!=""){
        var pos=ASSIGNEE_MatchSubstring(str.toLowerCase(),prefix);
        if(pos!=-1){
            return (HtmlEscape(str.substr(0,pos))+"<b>"+HtmlEscape(str.substr(pos,prefix.length))+"</b>"+HtmlEscape(str.substr(pos+prefix.length)));
        }
    }
    return HtmlEscape(str);
}


// Custom Functions

function U_MakeUnique(url){
	var randVal = Math.round(Math.random()*2147483648.000000);
    if(url.indexOf('?')<0){
        return url + '?' + randVal;
    }
    return url + randVal;
}

function LoadUsers(url){
    var xmlhttp=XmlHttpCreate();
    var handler=
		function (){
			try{
				if(4==xmlhttp.readyState && 200==xmlhttp.status){
					var new_users=eval(xmlhttp.responseText);
					if(String(_users_)!=String(new_users)){
						_users_=new_users;
						USER_ContactsChanged();
						Debug("Contacts updated");
					}
				}
			}
			catch(e){
				DumpException(e);
			}
		};
    XmlHttpGET(xmlhttp,url,handler);
}

function LoadAssignees(url){
    var xmlhttp=XmlHttpCreate();
    var handler=
		function (){
			try{
				if(4==xmlhttp.readyState && 200==xmlhttp.status){
					var new_assignees=eval(xmlhttp.responseText);
					if(String(_assignees_)!=String(new_assignees)){
						_assignees_=new_assignees;
						ASSIGNEE_ContactsChanged();
						Debug("Contacts updated");
					}
				}
			}
			catch(e){
				DumpException(e);
			}
		};
    XmlHttpGET(xmlhttp,url,handler);
}

// Init and Load AC

var _users_=[];
var _assignees_=[];

