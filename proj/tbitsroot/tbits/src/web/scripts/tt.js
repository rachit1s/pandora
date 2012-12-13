/** $Id: $ */
// {{{ docs <-- this is a VIM (text editor) text fold

/**
 * Title: DOM Library Core
 * Version: 0.60
 *
 * Summary:
 * A set of commonly used functions that make it easier to create javascript
 * applications that rely on the DOM.
 *
 * Updated: 2004/11/12
 *
 * Maintainer: Dan Allen <dan.allen@mojavelinux.com>
 * Maintainer: Jason Rust <jrust@rustyparts.com>
 *
 * License: LGPL
 */

// }}}
// {{{ global constants

/**
 * Global constants (DO NOT EDIT)
 */

// -- Browsers --
var domLib_userAgent = navigator.userAgent.toLowerCase();
var domLib_isMac = navigator.appVersion.indexOf('Mac') != -1 ? 1 : 0;
var domLib_isOpera = domLib_userAgent.indexOf('opera') != -1 ? 1 : 0;
var domLib_isOpera7 = domLib_userAgent.indexOf('opera/7') != -1 || domLib_userAgent.indexOf('opera 7') != -1 ? 1 : 0;
// Both konqueror and safari use the khtml rendering engine
var domLib_isKonq = (domLib_userAgent.indexOf('konq') != -1 || domLib_userAgent.indexOf('safari') != -1) ? 1 : 0;
var domLib_isIE = !domLib_isKonq && !domLib_isOpera && (domLib_userAgent.indexOf('msie 5') != -1 || domLib_userAgent.indexOf('msie 6') != -1);
var domLib_isIE5up = domLib_isIE || domLib_userAgent.indexOf("msie 7.0" != -1) || domLib_userAgent.indexOf("msie 8.0" != -1);
var domLib_isIE50 = domLib_isIE && domLib_userAgent.indexOf('msie 5.0') != -1;
var domLib_isIE55 = domLib_isIE && domLib_userAgent.indexOf('msie 5.5') != -1;
var domLib_isIE5 = domLib_isIE50 || domLib_isIE55;
// silly safari uses string "khtml, like gecko", so check for destinctive /
var domLib_isGecko = domLib_userAgent.indexOf('gecko/') != -1 ? 1 : 0;
var domLib_isMacIE = (domLib_isIE && domLib_isMac); 
var domLib_isIE55up = domLib_isIE5up && !domLib_isIE50 && !domLib_isMacIE;
var domLib_isIE6up = domLib_isIE55up && !domLib_isIE55;

// -- Abilities --
var domLib_standardsMode = (document.compatMode && document.compatMode == 'CSS1Compat');
var domLib_useLibrary = (domLib_isOpera7 || domLib_isKonq || domLib_isIE55up || domLib_isGecko || domLib_isMacIE);
var domLib_canTimeout = !(domLib_isKonq || domLib_isIE55up || domLib_isMacIE);
var domLib_canFade = (domLib_isGecko || domLib_isIE55up);
var domLib_canDrawOverSelect = (domLib_isOpera || domLib_isMac);

// -- Event Variables --
var domLib_eventTarget = domLib_isIE ? 'srcElement' : 'currentTarget';
var domLib_eventButton = domLib_isIE ? 'button' : 'which';
var domLib_eventTo = domLib_isIE ? 'toElement' : 'relatedTarget';
var domLib_stylePointer = domLib_isIE ? 'hand' : 'pointer';
// NOTE: a bug exists in Opera that prevents maxWidth from being set to 'none', so we make it huge
var domLib_styleNoMaxWidth = domLib_isOpera ? '10000px' : 'none';
var domLib_hidePosition = '-1000px';
var domLib_scrollbarWidth = 14;
var domLib_autoId = 1;
var domLib_zIndex = 100;

// -- Detection --
var domLib_selectElements;

var domLib_timeoutStateId = 0;
var domLib_timeoutStates = new Hash();

var domTT_classPrefix = 'domTTTBits';

// }}}
// {{{ Object.prototype.clone

Object.prototype.clone = function()
{
	var copy = {};
	for (var i in this)
	{
		var value = this[i];
		try
		{
			if (value != null && typeof(value) == 'object' && value != window && !value.nodeType)
			{
				// for IE5 which doesn't inherit prototype
				value.clone = Object.clone;
				copy[i] = value.clone();
			}
			else
			{
				copy[i] = value;
			}
		}
		catch(e)
		{
			copy[i] = value;
		}
	}

	return copy;
}

// }}}
// {{{ class Hash()

function Hash()
{
	this.length = 0;
    this.numericLength = 0; 
	this.elementData = [];
	for (var i = 0; i < arguments.length; i += 2)
	{
		if (typeof(arguments[i + 1]) != 'undefined')
		{
			this.elementData[arguments[i]] = arguments[i + 1];
			this.length++;
            if (arguments[i] == parseInt(arguments[i])) 
            {
                this.numericLength++;
            }
		}
	}
}

// using prototype as opposed to inner functions saves on memory 
Hash.prototype.get = function(in_key)
{
    return this.elementData[in_key];
}

Hash.prototype.set = function(in_key, in_value)
{
    if (typeof(in_value) != 'undefined')
    {
        if (typeof(this.elementData[in_key]) == 'undefined')
        {
            this.length++;
            if (in_key == parseInt(in_key)) 
            {
                this.numericLength++;
            }
        }

        return this.elementData[in_key] = in_value;
    }

    return false;
}

Hash.prototype.remove = function(in_key)
{
    var tmp_value;
    if (typeof(this.elementData[in_key]) != 'undefined')
    {
        this.length--;
        if (in_key == parseInt(in_key)) 
        {
            this.numericLength--;
        }

        tmp_value = this.elementData[in_key];
        delete this.elementData[in_key];
    }

    return tmp_value;
}

Hash.prototype.size = function()
{
    return this.length;
}

Hash.prototype.has = function(in_key)
{
    return typeof(this.elementData[in_key]) != 'undefined';
}

Hash.prototype.merge = function(in_hash)
{
    for (var tmp_key in in_hash.elementData) 
    {
        if (typeof(this.elementData[tmp_key]) == 'undefined') 
        {
            this.length++;
            if (tmp_key == parseInt(tmp_key)) 
            {
                this.numericLength++;
            }
        }

        this.elementData[tmp_key] = in_hash.elementData[tmp_key];
    }
}

