function getJsonWindowInfo( callback ){
	chrome.windows.getAll({"populate":true}, function onwindowInfo( windows ) {
	var data="[";
	for( var i = 0 ; i < windows.length ; i++ ){
		win = windows[i]
		data += win.id + ":{";
		if( win.tabs && win.tabs.length > 0 ){
			for( var j = 0 ; j < win.tabs.length ; j++ )
			{
				data += win.tabs[j].id;
				if( j != win.tabs.length -1 ) data += ",";
			}
		}
		data += "}";
		if( i != windows.length -1 ) data += ",";
	}
		data += "]"

		callback(data);
	}
	);
}