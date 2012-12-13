// To create an autoComplete field.
function autoCompleteFunction (userinput, usercontainer, itemsList, isDelimeterRequired){
	// Instantiate second JS Array DataSource
	oACDS = new YAHOO.widget.DS_JSArray(itemsList);
								
	// Instantiate first AutoComplete
	oAutoComp = new YAHOO.widget.AutoComplete(userinput,usercontainer,oACDS);
	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.typeAhead = true;
	oAutoComp.useShadow = true;
	oAutoComp.minQueryLength = 1;
	oAutoComp.maxResultsDisplayed = 10; 
	if (isDelimeterRequired == true)
		oAutoComp.delimChar = [",",";"];	
	oAutoComp.textboxFocusEvent.subscribe(function(){
		var sInputValue = YAHOO.util.Dom.get(userinput).value;
		if(sInputValue.length === 0) {
			var oSelf = this;
			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
		}
	});								
}

function showHideSpellCheckButton( id, value )
{
	document.getElementById(id).style.visibility=value ;
}

function showRTE(rtEditor)
{
            var fc = rtEditor.get('element').previousSibling,
                el = rtEditor.get('element');
           // var p = fc.parentNode;
           // Dom.setStyle(p,'display','block');
            Dom.setStyle(fc, 'position', 'static');
            Dom.setStyle(fc, 'top', '0');
            Dom.setStyle(fc, 'left', '0');
            Dom.setStyle(el, 'visibility', 'hidden');
            Dom.setStyle(el, 'top', '-9999px');
            Dom.setStyle(el, 'left', '-9999px');
            Dom.setStyle(el, 'position', 'absolute');
            //rtEditor.get('element_cont').addClass('yui-editor-container');
            //rtEditor._setDesignMode('on');
 }        

function hideRTE(rtEditor)
{
	     // alert('inside hideRTE');
	        var fc = rtEditor.get('element').previousSibling,
            el = rtEditor.get('element');
	     // var p= fc.parentNode;
	     //Dom.setStyle(p,'display','none');
            Dom.setStyle(fc, 'position', 'absolute');
         // Dom.setStyle(fc,'display','none');
            Dom.setStyle(fc, 'top', '-9999px');
            Dom.setStyle(fc, 'left', '-9999px');
            rtEditor.get('element_cont').removeClass('yui-editor-container');
            Dom.setStyle(el, 'visibility', 'hidden');
            Dom.setStyle(el, 'top', '-9999px');
            Dom.setStyle(el, 'left', '-9999px');
            Dom.setStyle(el, 'position', 'absolute');
            
         // hide spellcheck button
            var textareaId = rtEditor.get('textarea').id ;
            var spellBtnId = textareaId + '_spellCheckButton' ;
            showHideSpellCheckButton(spellBtnId,'hidden') ;
}

function controlSummary(btnID){
	var btn = new YAHOO.widget.Button(btnID);
	btn.removeClass("yui-button");
	btn.removeClass("yui-push-button");
	btn.removeClass("yui-button-focus");
	btn.removeClass("yui-push-button-focus");
	btn.on('click',function(ev){
		Event.stopEvent(ev);
		var val = document.getElementById("summaryLabel").style.display;
    	var ed = CKEDITOR.instances.summary;    	
		if(val=='none')
	        {							    			 						    	            
	            if (!ed){
			   		document.getElementById("summaryLabel").style.display="inline";			   		
			   		document.getElementById("summary_tglBtn-button").style.display = "inline";
			   		document.getElementById("summary").style.display="inline";
			   		CKEDITOR.config.height = 100;
			   		CKEDITOR.config.toolbarStartupExpanded = false;
			   		CKEDITOR.config.enterMode = CKEDITOR.ENTER_BR;
			        CKEDITOR.replace('summary');							    	            	
	            }
	            else
	            	alert('Summary editor does not exist');
	        }
	   else   
	        { 
		   		if (ed){
	            	ed.destroy();
	            	ed=null;
	            	document.getElementById("summaryLabel").style.display="none";
	            	document.getElementById("summary").style.display="none";
	            	document.getElementById("summary_tglBtn-button").style.display = "none";
		   		}
	        }     					
	
   });
}   

function dataObj(editorObj,fieldLabel)
{
    this.editorObj=editorObj;
    this.fieldLabel=fieldLabel;
}   

function postRender(e,obj){
	
	//alert('postRender called') ;
	var fieldLabel=obj.fieldLabel;
	var editorObj =obj.editorObj;
	//alert("fieldLabeL:"+fieldLabel);
	//alert("editorObj:"+editorObj);
	document.getElementById(fieldLabel).style.display="none";//hides the Label
	hideRTE(editorObj); //hides the field 
}