Hash.prototype.compare = function(in_hash)
{
    if (this.length != in_hash.length) 
    {
        return false;
    }

    for (var tmp_key in this.elementData) 
    {
        if (this.elementData[tmp_key] != in_hash.elementData[tmp_key]) 
        {
            return false;
        }
    }
    
    return true;
}

// }}}
// {{{ domLib_isDescendantOf()

function domLib_isDescendantOf(in_object, in_ancestor)
{
	if (in_object == in_ancestor)
	{
		return true;
	}

	while (in_object != document.documentElement)
	{
		try
		{
			if ((tmp_object = in_object.offsetParent) && tmp_object == in_ancestor)
			{
				return true;
			}
			else if ((tmp_object = in_object.parentNode) == in_ancestor)
			{
				return true;
			}
			else
			{
				in_object = tmp_object;
			}
		}
		// in case we get some wierd error, just assume we haven't gone out yet
		catch(e)
		{
			return true;
		}
	}

	return false;
}

// }}}
// {{{ domLib_detectCollisions()

// :WARNING: hideList is being used as an object property and is not a string
function domLib_detectCollisions(in_object, in_recover)
{
	if (domLib_canDrawOverSelect)
	{
		return;
	}

	if (typeof(domLib_selectElements) == 'undefined')
	{
		domLib_selectElements = document.getElementsByTagName('select');
	}

	// if we don't have a tip, then unhide selects
	if (in_recover)
	{
		for (var cnt = 0; cnt < domLib_selectElements.length; cnt++)
		{
			var thisSelect = domLib_selectElements[cnt];

			if (!thisSelect.hideList)
			{
				thisSelect.hideList = new Hash();
			}

			// if this is mozilla and it is a regular select or it is multiple and the
			// size is not set, then we don't need to unhide
			if (domLib_isGecko && (!thisSelect.multiple || thisSelect.size < 0))
			{
				continue;
			}

			thisSelect.hideList.remove(in_object.id);
			if (!thisSelect.hideList.length)
			{
				domLib_selectElements[cnt].style.visibility = 'visible';
			}
		}

		return;
	}

	// okay, we have a tip, so hunt and destroy
	var objectOffsets = domLib_getOffsets(in_object);

	for (var cnt = 0; cnt < domLib_selectElements.length; cnt++)
	{
		var thisSelect = domLib_selectElements[cnt];

		// if this is mozilla and not a multiple-select or the multiple select size
		// is not defined, then continue since mozilla does not have an issue
		if (domLib_isGecko && (!thisSelect.multiple || thisSelect.size < 0))
		{
			continue;
		}

		// if the select is in the tip, then skip it
		// :WARNING: is this too costly?
		if (domLib_isDescendantOf(thisSelect, in_object))
		{
			continue;
		}

		if (!thisSelect.hideList)
		{
			thisSelect.hideList = new Hash();
		}

		var selectOffsets = domLib_getOffsets(thisSelect); 
		// for mozilla we only have to worry about the scrollbar itself
		if (domLib_isGecko)
		{
			selectOffsets.set('left', selectOffsets.get('left') + thisSelect.offsetWidth - domLib_scrollbarWidth);
			selectOffsets.set('leftCenter', selectOffsets.get('left') + domLib_scrollbarWidth/2);
			selectOffsets.set('radius', Math.max(thisSelect.offsetHeight, domLib_scrollbarWidth/2));
		}

		var center2centerDistance = Math.sqrt(Math.pow(selectOffsets.get('leftCenter') - objectOffsets.get('leftCenter'), 2) + Math.pow(selectOffsets.get('topCenter') - objectOffsets.get('topCenter'), 2));
		var radiusSum = selectOffsets.get('radius') + objectOffsets.get('radius');
		// the encompassing circles are overlapping, get in for a closer look
		if (center2centerDistance < radiusSum)
		{
			// tip is left of select
			if ((objectOffsets.get('leftCenter') <= selectOffsets.get('leftCenter') && objectOffsets.get('right') < selectOffsets.get('left')) ||
			// tip is right of select
				(objectOffsets.get('leftCenter') > selectOffsets.get('leftCenter') && objectOffsets.get('left') > selectOffsets.get('right')) ||
			// tip is above select
				(objectOffsets.get('topCenter') <= selectOffsets.get('topCenter') && objectOffsets.get('bottom') < selectOffsets.get('top')) ||
			// tip is below select
				(objectOffsets.get('topCenter') > selectOffsets.get('topCenter') && objectOffsets.get('top') > selectOffsets.get('bottom')))
			{
				thisSelect.hideList.remove(in_object.id);
				if (!thisSelect.hideList.length)
				{
					thisSelect.style.visibility = 'visible';
				}
			}
			else
			{
				thisSelect.hideList.set(in_object.id, true);
				thisSelect.style.visibility = 'hidden';
			}
		}
	}
}

// }}}
// {{{ domLib_getOffsets()

function domLib_getOffsets(in_object)
{
	var originalObject = in_object;
	var originalWidth = in_object.offsetWidth;
	var originalHeight = in_object.offsetHeight;
	var offsetLeft = 0;
	var offsetTop = 0;

	while (in_object)
	{
		offsetLeft += in_object.offsetLeft;
		offsetTop += in_object.offsetTop;
		in_object = in_object.offsetParent;
	}

    // MacIE misreports the offsets (even with margin: 0 in body{}), still not perfect
    if (domLib_isMacIE) {
        offsetLeft += 10;
        offsetTop += 10;
    }

	return new Hash(
		'left',			offsetLeft,
		'top',			offsetTop,
		'right',		offsetLeft + originalWidth,
		'bottom',		offsetTop + originalHeight,
		'leftCenter',	offsetLeft + originalWidth/2,
		'topCenter',	offsetTop + originalHeight/2,
		'radius',		Math.max(originalWidth, originalHeight) 
	);
}

// }}}
// {{{ domLib_setTimeout()

function domLib_setTimeout(in_function, in_timeout, in_args)
{
	if (typeof(in_args) == 'undefined')
	{
		in_args = [];
	}

    if (in_timeout < 0)
	{
        return -1;
    }
	else if (in_timeout == 0)
	{
		in_function(in_args);
		return 0;
	}

	// must make a copy of the arguments so that we release the reference
	if (typeof(in_args.clone) != 'function')
	{
		in_args.clone = Object.clone;
	}

	var args = in_args.clone();

	if (domLib_canTimeout)
	{
		return setTimeout(function() { in_function(args); }, in_timeout);
	}
	else
	{
		var id = domLib_timeoutStateId++;
		var data = new Hash();
		data.set('function', in_function);
		data.set('args', args);
		domLib_timeoutStates.set(id, data);

		data.set('timeoutId', setTimeout('domLib_timeoutStates.get(' + id + ').get(\'function\')(domLib_timeoutStates.get(' + id + ').get(\'args\')); domLib_timeoutStates.remove(' + id + ');', in_timeout));
		return id;
	}
}

