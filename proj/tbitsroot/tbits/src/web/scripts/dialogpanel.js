YAHOO.namespace("tbits.search");
function showPanel(e, page)
{
	var panel = YAHOO.tbits.search.dialogPanel;
	if(!panel.visible)
	{
		var frame = document.getElementById("importdatapage");
		frame.src = page;
		panel.cfg.queueProperty("visible", true);
		panel.render();
		panel.center();
	}
	else
	{
		panel.close();
	}
}
function initPanel() {
	// Setup constants
	
	// QUIRKS FLAG, FOR BOX MODEL
	var IE_QUIRKS = (YAHOO.env.ua.ie && document.compatMode == "BackCompat");
	
	// UNDERLAY/IFRAME SYNC REQUIRED
	var IE_SYNC = (YAHOO.env.ua.ie == 6 || (YAHOO.env.ua.ie == 7 && IE_QUIRKS));
	
	// PADDING USED FOR BODY ELEMENT (Hardcoded for example)
	var PANEL_BODY_PADDING = (10*2) // 10px top/bottom padding applied to Panel body element. The top/bottom border width is 0
	
	// Create a panel Instance, from the 'resizablepanel' DIV standard module markup
	YAHOO.tbits.search.dialogPanel = new YAHOO.widget.Panel('resizablepanel', {
	    draggable: true,
	    width: '500px',
	    context: ["page", "tl", "bl"],
	    visible: false,
	    modal: true
	});
	//panel.render();
	
	// Create Resize instance, binding it to the 'resizablepanel' DIV 
	var resize = new YAHOO.util.Resize('resizablepanel', {
	    handles: ['br'],
	    autoRatio: false,
	    minWidth: 342,
	    minHeight: 337,
	    status: true
	});
	var panel = YAHOO.tbits.search.dialogPanel;
	// Setup resize handler to update the size of the Panel's body element
	// whenever the size of the 'resizablepanel' DIV changes
	resize.on('resize', function(args) {
	    //alert('resizing');
	    var panelHeight = args.height;
	
	    var headerHeight = this.header.offsetHeight; // Content + Padding + Border
	    var footerHeight = this.footer.offsetHeight; // Content + Padding + Border
	
	    var bodyHeight = (panelHeight - headerHeight - footerHeight);
	    var bodyContentHeight = (IE_QUIRKS) ? bodyHeight : bodyHeight - PANEL_BODY_PADDING;
	
	    YAHOO.util.Dom.setStyle(this.body, 'height', bodyContentHeight + 'px');
	
	    if (IE_SYNC) {
	
	        // Keep the underlay and iframe size in sync.
	
	        // You could also set the width property, to achieve the 
	        // same results, if you wanted to keep the panel's internal
	        // width property in sync with the DOM width. 
	
	        this.sizeUnderlay();
	
	        // Syncing the iframe can be expensive. Disable iframe if you
	        // don't need it.
	
	        this.syncIframe();
	    }
	}, panel, true);
	
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },  							
										  { fn: panel.hide,
											scope:panel,
											correctScope:true }, "keyup" ); 
											// keyup is used here because Safari won't recognize the ESC
											// keydown event, which would normally be used by default
	
	panel.cfg.queueProperty("keylisteners", kl);
}