//function getRichTextArea(divId){
//	return rtEditor = getRichTextEditor(divId, '90%', '300px');
//	}

//To get a rich text editor.
function getRichTextArea(divId, rtWidth, rtHeight){
	//alert('rtwidth =' + rtWidth + '  rtHeight = ' + rtHeight ) ;
	if( (typeof(rtWidth) == 'undefined') || rtWidth == null )
	{
		rtWidth = '99%' ;		
	}
	if( (typeof(rtHeight) == 'undefined') || rtHeight == null )
	{
		rtHeight = '150px' ;
	}
	
	//The sizes to map to the names
    var sizes = {
    		'8':8, '9':9, '10':10, '11':11, '12':12, '13':13, '14':14, '15':15, '16':16, '17':17, '18':18, '19':19, '20':20,
    		'21':21, '22':22, '23':23, '24':24, '25':25, '26':26, '27':27, '28':28, '29':29, '30':30, '31':31, '32':32, '33':33,
    		'34':34, '35':35, '36':36, '37':37, '38':38, '39':39, '40':40, '41':41, '42':42, '43':43, '44':44, '45':45, '46':46,
    		'47':47, '48':48, '49':49, '50':50, '51':51, '52':52, '53':53, '54':54, '55':55, '56':56, '57':57, '58':58, '59':59,
    		'60':60, '61':61, '62':62, '63':63, '64':64, '65':65, '66':66, '67':67, '68':68, '69':69, '70':70, '71':71, '72':72,
    		'73':73, '74':74, '75':75
    };
	
	//alert('rtwidth =' + rtWidth + '  rtHeight = ' + rtHeight ) ;
	//{ type: 'spin', label: '13', value: 'fontsize', range: [ 9, 75 ], disabled: true }
	var rtEditor = new YAHOO.widget.Editor(divId, {
			height: rtHeight,
			width: rtWidth,
			dompath: false, //Turns on the bar at the bottom
			animate: true, //Animates the opening, closing and moving of Editor windows
			autoHeight:true,
			extracss: '.yui-spellcheck { background-color: yellow; }',
			toolbar: {
				titlebar: '',
				collapse: false,
				buttons: [
					{ group: 'fontstyle',
						buttons: [
							{ type: 'select', label: 'Arial', value: 'fontname', disabled: true,
								menu: [
									{ text: 'Arial', checked: true },
									{ text: 'Arial Black' },
									{ text: 'Comic Sans MS' },
									{ text: 'Courier New' },
									{ text: 'Lucida Console' },
									{ text: 'Tahoma' },
									{ text: 'Times New Roman' },
									{ text: 'Trebuchet MS' },
									{ text: 'Verdana' }
								]
							}
						]
					},
					{ type: 'separator' },
					{ group: 'textstyle',
						buttons: [
							{ type: 'push', label: 'Bold CTRL + SHIFT + B', value: 'bold' },
							{ type: 'push', label: 'Italic CTRL + SHIFT + I', value: 'italic' },
							{ type: 'push', label: 'Underline CTRL + SHIFT + U', value: 'underline' },
							{ type: 'separator' },
							{ type: 'push', label: 'Subscript', value: 'subscript', disabled: true },
							{ type: 'push', label: 'Superscript', value: 'superscript', disabled: true },
							{ type: 'separator' },
							{ type: 'color', label: 'Font Color', value: 'forecolor', disabled: true },
							{ type: 'color', label: 'Background Color', value: 'backcolor', disabled: true },
							{ type: 'separator' },
							{ type: 'push', label: 'Remove Formatting', value: 'removeformat', disabled: true },
							{ type: 'push', label: 'Show/Hide Hidden Elements', value: 'hiddenelements' },
							{ type: 'separator' },
							{ type: 'push', label: 'Undo', value: 'undo' },
							{ type: 'push', label: 'Redo', value: 'redo' }
						]
					},
					{ type: 'separator' },
					{ group: 'alignment',
						buttons: [
							{ type: 'push', label: 'Align Left CTRL + SHIFT + [', value: 'justifyleft' },
							{ type: 'push', label: 'Align Center CTRL + SHIFT + |', value: 'justifycenter' },
							{ type: 'push', label: 'Align Right CTRL + SHIFT + ]', value: 'justifyright' },
							{ type: 'push', label: 'Justify', value: 'justifyfull' }
						]
					},
					{ type: 'separator' },
					{ group: 'parastyle',
						buttons: [
						{ type: 'select', label: 'Normal', value: 'heading', disabled: true,
							menu: [
								{ text: 'Normal', value: 'none', checked: true },
								{ text: 'Header 1', value: 'h1' },
								{ text: 'Header 2', value: 'h2' },
								{ text: 'Header 3', value: 'h3' },
								{ text: 'Header 4', value: 'h4' },
								{ text: 'Header 5', value: 'h5' },
								{ text: 'Header 6', value: 'h6' }
							]
						}
						]
					},
					{ type: 'separator' },
					{ group: 'indent',
						buttons: [
							{ type: 'push', label: 'Indent', value: 'indent', disabled: true },
							{ type: 'push', label: 'Outdent', value: 'outdent', disabled: true }
							]
					},
					{ type: 'separator' },
					{ group: 'list',
						buttons: [
							{ type: 'push', label: 'Create an Unordered List', value: 'insertunorderedlist' },
							{ type: 'push', label: 'Create an Ordered List', value: 'insertorderedlist' },
							{ type: 'push', text: 'CheckSpelling', label: 'Check Spelling', value: 'spellcheck' }
						]
					}
				]
			}
		});
	
	//Change the default toolbar button for fontsize to a new one.
	rtEditor._defaultToolbar.buttons[0].buttons[1] = {
        type: 'select', label: '13', value: 'fontsize2', disabled: true,
            menu: [
					{ text: '8' }, { text: '9' }, { text: '10' }, { text: '11' }, { text: '12' }, { text: '13' }, { text: '14' }, 
					{ text: '15' }, { text: '16' }, { text: '17' }, { text: '18' }, { text: '19' }, { text: '20' }, { text: '21' },
					{ text: '22' }, { text: '23' }, { text: '24' }, { text: '25' }, { text: '26' }, { text: '27' }, { text: '28' },
					{ text: '29' }, { text: '30' }, { text: '31' }, { text: '32' }, { text: '33' }, { text: '34' },	{ text: '35' }, 
					{ text: '36' }, { text: '37' }, { text: '38' }, { text: '39' }, { text: '40' }, { text: '41' }, { text: '42' }, 
					{ text: '43' }, { text: '44' }, { text: '45' }, { text: '46' }, { text: '47' }, { text: '48' }, { text: '49' }, 
					{ text: '50' }, { text: '51' }, { text: '52' }, { text: '53' }, { text: '54' }, { text: '55' }, { text: '56' },
					{ text: '57' }, { text: '58' }, { text: '59' }, { text: '60' }, { text: '61' }, { text: '62' }, { text: '63' }, 
					{ text: '64' }, { text: '65' }, { text: '66' }, { text: '67' }, { text: '68' }, { text: '69' }, { text: '70' },
					{ text: '71' }, { text: '72' }, { text: '73' }, { text: '74' }, { text: '75' }
            ]
    };
    //Override the _handleFontSize method with our own
	rtEditor._handleFontSize = function(o) {
        var button = this.toolbar.getButtonById(o.button.id);
        var value = o.button.value; //The selected value
        var out = sizes[value]; //The pixel size
        button.set('label', value);
        this._updateMenuChecked('fontsize2', value);
        this.execCommand('fontsize', out + 'px');
        this.STOP_EXEC_COMMAND = true;
    };
	
  //Add this button to the _disabled array so it turns on and off with selections
    rtEditor._disabled[rtEditor._disabled.length] = 'fontsize2';
    
    
    //Now update the status of the button to reflect the style of the current element.
    //Not sure what you would do if you ran into an element with a size that is not in our list.
    //Maybe we add a method to round the size to the proper size??
    rtEditor.on('afterNodeChange', function() {
        var elm = this._getSelectedElement(),
            button = this.toolbar.getButtonByValue('fontsize2'),
            label = '13';

        if (!this._isElement(elm, 'body') && !this._isElement(elm, 'img')) {
            this.toolbar.enableButton('fontsize2');
            var fs = parseInt(Dom.getStyle(elm, 'fontSize'),10);
            for (var i in sizes) {
                if (fs == sizes[i]) {
                    label = i;
                    break;
                }
            }
            button.set('label', label);
        } else {
            button.set('label', label);
        }
    }, rtEditor, true);
    
  //Setup our new listener
    rtEditor.on('editorContentLoaded', function() {
        this.toolbar.on('fontsize2Click', function(o) {
            this._handleFontSize(o);
        }, this, true);
        
    }, rtEditor, true);
	
	

	rtEditor.on('editorContentLoaded', function(){
		//"description_editor" is the iframe id generated for the "description"(request description textArea) field
		var iDoc = document.getElementById(divId + "_editor").contentWindow.document;
		//subscribeToEditorSubmitEvent(iDoc);
	}, rtEditor, true);
  rtEditor.status='on';
  return rtEditor;
	
}