// }}}
// {{{ domLib_clearTimeout()

function domLib_clearTimeout(in_id)
{
	if (domLib_canTimeout)
	{
		clearTimeout(in_id);
	}
	else
	{
		if (domLib_timeoutStates.has(in_id))
		{
			clearTimeout(domLib_timeoutStates.get(in_id).get('timeoutId'))
			domLib_timeoutStates.remove(in_id);
		}
	}
}

// }}}
// {{{ domLib_getEventPosition()

function domLib_getEventPosition(in_eventObj)
{
	var eventPosition = new Hash('x', 0, 'y', 0, 'scroll_x', 0, 'scroll_y', 0);

	// IE varies depending on standard compliance mode
	if (domLib_isIE)
	{
		var doc = (domLib_standardsMode ? document.documentElement : document.body);
		// NOTE: events may fire before the body has been loaded
		if (doc)
		{
			eventPosition.set('x', in_eventObj.clientX + doc.scrollLeft);
			eventPosition.set('y', in_eventObj.clientY + doc.scrollTop);
			eventPosition.set('scroll_x', doc.scrollLeft);
			eventPosition.set('scroll_y', doc.scrollTop);
		}
	}
	else
	{
		eventPosition.set('x', in_eventObj.pageX);
		eventPosition.set('y', in_eventObj.pageY);
		eventPosition.set('scroll_x', in_eventObj.pageX - in_eventObj.clientX);
		eventPosition.set('scroll_y', in_eventObj.pageY - in_eventObj.clientY);
	}

	return eventPosition;
}

// }}}
// {{{ domLib_cancelBubble()

function domLib_cancelBubble(in_event)
{
    var eventObj = in_event ? in_event : window.event;
    eventObj.cancelBubble = true;
}

// }}}
// {{{ domLib_getIFrameReference()

function domLib_getIFrameReference(in_frame)
{
	if (domLib_isGecko || domLib_isIE)
	{
		return in_frame.frameElement;
	}
	else
	{
		// we could either do it this way or require an id on the frame
		// equivalent to the name
		var name = in_frame.name;
		if (!name || !in_frame.parent)
		{
			return;
		}

		var candidates = in_frame.parent.document.getElementsByTagName('iframe');
		for (var i = 0; i < candidates.length; i++)
		{
			if (candidates[i].name == name)
			{
				return candidates[i];
			}
		}
	}
}

// }}}
// {{{ makeTrue()

function makeTrue()
{
	return true;
}

// }}}
// {{{ makeFalse()

function makeFalse()
{
	return false;
}

// }}}

/*-----------------------------*/

/** $Id: $ */
/**
 * alphaAPI
 * Original Author: chrisken
 * Original Url: http://www.cs.utexas.edu/users/chrisken/alphaapi.html
 *
 * Modified by dallen
 */
function alphaAPI(element, fadeInDelay, fadeOutDelay, startAlpha, stopAlpha, offsetTime, deltaAlpha)
{
	// {{{ properties

	this.element = typeof(element) == 'object' ? element : document.getElementById(element);
	this.fadeInDelay = fadeInDelay || 40;
	this.fadeOutDelay = fadeOutDelay || this.fadeInDelay;
	this.startAlpha = startAlpha;
	this.stopAlpha = stopAlpha;
	// make sure a filter exists so an error is not thrown
	if (typeof(this.element.filters) == 'object')
	{
		if (typeof(this.element.filters.alpha) == 'undefined')
		{
			this.element.style.filter += 'alpha(opacity=100)';
		}
	}

	this.offsetTime = (offsetTime || 0) * 1000;
	this.deltaAlpha = deltaAlpha || 10;
	this.timer = null;
	this.paused = false;
	this.started = false;
	this.cycle = false;
	this.command = function() {};
    return this;

	// }}}
}

// use prototype methods to save memory
// {{{ repeat()

alphaAPI.prototype.repeat = function(repeat)
{
    this.cycle = repeat ? true : false;
}

// }}}
// {{{ setAlphaBy()

alphaAPI.prototype.setAlphaBy = function(deltaAlpha)
{
    this.setAlpha(this.getAlpha() + deltaAlpha);
}

// }}}
// {{{ toggle()

alphaAPI.prototype.toggle = function()
{
    if (!this.started)
    {
        this.start();
    }
    else if (this.paused)
    {
        this.unpause();
    }
    else
    {
        this.pause();
    }
}

// }}}
// {{{ timeout()

alphaAPI.prototype.timeout = function(command, delay)
{
    this.command = command;
    this.timer = setTimeout(command, delay);
}

// }}}
// {{{ setAlpha()

alphaAPI.prototype.setAlpha = function(opacity)
{
    if (typeof(this.element.filters) == 'object')
    {
        this.element.filters.alpha.opacity = opacity;
    }
    else if (this.element.style.setProperty)
    {
        this.element.style.setProperty('-moz-opacity', opacity / 100, '');
    }
}	

// }}}
// {{{ getAlpha()

alphaAPI.prototype.getAlpha = function()
{
    if (typeof(this.element.filters) == 'object')
    {
        return this.element.filters.alpha.opacity;
    }
    else if (this.element.style.getPropertyValue)
    {
        return this.element.style.getPropertyValue('-moz-opacity') * 100;
    }

    return 100;
}

// }}}
// {{{ start()

alphaAPI.prototype.start = function()
{
    this.started = true;
    this.setAlpha(this.startAlpha);
    // determine direction
    if (this.startAlpha > this.stopAlpha)
    {
        var instance = this;
        this.timeout(function() { instance.fadeOut(); }, this.offsetTime);
    }
    else
    {
        var instance = this;
        this.timeout(function() { instance.fadeIn(); }, this.offsetTime);
    }
}

// }}}
// {{{ stop()

alphaAPI.prototype.stop = function()
{
    this.started = false;
    this.setAlpha(this.stopAlpha);
    this.stopTimer();
    this.command = function() {};
}

// }}}
// {{{ reset()

