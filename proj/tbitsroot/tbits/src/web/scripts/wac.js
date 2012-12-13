/* start dhtml building blocks */
// **** changed here 1 for delimiter (;,) 
// **** changed here 2 for removing special chars from input
function freezeEvent(e) 
{
    if (e.preventDefault)
    {
        e.preventDefault();
    }

    e.returnValue = false;
    e.cancelBubble = true;

    if (e.stopPropagation) 
    {
        e.stopPropagation();
    }
    return false;
}//freezeEvent

function isWithinNode(e,i,c,t,obj) 
{
    answer = false;
    te = e;
    while(te && !answer) 
    {
	    if	((te.id && (te.id == i)) || (te.className && (te.className == i+"Class"))
		    	|| (!t && c && te.className && (te.className == c))
			    || (!t && c && te.className && (te.className.indexOf(c) != -1))
			    || (t && te.tagName && (te.tagName == t))
			    || (obj && (te == obj))
		    ) 
		{
		    answer = te;
	    }
	    else 
	    {
		    te = te.parentNode;
	    }
    }
    return te;
}//isWithinNode

function getEvent(event) 
{
    return (event ? event : window.event);
}//getEvent()

function getEventElement(e) 
{
    return (e.srcElement ? e.srcElement: (e.target ? e.target : e.currentTarget));
}//getEventElement()

function findElementPosX(obj) 
{
	curleft = 0;
	if (obj.offsetParent) 
	{
		while (obj.offsetParent) 
		{
			curleft += obj.offsetLeft;
			obj = obj.offsetParent;
		}
	}//if offsetParent exists
	else if (obj.x)
	{
		curleft += obj.x
	}
	return curleft;
}//findElementPosX

function findElementPosY(obj) 
{
	curtop = 0;
	if (obj.offsetParent) 
	{
		while (obj.offsetParent) 
		{
			curtop += obj.offsetTop;
			obj = obj.offsetParent;
		}
	}//if offsetParent exists
	else if (obj.y)
	{
		curtop += obj.y
	}
	return curtop;
}//findElementPosY

/* end dhtml building blocks */

function handleKeyPress(event) 
{
    e = getEvent(event);
    eL = getEventElement(e);

    upEl = isWithinNode(eL,null,"wickEnabled",null,null);

    kc = e["keyCode"];

    if (siw && ((kc == 13) || (kc == 9))) 
    {
	    siw.selectingSomething = true;
	    if (siw.isSafari)
	    {
	        siw.inputBox.blur();   //hack to "wake up" safari
	    }
	    siw.inputBox.focus();
	    siw.inputBox.value = siw.inputBox.value.replace(/[ \r\n\t\f\s]+$/gi,' ');
	    hideSmartInputFloater();
	    e.cancelBubble = true;
	    e.returnValue = false;
    } 
    else if (upEl && (kc != 38) && (kc != 40) && (kc != 37) && (kc != 39) && (kc != 13) && (kc != 27)) 
    {
	    if (!siw || (siw && !siw.selectingSomething)) 
	    {
		    processSmartInput(upEl);
	    }
    }
    else if (siw && siw.inputBox) 
    {
	    siw.inputBox.focus(); //kinda part of the hack.
    }
}//handleKeyPress()


function handleKeyDown(event)
{
    e = getEvent(event);
    eL = getEventElement(e);

    if (siw && (kc = e["keyCode"])) 
    {
	    if (kc == 40) 
	    {
		    siw.selectingSomething = true;
		    freezeEvent(e);
		    if (siw.isGecko) siw.inputBox.blur(); /* Gecko hack */
		    selectNextSmartInputMatchItem();
	    }
	    else if (kc == 38) 
	    {
		    siw.selectingSomething = true;
		    freezeEvent(e);
		    if (siw.isGecko) siw.inputBox.blur();
		    selectPreviousSmartInputMatchItem();
	    }
	    else if ((kc == 13) || (kc == 9)) 
	    {
		    siw.selectingSomething = true;
		    activateCurrentSmartInputMatch();
		    freezeEvent(e);
		    e.cancelBubble = true;
		    e.returnValue = false;
	    }
	    else if (kc == 27)  
	    {
		    hideSmartInputFloater();
		    freezeEvent(e);
	    }
	    else
	    {
		    siw.selectingSomething = false;
	    }
    }
}//handleKeyDown()