/*function toggleControl(rtEditor,btnId) {
    //var Dom = YAHOO.util.Dom,
      //  Event = YAHOO.util.Event;
	 var _button = new YAHOO.widget.Button(btnId);
 //   _button.addClass(btnId);
	 //_button.addClass("sx");
     var state = 'on';
     rtEditor.state=state;
    _button.on('click', function(ev) {
        Event.stopEvent(ev);
        if (state == 'on') {
            //YAHOO.log('state is on, so turn off', 'info', 'example');
            state = 'off';
            rtEditor.state=state;
            rtEditor.saveHTML();
            //YAHOO.log('Save the Editors HTML', 'info', 'example');
            var stripHTML = /<[^><]*>/g;
            rtEditor.get('textarea').value = rtEditor.get('textarea').value.replace(/<br>/gi, '\n').replace(stripHTML, '');
            //YAHOO.log('Strip the HTML markup from the string.', 'info', 'example');
            //YAHOO.log('Set Editor container to position: absolute, top: -9999px, left: -9999px. Set textarea visible', 'info', 'example');

            var fc = rtEditor.get('element').previousSibling,
                el = rtEditor.get('element');

            Dom.setStyle(fc, 'position', 'absolute');
            Dom.setStyle(fc, 'top', '-9999px');
            Dom.setStyle(fc, 'left', '-9999px');
            rtEditor.get('element_cont').removeClass('yui-editor-container');
            Dom.setStyle(el, 'visibility', 'visible');
            Dom.setStyle(el, 'top', '');
            Dom.setStyle(el, 'left', '');
            Dom.setStyle(el, 'position', 'static');
            
         // show spellcheck button
            var textareaId = rtEditor.get('textarea').id ;
            var spellBtnId = textareaId + '_spellCheckButton' ;
            showHideSpellCheckButton(spellBtnId,'visible') ;

	 //set content type
		var contentTypeId = textareaId + '_content_type';
		var contentTypeField = document.getElementById(contentTypeId);
		if(contentTypeField != null)
			contentTypeField.value = "text";
        } else {
            //YAHOO.log('state is off, so turn on', 'info', 'example');
            state = 'on';
            rtEditor.state=state;
            //YAHOO.log('Set Editor container to position: static, top: 0, left: 0. Set textarea to hidden', 'info', 'example');

            var fc = rtEditor.get('element').previousSibling,
                el = rtEditor.get('element');

            Dom.setStyle(fc, 'position', 'static');
            Dom.setStyle(fc, 'top', '0');
            Dom.setStyle(fc, 'left', '0');
            Dom.setStyle(el, 'visibility', 'hidden');
            Dom.setStyle(el, 'top', '-9999px');
            Dom.setStyle(el, 'left', '-9999px');
            Dom.setStyle(el, 'position', 'absolute');
            rtEditor.get('element_cont').addClass('yui-editor-container');
            //YAHOO.log('Reset designMode on the Editor', 'info', 'example');
            rtEditor._setDesignMode('on');
            //YAHOO.log('Inject the HTML from the textarea into the editor', 'info', 'example');
            rtEditor.setEditorHTML(rtEditor.get('textarea').value.replace(/\n/g, '<br>'));
            
            // hide spellcheck button
            var textareaId = rtEditor.get('textarea').id ;
            var spellBtnId = textareaId + '_spellCheckButton' ;
            showHideSpellCheckButton(spellBtnId,'hidden') ;

	    //set content type
		var contentTypeId = textareaId + '_content_type';
		var contentTypeField = document.getElementById(contentTypeId);
		if(contentTypeField != null)
			contentTypeField.value = "html";
        }
    });
}*/