alphaAPI.prototype.reset = function()
{
    this.started = false;
    this.setAlpha(this.startAlpha);
    this.stopTimer();
    this.command = function() {};
}

// }}}
// {{{ pause()

alphaAPI.prototype.pause = function()
{
    this.paused = true;
    this.stopTimer();
}

// }}}
// {{{ unpause()

alphaAPI.prototype.unpause = function()
{
    this.paused = false;
    if (!this.started)
    { 
        this.start();
    }
    else
    {
        this.command(); 
    }
}

// }}}
// {{{ stopTimer()

alphaAPI.prototype.stopTimer = function()
{
    clearTimeout(this.timer);
    this.timer = null;
}

// }}}
// {{{ fadeOut()

alphaAPI.prototype.fadeOut = function()
{
    this.stopTimer();
    if (this.getAlpha() > this.stopAlpha)
    {
        this.setAlphaBy(-1 * this.deltaAlpha);
        var instance = this;
        this.timeout(function() { instance.fadeOut(); }, this.fadeOutDelay);
    }
    else
    {
        if (this.cycle)
        {
            var instance = this;
            this.timeout(function() { instance.fadeIn(); }, this.fadeInDelay);
        }
        else
        {
            this.started = false;
        }
    }
}

// }}}
// {{{ fadeIn()

alphaAPI.prototype.fadeIn = function()
{
    this.stopTimer();
    if (this.getAlpha() < this.startAlpha)
    {
        this.setAlphaBy(this.deltaAlpha);
        var instance = this;
        this.timeout(function() { instance.fadeIn(); }, this.fadeInDelay);
    }
    else
    {
        if (this.cycle)
        {
            var instance = this;
            this.timeout(function() { instance.fadeOut(); }, this.fadeOutDelay);
        }
        else
        {
            this.started = false;
        }
    }
}

// }}}

/*--------------------------------*/

/** $Id: $ */
// {{{ docs <-- this is a VIM (text editor) text fold

/**
 * Title: DOM Tooltip Library
 * Version: 0.70
 *
 * Summary:
 * Allows developers to add custom tooltips to the webpages.  Tooltips are
 * controlled through three style class definitions.  This library also detects
 * collisions against native widgets in the browser that cannot handle the
 * zIndex property.  But this library is even more than that...with all the
 * features it has, it has the potential to replace the need for popups
 * entirely as it can embed just about any html inside the tooltip, leading to
 * the possibility of having whole forms or iframes right inside the tip...even
 * other programs!!!
 *
 * Maintainer: Dan Allen <dan.allen@mojavelinux.com>
 *
 * License: LGPL
 * However, if you use this library, you become an official bug reporter :)
 * Please post to my forum where you use it so that I get a chance to see my
 * baby in action.  If you are doing this for commercial work perhaps you could
 * send me a few Starbucks Coffee gift dollars to encourage future developement
 * (NOT REQUIRED).  E-mail me for my address.
 *
 * Homepage: http://www.mojavelinux.com/forum/viewtopic.php?t=127
 *
 * Freshmeat Project: http://freshmeat.net/projects/domtt/?topic_id=92
 *
 * Updated: 2004/11/12
 *
 * Supported Browsers:
 * Mozilla (Gecko), IE 5.5+, IE on Mac, Safari, Konqueror, Opera 7
 *
 * Usage:
 * All this is required is to put the function call in the event tag for an
 * html element. The status option (for changing the status bar text) is only
 * available through all events, but when used with 'onmouseover' you have to
 * return true so that the browser does not display the link text in the status
 * bar.  To do this, wrap the domTT_activate call in the function makeTrue(),
 * which will just return true, and then prefix it with a 'return'
 *
 * Example: <a href="index.html" onmouseover="return makeTrue(domTT_activate(this, event, 'caption', 'Help', 'content', 'This is a link with a tooltip', 'statusText', 'Link', 'trial', true));">click me</a>
 *
 * Options:
 * Each option is followed by the value for that option.  The variable event
 * must be the first parameter, as shown above.  The options avaiable are:
 *
 *	predefined (optional, must be first item if used, loads default values)
 *	caption (optional)
 *	content (required)
 *	closeLink (optional, defaults to domTT_closeLink global setting variable)
 *	statusText (optional, if used with mouseover must wrap call in 'return domTT_true()')
 *	type (optional, defaults to 'greasy' but can be 'sticky' or 'velcro')
 *	classPrefix (optional, defaults to 'domTT', for changing style class)
 *	delay (optional, defaults to global delay value domTT_activateDelay)
 *	parent (optional, defaults to document.body; switches to window.parent.document.body if inframe set)
 *	closeAction (optional, defaults to global domTT_closeAction, either 'hide' or 'remove')
 *	trail (optional, follow the mouse cursor while tooltip is active)
**/

// }}}
// {{{ Settings (editable)

/**
 * Settings (editable)
 */
var domTT_offsetX = 0;
var domTT_offsetY = 2;
var domTT_direction = 'southeast';
var domTT_mouseHeight = 5;
var domTT_closeLink = 'X';
var domTT_screenEdgePadding = 0;
var domTT_activateDelay = 500;
var domTT_maxWidth = 300;
var domTT_useGlobalMousePosition = true;
var domTT_classPrefix = 'domTT';
var domTT_fade = 'neither';
var domTT_lifetime = 0;
var domTT_grid = 0;
var domTT_closeAction = 'hide';
var domTT_dragStickyTips;
if (typeof(domTT_dragStickyTips) == 'undefined')
{
	var domTT_dragStickyTips = false;
}

// }}}
// {{{ Global constants

/**
 * Global constants (DO NOT EDIT)
 */
var domTT_predefined = new Hash();
var domTT_tooltips = new Hash();

// }}}
// {{{ document.onmousemove

if (domLib_useLibrary && domTT_useGlobalMousePosition)
{
	var domTT_mousePosition = new Hash();
	document.onmousemove = function(in_event)
	{
		if (typeof(in_event) == 'undefined')
		{
			in_event = event;
		}

		domTT_mousePosition = domLib_getEventPosition(in_event);
		if (domTT_dragStickyTips && domTT_dragMouseDown)
		{
			domTT_dragUpdate(in_event);
		}
	}
}

// }}}
// {{{ domTT_activate()