function handleFocus(event) 
{
	e = getEvent(event);
	eL = getEventElement(e);		userSetup();	
	if (focEl = isWithinNode(eL,null,"wickEnabled",null,null)) 
	{
	    collection = hashCollections[focEl.id] ;
	    if (!collection) collection = ourDefaultCollection;
		if ( siw )
			siw.revisedCollection = null;
	    if (!siw || (siw && !siw.selectingSomething)) 
	    {
	        processSmartInput(focEl);
	    }
	}
}//handleFocus()

function handleBlur(event) 
{
	e = getEvent(event);
	eL = getEventElement(e);
	if (blurEl = isWithinNode(eL,null,"wickEnabled",null,null)) 
	{
		if (siw && !siw.selectingSomething) hideSmartInputFloater();
	}
}//handleBlur()

function handleClick(event) 
{
	e2 = getEvent(event);
	eL2 = getEventElement(e2);
	if (siw && siw.selectingSomething) 
	{
		selectFromMouseClick();
	}
}//handleClick()

function handleMouseOver(event) 
{
	e = getEvent(event);
	eL = getEventElement(e);
	if (siw && (mEl = isWithinNode(eL,null,"matchedSmartInputItem",null,null))) 
	{
		siw.selectingSomething = true;
		selectFromMouseOver(mEl);
	} 
	else if (isWithinNode(eL,null,"siwCredit",null,null)) 
	{
		siw.selectingSomething = true;
	}
	else if (siw) 
	{
		siw.selectingSomething = false;
	}
}//handleMouseOver

function showSmartInputFloater() 
{
    if (!siw.floater.style.display || (siw.floater.style.display=="none")) 
    {
	    if (!siw.customFloater) 
	    {
		    x = findElementPosX(siw.inputBox);
		    y = findElementPosY(siw.inputBox) + siw.inputBox.offsetHeight;
		    //hack: browser-specific adjustments.
		    if (!siw.isGecko && !siw.isWinIE) x += 8;
		    if (!siw.isGecko && !siw.isWinIE) y += 10;
		    siw.floater.style.left = x;
		    siw.floater.style.top = y;

 		    siw.frameCtrl.style.left = x;
		    siw.frameCtrl.style.top = y;
	    }
	    else
	    {
	        //you may
	        //do additional things for your custom floater
	        //beyond setting display and visibility
	    }
		siw.frameCtrl.style.display = "block";
		siw.frameCtrl.style.visibility = "visible";

	    siw.floater.style.display="block";
	    siw.floater.style.visibility="visible";

    }
	siw.frameCtrl.style.height = siw.floater.clientHeight ;
	siw.frameCtrl.style.width = siw.floater.clientWidth;
		
}//showSmartInputFloater()

function hideSmartInputFloater() 
{
    if (siw) 
    {
        siw.floater.style.display="none";
        siw.floater.style.visibility="hidden";

		siw.frameCtrl.style.display = "none";
		siw.frameCtrl.style.visiblity = "hidden";

        siw = null;
    }//siw exists
}//hideSmartInputFloater

function processSmartInput(inputBox) 
{
    if (!siw) siw = new smartInputWindow();
    siw.inputBox = inputBox;

    classData = inputBox.className.split(" ");
    siwDirectives = null;
    for (i=0;(!siwDirectives && classData[i]);i++) 
    {
	    if (classData[i].indexOf("wickEnabled") != -1)
		    siwDirectives = classData[i];
    }

    if (siwDirectives && (siwDirectives.indexOf(":") != -1)) 
    {
        siw.customFloater = true;
        newFloaterId = siwDirectives.split(":")[1];
        siw.floater = document.getElementById(newFloaterId);
        siw.floaterContent = siw.floater.getElementsByTagName("div")[0];
    }

    setSmartInputData();
    if (siw.matchCollection && (siw.matchCollection.length > 0)) selectSmartInputMatchItem(0);
    content = getSmartInputBoxContent();
    if (content) 
    {
	    modifySmartInputBoxContent(content);
	    showSmartInputFloater();
    }
    else hideSmartInputFloater();
}//processSmartInput()