function toggleControl(rtEditorName, btnId) {
	 var _button = new YAHOO.widget.Button(btnId);
     var state = 'on';
    _button.on('click', function(ev) {
        Event.stopEvent(ev);
        var rtEditor = CKEDITOR.instances[rtEditorName];
        if (rtEditor){
        	rtEditor.destroy();
        	rtEditor == null;
        }
        else{
        	if (rtEditorName == 'description'){
        		CKEDITOR.config.height = 300;
        		CKEDITOR.config.toolbarStartupExpanded = true;
        	}
        	else{	
        		CKEDITOR.config.height = 100;
        		CKEDITOR.config.toolbarStartupExpanded = false;
        	}
	   		
	   		CKEDITOR.config.enterMode = CKEDITOR.ENTER_BR;
	        CKEDITOR.replace(rtEditorName);
        }
    });
}

function saveAllRTEs(){
	var i,e,items = CKEDITOR.instances;

	for (i in items) {
		e=items[i];
		if( e ){
			e.updateElement();
		}
	}
}

function getRichTextContent(){
	YAHOO.widget.EditorInfo.getEditorById("description").saveHTML();
}

function getRichTextContent(divId){
	YAHOO.widget.EditorInfo.getEditorById(divId).saveHTML();
}

function focusSubmitButton(){
	document.getElementById("btnSubmit2").focus();
}