function domTT_activate(in_this, in_event)
{
	if (!domLib_useLibrary) { return false; }

	// make sure in_event is set (for IE, some cases we have to use window.event)
	if (typeof(in_event) == 'undefined')
	{
		in_event = window.event;
	}

	var owner = document.body;
	// we have an active event so get the owner
	if (in_event.type.match(/key|mouse|click|contextmenu/i))
	{
		// make sure we have nothing higher than the body element
		if (in_this.nodeType && in_this.nodeType != 9)
		{
			var owner = in_this;
		}
	}
	// non active event (make sure we were passed a string id)
	else
	{
		if (typeof(in_this) == 'string' && !(owner = document.getElementById(in_this)))
		{
			owner = document.body.appendChild(document.createElement('div'));
			owner.style.display = 'none';
			owner.id = in_this;
		}
	}

	// make sure the owner has a unique id
	if (!owner.id)
	{
		owner.id = '__autoId' + domLib_autoId++;
	}

	var tooltip = domTT_tooltips.get(owner.id);
	if (tooltip)
	{
		if (tooltip.get('eventType') != in_event.type)
		{
			if (tooltip.get('type') == 'greasy')
			{
				tooltip.set('closeAction', 'destroy');
				domTT_deactivate(owner.id);
			}
			else if (tooltip.get('status') != 'inactive')
			{
				return owner.id;
			}
		}
		else
		{
			if (tooltip.get('status') == 'inactive')
			{
				tooltip.set('status', 'pending');
				tooltip.set('activateTimeout', domLib_setTimeout(domTT_runShow, tooltip.get('delay'), [owner.id, in_event]));

				return owner.id;
			}
			// either pending or active, let it be
			else
			{
				return owner.id;
			}
		}
	}

	// setup the default options hash
	var options = new Hash(
		'caption',		'',
		'content',		'',
		'closeLink',	domTT_closeLink,
		'parent',		document.body,
		'position',		'absolute',
		'type',			'greasy',
		'direction',	domTT_direction,
		'delay',		domTT_activateDelay,
		'classPrefix',	domTT_classPrefix,
		'closeAction',	domTT_closeAction,
		'lifetime',		domTT_lifetime,
		'grid',			domTT_grid,
		'fade',			domTT_fade,
		'trail',		false,
		'inframe',		false
	);

	// load in the options from the function call
	for (var i = 2; i < arguments.length; i += 2)
	{
		// load in predefined
		if (arguments[i] == 'predefined')
		{
			var predefinedOptions = domTT_predefined.get(arguments[i + 1]);
			for (var j in predefinedOptions.elementData)
			{
				options.set(j, predefinedOptions.get(j));
			}
		}
		// set option
		else
		{
			options.set(arguments[i], arguments[i + 1]);
		}
	}

	options.set('eventType', in_event.type);

	// immediately set the status text if provided
	if (options.has('statusText'))
	{
		try { window.status = options.get('statusText'); } catch(e) {}
	}

	// if we didn't give content...assume we just wanted to change the status and return
	if (!options.has('content') || options.get('content') == '')
	{
		if (typeof(owner.onmouseout) != 'function')
		{
			owner.onmouseout = function(in_event) { domTT_mouseout(this, in_event); };
		}

		return owner.id;
	}

	options.set('owner', owner);
	options.set('id', '[domTT]' + owner.id);
	try
	{
	domTT_create(options);
	} catch (e)
	{
	alert(e);
	}
	// determine the show delay
	options.set('delay', in_event.type.match(/click|mousedown|contextmenu/i) ? 0 : parseInt(options.get('delay')));
	domTT_tooltips.set(owner.id, options);
	options.set('status', 'pending');
	options.set('activateTimeout', domLib_setTimeout(domTT_runShow, options.get('delay'), [owner.id, in_event]));	
	return owner.id;
}

// }}}
// {{{ domTT_create()