function smartInputMatch(cleanValue, value) 
{
	this.cleanValue = cleanValue;
	this.value = value;
	this.isSelected = false;
}//smartInputMatch

function simplify(s) 
{
    return s.replace(/^[ \s\f\t\n\r]+/,'').replace(/[ \s\f\t\n\r]+$/,'');
    //.replace(/[Ž,,,‘,\u00E9,\u00E8,\u00EA,\u00EB]/gi,"e").replace(/[ˆ,‰,\u00E0,\u00E2]/gi,"a").
}//simplify

function getUserInputToMatch(s) 
{
    a = s;
    fields = s.split(",").join(";").split(";");  // **** changed here 1
    if (fields.length > 0) 
    {
	a = fields[fields.length - 1];
	a = a.replace(/^[ \s\f\t\n\r]+/,'');
        if(a.length > 0){
           var firstChar = a.charAt(0);
           if(specialChars.join('').indexOf(firstChar) == -1){
           }else{
           a=a.substring(1,a.length);
           } // **** changed here 2
    }
    }
return a;
}//getUserInputToMatch

    // **** changed here 2 whole new function
function getUserInputToMatchAsItIs(s) 
{
    a = s;
    fields = s.split(",").join(";").split(";");  

    if (fields.length > 0) 
    {
	a = fields[fields.length - 1];
	a = a.replace(/^[ \s\f\t\n\r]+/,'');
    }
    
return a;
}//getUserInputToMatchAsItIs

function getUserInputBase() 
{
    s = siw.inputBox.value;
    a = s;
    var lastComma = s.lastIndexOf(",");
    var lastSemiColon = s.lastIndexOf(";");
    if (lastComma < lastSemiColon) lastComma = lastSemiColon;
    if (lastComma != -1) 
    {
	    a = a.replace(/^(.*(,|;)[ \r\n\t\f\s]*).*$/i,'$1'); //
	   // **** changed here 1
    }
    else
    {
	    a = "";
	}
    return a;
}//getUserInputBase()

function runMatchingLogic(userInput, standalone) 
{
    	userInput = simplify(userInput);
	uifc = userInput.charAt(0);
	if (uifc == '"') uifc = (n = userInput.charAt(1)) ? n : "z";
	if (standalone) userInput = uifc;
	if (siw) siw.matchCollection = new Array();
	pointerToCollectionToUse = collection;
	if (siw && siw.revisedCollection && 
	    (siw.revisedCollection.length > 0) && 
	    siw.lastUserInput && 
	    (userInput.indexOf(siw.lastUserInput) == 0) && 
		siw.lastBoxID == siw.inputBox.id )
	{
		pointerToCollectionToUse = siw.revisedCollection;
	} 
	else if (collectionIndex[userInput] && (collectionIndex[userInput].length > 0) && siw.lastBoxID == siw.inputBox.id ) 
	{
		pointerToCollectionToUse = collectionIndex[userInput];
	} 
	else if (collectionIndex[uifc] && (collectionIndex[uifc].length > 0) && siw.lastBoxID == siw.inputBox.id ) 
	{
		pointerToCollectionToUse = collectionIndex[uifc];
	} 
	else if (siw && (userInput.length == 1) && (!collectionIndex[uifc]) && siw.lastBoxID == siw.inputBox.id ) 
	{
		siw.buildIndex = true;
	} 
	else if (siw) 
	{
		siw.buildIndex = false;
	}
	
	tempCollection = new Array();

    try
    {
	    re1m = new RegExp("^([ \"\>\<\-]*)("+userInput+")","i");
	    re2m = new RegExp("([ \"\>\<\-]+)("+userInput+")","i");
	    re1 = new RegExp("^([ \"\}\{\-]*)("+userInput+")","gi");
	    re2 = new RegExp("([ \"\}\{\-]+)("+userInput+")","gi");
    	
	    for (i=0,j=0;(i<pointerToCollectionToUse.length);i++) 
	    {
		    displayMatches = ((!standalone) && (j < siw.MAX_MATCHES));
		    entry = pointerToCollectionToUse[i];
		    mEntry = simplify(entry);
		    if (!standalone && (mEntry.indexOf(userInput) == 0)) 
		    {
			    userInput = userInput.replace(/\>/gi,'\\}').replace(/\< ?/gi,'\\{');
			    re = new RegExp("(" + userInput + ")","i");
			    if (displayMatches) 
			    {
				    siw.matchCollection[j] = new smartInputMatch(entry, mEntry.replace(/\>/gi,'}').replace(/\< ?/gi,'{').replace(re,"<b>$1</b>"));
			    }
			    tempCollection[j] = entry;
			    j++;		
		    } 
		    else if (mEntry.match(re1m) || mEntry.match(re2m)) 
		    {
			    if (!standalone && displayMatches) 
			    {
				    siw.matchCollection[j] = new smartInputMatch(entry, mEntry.replace(/\>/gi,'}').replace(/\</gi,'{').replace(re1,"$1<b>$2</b>").replace(re2,"$1<b>$2</b>"));
			    }
			    tempCollection[j] = entry;
			    j++;
		    }
	    }//loop thru collection
	    if (siw) 
	    {
		    siw.lastUserInput = userInput;
		    siw.revisedCollection = tempCollection.join(",").split(",");
		    collectionIndex[userInput] = tempCollection.join(",").split(",");
			siw.lastBoxID = siw.inputBox.id ;
	    }
	    if (standalone || siw.buildIndex) 
	    {
		    collectionIndex[uifc] = tempCollection.join(",").split(",");
		    if (siw) siw.buildIndex = false;
	    }
    }
    catch (e)
    {
    }
}//runMatchingLogic