function createBAMenuButtonAndSubscribeToEvents (containerName, baMenuTree, baMenuList, sysPrefixLabel, pageType){
	//	Create a Button instance, wrapping the text label in an <EM>
	//	tag that will be given a fixed width of 10em.									
	var oButton = new YAHOO.widget.Button ({type: "menu", 
											id: "baButton",
											label: sysPrefixLabel,
											menu: baMenuTree, 
											container: containerName,
											value: "" }); 
	//	"selectedMenuItemChange" event handler for a Button that will set 
	//	the Button's "label" attribute to the value of the "text" 
	//	configuration property of the MenuItem that was clicked.							
	var onSelectedMenuItemChange = function (event){
		var oMenuItem = event.newValue;
		if (!isStringExists(baMenuList, oMenuItem)){
			this.set("label", ("<em class=\"yui-button-label\">" + 
						oMenuItem.cfg.getProperty("text") + "</em>"));
			this.set("value", oMenuItem.cfg.getProperty("value"));
		}
		else
			return;
	};

	var onMenuClick = function (p_sType, p_aArgs) {
		var oEvent = p_aArgs[0],//	DOM event								
		oMenuItem = p_aArgs[1];	//	MenuItem instance that was the 
								//	target of the event		
		if (oMenuItem && (!isStringExists(baMenuList, oMenuItem.value))){
			if (pageType == "search")
				onClickBusinessArea(oMenuItem.value);
			else if (pageType == "request")
				onPropertyChangeSysId(oMenuItem.value);
		}
		else
			return;
	};

	oButton.on("selectedMenuItemChange", onSelectedMenuItemChange);	
	oButton.getMenu().subscribe("click", onMenuClick);
}

function isStringExists (parentArray, testString){												
	for (var i=0; i<parentArray.length;i++){
	 if (parentArray[i]==testString)
		return true;
	}
		return false;
}

/*
takes the html text and the word to be replaced in this html. And returns the new html text
*/
function getSpellCheckReplacementHtml( html, word )
{
	// this replacement mechanism needs to be improved.
	/* we have to replace the text starting from most restricted text to the least restricted text
	   otherwise the text will get replaced repeatedly. Think why ?	
	   order of replacement.
	   1. words that are tightly bounded by the tags on either side	
		(<\S[^><]*>)nitiraj(<\S[^><]*>) --> $1<span class='yui-spellcheck'>nitiraj</span>$2
	   2. words that are at the start or at the end of the body and have tags on the their free part 
	   3. words that are only bounded with a tag on one side and have whitespace on the other
	   4. words that have whitespace on both sides.
	*/
    	/* case : 1  */
	//alert( "myHTML:\n"+html);
	var regEx1 = "(<[^><]+>)" + word +  "(<[^><]+>)" ;
	//alert( 'my regEx1 = ' + regEx1 ) ;
	var regExObj1 = new RegExp( regEx1, 'g' ) ;
	//alert( 'my regExObj1 : ' + regExObj1 ) ;
	var replacementStr1 = "$1<span class='yui-spellcheck'>" + word + "</span>$2" ;
	//alert( 'my replacement String : ' + replacementStr1 ) ;
	html = html.replace(regExObj1 , replacementStr1 ) ;  
	//alert( "myHTML:\n"+html);
	
	/* case : 2start  */
	var regEx2start = "^" + word +  "((<[^><]+>)|(\\s))" ;
	//alert( 'my regEx2start = ' + regEx2start ) ;
	var regExObj2start = new RegExp( regEx2start, 'g' ) ;
	//alert( 'my regExObj2start : ' + regExObj2start ) ;
	var replacementStr2start = "<span class='yui-spellcheck'>" + word + "</span>$1" ;
	//alert( 'my replacement String : ' + replacementStr2start ) ;
	html = html.replace(regExObj2start , replacementStr2start ) ;  
	//alert( "myHTML:\n"+html);
	
	/* case : 2end  */
	var regEx2end = "((<[^><]+>)|(\\s))" + word +  "$" ;
	//alert( 'my regEx2end = ' + regEx2end ) ;
	var regExObj2end = new RegExp( regEx2end, 'g' ) ;
	//alert( 'my regExObj2end : ' + regExObj2end ) ;
	var replacementStr2end = "$1<span class='yui-spellcheck'>" + word + "</span>" ;
	//alert( 'my replacement String : ' + replacementStr2end ) ;
	html = html.replace(regExObj2end , replacementStr2end ) ;  
	//alert( "myHTML:\n"+html);
	
	/* case : 3start */
	var regEx3start = "(<[^><]+>)" + word +  "(\\s)" ;
	//alert( 'my regEx3start = ' + regEx3start ) ;
	var regExObj3start = new RegExp( regEx3start, 'g' ) ;
	//alert( 'my regExObj3start : ' + regExObj3start ) ;
	var replacementStr3start = "$1<span class='yui-spellcheck'>" + word + "</span>$2" ;
	//alert( 'my replacement String : ' + replacementStr3start ) ;
	html = html.replace(regExObj3start , replacementStr3start ) ;
		//alert( "myHTML:\n"+html);
		
		/* case : 3end  */
	var regEx3end = "(\\s)" + word + "(<[^><]+>)" ;
	//alert( 'my regEx3end = ' + regEx3end ) ;
	var regExObj3end = new RegExp( regEx3end, 'g' ) ;
	//alert( 'my regExObj3end : ' + regExObj3end ) ;
	var replacementStr3end = "$1<span class='yui-spellcheck'>" + word + "</span>$2" ;
	//alert( 'my replacement String : ' + replacementStr3end ) ;
	html = html.replace(regExObj3end , replacementStr3end ) ;  
	//alert( "myHTML:\n"+html);
	
	/* case : 4  */
	var regEx4 = "(\\s)" + word +  "(\\s)" ;
	//alert( 'my regEx4 = ' + regEx4 ) ;
	var regExObj4 = new RegExp( regEx4, 'g' ) ;
	//alert( 'my regExObj4 : ' + regExObj4 ) ;
	var replacementStr4 = "$1<span class='yui-spellcheck'>" + word + "</span>$2" ;
	//alert( 'my replacement String : ' + replacementStr4 ) ;
	html = html.replace(regExObj4 , replacementStr4 ) ;  
	//alert( "myHTML:\n"+html);
	
	/// old
	//html = html.replace(new RegExp(word, 'g'), '<span class="yui-spellcheck">' + word + '</span>');
		
	return html ;
}