function domTT_create(in_options)
{
	var tipOwner = in_options.get('owner');
	var parentObj = in_options.get('parent');
	var parentDoc = parentObj.ownerDocument;

	// create the tooltip and hide it
	var tipObj = parentObj.appendChild(parentDoc.createElement('div'));
	tipObj.style.position = 'absolute';
	tipObj.style.left = '0px';
	tipObj.style.top = '0px';
	tipObj.style.visibility = 'hidden';
	tipObj.id = in_options.get('id');
	tipObj.className = in_options.get('classPrefix');

	// content of tip as object
	var content;

	if (in_options.get('caption') || (in_options.get('type') == 'sticky' && in_options.get('caption') !== false))
	{

		// layout the tip with a hidden formatting table
		var tipLayoutTable = tipObj.appendChild(parentDoc.createElement('table'));
		tipLayoutTable.style.borderCollapse = 'collapse';
		if (domLib_isKonq)
		{
			tipLayoutTable.cellSpacing = 0;
		}

		var tipLayoutTbody = tipLayoutTable.appendChild(parentDoc.createElement('tbody'));

		var numCaptionCells = 0;
		var captionRow = tipLayoutTbody.appendChild(parentDoc.createElement('tr'));
		var captionCell = captionRow.appendChild(parentDoc.createElement('td'));
		captionCell.style.padding = '0px';
		captionCell.style.width = '98%';
		
		var caption = captionCell.appendChild(parentDoc.createElement('div'));
		caption.className = in_options.get('classPrefix') + 'Caption';
		caption.style.height = '100%';
		caption.style.textAlign = "center";
		caption.innerHTML = in_options.get('caption');
		// caption.appendChild(parentDoc.createTextNode(in_options.get('caption')));

		if (in_options.get('type') == 'sticky' || in_options.get('type') == 'greasy' || in_options.get('type') == 'velcro')
		{
			var numCaptionCells = 2;
			var closeLinkCell = captionRow.appendChild(parentDoc.createElement('td'));
			closeLinkCell.style.padding = '0px';
		    closeLinkCell.style.width = '2%';

			var closeLink = closeLinkCell.appendChild(parentDoc.createElement('div'));
			closeLink.className = in_options.get('classPrefix') + 'Caption';
			closeLink.style.height = '100%';
			closeLink.style.textAlign = 'right';
			closeLink.style.cursor = domLib_stylePointer;
			// merge the styles of the two cells
			closeLink.style.borderLeftWidth = caption.style.borderRightWidth = '0px';
			closeLink.style.paddingLeft = caption.style.paddingRight = '0px';
			closeLink.style.marginLeft = caption.style.marginRight = '0px';
			if (in_options.get('closeLink').nodeType)
			{
				closeLink.appendChild(in_options.get('closeLink').cloneNode(1));
			}
			else
			{
				closeLink.innerHTML = in_options.get('closeLink');
			}

			closeLink.onclick = function() { domTT_deactivate(tipOwner.id); };
			closeLink.onmousedown = function(in_event) { if (typeof(in_event) == 'undefined') { in_event = event; } in_event.cancelBubble = true; };
            // MacIE has to have a newline at the end and must be made with createTextNode()
            if (domLib_isMacIE) {
                closeLinkCell.appendChild(parentDoc.createTextNode("\n"));
            }
		}

        // MacIE has to have a newline at the end and must be made with createTextNode()
        if (domLib_isMacIE) {
            captionCell.appendChild(parentDoc.createTextNode("\n"));
        }

		var contentRow = tipLayoutTbody.appendChild(parentDoc.createElement('tr'));
		var contentCell = contentRow.appendChild(parentDoc.createElement('td'));
		contentCell.style.padding = '0px';
		if (numCaptionCells)
		{
			if (domLib_isIE)
			{
				contentCell.colSpan = numCaptionCells;
			}
			else
			{
				contentCell.setAttribute('colspan', numCaptionCells);
			}
		}

		content = contentCell.appendChild(parentDoc.createElement('div'));
		if (domLib_isIE50)
		{
			content.style.height = '100%';
		}
	}
	else
	{
		content = tipObj.appendChild(parentDoc.createElement('div'));
	}

	content.className = in_options.get('classPrefix') + 'Content';

	if (in_options.get('content').nodeType)
	{
		content.appendChild(in_options.get('content').cloneNode(1));
	}
	else
	{
		content.innerHTML = in_options.get('content');
	}

	// adjust the width if specified
	if (in_options.has('width'))
	{
		tipObj.style.width = parseInt(in_options.get('width')) + 'px';
	}

	// check if we are overridding the maxWidth
	// if the browser supports maxWidth, the global setting will be ignored (assume stylesheet)
	var maxWidth = domTT_maxWidth;
	if (in_options.has('maxWidth'))
	{
		if ((maxWidth = in_options.get('maxWidth')) === false)
		{
			tipObj.style.maxWidth = domLib_styleNoMaxWidth;
		}
		else
		{
			maxWidth = parseInt(in_options.get('maxWidth'));
			tipObj.style.maxWidth = maxWidth + 'px';
		}
	}

	// :HACK: fix lack of maxWidth in CSS for Konq and IE
	if (maxWidth !== false && (domLib_isIE || domLib_isKonq) && tipObj.offsetWidth > maxWidth)
	{
		tipObj.style.width = maxWidth + 'px';
	}

	// store placement offsets from event position
	var offset_x, offset_y;

	// tooltip floats
	if (in_options.get('position') == 'absolute' && !(in_options.has('x') && in_options.has('y')))
	{
		// determine the offset relative to the pointer
		switch (in_options.get('direction'))
		{
			case 'northeast':
				offset_x = domTT_offsetX;
				offset_y = 0 - tipObj.offsetHeight - domTT_offsetY;
			break;
			case 'northwest':
				offset_x = 0 - tipObj.offsetWidth - domTT_offsetX;
				offset_y = 0 - tipObj.offsetHeight - domTT_offsetY;
			break;
			case 'southwest':
				offset_x = 0 - tipObj.offsetWidth - domTT_offsetX;
				offset_y = domTT_mouseHeight + domTT_offsetY;
			break;
			case 'southeast':
				offset_x = domTT_offsetX;
				offset_y = domTT_mouseHeight + domTT_offsetY;
			break;
		}

		// if we are in an iframe, get the offsets of the iframe in the parent document
		if (in_options.get('inframe'))
		{
			var iframeObj = domLib_getIFrameReference(window);
			if (iframeObj)
			{
				var frameOffsets = domLib_getOffsets(iframeObj);
				offset_x += frameOffsets.get('left');
				offset_y += frameOffsets.get('top');
			}
		}
	}
	// tooltip is fixed
	else
	{
		offset_x = 0;
		offset_y = 0;
		in_options.set('trail', false);
	}

	in_options.set('offsetX', offset_x);
	in_options.set('offsetY', offset_y);
	in_options.set('offsetWidth', tipObj.offsetWidth);
	in_options.set('offsetHeight', tipObj.offsetHeight);
	if (domLib_canFade && typeof(alphaAPI) == 'function')
	{
		if (in_options.get('fade') != 'neither')
		{
			var fadeHandler = new alphaAPI(tipObj, 50, 50, 100, 0, null, 10);
			fadeHandler.setAlpha(0);
			in_options.set('fadeHandler', fadeHandler);
		}
	}
	else
	{
		in_options.set('fade', 'neither');
	}

	// setup mouse events
	if (in_options.get('trail') && typeof(tipOwner.onmousemove) != 'function')
	{
		tipOwner.onmousemove = function(in_event) { domTT_mousemove(this, in_event); };
	}

	if (typeof(tipOwner.onmouseout) != 'function')
	{
		tipOwner.onmouseout = function(in_event) { domTT_mouseout(this, in_event); };
	}

	if (in_options.get('type') == 'sticky')
	{
		if (in_options.get('position') == 'absolute' && domTT_dragStickyTips)
		{
			if (domLib_isIE)
			{
				captionRow.onselectstart = function() { return false; };
			}

			// setup drag
			captionRow.onmousedown = function(in_event) { domTT_dragStart(tipObj, in_event);  };
			captionRow.onmousemove = function(in_event) { domTT_dragUpdate(in_event); };
			captionRow.onmouseup = function() { domTT_dragStop(); };
		}
	}
	else if (in_options.get('type') == 'velcro')
	{
		tipObj.onmouseout = function(in_event) { if (typeof(in_event) == 'undefined') { in_event = event; } if (!domLib_isDescendantOf(in_event[domLib_eventTo], tipObj)) { domTT_deactivate(tipOwner.id); }};
		tipObj.onclick = function(in_event) { if (typeof(in_event) == 'undefined') { in_event = event; }  domTT_deactivate(tipOwner.id);};	}

	if (in_options.get('position') == 'relative')
	{
		tipObj.style.position = 'relative';
	}

	in_options.set('node', tipObj);
	in_options.set('status', 'inactive');
}

// }}}
// {{{ domTT_show()