function setSmartInputData() 
{
    if (siw) 
    {
        orgUserInput = siw.inputBox.value;
        orgUserInput = getUserInputToMatch(orgUserInput);
        userInput = orgUserInput.
                        toLowerCase().
                        replace(/[\r\n\t\f\s]+/gi,' ').
                        replace(/^ +/gi,'').
                        replace(/ +$/gi,'').
                        replace(/ +/gi,' ').
                        replace(/\\/gi,'').
                        replace(/\[/gi,'').
                        replace(/\(/gi,'').
                        replace(/\./gi,'\.').
                        replace(/\?/gi,'');
        if (userInput && (userInput != "") && (userInput != '"')) 
        {
	        runMatchingLogic(userInput);
        }//if userinput not blank and is meaningful
        else 
        {
            siw.matchCollection = null;
        }
    }//siw exists ... uhmkaaayyyyy
}//setSmartInputData

function getSmartInputBoxContent() 
{
    a = null;
    if (siw && siw.matchCollection && (siw.matchCollection.length > 0)) 
    {
        a = '';
        for (i = 0;i < siw.matchCollection.length; i++) 
        {

            selectedString = siw.matchCollection[i].isSelected ? ' selectedSmartInputItem' : '';
            a += '<p class="matchedSmartInputItem' + selectedString + '">' + siw.matchCollection[i].value.replace(/\{ */gi,"&lt;").replace(/\} */gi,"&gt;") + '</p>';
// add
        }//
    }//siw exists
    return a;
}//getSmartInputBoxContent

function modifySmartInputBoxContent(content) 
{
    //todo: remove credits 'cuz no one gives a shit ;] - done
    siw.floaterContent.innerHTML = '<div id="smartInputResults">' + content + (siw.showCredit ? ('<p class="siwCredit">Powered By: <a target="PhrawgBlog" href="http://chrisholland.blogspot.com/?from=smartinput&ref='+escape(location.href)+'">Chris Holland</a></p>') : '') +'</div>';
    siw.matchListDisplay = document.getElementById("smartInputResults");
}//modifySmartInputBoxContent()

function selectFromMouseOver(o) 
{
	get(o);
    currentIndex = getCurrentlySelectedSmartInputItem();
    if (currentIndex != null) deSelectSmartInputMatchItem(currentIndex);
    newIndex = getIndexFromElement(o);
    selectSmartInputMatchItem(newIndex);
    modifySmartInputBoxContent(getSmartInputBoxContent());
}//selectFromMouseOver

function selectFromMouseClick() 
{
    activateCurrentSmartInputMatch();
    siw.inputBox.focus();
    hideSmartInputFloater();
}//selectFromMouseClick

function getIndexFromElement(o) 
{
    index = 0;
    while(o = o.previousSibling) 
    {
        index++;
    }//
    return index;
}//getIndexFromElement

function getCurrentlySelectedSmartInputItem() 
{
    answer = null;
    for (i = 0; ((i < siw.matchCollection.length) && !answer) ; i++) 
    {
	    if (siw.matchCollection[i].isSelected)
		    answer = i;
    }//
    return answer;
}//getCurrentlySelectedSmartInputItem

function selectSmartInputMatchItem(index) 
{
	siw.matchCollection[index].isSelected = true;
}//selectSmartInputMatchItem()

function deSelectSmartInputMatchItem(index) 
{
	siw.matchCollection[index].isSelected = false;
}//deSelectSmartInputMatchItem()

function selectNextSmartInputMatchItem() 
{
    currentIndex = getCurrentlySelectedSmartInputItem();
    if (currentIndex != null) 
    {
	    deSelectSmartInputMatchItem(currentIndex);
	    if ((currentIndex + 1) < siw.matchCollection.length)
	    {
 		    selectSmartInputMatchItem(currentIndex + 1);
	    }
	    else
	    {
		    selectSmartInputMatchItem(0);
		}
    } 
    else 
    {
	    selectSmartInputMatchItem(0);
    }
    modifySmartInputBoxContent(getSmartInputBoxContent());
}//selectNextSmartInputMatchItem

function selectPreviousSmartInputMatchItem() 
{
    currentIndex = getCurrentlySelectedSmartInputItem();
    if (currentIndex != null) 
    {
	    deSelectSmartInputMatchItem(currentIndex);
	    if ((currentIndex - 1) >= 0)
 		    selectSmartInputMatchItem(currentIndex - 1);
	    else
		    selectSmartInputMatchItem(siw.matchCollection.length - 1);
    }
    else 
    {
	    selectSmartInputMatchItem(siw.matchCollection.length - 1);
    }
    modifySmartInputBoxContent(getSmartInputBoxContent());
}//selectPreviousSmartInputMatchItem

function activateCurrentSmartInputMatch() 
{
	baseValue = getUserInputBase();
	if ((selIndex = getCurrentlySelectedSmartInputItem()) != null) 
	{
		addedValue = siw.matchCollection[selIndex].cleanValue;
		var start = addedValue.indexOf("<");
		if (start > 0)
		{
		    var end = addedValue.indexOf(">", start);
		    addedValue = addedValue.substring(start + 1, end);
		}


// **** changed here 2

                var inputToMatch =
		getUserInputToMatchAsItIs(siw.inputBox.value);
                if(inputToMatch.length > 0){
                    var firstChar = inputToMatch.charAt(0);
                    if(specialChars.join('').indexOf(firstChar) ==
		    -1){
                       }else{
addedValue=firstChar+addedValue;
}
}      
	
	theString = (baseValue ? baseValue : "") + addedValue + ", ";
		siw.inputBox.value = trim(theString);
		runMatchingLogic(addedValue, true);
	}
}//activateCurrentSmartInputMatch

function smartInputWindow () 
{
	this.customFloater = false;
    this.frameCtrl = document.getElementById("floaterFrame"); 
	this.floater = document.getElementById("smartInputFloater");
	this.floaterContent = document.getElementById("smartInputFloaterContent");
	this.selectedSmartInputItem = null;
	this.MAX_MATCHES = 15;
	this.isGecko = (navigator.userAgent.indexOf("Gecko/200") != -1);
	this.isSafari = (navigator.userAgent.indexOf("Safari") != -1);
	this.isWinIE = ((navigator.userAgent.indexOf("Win") != -1 ) && (navigator.userAgent.indexOf("MSIE") != -1 ));
	this.showCredit = false;
}//smartInputWindow Object

function registerSmartInputListeners() 
{
    inputs = document.getElementsByTagName("input");
    texts = document.getElementsByTagName("textarea");
    allinputs = new Array();
    z = 0;
    y = 0;
    while(inputs[z]) 
    {
        allinputs[z] = inputs[z];
        z++;
    }//
    while(texts[y]) 
    {
        allinputs[z] = texts[y];
        z++;
        y++;
    }//

    for (i=0; i < allinputs.length;i++) 
    {
	    if ((c = allinputs[i].className) && (c.indexOf("wickEnabled") >= 0)) 
	    {
		    allinputs[i].setAttribute("autocomplete","OFF");
		    allinputs[i].onfocus = handleFocus;
		    allinputs[i].onblur = handleBlur;
		    allinputs[i].onkeydown = handleKeyDown;
		    allinputs[i].onkeyup = handleKeyPress;
	    }
    }//loop thru inputs
}//registerSmartInputListeners


function registerInputListener(input)
{
    if (input)
    {
        var ctrl = document.getElementById(input);
        if (ctrl)
        {
		    ctrl.setAttribute("autocomplete","OFF");
		    ctrl.onfocus = handleFocus;
		    ctrl.onblur = handleBlur;
		    ctrl.onkeydown = handleKeyDown;
		    ctrl.onkeyup = handleKeyPress;
        }
    }
}

var collection = new Array();
var ourDefaultCollection = new Array();
var hashCollections = new Array();
siw = null;

if (document.addEventListener) 
{
	document.addEventListener("keydown", handleKeyDown, false);
	document.addEventListener("keyup", handleKeyPress, false);
	document.addEventListener("mouseup", handleClick, false);
	document.addEventListener("mouseover", handleMouseOver, false);
}
else 
{
	document.onkeydown = handleKeyDown;
	document.onkeyup = handleKeyPress;
	document.onmouseup = handleClick;
	document.onmouseover = handleMouseOver;
}

//registerSmartInputListeners();

document.write
(
'<iframe scrolling="auto" frameborder="0" style="position: ABSOLUTE; filter: alpha{opacity=0}; display:none;" src="about:blank" name="floaterFrame"'+
 'id="floaterFrame"></iframe>'
);


document.write 
(
'<table id="smartInputFloater" class="floater" cellpadding="0" cellspacing="0"><tr><td id="smartInputFloaterContent" nowrap="nowrap">'
+'<\/td><\/tr><\/table>'
);


//note: instruct users to the fact that no commas should be present in entries.
//it would make things insanely messy.
//this is why i'm filtering commas here:
for (x=0;x<collection.length;x++) 
{
    if (collection[x] != null)
    {
        collection[x] =
	collection[x].replace(/\,/gi,'').replace(/\;/gi,''); //
//	changed here 1
    }
}//

specialChars = new Array('*');
collectionIndex = new Array();

ds = "";
function debug(s) 
{
    ds += ( s + "\n");
}

function trim(str)
{
	str = str.replace(/^\s+/, "");
	str = str.replace(/\s+$/, "");
	return str;
}

function registerCollection( id, arr )
{
	if ( arr && arr.length )
	{
        //note: instruct users to the fact that no commas should be present in entries.
        //it would make things insanely messy.
        //this is why i'm filtering commas here:
        for (x=0;x<arr.length;x++) 
        {
            try
            {
                if (arr[x] != null)
                {
                    arr[x] = arr[x].replace(/\,/gi,'').replace(/\;/gi,''); 
                }
            }
            catch (e)
            {
                alert(arr[x]);
            }
        }//
		hashCollections[ id ] = arr;
    }
	else
	{
	    // Ideally, this event should be posted to the server that triggers some mail to the DEV group.
	}

}

function registerDefaultCollection(arr)
{
    //note: instruct users to the fact that no commas should be present in entries.
    //it would make things insanely messy.
    //this is why i'm filtering commas here:
    for (x=0;x<arr.length;x++) 
    {
        try
        {
            if (arr[x] != null)
            {
                arr[x] = arr[x].replace(/\,/gi,'').replace(/\;/gi,''); 
            }
        }
        catch (e)
        {
            alert(arr[x]);
        }
    }//
    ourDefaultCollection = arr;
}

function get(o)
{
    if (o)
    {
        var str = "";
        for (i in o)
        {
            str = o[i];
        }
        return o["innerHTML"];
    }
    else
    {
        return "null object";
    }
}