function configureSpellCheck(rtEditor)
{
	try
	{
	/* Helper functions for spell check
	 */
	rtEditor._handleWindowClose = function() {
	};

	rtEditor._handleWindow = function() {
		try
		{
//		alert( '_handleWindow called for : ' + this ) ;
	    this.nodeChange();
//	    alert( '_handleWindow after nodechange call : ') ;
	    var el = this.currentElement[0],
	    win = new YAHOO.widget.EditorWindow('spellcheck', {
	        width: '170px'
	    });
	 //   alert( '_handleWindow created the window : ' + win ) ;
	    var textareaID = this.get('textarea').id;
	    var body = document.getElementById('spell_suggest_' + textareaID);
	 //   alert( '_handleWindow body : ' + body ) ;
	    body.innerHTML = '<strong>Suggestions:</strong><br>';
	    var ul = document.createElement('ul');
	    ul.className = 'yui-spellcheck-list';
	    body.appendChild(ul);

	    var list = '';
	    //Change this code to suit your backend checker
	    for (var i = 0; i < this._spellData.length; i++) {
	        if (el.innerHTML == this._spellData[i].word) {
	            for (var s = 0; s < this._spellData[i].suggestions.length; s++) {
	                list = list + '<li title="Replace (' + this._spellData[i].word + ') with (' + this._spellData[i].suggestions[s] + ')">' + this._spellData[i].suggestions[s] + '</li>';
	            }
	        }
	    }

	 //   alert('list : ' + list ) ;
	    ul.innerHTML = list;
	 //   alert( '_handleWindow after the for loop' ) ;
	    YAHOO.util.Event.on(ul, 'click', function(ev) {
	        var tar = YAHOO.util.Event.getTarget(ev);
	        YAHOO.util.Event.stopEvent(ev);
	        if (this._isElement(tar, 'li')) {
	            el.innerHTML = tar.innerHTML;
	            YAHOO.util.Dom.removeClass(el, 'yui-spellcheck');
	            YAHOO.util.Dom.addClass(el, 'yui-none');

	            var next = YAHOO.util.Dom.getElementsByClassName('yui-spellcheck', 'span', this._getDoc().body)[0];
	            if (next) {
	                this.STOP_NODE_CHANGE = true;
	                this.currentElement = [next];
	                this._handleWindow();
	            } else {
	                this.checking = false;
	                this.toolbar.set('disabled', false);
	                this.closeWindow();
	            }
	            this.nodeChange();
	        }
	    }, this, true);

	    this.on('afterOpenWindow', function() {            
	        this.get('panel').syncIframe();
	        var l = parseInt(this.currentWindow._knob.style.left, 10);
	        if (l === 40) {
	           this.currentWindow._knob.style.left = '1px';
	        }
	    }, this, true);

	    win.setHeader('Spelling Suggestions');
	    this.isOpen = true;
	    //win.setBody(body);
//	    alert('before calling this.openWindow');
	    this.openWindow(win);
	  //  alert('after calling this.openWindow') ;
		}
		catch(e)
		{
			alert('caught an exception in _handleWindow : ' + e.description + ' : ' + e.message ) ;
		}
	};
	
	 rtEditor._handleClick = function(ev) {
	        if (this._isNonEditable(ev)) {
	            return false;
	        }
	        this._setCurrentEvent(ev);
	        var tar =YAHOO.util.Event.getTarget(ev);
	        if (this.currentWindow) {
	            if (!YAHOO.util.Dom.hasClass(tar, 'yui-spellcheck')) {
	                this.closeWindow();
	            }
	        }
	        if (!YAHOO.util.Dom.hasClass(tar, 'yui-spellcheck')) {
	            if (YAHOO.widget.EditorInfo.window.win && YAHOO.widget.EditorInfo.window.scope) {
	                YAHOO.widget.EditorInfo.window.scope.closeWindow.call(YAHOO.widget.EditorInfo.window.scope);
	            }
	        }
	        if (this.browser.webkit) {
	            var tar =YAHOO.util.Event.getTarget(ev);
	            if (this._isElement(tar, 'a') || this._isElement(tar.parentNode, 'a')) {
	                YAHOO.util.Event.stopEvent(ev);
	                this.nodeChange();
	            }
	        } else {
	            this.nodeChange();
	        }
	    };
	 	    
	    rtEditor.checking = false;

	    rtEditor.isOpen = false;
	    
	    rtEditor._checkSpelling = function(o) {
		var data = eval('(' + o.responseText + ')');
        var html = this._getDoc().body.innerHTML;
        if(data.length == 0 )
        {
        	alert('No spellcheck suggessions. Please click OK') ;
        	this.checking = false;
            var el = YAHOO.util.Dom.getElementsByClassName('yui-spellcheck', 'span', this._getDoc().body);
            //More work needed here for cleanup..
            YAHOO.util.Dom.removeClass(el, 'yui-spellcheck');
            YAHOO.util.Dom.addClass(el, 'yui-none');
            this.toolbar.set('disabled', false);
            this.nodeChange();
        	return ;
        }
        for (var i = 0; i < data.length; i++) {
		     html = getSpellCheckReplacementHtml( html, data[i].word ) ;
	        }
	        this.setEditorHTML(html);
	        this._spellData = data;
	    };

	    rtEditor.on('windowspellcheckClose', function() {
	       this._handleWindowClose();
	        this.isOpen = false;
	    }, rtEditor, true);
	    
	    rtEditor.on('editorMouseDown', function(args) 
	    {
	        var el = this._getSelectedElement();
	        var el = YAHOO.util.Event.getTarget(args.ev);
	        if (YAHOO.util.Dom.hasClass(el, 'yui-spellcheck')) 
	        {
	            this.currentElement = [el];
	            if (this.isOpen) 
	            {
	                this.closeWindow();
	                this._handleWindow();
	            } 
	            else 
	            {
	                this._handleWindow();
	            }
	            return false;
	        }
	    }, rtEditor, true);
	    
	    rtEditor.on('editorKeyDown', function(ev) {
	        if (this.checking) {
	            //We are spell checking, stop the event
	            YAHOO.util.Event.stopEvent(ev.ev);
	        }
	    }, rtEditor, true);
	    
	    rtEditor.on('afterNodeChange', function() {
	        this.toolbar.enableButton('spellcheck');
	        if (this.checking) {
	            this.toolbar.set('disabled', true);
	            this.toolbar.getButtonByValue('spellcheck').set('disabled', false);
	            this.toolbar.selectButton('spellcheck');
	        }
	    }, rtEditor, true);
	    
	    rtEditor.on('editorContentLoaded', function() {	    
	        this._getDoc().body.spellcheck = false; //Turn off native spell check
	    }, rtEditor, true);
	    
	    rtEditor.on('windowRender', function() {
		try
		{
	        var body = document.createElement('div');
	        body.id = 'spell_suggest_' + this.get('textarea').id;
	        this.get('panel').body.getElementsByTagName('fieldset')[0].appendChild(body);

	        this._windows.spellcheck = {
	            body: body
	        };
		}
		catch(e)
		{
			alert('caught an exception in windowRender : ' + e.description + ' : ' + e.message ) ;
		}
	    }, rtEditor, true);
	    
	    rtEditor.on('toolbarLoaded', function() {

	        this.toolbar.on('spellcheckClick', function() {
		      if (!this.checking) {
	                this.checking = true;
	                this.saveHTML() ;
			var stripHTML = /<[^><]*>/g;
			var myValue = this.get('textarea').value.replace(/<br>/gi, '\n').replace(/&nbsp;/gi,' ').replace(stripHTML, '');	
			var mytmpform = document.createElement("form") ;
			var mytmptextarea = document.createElement("textarea");
			mytmptextarea.name="MY_STRING" ;
			mytmptextarea.value=myValue ;
			mytmpform.appendChild(mytmptextarea);
			var myYUIform = YAHOO.util.Connect.setForm(mytmpform);
			var spellCheckUrl = YAHOO.transbit.tbits.contextPath + "spellcheck/";
			this._conn = YAHOO.util.Connect.asyncRequest('post', spellCheckUrl, 
				{		
					success: this._checkSpelling,
					failure: function() { alert ("Network error. Cannot complete spellcheck") ; },
					scope: this
				}, null); 
	            } else {
	                this.checking = false;
	                var el = YAHOO.util.Dom.getElementsByClassName('yui-spellcheck', 'span', this._getDoc().body);
	                //More work needed here for cleanup..
	                YAHOO.util.Dom.removeClass(el, 'yui-spellcheck');
	                YAHOO.util.Dom.addClass(el, 'yui-none');
	                this.toolbar.set('disabled', false);
	                this.nodeChange();
	            }
	            return false;
	        }, this, true);
	    }, rtEditor, true);
	    
	}
	catch(e)
	{
		alert('exception in configureSpellCheck : ' + e.description ) ;
	}
}