function domTT_show(in_ownerId, in_event)
{
	// should always find one since this call would be cancelled if tip was killed
	var tooltip = domTT_tooltips.get(in_ownerId);
	var status = tooltip.get('status');
	var tipObj = tooltip.get('node');

	if (tooltip.get('position') == 'absolute')
	{
		var mouse_x, mouse_y;

		if (tooltip.has('x') && tooltip.has('y'))
		{
			mouse_x = tooltip.get('x');
			mouse_y = tooltip.get('y');
		}
		else if (!domTT_useGlobalMousePosition || status == 'active' || tooltip.get('delay') == 0)
		{
			var eventPosition = domLib_getEventPosition(in_event);
			mouse_x = eventPosition.get('x');
			mouse_y = eventPosition.get('y');
			if (tooltip.get('inframe'))
			{
				mouse_x -= eventPosition.get('scroll_x');
				mouse_y -= eventPosition.get('scroll_y');
			}
		}
		else
		{
			mouse_x = domTT_mousePosition.get('x');
			mouse_y = domTT_mousePosition.get('y');
			//window.status = 'x: ' + mouse_x + ', y: ' + mouse_y;
			if (tooltip.get('inframe'))
			{
				mouse_x -= domTT_mousePosition.get('scroll_x');
				mouse_y -= domTT_mousePosition.get('scroll_y');
			}
		}

		// we are using a grid for updates
		if (tooltip.get('grid'))
		{
			// if this is not a mousemove event or it is a mousemove event on an active tip and
			// the movement is bigger than the grid
			if (in_event.type != 'mousemove' || (status == 'active' && (Math.abs(tooltip.get('lastX') - mouse_x) > tooltip.get('grid') || Math.abs(tooltip.get('lastY') - mouse_y) > tooltip.get('grid'))))
			{
				tooltip.set('lastX', mouse_x);
				tooltip.set('lastY', mouse_y);
			}
			// did not satisfy the grid movement requirement
			else
			{
				return false;
			}
		}

		var coordinates = {'x' : mouse_x + tooltip.get('offsetX'), 'y' : mouse_y + tooltip.get('offsetY')};
		coordinates = domTT_correctEdgeBleed(tooltip.get('offsetWidth'), tooltip.get('offsetHeight'), coordinates.x, coordinates.y, domTT_offsetX, domTT_offsetY, tooltip.get('type'), tooltip.get('inframe') ? window.parent : window);

		// update the position
		tipObj.style.left = coordinates.x + 'px';
		tipObj.style.top = coordinates.y + 'px';

		// increase the tip zIndex so it goes over previously shown tips
		tipObj.style.zIndex = domLib_zIndex++;				 if(document.getElementById('draftsIFrame'))
		{
			document.getElementById('draftsIFrame').style.left = tipObj.style.left;
    		document.getElementById('draftsIFrame').style.top = tipObj.style.top;	
    		document.getElementById('draftsIFrame').style.height = tipObj.clientHeight;
    		document.getElementById('draftsIFrame').style.width = tipObj.clientWidth;
        	document.getElementById('draftsIFrame').style.zIndex = 20;        }	
		
  	}

	// if tip is not active, active it now and check for a fade in
	if (status == 'pending')
	{
		// unhide the tooltip
		tooltip.set('status', 'active');
										tipObj.style.display = '';
		tipObj.style.visibility = 'visible';
	
		 if(document.getElementById('draftsIFrame'))
		{        	document.getElementById('draftsIFrame').style.display= 'block';
			document.getElementById('draftsIFrame').style.visibility = 'visible';		}	
		var fade = tooltip.get('fade');
		if (fade != 'neither')
		{
			var fadeHandler = tooltip.get('fadeHandler');
			if (fade == 'out' || fade == 'both')
			{
				fadeHandler.pause();
				if (fade == 'out')
				{
					fadeHandler.reset();
				}
			}

			if (fade == 'in' || fade == 'both')
			{
				fadeHandler.fadeIn();
			}
		}

		if (tooltip.get('type') == 'greasy' && tooltip.get('lifetime') != 0)
		{
			tooltip.set('lifetimeTimeout', domLib_setTimeout(domTT_runDeactivate, tooltip.get('lifetime'), [in_ownerId]));
		}
	}

	/*if (tooltip.get('position') == 'absolute')
	{		domLib_detectCollisions(tipObj);
	}*/					
 }

// }}}
// {{{ domTT_deactivate()

function domTT_deactivate(in_ownerId)
{
	var tooltip = domTT_tooltips.get(in_ownerId);
	if (tooltip)
	{
		var status = tooltip.get('status');
		if (status == 'pending')
		{
			// cancel the creation of this tip if it is still pending
			domLib_clearTimeout(tooltip.get('activateTimeout'));
			tooltip.set('status', 'inactive');
		}
		else if (status == 'active')
		{
			if (tooltip.get('lifetime'))
			{
				domLib_clearTimeout(tooltip.get('lifetimeTimeout'));
			}

			var tipObj = tooltip.get('node');
			if (tooltip.get('closeAction') == 'hide')
			{
				var fade = tooltip.get('fade');
				if (fade != 'neither')
				{
					var fadeHandler = tooltip.get('fadeHandler');
					if (fade == 'out' || fade == 'both')
					{
						fadeHandler.pause();
						fadeHandler.fadeOut();
					}
					else
					{
						fadeHandler.stop();
					}
				}
				else
				{
					tipObj.style.display = 'none';
				}
			}
			else
			{
				tooltip.get('parent').removeChild(tipObj);
				domTT_tooltips.remove(in_ownerId);
			}

			tooltip.set('status', 'inactive');
			// unhide all of the selects that are owned by this object
			domLib_detectCollisions(tipObj, true); 
		}
		if(document.getElementById('draftsIFrame'))
		{
			document.getElementById('draftsIFrame').style.display= 'none';
    		document.getElementById('draftsIFrame').style.visibility ="hidden";    	}	
	}
}

// }}}
// {{{ domTT_mouseout()

function domTT_mouseout(in_owner, in_event)
{
	if (!domLib_useLibrary) { return false; }

	if (typeof(in_event) == 'undefined')
	{
		in_event = event;
	}

	var toChild = domLib_isDescendantOf(in_event[domLib_eventTo], in_owner);
	var tooltip = domTT_tooltips.get(in_owner.id);
	if (tooltip && (tooltip.get('type') == 'greasy' || tooltip.get('status') != 'active'))
	{
		// deactivate tip if exists and we moved away from the owner
		if (!toChild)
		{
			domTT_deactivate(in_owner.id);
		}
	}
	else if (!toChild)
	{
		try { window.status = window.defaultStatus; } catch(e) {}
	}
}

// }}}
// {{{ domTT_mousemove()