function createRichTextEditorsInRequestPage(){
	try{		
		CKEDITOR.config.toolbar_Full = [
										['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print'],
										['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
										['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
										['NumberedList','BulletedList','-','Outdent','Indent'],
										['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
										['Link','Unlink'],
										['Table','HorizontalRule','SpecialChar'],
										'/',
										['Styles','Format','Font','FontSize'],
										['TextColor','BGColor'],
										['Maximize']
										];
		CKEDITOR.replaceAll( function(textarea, config){					
	       			if (textarea.name == 'summary'){
	       				document.getElementById("summaryLabel").style.display="none";			   		
				   		document.getElementById("summary_tglBtn-button").style.display = "none";
				   		document.getElementById("summary").style.display="none";
		       			return false;
	       			}
	       			else if (textarea.name != 'description'){
	       				config.toolbarStartupExpanded = false;
	       				config.height = 100;
	       				config.enterMode = CKEDITOR.ENTER_BR;
		       		}
	       			else{
		       			config.height = 300;
		       			//Should comment out the enterMode for description if formatting issues occur, as CKEDTIOR guys
		       			//have mentioned that CKEDITOR.ENTER_P which is the default mode, and is the one for which CKEDIOR
		       			//is optimized for.
		       			config.enterMode = CKEDITOR.ENTER_BR;		       			
	       			}
		       		return true;
	   		} );
				
		CKEDITOR.on('instanceReady', function( e ){
			var ed = e.editor;
			if (ed){
				var writer = ed.dataProcessor.writer;
				writer.setRules( 'p',
				    {
				        // Indicates that this tag causes indentation on line breaks inside of it.
				        indent : true,
				        // Insert a line break before the <p> tag.
				        breakBeforeOpen : false,
				        // Insert a line break after the <p> tag.
				        breakAfterOpen : false,
				        // Insert a line break before the </p> closing tag.
				        breakBeforeClose : false,
				        // Insert a line break after the </p> closing tag.
				        breakAfterClose : true
				    });
				writer.setRules( 'ol',
					    {
					        // Indicates that this tag causes indentation on line breaks inside of it.
					        indent : true,
					        // Insert a line break before the <p> tag.
					        breakBeforeOpen : true,
					        // Insert a line break after the <p> tag.
					        breakAfterOpen : false,
					        // Insert a line break before the </p> closing tag.
					        breakBeforeClose : false,
					        // Insert a line break after the </p> closing tag.
					        breakAfterClose : true
					    });			
			
				if( document.getElementById("description").disabled && (ed.name == 'description'))
				{
					ed.readOnly( true );
					document.getElementById(ed.name + '_tglBtn').style.visibility='hidden' ;															
				}
			}			
		}); 
		
	}catch(e){
			alert("Error occurred while creating rich text editors: " + e);
	}							
}