function domTT_mousemove(in_owner, in_event)
{
	if (!domLib_useLibrary) { return false; }

	if (typeof(in_event) == 'undefined')
	{
		in_event = event;
	}

	var tooltip = domTT_tooltips.get(in_owner.id);
	if (tooltip && tooltip.get('trail') && tooltip.get('status') == 'active')
	{
		domTT_show(in_owner.id, in_event);
	}
}

// }}}
// {{{ domTT_addPredefined()

function domTT_addPredefined(in_id)
{
	var options = new Hash();
	for (var i = 1; i < arguments.length; i += 2)
	{
		options.set(arguments[i], arguments[i + 1]);
	}

	domTT_predefined.set(in_id, options);
}

// }}}
// {{{ domTT_correctEdgeBleed()

function domTT_correctEdgeBleed(in_width, in_height, in_x, in_y, in_offsetX, in_offsetY, in_type, in_window)
{
	var win, doc;
	var bleedRight, bleedBottom;
	var pageHeight, pageWidth, pageYOffset, pageXOffset;

	win = (typeof(in_window) == 'undefined' ? window : in_window);

	// Gecko and IE swaps values of clientHeight, clientWidth properties when
	// in standards compliance mode from documentElement to document.body
	if (domLib_standardsMode && (domLib_isIE || domLib_isGecko))
	{
		doc = win.document.documentElement;
	}
	else
	{
		doc = win.document.body;
	}

	// for IE in compliance mode, maybe others
	if (domLib_isIE)
	{
		pageHeight = doc.clientHeight;
		pageWidth = doc.clientWidth;
		pageYOffset = doc.scrollTop;
		pageXOffset = doc.scrollLeft;
	}
	else
	{
		pageHeight = doc.clientHeight;
		pageWidth = doc.clientWidth;

		if (domLib_isKonq)
		{
			pageHeight = win.innerHeight;
		}

		pageYOffset = win.pageYOffset;
		pageXOffset = win.pageXOffset;
	}

	// we are bleeding off the right, move tip over to stay on page
	if ((bleedRight = (in_x - pageXOffset) + in_width - (pageWidth - domTT_screenEdgePadding)) > 0)
	{
		in_x -= bleedRight;
	}

	// we are bleeding to the left, move tip over to stay on page
	// we don't want an 'else if' here, because if the tip just
	// doesn't fit, we will go back to bleeding off the right
	if ((in_x - pageXOffset) < domTT_screenEdgePadding)
	{
		in_x = domTT_screenEdgePadding + pageXOffset;
	}

	// ** top/bottom corrections depends on type, because we can't end up with the mouse over
	// the tip if this is a greasy **
	// if we are bleeding off the bottom, flip to north
	if ((bleedBottom = (in_y - pageYOffset) + in_height - (pageHeight - domTT_screenEdgePadding)) > 0) {
		if (in_type == 'sticky')
		{
			in_y -= bleedBottom;
		}
		else
		{
			in_y -= in_height + (2 * in_offsetY) + domTT_mouseHeight;
		}
	}

	// if we are bleeding off the top, flip to south
	// we don't want an 'else if' here, because if the tip just
	// doesn't fit, we will go back to bleeding off the bottom
	if ((in_y - pageYOffset) < domTT_screenEdgePadding)
	{
		if (in_type == 'sticky')
		{
			in_y = domTT_screenEdgePadding + pageYOffset;
		}
		else
		{
			in_y += in_height + (2 * in_offsetY) + domTT_mouseHeight;
		}
	}

	return {'x' : in_x, 'y' : in_y};
}

// }}}
// {{{ domTT_isActive()

function domTT_isActive(in_ownerId)
{
	var tooltip = domTT_tooltips.get(in_ownerId);
	if (!tooltip || tooltip.get('status') != 'active')
	{
		return false;
	}
	else
	{
		return true;
	}
}

// }}}
// {{{ domTT_runXXX()

// All of these domMenu_runXXX() methods are used by the event handling sections to
// avoid the circular memory leaks caused by inner functions
function domTT_runDeactivate(args) { domTT_deactivate(args[0]); }
function domTT_runShow(args) { domTT_show(args[0], args[1]); }

// }}}

/*------------------------------*/

/** $Id: $ */
var domTT_dragStickyTips = true;
var domTT_currentDragTarget;
var domTT_dragMouseDown;
var domTT_dragOffsetLeft;
var domTT_dragOffsetTop;
// {{{ domTT_dragStart()

function domTT_dragStart(in_this, in_event)
{
	if (typeof(in_event) == 'undefined')
	{
		in_event = event;
	}

	var eventButton = in_event[domLib_eventButton];
	if (eventButton != 1 && !domLib_isKonq)
	{
		return;
	}

	domTT_currentDragTarget = in_this;
	in_this.style.cursor = 'move';

	// upgrade our z-index
	in_this.style.zIndex = ++domLib_zIndex;

	var eventPosition = domLib_getEventPosition(in_event);

	var targetPosition = domLib_getOffsets(in_this);
	domTT_dragOffsetLeft = eventPosition.get('x') - targetPosition.get('left');
	domTT_dragOffsetTop = eventPosition.get('y') - targetPosition.get('top');
	domTT_dragMouseDown = true;
}

// }}}
// {{{ domTT_dragUpdate()

function domTT_dragUpdate(in_event)
{
	if (domTT_dragMouseDown)
	{
		if (domLib_isGecko)
		{
			window.getSelection().removeAllRanges()
		}

		if (domTT_useGlobalMousePosition)
		{
			var eventPosition = domTT_mousePosition;
		}
		else
		{
			if (typeof(in_event) == 'undefined')
			{
				in_event = event;
			}

			var eventPosition = domLib_getEventPosition(in_event);
		}

		domTT_currentDragTarget.style.left = (eventPosition.get('x') - domTT_dragOffsetLeft) + 'px';
		domTT_currentDragTarget.style.top = (eventPosition.get('y') - domTT_dragOffsetTop) + 'px';

		// update the collision detection
		domLib_detectCollisions(domTT_currentDragTarget);
	}
}

// }}}
// {{{ domTT_dragStop()

function domTT_dragStop()
{
	if (domTT_dragMouseDown) {
		domTT_dragMouseDown = false; 
		domTT_currentDragTarget.style.cursor = 'default';
		domTT_currentDragTarget = null;
		if (domLib_isGecko)
		{
			window.getSelection().removeAllRanges()
		}
	}
}

// }}}
