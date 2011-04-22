//java/com/google/blogger/b2/staticresources/html/app/scripts/detect.js

var DOM = (document.getElementById);
if (DOM) var Detect = new BrowserDetector();


/**
 * Author: Chris Wetherell
 * BrowserDetector (object)
 *
 * A class for detecting version 5 browsers by the Javascript objects
 * they support and not their user agent strings (which can be
 * spoofed).
 *
 * Warning: Though slow to develop, browsers may begin to add
 * DOM support in later versions which might require changes to this
 * file.
 *
 * Warning: No one lives forever.  Presumably.
 *
 * Typical usage:
 * Detect = new BrowserDetector();
 * if (Detect.IE()) //IE-only code...
 */
function BrowserDetector()
{

  //IE 4+
  this.IE = function()
  {
    try {
      return this.Run(document.all && !document.contains) != false;
    } catch (e) {
      /* IE 5.01 doesn't support the 'contains' object and
         fails the first test */
      if (document.all) return true;
      return false;
    }
  }

  //IE 5.5+
  this.IE_5_5_newer = function()
  {
    try {
      return (this.Run(this.IE() && Array.prototype.pop && !this.OPERA()) !=
              false);
    } catch (e) {return false;}
  }

  //IE 5, Macintosh
  this.IE_5_Mac = function()
  {
      try {
        return (true == undefined);
      } catch (e) {
        return (document.all &&
                document.getElementById &&
                !document.mimeType &&
                !this.OPERA()) != false;
      }
  }

  //Opera 7+
  this.OPERA = function()
  {
    try {
      return this.Run(window.opera) != false;
    } catch (e) {return false;}
  }

  //Gecko, actually Mozilla 1.2+
  this.MOZILLA = function()
  {
    try {
      return this.Run(
          document.implementation
          && document.implementation.createDocument
          && !document.contains
          && !this.OPERA()
          ) != false;
    } catch (e) {return false;}
  }

  //Safari
  this.SAFARI = function()
  {
    try {
      return this.Run(
          document.implementation
          && document.implementation.createDocument
          && document.contains
          ) != false;
      } catch (e) {return false;}
  }

  //Any browser which supports the W3C DOM
  this.DOM = function()
  {
    return (document.getElementById);
  }

  this.Run = function(test)
  {
    if (test == undefined) {
      return false;
    } else {
      return test;
    }
  }

  // This uses useragent for finer detection. If people spoof it, it's their
  // own fault when things break
  this.geckoVersion = function() {
    var matches = navigator.userAgent.match(/Gecko\/(\d*)/);
    if (matches && matches.length > 1) {
      return matches[1];
    }

    return null;
  }
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/dom.common.js

// Commonly-used functions, reduced.

function d(s) {return document.getElementById(s);}
function dE(o,s) {return o.getElementsByTagName(s);}

/**
 * toggleDisplay()
 *
 * Will toggle the display property of the style object for any
 * DOM element or object that supports style as a property.
 *
 * Warning: This'll wreak havoc if applied to <TR> elements. Those
 * babies got different types "table-row" | "block" dependant on
 * what browser's being used.
 *
 * Warning: Written in Texas.  Yeehaw.
 *
 * Typical usage:
 * toggleDisplay(document.getElementById("foo"));
 */
function toggleDisplay(o) {
  var display = getStyle(o, 'display');

  if (o.style) {
    o.style.display =
        (display != 'none') ? 'none' : getDisplayStyleByTagName(o);
  }
}


function getDisplayStyleByTagName(o) {
  var n = o.nodeName.toLowerCase();
  return (n == 'span' ||
          n == 'img' ||
          n == 'a') ? 'inline' : 'block';
}


/**
 * hideElement()
 *
 * Hides an element from view.
 *
 * Typical usage:
 * hideElement(getElement("the-id-of-the-element"));
 */
function hideElement(o) {
  if (o && o.style) o.style.display = 'none';
}


/**
 * showElement()
 *
 * Shows an element that was hidden from view.
 *
 * Typical usage:
 * showElement(getElement("the-id-of-the-element"));
 */
function showElement(o) {
  if (o && o.style) o.style.display = getDisplayStyleByTagName(o);
}


/**
 * getElement()
 *
 * Returns an element by its ID or shows an alert if it can't be found.
 *
 * Typical usage:
 * getElement("the-id-of-the-element");
 */
function getElement(id) {
  var e = d(id);
  if (!e) {
    alert('Cannot get element: ' + id);
  }
  return e;
}

/**
 * setInnerHTML()
 *
 * Sets the innerHTML of an element or shows an alert if can't be set.
 *
 * Typical usage:
 * setInnerHTML("the-id-of-the-element");
 */
function setInnerHTML(id, html) {
  try {
    getElement(id).innerHTML = html;
  } catch (ex) {
    alert('Cannot set inner HTML: ' + id);
  }
}


/**
 * setCssStyle()
 *
 * Sets the style of an element by its id or shows an alert if can't be set.
 *
 * Typical usage:
 * setCssStyle("the-id-of-the-element", "display", "block");
 */
function setCssStyle(id, name, value) {
  try {
    getElement(id).style[name] = value;
  } catch (ex) {
    alert('Cannot set style: ' + id);
  }
}


/**
 * getStyle()
 *
 * Gets the computed style of any object.
 *
 * WARNING: Produces unexpected results in Safari.  To achieve best
 * results, explicitly set the style property for that browser when the
 * element is rendered.
 *
 * Typical usage:
 * getStyle(object, "display");
 */
function getStyle(el, style) {
  if (!document.getElementById || !el) return;

  if (document.defaultView
      && document.defaultView.getComputedStyle) {
      return document.defaultView.
        getComputedStyle(el, '').getPropertyValue(style);
  } else if (el.currentStyle) {
    return el.currentStyle[style];
  } else {
    return el.style.display;
  }
}


/**
 * getStyleAttribute()
 *
 * Returns the style attribute of the specified node.
 */
function getStyleAttribute(node) {
  if (Detect.IE()) {
    return node.getAttribute('style').value;
  } else {
    return node.getAttribute('style');
  }
}


/*
 * showProps()
 *
 * Displays all the properties for a given element
 */
function showProps(o) {
  var s = '';
  for (var p in o) {
    s += p + ': '+ o[p] + '\n<br />';
  }
  document.write(s);
}


function setIFrameEvent(iframe, eventName, func) {
  if (document.all) {
    eval('getIFrameDocument(iframe).on' + eventName + ' = func;');
  } else {
    iframe.contentWindow.addEventListener(eventName, func, true);
  }
}


function setIFrameBody(iframe, strStyle, innerHtml) {
  if (!innerHtml) innerHtml = '';
  if (innerHtml == '' && Detect.IE()) {
    innerHtml = '<div></div>';
  }
  var doc = getIFrameDocument(iframe);
  doc.open();
  doc.write('<head></head><body style="' + strStyle + '">' +
      innerHtml + '</body>');
  doc.close();
}


function getIFrameDocument(iframe) {
  if (Detect.IE()) {
    return iframe.document;
  } else {
    return iframe.contentDocument;
  }
}


function getIFrame(strId) {
  if (Detect.IE()) {
    return document.frames[strId];
  } else {
    return document.getElementById(strId);
  }
}


function createElementandAppend(nodeName, strId, appendTo) {
  var el = document.createElement(nodeName);
  el.setAttribute('id', strId);
  if (appendTo) {
    appendTo.appendChild(el);
  } else {
    document.body.appendChild(el);
  }
  return el;
}


function createElementandInsertBefore(nodeName, strId, appendTo, sibling) {
  var el = document.createElement(nodeName);
  el.setAttribute('id', strId);
  if (appendTo) {
    appendTo.insertBefore(el, sibling);
  } else {
    document.body.insertBefore(el, sibling);
  }
  return el;
}


/**
* getXY()
 *
 * Returns the position of any element as an object.
 *
 * Typical usage:
 * var pos = getXY(object);
 * alert(pos.x + " " +pos.y);
 */
function getXY(el) {
  var x = el.offsetLeft;
  var y = el.offsetTop;
  if (el.offsetParent != null) {
    var pos = getXY(el.offsetParent);
    x += pos.x;
    y += pos.y;
  }
  return {x: x, y: y};
}


// The following 3 functions are taken from common.js
function hasClass(el, cl) {
  if (el == null || el.className == null) return false;
  var classes = el.className.split(' ');
  for (var i = 0; i < classes.length; i++) {
    if (classes[i] == cl) {
      return true;
    }
  }
  return false;
}


// Add a class to element
function addClass(el, cl) {
  if (hasClass(el, cl)) return;
  el.className += ' ' + cl;
}


// Remove a class from an element
function removeClass(el, cl) {
  if (el.className == null) return;
  var classes = el.className.split(' ');
  var result = [];
  var changed = false;
  for (var i = 0; i < classes.length; i++) {
    if (classes[i] != cl) {
      if (classes[i]) { result.push(classes[i]); }
    } else {
      changed = true;
    }
  }
  if (changed) { el.className = result.join(' '); }
}


function toggleClass(el, cl) {
  if (hasClass(el, cl)) {
    removeClass(el, cl);
  } else {
    addClass(el, cl);
  }
}


/* Constants for node types, since IE doesn't support Node.TEXT_NODE */
var TEXT_NODE = 3;
var ELEMENT_NODE = 1;

//java/com/google/blogger/b2/staticresources/html/app/scripts/dom.selection.js

function surroundFrameSelection(frame, tagName) {
  var win = frame.contentWindow;
  surroundSelection(win, tagName);
}


function surroundSelection(win, tagName) {
  if (Detect.IE()) {
    surroundSelection_IE(win.document, tagName);
  } else {
    var doc = (win.contentDocument) ? win.contentDocument : document;
    var el = doc.createElement(tagName);
    surroundSelection_DOM(win, el);
  }
}


function insertNodeAtSelection(win, tag, fragment) {
  if (Detect.IE()) {
    var doc = win.document;
    var range = doc.selection.createRange();
    insertNodeAtSelection_IE(doc, tag, fragment.innerHTML);
  } else {
    var doc = (win.contentDocument) ? win.contentDocument : document;
    var el = doc.createElement(tag);
    insertNodeAtSelection_DOM(win, el, fragment);
  }
}


function insertNodeAtSelection_IE(doc, tag, html) {
  try {
    var range = doc.selection.createRange();
    var startTag = '<' + tag + '>';
    var endTag = '</' + tag + '>';
    var replaceString = startTag + html + endTag;

    var isCollapsed = range.text == '';
    range.pasteHTML(replaceString);

    if (!isCollapsed) {
      // move selection to html contained within the surrounding node
      range.moveToElementText(range.parentElement().childNodes[0]);
      range.select();
    }
  } catch (e) {
    RichEdit.addDebugMsg('insertNodeAtSelection_IE() failed for "' + tag + '"');
  }
}


function surroundSelection_IE(doc, tag) {
  try {
    var range = doc.selection.createRange();
    var html = range.htmlText;

    // get rid of beginning newline
    if (html.substring(0, 2) == '\r\n') html = html.substring(2, html.length);

    // resolve IE's special DIV cases
    html = replaceEmptyDIVsWithBRs(html);

    insertNodeAtSelection_IE(doc, tag, html);
  } catch (e) {
    RichEdit.addDebugMsg('surroundSelection_IE() failed for "' + tag + '"');
  }
}


function surroundSelection_DOM(win, tag) {
  try {
    var sel = win.getSelection();
    var range = sel.getRangeAt(0);
    insertNodeAtSelection_DOM(win, tag, range.cloneContents());
  } catch (e) {
    RichEdit.addDebugMsg('surroundSelection_DOM() failed for "' + tag + '"');
  }
}


/*
 * This function was taken from The Mozilla Organization's Midas demo. It has
 * been modified.  In the future we may instead be able to use the
 * surroundContents() method of the range object, but a bug exists as of
 * 7/6/2004 that prohibits our use of it in Mozilla.
 * (http://bugzilla.mozilla.org/show_bug.cgi?id=135928)
 */
function insertNodeAtSelection_DOM(win, insertNode, html)
{
  // get current selection
  var sel = win.getSelection();

  // get the first range of the selection
  // (there's almost always only one range)
  var range = sel.getRangeAt(0);

  // insert specified HTML into the node passed by argument
  insertNode.appendChild(html);

  // deselect everything
  sel.removeAllRanges();

  // remove content of current selection from document
  range.deleteContents();

  // get location of current selection
  var container = range.startContainer;
  var pos = range.startOffset;

  // make a new range for the new selection
  range = document.createRange();

  var afterNode;

  if (container.nodeType == 3 && insertNode.nodeType == 3) {
    // if we insert text in a textnode, do optimized insertion
    container.insertData(pos, insertNode.nodeValue);

  } else {
    if (container.nodeType == 3) {
      // when inserting into a textnode
      // we create 2 new textnodes
      // and put the insertNode in between

      var textNode = container;
      container = textNode.parentNode;
      var text = textNode.nodeValue;

      // text before the split
      var textBefore = text.substr(0, pos);
      // text after the split
      var textAfter = text.substr(pos);

      var beforeNode = document.createTextNode(textBefore);
      var afterNode = document.createTextNode(textAfter);

      // insert the 3 new nodes before the old one
      container.insertBefore(afterNode, textNode);
      container.insertBefore(insertNode, afterNode);
      container.insertBefore(beforeNode, insertNode);

      // remove the old node
      container.removeChild(textNode);
    } else {
      // else simply insert the node
      afterNode = container.childNodes[pos];
      container.insertBefore(insertNode, afterNode);
    }
  }

  // select the modified html
  range.setEnd(insertNode, insertNode.childNodes.length);
  range.setStart(insertNode, insertNode);
  sel.addRange(range);
}


/*
 * getRangeAsDocumentFragment()
 *
 * Returns an HTML Document fragment representing the contents of the
 * supplied selection range.
 */
function getRangeAsDocumentFragment(range) {
  try {
    if (Detect.IE()) {
      var el = document.createElement('span');
      el.innerHTML = range.htmlText;
      return el;
    } else {
      return range.cloneContents();
    }
  } catch (e) {
    RichEdit.addDebugMsg('--getRangeAsDocumentFragment() failed');
    return null;
  }
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/event.keypress.js

PUNCTUATIONS_STR = "`~!@#$%^&*()_+-=[]\\{}|;':\",./<>? \t\r\n";
BACKSPACE = null;
DELETE = null;
DIGIT = null;
ESCAPE = null;
PUNCTUATION = null;
RETURN = null;
SPACE = null;
TAB = null;

CTRL_SHFT_A = null;
CTRL_SHFT_B = null;
CTRL_SHFT_T = null;
CTRL_SHFT_L = null;
CTRL_SHFT_P = null;
CTRL_SHFT_S = null;
CTRL_SHFT_D = null;
CTRL_SHFT_U = null;
CTRL_SHFT_Z = null;
CTRL_B = null;
CTRL_D = null;
CTRL_G = null;
CTRL_I = null;
CTRL_L = null;
CTRL_P = null;
CTRL_S = null;
CTRL_Y = null;
CTRL_Z = null;

IE_KEYSET = (Detect.IE() || Detect.SAFARI());

LEFT_ARROW = null;
DOWN_ARROW = null;
RIGHT_ARROW = null;
UP_ARROW = null;

/* Some global variables used for optimization */

// This is set to getKey(e) in the setKeysetByEvent() that can be shared
// by all functions
currentKeyFromEvent = null;

// Result of isCtrlKeyPressed(e);
currentCtrlKeyPressedFromEvent = null;

// Result of isShiftKeyPressed(e);
currentShiftKeyPressedFromEvent = null;

function setKeysetByEvent(e) {
  // IE delivers a different keyset per key event type.  Additionally,
  // 'keydown' is different than 'keypress' but only in terms of the
  // ctrl + shift combination.  Ugh.  Safari is more consistent.  Gecko is
  // right on.

  // set up the globally shared variables
  currentCtrlKeyPressedFromEvent = isCtrlKeyPressed(e);
  currentShiftKeyPressedFromEvent = isShiftKeyPressed(e);
  currentKeyFromEvent = getKey(e);

  IE_CTRL_SHIFT_KEYSET = IE_KEYSET;
  if (Detect.IE() && e && e.type == 'keydown') IE_CTRL_SHIFT_KEYSET = false;

  BACKSPACE = (currentKeyFromEvent == BACKSPACE_KEYCODE);
  DELETE = (Detect.IE()) ?
           (currentKeyFromEvent == DELETE_KEYCODE)
      : (getEvent(e).keyCode == 46 && getEvent(e).charCode == 0);
  ESCAPE = (currentKeyFromEvent == ESC_KEYCODE);
  RETURN = (currentKeyFromEvent == ENTER_KEYCODE);
  SPACE = (currentKeyFromEvent == SPACE_KEYCODE);
  TAB = (currentKeyFromEvent == TAB_KEYCODE);

  LEFT_ARROW = (currentKeyFromEvent == LEFT_KEYCODE);
  RIGHT_ARROW = (currentKeyFromEvent == RIGHT_KEYCODE);
  UP_ARROW = (currentKeyFromEvent == UP_KEYCODE);
  DOWN_ARROW = (currentKeyFromEvent == DOWN_KEYCODE);

  DIGIT = (!currentShiftKeyPressedFromEvent &&
          ((currentKeyFromEvent >= 48 && currentKeyFromEvent <= 57) ||
           (currentKeyFromEvent >= 96 && currentKeyFromEvent <= 105)));// keypad

  // Alphabets
  ALPHA = false;
  if (e.type == 'keypress') {
    ALPHA = ((currentKeyFromEvent >= 65 && currentKeyFromEvent <= 90) ||
             (currentKeyFromEvent >= 97 && currentKeyFromEvent <= 122));
  } else {
    ALPHA = (currentKeyFromEvent >= 65 && currentKeyFromEvent <= 90);
  }

  CTRL_SHFT_A = (IE_CTRL_SHIFT_KEYSET) ?
                isKeyPressedWithCtrlShift(1, e) :
                isKeyPressedWithCtrlShift(65, e);

  CTRL_SHFT_B = (IE_CTRL_SHIFT_KEYSET) ?
                isKeyPressedWithCtrlShift(2, e) :
                isKeyPressedWithCtrlShift(66, e);

  CTRL_SHFT_D = (IE_CTRL_SHIFT_KEYSET) ?
                isKeyPressedWithCtrlShift(4, e) :
                isKeyPressedWithCtrlShift(68, e);

  CTRL_SHFT_L = (IE_CTRL_SHIFT_KEYSET) ?
                isKeyPressedWithCtrlShift(12, e) :
                isKeyPressedWithCtrlShift(76, e);

  CTRL_SHFT_P = (IE_CTRL_SHIFT_KEYSET) ?
                isKeyPressedWithCtrlShift(16, e) :
                isKeyPressedWithCtrlShift(80, e);

  CTRL_SHFT_S = (IE_CTRL_SHIFT_KEYSET) ?
                isKeyPressedWithCtrlShift(19, e) :
                isKeyPressedWithCtrlShift(83, e);

  CTRL_SHFT_T = (IE_CTRL_SHIFT_KEYSET) ?
                isKeyPressedWithCtrlShift(20, e) :
                isKeyPressedWithCtrlShift(84, e);

  CTRL_SHFT_U = (IE_CTRL_SHIFT_KEYSET) ?
                isKeyPressedWithCtrlShift(21, e) :
                isKeyPressedWithCtrlShift(85, e);

  CTRL_SHFT_Z = (IE_CTRL_SHIFT_KEYSET) ?
                isKeyPressedWithCtrlShift(26, e) :
                isKeyPressedWithCtrlShift(90, e);

  CTRL_B = (IE_KEYSET) ?
           isKeyPressedWithCtrl(66, e) : isKeyPressedWithCtrl(98, e);

  CTRL_D = (IE_KEYSET) ?
           isKeyPressedWithCtrl(68, e) : isKeyPressedWithCtrl(100, e);

  CTRL_G = (IE_KEYSET) ?
           isKeyPressedWithCtrl(71, e) : isKeyPressedWithCtrl(103, e);

  CTRL_I = (IE_KEYSET) ?
           isKeyPressedWithCtrl(73, e) : isKeyPressedWithCtrl(105, e);

  CTRL_L = (IE_KEYSET) ?
           isKeyPressedWithCtrl(76, e) : isKeyPressedWithCtrl(108, e);

  CTRL_P = (IE_KEYSET) ?
           isKeyPressedWithCtrl(80, e) && !isShiftKeyPressed(e) :
           isKeyPressedWithCtrl(112, e) && !isShiftKeyPressed(e);

  CTRL_S = (IE_KEYSET) ?
           isKeyPressedWithCtrl(83, e) : isKeyPressedWithCtrl(115, e);

  CTRL_Y = (IE_KEYSET) ?
           isKeyPressedWithCtrl(89, e) : isKeyPressedWithCtrl(121, e);

  CTRL_Z = (IE_KEYSET) ?
           isKeyPressedWithCtrl(90, e) : isKeyPressedWithCtrl(122, e);

  /*
    We see for 'keycode' of the pressed key if the event-type is keydown or
    keyup, and 'charcode' if the event-type is keypress.
  */
  if (e.type == 'keydown' || e.type == 'keyup') {
    PUNCTUATION =
    // ! @ # $ % ^ & * ( )
    (currentKeyFromEvent >= 48 && currentKeyFromEvent <= 57
        && currentShiftKeyPressedFromEvent) ||
    // ; : + = , < _ - . > / ? ~ `
    (currentKeyFromEvent >= 186 && currentKeyFromEvent <= 192) ||
    // { [ } ] \ | ' "
    (currentKeyFromEvent >= 219 && currentKeyFromEvent <= 222);
  } else {
    // Kepress event
    var str = null;
    if (Detect.IE()) {
      str = String.fromCharCode(currentKeyFromEvent);
    } else if (!(e.event_ && e.event_.isChar)) {
      str = String.fromCharCode(e.charCode);
    } else {
      PUNCTUATION = false;
    }

    if (str) {
      PUNCTUATION = PUNCTUATIONS_STR.indexOf(str) != -1;
    }
  }
}

/**
 * isCtrlShiftKeyPressed()
 *
 * Determine by char index whether a certain key's been pressed in conjunction
 * with the CTRL and SHIFT keys.
 */
function isKeyPressedWithCtrlShift(num, e) {
  var key = getKeyAfterCtrlAndShift(e);
  if (key) return (key == num);
  return false;
}

function isKeyPressedWithCtrl(num, e) {
  var key = getKeyAfterCtrl(e);
  if (key) return (key == num);
  return false;
}

function isKeyPressedWithShift(num, e) {
  var key = getKeyAfterShift(e);
  if (key) return (key == num);
  return false;
}

// The following functions help manage some differing browser event models and
// key detection.
function getKeyAfterCtrl(e) {
  if (currentCtrlKeyPressedFromEvent) { return currentKeyFromEvent; }
  return false;
}

function getKeyAfterShift(e) {
  if (currentShiftKeyPressedFromEvent) { return currentKeyFromEvent; }
  return false;
}

function getKeyAfterCtrlAndShift(e) {
  if (currentCtrlKeyPressedFromEvent && currentShiftKeyPressedFromEvent) {
    return currentKeyFromEvent;
  }
  return false;
}

function isCtrlKeyPressed(e) {
  return getEvent(e).ctrlKey;
}

function isShiftKeyPressed(e) {
  return getEvent(e).shiftKey;
}

function isAltKeyPressed(e) {
  return getEvent(e).altKey;
}

function getKey(e) {
  var key = getEvent(e).keyCode;
  if (!key) key = getEvent(e).charCode;
  return key;
}

function getEventSource(evt) {
  if (Detect.IE()) {
    return evt.srcElement;
  } else {
    return evt.target;
  }
}

function getEvent(e) {
  return (!e) ? event : e;
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/common.js
// Copied from .../google3/javascript/common.js

//------------------------------------------------------------------------
// This file contains common utilities and basic javascript infrastructure.
//
// Notes:
// * Press 'D' to toggle debug mode.
//
// Functions:
//
// - Assertions
// DEPRECATED: Use assert.js
// AssertTrue(): assert an expression. Throws an exception if false.
// Fail(): Throws an exception. (Mark block of code that should be unreachable)
// AssertEquals(): assert that two values are equal.
// AssertNumArgs(): assert number of arguments for the function
// AssertType(): assert that a value has a particular type
//
// - Cookies
// SetCookie(): Sets a cookie.
// GetCookie(): Gets a cookie value.
//
// - Dynamic HTML/DOM utilities
// MaybeGetElement(): get an element by its id
// GetElement(): get an element by its id
// ShowElement(): Show/hide element by setting the "display" css property.
// ShowBlockElement(): Show/hide block element
// AppendNewElement(): Create and append a html element to a parent node.
// HasClass(): check if element has a given class
// AddClass(): add a class to an element
// RemoveClass(): remove a class from an element
//
// - Window/Screen utiltiies
// GetPageOffsetLeft(): get the X page offset of an element
// GetPageOffsetTop(): get the Y page offset of an element
// GetPageOffset(): get the X and Y page offsets of an element
// GetPageOffsetRight() : get X page offset of the right side of an element
// GetPageOffsetBottom() : get Y page offset of the bottom of an element
// GetScrollTop(): get the vertical scrolling pos of a window.
// GetScrollLeft(): get the horizontal scrolling pos of a window
//
// - String utilties
// HtmlEscape(): html escapes a string
// HtmlUnescape(): remove html-escaping.
// CollapseWhitespace(): collapse multiple whitespace into one whitespace.
// Trim(): trim whitespace on ends of string
// IsEmpty(): check if CollapseWhiteSpace(String) == ""
// IsLetterOrDigit(): check if a character is a letter or a digit
//
// - TextArea utilities
// SetCursorPos(): sets the cursor position in a textfield
//
// - Array utilities
// FindInArray(): do a linear search to find an element value.
// DeleteArrayElement(): return a new array with a specific value removed.
//
// - Miscellaneous
// IsDefined(): returns true if argument is not undefined
//------------------------------------------------------------------------

// browser detection
var agent = navigator.userAgent.toLowerCase();
var is_ie = (agent.indexOf('msie') != -1);
var is_konqueror = (agent.indexOf('konqueror') != -1);
var is_safari = (agent.indexOf('safari') != -1) || is_konqueror;
var is_nav = !is_ie && !is_safari && (agent.indexOf('mozilla') != -1);
var is_win = (agent.indexOf('win') != -1);
delete agent;


var BACKSPACE_KEYCODE = 8;
var COMMA_KEYCODE = 188;                // ',' key
var DEBUG_KEYCODE = 68;                 // 'D' key
var DELETE_KEYCODE = 46;
var DOWN_KEYCODE = 40;                  // DOWN arrow key
var ENTER_KEYCODE = 13;                 // ENTER key
var ESC_KEYCODE = 27;                   // ESC key
var LEFT_KEYCODE = 37;                  // LEFT arrow key
var RIGHT_KEYCODE = 39;                 // RIGHT arrow key
var SPACE_KEYCODE = 32;                 // space bar
var TAB_KEYCODE = 9;                    // TAB key
var UP_KEYCODE = 38;                    // UP arrow key
var SHIFT_KEYCODE = 16;

//------------------------------------------------------------------------
// Assertions
// DEPRECATED: Use assert.js
//------------------------------------------------------------------------
/**
 * DEPRECATED: Use assert.js
 */
function raise(msg) {
  if (typeof Error != 'undefined') {
    throw new Error(msg || 'Assertion Failed');
  } else {
    throw (msg);
  }
}

/**
 * DEPRECATED: Use assert.js
 *
 * Fail() is useful for marking logic paths that should
 * not be reached. For example, if you have a class that uses
 * ints for enums:
 *
 * MyClass.ENUM_FOO = 1;
 * MyClass.ENUM_BAR = 2;
 * MyClass.ENUM_BAZ = 3;
 *
 * And a switch statement elsewhere in your code that
 * has cases for each of these enums, then you can
 * "protect" your code as follows:
 *
 * switch(type) {
 *   case MyClass.ENUM_FOO: doFooThing(); break;
 *   case MyClass.ENUM_BAR: doBarThing(); break;
 *   case MyClass.ENUM_BAZ: doBazThing(); break;
 *   default:
 *     Fail("No enum in MyClass with value: " + type);
 * }
 *
 * This way, if someone introduces a new value for this enum
 * without noticing this switch statement, then the code will
 * fail if the logic allows it to reach the switch with the
 * new value, alerting the developer that he should add a
 * case to the switch to handle the new value he has introduced.
 *
 * @param {string} opt_msg to display for failure
 *                 DEFAULT: "Assertion failed".
 */
function Fail(opt_msg) {
  if (opt_msg === undefined) opt_msg = 'Assertion failed';
  if (IsDefined(DumpError)) DumpError(opt_msg + '\n');
  raise(opt_msg);
}

/**
 * DEPRECATED: Use assert.js
 *
 * Asserts that an expression is true (non-zero and non-null).
 *
 * Note that it is critical not to pass logic
 * with side-effects as the expression for AssertTrue
 * because if the assertions are removed by the
 * JSCompiler, then the expression will be removed
 * as well, in which case the side-effects will
 * be lost. So instead of this:
 *
 *  AssertTrue( criticalComputation() );
 *
 * Do this:
 *
 *  var result = criticalComputation();
 *  AssertTrue(result);
 *
 * @param {anything} expression to evaluate.
 * @param {string}   opt_msg to display if the assertion fails.
 *
 */
function AssertTrue(expression, opt_msg) {
  if (!expression) {
    if (opt_msg === undefined) opt_msg = 'Assertion failed';
    Fail(opt_msg);
  }
}

/**
 * DEPRECATED: Use assert.js
 *
 * Asserts that two values are the same.
 *
 * @param {anything} val1
 * @param {anything} val2
 * @param {string} opt_msg to display if the assertion fails.
 */
function AssertEquals(val1, val2, opt_msg) {
  if (val1 != val2) {
    if (opt_msg === undefined) {
      opt_msg = 'AssertEquals failed: <' + val1 + '> != <' + val2 + '>';
    }
    Fail(opt_msg);
  }
}

/**
 * DEPRECATED: Use assert.js
 *
 * Asserts that a value is of the provided type.
 *
 *   AssertType(6, Number);
 *   AssertType("ijk", String);
 *   AssertType([], Array);
 *   AssertType({}, Object);
 *   AssertType(ICAL_Date.now(), ICAL_Date);
 *
 * @param {anything} value
 * @param {constructor function} type
 * @param {string} opt_msg to display if the assertion fails.
 */
function AssertType(value, type, opt_msg) {
  // for backwards compatability only
  if (typeof value == type) return;

  if (value || value == '') {
    try {
      if (type == AssertTypeMap[typeof value] || value instanceof type) return;
    } catch (e) { /* failure, type was an illegal argument to instanceof */ }
  }
  if (opt_msg === undefined) {
    if (typeof type == 'function') {
      var match = type.toString().match(/^\s*function\s+([^\s\{]+)/);
      if (match) type = match[1];
    }
    opt_msg = 'AssertType failed: <' + value + '> not typeof '+ type;
  }
  Fail(opt_msg);
}

var AssertTypeMap = {
  'string' : String,
  'number' : Number,
  'boolean' : Boolean
};

/**
 * DEPRECATED: Use assert.js
 *
 * Asserts that the number of arguments to a
 * function is num. For example:
 *
 * function myFunc(one, two, three) [
 *   AssertNumArgs(3);
 *   ...
 * }
 *
 * myFunc(1, 2); // assertion fails!
 *
 * Note that AssertNumArgs does not take the function
 * as an argument; it is simply used in the context
 * of the function.
 *
 * @param {int} number of arguments expected.
 * @param {string} opt_msg to display if the assertion fails.
 */
function AssertNumArgs(num, opt_msg) {
  var caller = AssertNumArgs.caller;  // This is not supported in safari 1.0
  if (caller && caller.arguments.length != num) {
    if (opt_msg === undefined) {
      opt_msg = caller.name + ' expected ' + num + ' arguments '
                  + ' but received ' + caller.arguments.length;
    }
    Fail(opt_msg);
  }
}

//------------------------------------------------------------------------
// Cookies
//------------------------------------------------------------------------
var ILLEGAL_COOKIE_CHARS_RE = /[\s;]/;
/**
 * Sets a cookie.
 * The max_age can be -1 to set a session cookie. To expire cookies, use
 * ExpireCookie() instead.
 *
 * @param name The cookie name.
 * @param value The cookie value.
 * @param opt_max_age The max age in seconds (from now). Use -1 to set a
 *   session cookie. If not provided, the default is -1 (i.e. set a session
 *   cookie).
 * @param opt_path The path of the cookie, or null to not specify a path
 *   attribute (browser will use the full request path). If not provided, the
 *   default is '/' (i.e. path=/).
 * @param opt_domain The domain of the cookie, or null to not specify a domain
 *   attribute (brower will use the full request host name). If not provided,
 *   the default is null (i.e. let browser use full request host name).
 * @return Void.
 */
function SetCookie(name, value, opt_max_age, opt_path, opt_domain) {
  value = '' + value;
  AssertTrue((typeof name == 'string' &&
              typeof value == 'string' &&
              !name.match(ILLEGAL_COOKIE_CHARS_RE) &&
              !value.match(ILLEGAL_COOKIE_CHARS_RE)),
             'trying to set an invalid cookie');

  if (!IsDefined(opt_max_age)) opt_max_age = -1;
  if (!IsDefined(opt_path)) opt_path = '/';
  if (!IsDefined(opt_domain)) opt_domain = null;

  var domain_str = (opt_domain == null) ? '' : ';domain=' + opt_domain;
  var path_str = (opt_path == null) ? '' : ';path=' + opt_path;

  var expires_str;

  // Case 1: Set a session cookie.
  if (opt_max_age < 0) {
    expires_str = '';

  // Case 2: Expire the cookie.
  // Note: We don't tell people about this option in the function doc because
  // we prefer people to use ExpireCookie() to expire cookies.
  } else if (opt_max_age == 0) {
    // Note: Don't use Jan 1, 1970 for date because NS 4.76 will try to convert
    // it to local time, and if the local time is before Jan 1, 1970, then the
    // browser will ignore the Expires attribute altogether.
    var pastDate = new Date(1970, 1 /*Feb*/, 1);  // Feb 1, 1970
    expires_str = ';expires=' + pastDate.toUTCString();

  // Case 3: Set a persistent cookie.
  } else {
    var futureDate = new Date(Now() + opt_max_age * 1000);
    expires_str = ';expires=' + futureDate.toUTCString();
  }

  document.cookie = name + '=' + value + domain_str + path_str + expires_str;
}

/** Returns the value for the first cookie with the given name
 * @param name : string.
 * @return a string or the empty string if no cookie found.
 */
function GetCookie(name) {
  var nameeq = name + '=';
  var cookie = String(document.cookie);
  for (var pos = -1; (pos = cookie.indexOf(nameeq, pos + 1)) >= 0;) {
    var i = pos;
    // walk back along string skipping whitespace and looking for a ; before
    // the name to make sure that we don't match cookies whose name contains
    // the given name as a suffix.
    while (--i >= 0) {
      var ch = cookie.charAt(i);
      if (ch == ';') {
        i = -1;  // indicate success
        break;
      } else if (' \t'.indexOf(ch) < 0) {
        break;
      }
    }
    if (-1 === i) {  // first cookie in the string or we found a ;
      var end = cookie.indexOf(';', pos);
      if (end < 0) { end = cookie.length; }
      return cookie.substring(pos + nameeq.length, end);
    }
  }
  return '';
}


//------------------------------------------------------------------------
// Time
//------------------------------------------------------------------------
function Now() {
  return (new Date()).getTime();
}

//------------------------------------------------------------------------
// Dynamic HTML/DOM utilities
//------------------------------------------------------------------------
// Gets a element by its id, may return null
function MaybeGetElement(win, id) {
  return win.document.getElementById(id);
}

// Same as MaybeGetElement except that it throws an exception if it's null
function GetElement(win, id) {
  var el = win.document.getElementById(id);
  if (!el) {
    DumpError('Element ' + id + ' not found.');
  }
  return el;
}

// Gets elements by its id/name
// IE treats getElementsByName as searching over ids, while Moz use names.
// so tags must have both id and name as the same string
function GetElements(win, id) {
  return win.document.getElementsByName(id);
}

// Show/hide an element.
function ShowElement(el, show) {
  el.style.display = show ? '' : 'none';
}

// Show/hide a block element.
// ShowElement() doesn't work if object has an initial class with display:none
function ShowBlockElement(el, show) {
  el.style.display = show ? 'block' : 'none';
}

// Show/hide an inline element.
// ShowElement() doesn't work when an element starts off display:none.
function ShowInlineElement(el, show) {
  el.style.display = show ? 'inline' : 'none';
}

// Append a new HTML element to a HTML node.
function AppendNewElement(win, parent, tag) {
  var e = win.document.createElement(tag);
  parent.appendChild(e);
  return e;
}

// Create a new TR containing the given td's
function Tr(win, tds) {
  var tr = win.document.createElement('TR');
  for (var i = 0; i < tds.length; i++) {
    tr.appendChild(tds[i]);
  }
  return tr;
}

// Create a new TD, with an optional colspan
function Td(win, opt_colspan) {
  var td = win.document.createElement('TD');
  if (opt_colspan) {
    td.colSpan = opt_colspan;
  }
  return td;
}


// Check if an element has a given class
function HasClass(el, cl) {
  if (el == null || el.className == null) return false;
  var classes = el.className.split(' ');
  for (var i = 0; i < classes.length; i++) {
    if (classes[i] == cl) {
      return true;
    }
  }
  return false;
}

// Add a class to element
function AddClass(el, cl) {
  if (HasClass(el, cl)) return;
  el.className += ' ' + cl;
}

// Remove a class from an element
function RemoveClass(el, cl) {
  if (el.className == null) return;
  var classes = el.className.split(' ');
  var result = [];
  var changed = false;
  for (var i = 0; i < classes.length; i++) {
    if (classes[i] != cl) {
      if (classes[i]) { result.push(classes[i]); }
    } else {
      changed = true;
    }
  }
  if (changed) { el.className = result.join(' '); }
}

// Performs an in-order traversal of the tree rooted at the given node
// (excluding the root node) and returns an array of nodes that match the
// given selector. The selector must implement the method:
//
// boolean select(node);
//
// This method is a generalization of the DOM method "getElementsByTagName"
//
function GetElementsBySelector(root, selector) {
  var nodes = [];
  for (var child = root.firstChild; child; child = child.nextSibling) {
    AddElementBySelector_(child, selector, nodes);
  }
  return nodes;
}

// Recursive helper for GetElemnetsBySelector()
function AddElementBySelector_(root, selector, nodes) {
  // First test the parent
  if (selector.select(root)) {
    nodes.push(root);
  }

  // Then recurse through the children
  for (var child = root.firstChild; child; child = child.nextSibling) {
    AddElementBySelector_(child, selector, nodes);
  }
}

//------------------------------------------------------------------------
// Window/screen utilities
// TODO: these should be renamed (e.g. GetWindowWidth to GetWindowInnerWidth
// and moved to geom.js)
//------------------------------------------------------------------------
// Get page offset of an element
function GetPageOffsetLeft(el) {
  var x = el.offsetLeft;
  if (el.offsetParent != null)
    x += GetPageOffsetLeft(el.offsetParent);
  return x;
}

// Get page offset of an element
function GetPageOffsetTop(el) {
  var y = el.offsetTop;
  if (el.offsetParent != null)
    y += GetPageOffsetTop(el.offsetParent);
  return y;
}

// Get page offset of an element
function GetPageOffset(el) {
  var x = el.offsetLeft;
  var y = el.offsetTop;
  if (el.offsetParent != null) {
    var pos = GetPageOffset(el.offsetParent);
    x += pos.x;
    y += pos.y;
  }
  return {x: x, y: y};
}

function GetPageOffsetRight(el) {
  return GetPageOffsetLeft(el) + el.offsetWidth;
}

function GetPageOffsetBottom(el) {
  return GetPageOffsetTop(el) + el.offsetHeight;
}

// Get the y position scroll offset.
function GetScrollTop(win) {
  // all except Explorer
  if ('pageYOffset' in win) {
    return win.pageYOffset;
  }
  // Explorer 6 Strict Mode
  else if ('documentElement' in win.document &&
           'scrollTop' in win.document.documentElement) {
    return win.document.documentElement.scrollTop;
  }
  // other Explorers
  else if ('scrollTop' in win.document.body) {
    return win.document.body.scrollTop;
  }

  return 0;
}

// Get the x position scroll offset.
function GetScrollLeft(win) {
  // all except Explorer
  if ('pageXOffset' in win) {
    return win.pageXOffset;
  }
  // Explorer 6 Strict Mode
  else if ('documentElement' in win.document &&
           'scrollLeft' in win.document.documentElement) {
    return win.document.documentElement.scrollLeft;
  }
  // other Explorers
  else if ('scrollLeft' in win.document.body) {
    return win.document.body.scrollLeft;
  }

  return 0;
}

//------------------------------------------------------------------------
// String utilities
//------------------------------------------------------------------------
// Do html escaping
var amp_re_ = /&/g;
var lt_re_ = /</g;
var gt_re_ = />/g;

// Convert text to HTML format. For efficiency, we just convert '&', '<', '>'
// characters.
// Note: Javascript >= 1.3 supports lambda expression in the replacement
// argument. But it's slower on IE.
// Note: we can also implement HtmlEscape by setting the value
// of a textnode and then reading the 'innerHTML' value, but that
// that turns out to be slower.
// Params: str: String to be escaped.
// Returns: The escaped string.
function HtmlEscape(str) {
  if (!str) return '';
  return str.replace(amp_re_, '&amp;').replace(lt_re_, '&lt;').
    replace(gt_re_, '&gt;').replace(quote_re_, '&quot;');
}

/** converts html entities to plain text.  It covers the most common named
 * entities and numeric entities.
 * It does not cover all named entities -- it covers &{lt,gt,amp,quot,nbsp}; but
 * does not handle some of the more obscure ones like &{ndash,eacute};.
 */
function HtmlUnescape(str) {
  if (!str) return '';
  return str.
    replace(/&#(\d+);/g,
      function(_, n) { return String.fromCharCode(parseInt(n, 10)); }).
    replace(/&#x([a-f0-9]+);/gi,
      function(_, n) { return String.fromCharCode(parseInt(n, 16)); }).
    replace(/&(\w+);/g, function(_, entity) {
      entity = entity.toLowerCase();
      return entity in HtmlUnescape.unesc ? HtmlUnescape.unesc[entity] : '?';
    });
}
HtmlUnescape.unesc = { lt: '<', gt: '>', quot: '"', nbsp: ' ', amp: '&' };

// Escape double quote '"' characters in addition to '&', '<', '>' so that a
// string can be included in an HTML tag attribute value within double quotes.
// Params: str: String to be escaped.
// Returns: The escaped string.
var quote_re_ = /\"/g;

var JS_SPECIAL_RE_ = /[\'\\\r\n\b\"<>&]/g;

function JSEscOne_(s) {
  if (!JSEscOne_.js_escs_) {
    var escapes = {};
    escapes['\\'] = '\\\\';
    escapes['\''] = '\\047';
    escapes['\n'] = '\\n';
    escapes['\r'] = '\\r';
    escapes['\b'] = '\\b';
    escapes['\"'] = '\\042';
    escapes['<'] = '\\074';
    escapes['>'] = '\\076';
    escapes['&'] = '\\046';

    JSEscOne_.js_escs_ = escapes;
  }

  return JSEscOne_.js_escs_[s];
}

// converts multiple ws chars to a single space, and strips
// leading and trailing ws
var spc_re_ = /\s+/g;
var beg_spc_re_ = /^ /;
var end_spc_re_ = / $/;
function CollapseWhitespace(str) {
  if (!str) return '';
  return str.replace(spc_re_, ' ').replace(beg_spc_re_, '').
    replace(end_spc_re_, '');
}

var newline_re_ = /\r?\n/g;
var spctab_re_ = /[ \t]+/g;
var nbsp_re_ = /\xa0/g;

function HtmlifyNewlines(str) {
  if (!str) return '';
  return str.replace(newline_re_, '<br>');
}

// URL encodes the string.
function UrlEncode(str) {
  return encodeURIComponent(str);
}

function Trim(str) {
  if (!str) return '';
  return str.replace(/^\s+/, '').replace(/\s+$/, '');
}

function EndsWith(str, suffix) {
  if (!str) return !suffix;
  return (str.lastIndexOf(suffix) == (str.length - suffix.length));
}

// Check if a string is empty
function IsEmpty(str) {
  return CollapseWhitespace(str) == '';
}

// Check if a character is a letter
function IsLetterOrDigit(ch) {
  return ((ch >= 'a' && ch <= 'z') ||
          (ch >= 'A' && ch <= 'Z') ||
         (ch >= '0' && ch <= '9'));
}

// Check if a character is a space character
function IsSpace(ch) {
  return (' \t\r\n'.indexOf(ch) >= 0);
}

//------------------------------------------------------------------------
// TextArea utilities
//------------------------------------------------------------------------

function SetCursorPos(win, textfield, pos) {
  if (IsDefined(textfield.selectionEnd) &&
      IsDefined(textfield.selectionStart)) {
    // Mozilla directly supports this
    textfield.selectionStart = pos;
    textfield.selectionEnd = pos;

  } else if (win.document.selection && textfield.createTextRange) {
    // IE has textranges. A textfield's textrange encompasses the
    // entire textfield's text by default
    var sel = textfield.createTextRange();

    sel.collapse(true);
    sel.move('character', pos);
    sel.select();
  }
}

//------------------------------------------------------------------------
// Array utilities
//------------------------------------------------------------------------
// Find an item in an array, returns the key, or -1 if not found
function FindInArray(array, x) {
  for (var i = 0; i < array.length; i++) {
    if (array[i] == x) {
      return i;
    }
  }
  return -1;
}

// Inserts an item into an array, if it's not already in the array
function InsertArray(array, x) {
  if (FindInArray(array, x) == -1) {
    array[array.length] = x;
  }
}

// Delete an element from an array
function DeleteArrayElement(array, x) {
  var i = 0;
  while (i < array.length && array[i] != x)
    i++;
  array.splice(i, 1);
}

function GetEventTarget(/*Event*/ ev) {
// Event is not a type in IE; IE uses Object for events
//  AssertType(ev, Event, 'arg passed to GetEventTarget not an Event');
  return ev.srcElement || ev.target;
}

//------------------------------------------------------------------------
// Misc
//------------------------------------------------------------------------
// Check if a value is defined
function IsDefined(value) {
  return (typeof value) != 'undefined';
}

function GetKeyCode(event) {
  var code;
  if (event.keyCode) {
    code = event.keyCode;
  } else if (event.which) {
    code = event.which;
  }
  return code;
}

// define a forid function to fetch a DOM node by id.
function forid_1(id) {
  return document.getElementById(id);
}
function forid_2(id) {
  return document.all[id];
}

/**
 * Fetch an HtmlElement by id.
 * DEPRECATED: use $ in dom.js
 */
var forid = document.getElementById ? forid_1 : forid_2;



function log(msg) {
  /* a top level window is its own parent.  Use != or else fails on IE with
   * infinite loop.
   */
  try {
    if (window.parent != window && window.parent.log) {
      window.parent.log(window.name + '::' + msg);
      return;
    }
  } catch (e) {
    // Error: uncaught exception: Permission denied to get property Window.log
  }
  var logPane = forid('log');
  if (logPane) {
    var logText = '<p class=logentry><span class=logdate>' + new Date() +
                  '</span><span class=logmsg>' + msg + '</span></p>';
    logPane.innerHTML = logText + logPane.innerHTML;
  } else {
    window.status = msg;
  }
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/post.constants.js
/**
 * Using a factory class to avoid namespace collisions
 * of common ID names for different purposes
 */
function Preview() {}
function Posting() {}
function HtmlSource() {}

// Common IDs, class names, and other constants
HtmlSource.TEXTAREA = 'textarea';

Preview.ID = 'preview';
Preview.TEXTAREA = HtmlSource.TEXTAREA;
Preview.PREVIEW_BUTTON = 'formatbar_PreviewAction';
Preview.HTML_PREVIEW_BUTTON = 'htmlbar_PreviewAction';

Posting.PUBLISH_BUTTON = 'publishButton-hidden';
Posting.DRAFT_BUTTON = 'saveButton-hidden';
Posting.AUTOSAVE_BUTTON = 'autosaveButton';
Posting.OPTIONS = 'postoptions';
Posting.TITLE = 'f-title';
Posting.URL = 'f-address';

//java/com/google/blogger/b2/staticresources/html/app/scripts/post.preview.toggle.js

/** -------------------------------------------------------
 * Author: Chris Wetherell
 * Inline Preview ("Toggle")
 *
 * A series of functions that will toggle a simply styled
 * preview of the post that could be generated by the form, if
 * submitted.
 *
 * Warning: Written on the highways between L.A. and Tuscon.
 *
 * Warning: Updated on the highways between New Mexico and Texas.
 *
 * Typical usage:
 * On an HTML element: onclick="toggle();"
  ------------------------------------------------------- */


  /**
   * toggle()
   *
   * A series of functions that will toggle a simply styled
   * preview of the post that could be generated by the form, if
   * submitted.
   *
   * Typical usage:
   * On an HTML element: onclick="toggle();"
   */
  function toggle(e) {
    // Deselect spellchecker higlights
    if (BLOG_finishSpellcheck) {
      BLOG_finishSpellcheck();
    }

    if (trOK) {
      _TR_clearMenus();
    }

    if (!document.getElementById) {
      // TODO(baugher): i18n, or fix.
      alert('This feature is not supported by your browser.');
      return;
    }
    if (RichEdit.PREVIEW_IS_HIDDEN) {
      showPreview();
      hideElement(d(Posting.OPTIONS));
    } else {
      hidePreview();
      restorePostOptions();
    }
  }

  /**
   * PreviewElements()
   *
   * An object which stores the various elements needed to be adjusted to
   * display a preview of the post.
   */
  var PreviewElements = function() {};

  function setPreviewElements() {
    PreviewElements.f = d(RichEdit.frameId);
    PreviewElements.t = getElement(Preview.TEXTAREA);
    PreviewElements.p = getElement('previewbody');
    PreviewElements.s = d('SubmitTwo');
    PreviewElements.k = d('key_commands');
    PreviewElements.k_p = d('key_commands_placeholder');
    PreviewElements.ed = d('editarea');
    PreviewElements.title = d(Posting.TITLE);
    PreviewElements.address = d(Posting.URL);
    PreviewElements.h1 = dE(getElement(Preview.ID), 'h1')[0];
    PreviewElements.b = (RichEdit.mode == RichEdit.DESIGN_MODE) ?
      d(Preview.PREVIEW_BUTTON) : d(Preview.HTML_PREVIEW_BUTTON);
  }

  /**
   * showPreview()
   *
   * Displays a preview of the post and hides form elements.
   */
  function showPreview() {
    setPreviewElements();

    // get the post body
    var strBody;
    if (RichEdit.mode == RichEdit.DESIGN_MODE) {
      strBody = getDesignModeHtml();
      if (Detect.IE()) strBody = RemoveLinksWithinTags(strBody);
    }
    if (RichEdit.mode == RichEdit.HTML_MODE) {
      strBody = PreviewElements.t.value;

      // Safari bug - if textarea has focus and
      // then disappears subsequent key capture fails
      if (PreviewElements.t.style.display != 'none' && Detect.SAFARI()) {
        PreviewElements.t.blur();
      }
    }

    // hide the edit area
    if (RichEdit.mode == RichEdit.DESIGN_MODE) {
      hideElement(PreviewElements.f);
    } else {
      hideElement(PreviewElements.t);
    }

    // change the preview label
    if (PreviewElements.b) PreviewElements.b.innerHTML = hide_preview;

    // ------------------------------------------
    // Transform the post body for inline viewing
    // ------------------------------------------

    // Replace text line breaks with HTML link breaks
    strBody = strBody.replace(/\n/g, '<br />');

    // Make images with relative links appear in Preview
    var blogURL = d('blogURL').value;
    strBody = strBody.replace(/<img src=\"\//g,
                              '<img src=\"' + blogURL);

    // Make all preview links open in a new window
    var anchors = PreviewElements.p.getElementsByTagName('A');
    for (a = 0; a < anchors.length; a++) {
      anchors[a].setAttribute('target', '_new');
    }

    // Make all blogger video expansions the real thing
    var tmpDiv = document.createElement('div');
    tmpDiv.innerHTML = strBody;
    BLOG_replaceAllExpansionsWithIframes(tmpDiv);
    strBody = tmpDiv.innerHTML;

    // ------------------------------------------
    // Set the preview area
    // ------------------------------------------

    showElement(PreviewElements.p);
    hideElement(PreviewElements.k);
    showElement(PreviewElements.k_p);

    // Add the post title, and if the URL field exists and
    // has content, then make the title a hyperlink.
    if (PreviewElements.title) {
      if (PreviewElements.title.value.length > 0) {
        showElement(PreviewElements.h1);
      } else {
        hideElement(PreviewElements.h1);
      }
      setPreviewTitle(getTitle());
    }

    // Make extra save buttons at the bottom of large posts.
    if (strBody.length > 1600) {
      PreviewElements.s.innerHTML = d('postButtons').innerHTML;
      showElement(PreviewElements.s);
    } else {
      PreviewElements.s.innerHTML = '';
    }

    // Copy and paste the post text from the form to the preview area
    PreviewElements.p.innerHTML = strBody;

    // Clean-up post body if it came from the WYSIWYG iframe
    if (RichEdit.ENABLE_IFRAME) {
      PreviewElements.p.innerHTML
        = convertAllFontsToSpans(cleanHTML(PreviewElements.p.innerHTML));
    }

    setFormatBarElements('none');
    setHtmlBarElements('none');
    RichEdit.PREVIEW_IS_HIDDEN = false;

    // To restore the editing area via key commands, Moz needs the focus
    // to be transferred from the invisible editor to a visible element
    if (Detect.MOZILLA()) PreviewElements.title.focus();
  }

  /**
   * hidePreview()
   *
   * Hides a preview of the post and displays the form elements.
   */
  function hidePreview() {
    setPreviewElements();

    // ------------------------------------------
    // Restore the editing area
    // ------------------------------------------

    if (RichEdit.mode == RichEdit.DESIGN_MODE) {
      showElement(PreviewElements.f);
    } else {
      showElement(PreviewElements.t);
    }
    if (RichEdit.showKeyCommands) {
      showElement(PreviewElements.k);
      hideElement(PreviewElements.k_p);
    }
    hideElement(PreviewElements.p);
    hideElement(PreviewElements.s);
    hideElement(PreviewElements.h1);

    setFormatBarElements('block');
    setHtmlBarElements('block');
    if (PreviewElements.b) PreviewElements.b.innerHTML = preview;

    RichEdit.PREVIEW_IS_HIDDEN = true;

    // Moz needs to be reminded to have the iframe editable after
    // a display change is made
    if (RichEdit.ENABLE_IFRAME
        && RichEdit.mode == RichEdit.DESIGN_MODE) {
      RichEdit.frameDoc.designMode = 'On';
    }
  }

  /**
   * setFormatBarElements()
   *
   * Shows (or hides) the glyphs and icons with the formatting bar.
   */
  function setFormatBarElements(display) {
    var bar = RichEdit.formatbar;
    var bar_SPANs = dE(bar, 'span');
    for (x = 0; x < bar_SPANs.length; x++) {
      var span = bar_SPANs[x];
      if (span.id != Preview.PREVIEW_BUTTON) {
        span.style.display = display;
      }
    }
    var bar_SELECTs = dE(bar, 'select');
    for (x = 0; x < bar_SELECTs.length; x++) {
      bar_SELECTs[x].style.display = display;
    }
    var bar_DIVs = dE(bar, 'div');
    for (x = 0; x < bar_DIVs.length; x++) {
      var div = bar_DIVs[x];
      if (div.className != 'clear') {
        div.style.display = display;
      }
    }
  }

  /**
   * setHtmlBarElements()
   *
   * Shows (or hides) the glyphs and icons within the html-formatting bar.
   */
  function setHtmlBarElements(display) {
    var bar = RichEdit.htmlbar;
    var bar_SPANs = dE(bar, 'span');
    for (x = 0; x < bar_SPANs.length; x++) {
      var span = bar_SPANs[x];
      if (span.id != Preview.HTML_PREVIEW_BUTTON) {
        span.style.display = display;
      }
    }
    var bar_DIVs = dE(bar, 'div');
    for (x = 0; x < bar_DIVs.length; x++) {
      var div = bar_DIVs[x];
      if (div.className != 'clear') {
        div.style.display = display;
      }
    }
  }

  /**
   * setPreviewTitle()
   *
   * Sets the title text of the post's preview
   *
   * Typical usage:
   * setPreviewTitle(getTitle());
   */
  function setPreviewTitle(s) {
    try {
      var h1 = dE(d(Preview.ID), 'h1')[0];
      if (h1.style.display == 'block') {
        h1.innerHTML = s;
      }
    } catch (e) {}
  }


  /**
   * getTitle()
   *
   * Returns the value of the title field except in the
   * case where there's a non-empty URL field where this
   * instead returns the title value wrapped in a hyperlink.
   *
   * Typical usage:
   * setPreviewTitle(getTitle());
   */
  function getTitle() {
    var title = d(Posting.TITLE);
    var address = d(Posting.URL);

    if (title) {
      var sTitle = title.value;
      if (address && address.value.length > 0) {
        sTitle = '<a target=\"new\" href=\"'
                  + address.value
                  + '\">'
                  + sTitle
                  + '</a>';
      }
    }

    return sTitle;
  }

//java/com/google/blogger/b2/staticresources/html/app/scripts/form.textbar.js
/** -------------------------------------------------------
 * Author: Chris Wetherell at Google, based on code by Chris
 * Wetherell before Google.
 *
 * Textbar() [object]
 *
 * A class that can take the currently selected (highlighted)
 * text and wrap it in HTML tags commonly used for formatting.
 *
 * TODO: This could be generalized for, say, template use(!)
 *
 * Based on code found at massless.org.  Which, given that *I* am
 * that site's author makes this credit declaration intentionally
 * confusing.
 *
 * Warning: Expects presence of dom.common.js and detect.js.
 *
 * Typical usage:
 * On an HTML element: onclick="Textbar.Bold();"
  ------------------------------------------------------- */

function Textbar() {}
Textbar.ELEMENT_ID;
Textbar.PREVIEW_BUTTON;

/* -----------
 * Format tags
 * ----------- */
// Undoing tag formatting in designMode is browser-specific.  Mozilla / Gecko
// prefers SPAN tag with style while IE prefers explicit formatting tags.
Textbar.Bold = function()
{
  if (!Blsp.running) {
    if (!Detect.IE()) {
      this.wrapSelection('<span style="font-weight:bold;">', '</span>');
    } else {
      this.wrapSelection('<strong>', '</strong>');
    }
  }
};

Textbar.Italic = function()
{
  if (!Blsp.running) {
    if (!Detect.IE()) {
      this.wrapSelection('<span style="font-style:italic;">', '</span>');
    } else {
      this.wrapSelection('<em>', '</em>');
    }
  }
};

Textbar.Blockquote = function()
{
  if (!Blsp.running) {
    this.wrapSelection('<blockquote>', '</blockquote>');
  }
};

Textbar.Link = function()
{
  if (!Blsp.running) {
    this.wrapSelectionWithLink(this.getElement());
  }
};

/**
 * getElement()
 *
 * For storing the element (usually a <textarea>) where the
 * text will be modified.
 */
Textbar.Element = false;
Textbar.getElement = function()
{
  if (!this.Element) {
     this.Element = d(this.ELEMENT_ID);
  }
  return this.Element;
};

/**
 * wrapSelection()
 *
 * Branches out the tag wrapping code for differing implementations
 * of selection management. *sigh*  Standards, where art thou?
 */
Textbar.wrapSelection = function(lft, rgt) {
  if (Detect.IE()) {
    this.IEWrap(lft, rgt);
  } else if (DOM) {
    this.mozWrap(lft, rgt);
  }
};

/**
 * wrapSelectionWithLink()
 *
 * Wrap a hyperlink around some text by prompting the user for
 * a URL.
 */
Textbar.wrapSelectionWithLink = function() {
  var my_link = prompt('Enter URL:', 'http://');
  if (my_link != null) {
    lft = '<a href=\"' + my_link + '\">';
    rgt = '</a>';
    this.wrapSelection(lft, rgt);
  }
  return;
};

/**
 * mozWrap()
 *
 * Wraps tags around text in Mozilla/Gecko browsers.
 */
Textbar.mozWrap = function(lft, rgt) {
  var txtarea = this.getElement();
  var v = txtarea.value;
  var selLength = txtarea.textLength;
  var selStart = txtarea.selectionStart;
  var selEnd = txtarea.selectionEnd;
  var scroll = txtarea.scrollTop;

  if (selEnd == 1 || selEnd == 2) selEnd = selLength;
  var s1 = (v).substring(0, selStart);
  var s2 = (v).substring(selStart, selEnd);
  var s3 = (v).substring(selEnd, selLength);
  txtarea.value = s1 + lft + s2 + rgt + s3;

  txtarea.scrollTop = scroll;

  // Note: Firefox (last tested with 1.0.6) does not display the
  // selection if the scroll has been set programmatically. Also
  // it won't give focus back to the text area unless the user clicks
  // or scrolls.

  txtarea.selectionStart = s1.length + lft.length + s2.length;
  txtarea.selectionEnd = txtarea.selectionStart;
};

/**
 * IEWrap()
 *
 * Wraps tags around text in Internet Explorer.
 */
Textbar.IEWrap = function(lft, rgt) {
  txtarea = this.getElement();
  strSelection = document.selection.createRange().text;

  if (strSelection != '') {
    document.selection.createRange().text = lft + strSelection + rgt;
  } else {
    txtarea.focus();
    strSelection = document.selection.createRange().text;
    txtarea.value = txtarea.value + lft + rgt;
  }

};

// Emulate the 'click' function in IE for Mozilla
// TODO: Add to cross-browser event library
if (Detect.MOZILLA()) {
  HTMLElement.prototype.click = function() {
    if (typeof this.onclick == 'function') {
      this.onclick({type: 'click'});
    }
  };
}

/**
 * activateKeyCommands()
 *
 * Based on a combination of keystrokes and keyholds, activate
 * a particular formatting method.
 *
 * Warning: In Safari, only the Preview button onclick command works
 * since there is no selection management yet for that browser.
 */
Textbar.activateKeyCommands = function(e) {
  if (Detect.IE()) e = window.event;

  setKeysetByEvent(e);

  if (CTRL_SHFT_P) d(Textbar.PREVIEW_BUTTON).click();

  if (CTRL_P) {
    d(Posting.PUBLISH_BUTTON).click();
    preventDefault(e);
  }

  if (CTRL_D || CTRL_SHFT_D) d(Posting.DRAFT_BUTTON).click();

  if (isAutosaveEnabled()) {
    if (CTRL_S || CTRL_SHFT_S) d(Posting.AUTOSAVE_BUTTON).click();
  }

  // WARNING: The following can delete data in a textarea in Safari as of
  // 1.2.1 (v125.1)
  if (!Detect.SAFARI()) {
    if (CTRL_B || CTRL_SHFT_B) Textbar.Bold();
    if (CTRL_I || CTRL_SHFT_T) Textbar.Italic();
    if (CTRL_L || CTRL_SHFT_L) Textbar.Blockquote();
    if (CTRL_SHFT_A) Textbar.Link();
  }

  // prevent sidebars or dialogs from opening with reserved keypresses
  if (CTRL_B || CTRL_I || CTRL_S || CTRL_D) {
    if (Detect.IE()) {
      e.returnValue = false;
    } else {
      e.preventDefault();
    }
  }

  return true;
};

//java/com/google/blogger/b2/staticresources/html/app/scripts/xmlhttp.js
// Copyright 2004-2006 Google Inc.
// All Rights Reserved.
//
// A bunch of XML HTTP recipes used to do RPC from within javascript from
// Gagan Saksena's wiki page
// http://wiki/twiki/bin/view/Main/JavaScriptRecipes

/** Candidate Active X types.
  * @private
  */
var _XH_ACTIVE_X_IDENTS = [
  'MSXML2.XMLHTTP.5.0', 'MSXML2.XMLHTTP.4.0', 'MSXML2.XMLHTTP.3.0',
  'MSXML2.XMLHTTP', 'MICROSOFT.XMLHTTP.1.0', 'MICROSOFT.XMLHTTP.1',
  'MICROSOFT.XMLHTTP'];
/** The active x identifier used for ie.
 * @private
 */
var _xh_ieProgId = undefined;

// Domain for XmlHTTPRequest.readyState
var XML_READY_STATE_UNINITIALIZED = 0;
var XML_READY_STATE_LOADING = 1;
var XML_READY_STATE_LOADED = 2;
var XML_READY_STATE_INTERACTIVE = 3;
var XML_READY_STATE_COMPLETED = 4;

/** initialize the private state used by other functions.
  * @private
  */
function _XH_XmlHttpInit() {
  // Nobody (on the web) is really sure which of the progid's listed is totally
  // necessary. It is known, for instance, that certain installations of IE will
  // not work with only Microsoft.XMLHTTP, as well as with MSXML2.XMLHTTP.
  // Safest course seems to be to do this -- include all known progids for
  // XmlHttp.
  if (typeof XMLHttpRequest == 'undefined' &&
      typeof ActiveXObject != 'undefined') {
    for (var i = 0; i < _XH_ACTIVE_X_IDENTS.length; i++) {
      var candidate = _XH_ACTIVE_X_IDENTS[i];

      try {
        new ActiveXObject(candidate);
        _xh_ieProgId = candidate;
        break;
      } catch (e) {
        // do nothing; try next choice
      }
    }
  }
}

_XH_XmlHttpInit();

/** create and return an xml http request object that can be passed to
  * {@link #XH_XmlHttpGET} or {@link #XH_XmlHttpPOST}.
  */
function XH_XmlHttpCreate() {
  if (_xh_ieProgId !== undefined) {
    return new ActiveXObject(_xh_ieProgId);
  } else if (window.XMLHttpRequest) {
    return new window.XMLHttpRequest();
  } else {
    return null;
  }
}

/** send a get request.
  * @param xmlhttp as from {@link XH_XmlHttpCreate} .
  * @param url the service to contact.
  * @param handler function called when the response is received.
  */
function XH_XmlHttpGET(xmlhttp, url, handler) {
  xmlhttp.onreadystatechange = handler;
  xmlhttp.open('GET', url, true);
  _XH_XmlHttpSend(xmlhttp, null);
}

/** send a post request.
  * @param xmlhttp as from {@link XH_XmlHttpCreate} .
  * @param url the service to contact.
  * @param data the request content.
  * @param handler function called when the response is received.
  */
function XH_XmlHttpPOST(xmlhttp, url, data, handler) {
  xmlhttp.onreadystatechange = handler;
  xmlhttp.open('POST', url, true);
  xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
  xmlhttp.setRequestHeader('Content-Length', data.length);
  _XH_XmlHttpSend(xmlhttp, data);
}

/** @private */
function _XH_XmlHttpSend(xmlhttp, data) {
  try {
    xmlhttp.send(data);
  } catch (e) {
    // you may want to log/debug this error
    // one that you should be aware of is e.number == -2146697208,
    // which occurs when the 'Languages...' setting in IE is empty.
    log('XMLHttpSend failed ' + e.toString() + '<br>' + e.stack);
    throw e;
  }
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/message.js
// Copyright 2004-2006 Google Inc.
// All Rights Reserved.
//
// msamuel@google.com


// Provides functions for sending messages back to the server

/** a serial number assigned to messages used to correlate log messages that are
  * written when the message is sent with messages generated when a response is
  * received.
  * @private
  */
var goo_msg_id_counter = 0;

if ('undefined' == typeof log) {
  log = function() { };
}

/** Helper function to get the status of a request object */
function Goo_GetStatus(req) {
  var status = -1;
  try {
    status = req.status;
  } catch (ex) {
    // firefox may throw an exception when you access request values
  }
  return status;
}

/** Helper function to get the status text of a request object */
function Goo_GetStatusText(req) {
  var status = null;
  try {
    status = req.statusText;
  } catch (ex) {
    // firefox may throw an exception when you access request values
  }
  return status;
}

/** callback called when a response is received.
  * @private
  */
function Goo_HandleResponse(req, msg_id, sendTime, handler) {
  if (req.readyState == XML_READY_STATE_COMPLETED) {
    var process = true;
    if (handler) {
      try {
        // compare to false so that functions without a return value will not
        // skip processing.  The result of a non-returning function is
        // undefined.
        process = !(false === (handler)(req));
      } catch (e) {
        log('Message (' + msg_id + ') handling failed: ' + e);
        throw e;
      }
    }

    var status = Goo_GetStatus(req);

    if (200 === status) {  // 200 is HTTP response OK
      log('Message (' + msg_id + ') received after ' +
          (new Date().getTime() - sendTime) + ' ms');
      try {
        var start = new Date().getTime();
        if (process && req.responseText.length) {
          eval(req.responseText);  // eval result unused
        }
        log('Message (' + msg_id + ') processing took ' +
            (new Date().getTime() - start) + ' ms');
      } catch (e) {
        log('Message (' + msg_id + ') processing failed: ' + e);
        alert(e + '\n' + e.stack + '\n\n' + req.responseText);
        throw e;
      }
    } else if (204 == status) {  // 204 is No Content
      log('Message (' + msg_id + ') received after ' +
          (new Date().getTime() - sendTime) + ' ms');
    } else {  // handle error codes and redirects
      log('Message (' + msg_id + ') failed with response ' +
          status + ' ' + Goo_GetStatusText(req) + ' after ' +
          (new Date().getTime() - sendTime) + ' ms.');
    }
  }
}

/** sends a message to a service.  The result should be javascript which is
  * evaluated in the context of this document.
  *
  * @param service the url to hit.
  * @param params cgi params as an array of strings where even elements are
  *   keys and odd values are elements.
  * @param opt_data the request content or undefined.
  * @param opt_handler undefined, or an callback that should be called with the
  *   response object as it's single argument.  If the handler returns false
  *   then the body content will *not* be evaluated as javascript.
  */
function Goo_SendMessage(service, params, opt_data, opt_handler) {
  var query = '';
  if (params) {
    var delim = '';
    for (var i = 0; i < params.length;) {
      var name = params[i++],
         value = params[i++];
      query += delim + encodeURIComponent(name);
      delim = '&';
      if (null !== value && undefined !== value) {
        query += '=' + encodeURIComponent(value.toString());
      }
    }
  }
  // allocate an id used to correlate log messages
  var msg_id = ++goo_msg_id_counter;

  var transaction = XH_XmlHttpCreate();
  if (!transaction) return false;

  var transactionStart = new Date().getTime();
  var handlerClosure = function() {
    Goo_HandleResponse(transaction, msg_id, transactionStart, opt_handler);
  };
  var sep = (service.indexOf('?') >= 0) ? '&' : '?';
  var url = query.length ? service + sep + query : service;

  var method = opt_data !== undefined ? 'POST' : 'GET';
  var logmsg = url;
  for (var pos = logmsg.length + 1;
       (pos = logmsg.lastIndexOf('&', pos - 1)) >= 0;) {
    logmsg = logmsg.substring(0, pos) + '&amp;' + logmsg.substring(pos + 1);
  }
  log('Message (' + msg_id + ') sent: ' + method + ' <tt>' + logmsg + '</tt>.');

  if (opt_data !== undefined) {
    XH_XmlHttpPOST(transaction, url, opt_data.toString(), handlerClosure);
  } else {
    XH_XmlHttpGET(transaction, url, handlerClosure);
  }
}

/** posts a message to a service.  The result should be javascript which is
  * evaluated in the context of this document.
  *
  * @param service the url to hit.
  * @param params cgi params as an array of strings where even elements are
  *   keys and odd values are elements.
  * @param opt_handler undefined, or an callback that should be called with the
  *   response object as it's single argument.  If the handler returns false
  *   then the body content will *not* be evaluated as javascript.
  */
function Goo_PostMessage(service, params, opt_handler) {
  var query = '';
  if (params) {
    var delim = '';
    for (var i = 0; i < params.length;) {
      var name = params[i++],
         value = params[i++];
      query += delim + encodeURIComponent(name);
      delim = '&';
      if (null !== value && undefined !== value) {
        query += '=' + encodeURIComponent(value.toString());
      }
    }
  }
  // allocate an id used to correlate log messages
  var msg_id = ++goo_msg_id_counter;

  var transaction = XH_XmlHttpCreate();
  var transactionStart = new Date().getTime();
  var handlerClosure = function() {
    Goo_HandleResponse(transaction, msg_id, transactionStart, opt_handler);
  };

  var logmsg = service;
  for (var pos = logmsg.length + 1;
       (pos = logmsg.lastIndexOf('&', pos - 1)) >= 0;) {
    logmsg = logmsg.substring(0, pos) + '&amp;' + logmsg.substring(pos + 1);
  }
  log('Message (' + msg_id + ') sent: POST <tt>' + logmsg + '</tt>.');

  // XH_XmlHttpPost automatically sets content type to
  // application/x-www-form-urlencoded
  XH_XmlHttpPOST(transaction, service, query, handlerClosure);
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/spellcheck.js
// Copyright 2005-2006, Google Inc.
// All Rights Reserved.

/**
 * Blogger embedded spellchecking.
 * Uses the Blsp Object and prefix --  Blogger Spellcheck.
 * This code is called from EditPost.gxp.
 * When the user click spellcheck, sends text back to
 * the server for spell checking, and then parses the reply and highlights the
 * errors. It also handles the removal of highlights and the click handling of
 * clicking on an error to show replacements.
 *
 * Has only been tested with FireFox 1.5 and IE.
 *
 * Be sure to include JsConstants before including this file as it includes
 * translated messages for this feature.
 *
 * The following are dependencies outside of the normal ones for Blogger's
 * richedit framework:
 *
 * TODO (thuang): all these functions should move into the Blsp object.
 *
 * Uses java/blogger/b2/staticresources/html/app/scripts/common.js
 *
 * @author johanges@google.com
 * @author thuang (Tina Huang)
 */

/** Spellchecker object */
var Blsp = new Object();

/** Array containing current errors */
Blsp.errors = [];

/** Offset in mistakes array to the first suggestion */
Blsp.SUGGESTION_OFFSET = 2;

/** The URI to call */
Blsp.SPELLCHECK_URI = '/spellcheck.do';

/** Special class used to tag SPANs used by the spellchecker */
Blsp.SPELLING_ERROR_CLASS = 'blsp-spelling-error';

/** Special class used to tag SPANs formerly used by the spellchecker */
Blsp.SPELLING_CORRECTED_CLASS = 'blsp-spelling-corrected';

/** Message flash delay in milliseconds */
Blsp.MESSAGE_FLASH_DELAY = 3000;

/**
 * Selector used to get the highlight spans.
 */
Blsp.highlightSelector = new Object();
Blsp.highlightSelector.select = function(node) {
   return HasClass(node, Blsp.SPELLING_ERROR_CLASS)
            || HasClass(node, Blsp.SPELLING_CORRECTED_CLASS);
};

/**
 * Used to pull out the rich edit iframe for setting the iframe event.
 * TODO (thuang): don't sniff the browser
 */
function BLOG_getRichEditIFrame() {
  return (Detect.IE()) ? getIFrame(RichEdit.frameId) : RichEdit.frame;
}

/**
 * Initializes spans used for showing info while spellchecking
 */
function BLOG_addSpellcheckHtml() {
  Blsp.spellcheckMessage = d('spellcheckMessage');

  if (Blsp.spellcheckMessage == null) {
    var div = document.createElement('div');
    div.id = 'recover';
    d('richbars').appendChild(div);

    Blsp.spellcheckMessage = document.createElement('span');
    Blsp.spellcheckMessage.id = 'spellcheckMessage';
    var div = d('recover');
    div.appendChild(Blsp.spellcheckMessage);
  }

  var iframedoc = RichEdit.frameDoc;
  if (iframedoc) {
    Blsp.editorBody = iframedoc.body;

    // Load the CSS stylesheet into the iframe for compose mode.
    var spellStyleElement = iframedoc.createElement('link');
    spellStyleElement.setAttribute('rel', 'stylesheet');
    spellStyleElement.setAttribute('type', 'text/css');
    var css = blogRTL ? '/css/spellcheck_rtl.css' : '/css/spellcheck.css';
    spellStyleElement.setAttribute('href', css);
    var spellDocHead = iframedoc.getElementsByTagName('head')[0];
    spellDocHead.appendChild(spellStyleElement);
  }
}

/**
 * During spellcheck, set the handler to the spelling keypress handler.
 * TODO (thuang): this code here just stacks event handlers in Firefox.
 * We should have the RichEdit install handlers and have its own way
 * to plug in additional handlers.
 */
function BLOG_setKeypressHandler() {
  var iframe = BLOG_getRichEditIFrame();
  var keyhandler = Blsp.running ? BLOG_keypressHandler
                                : RichEdit.activateKeyCommands;
  // the key codes aren't captured correctly for IE if called during keypress
  setIFrameEvent(iframe, 'keydown', keyhandler);
}

/**
 * Show the noMisspellings text for a few seconds
 */
function BLOG_noErrors() {
  Blsp.spellcheckMessage.innerHTML = no_misspellings_found;
  setTimeout(function() {
               Blsp.spellcheckMessage.innerHTML = '';
             }, Blsp.MESSAGE_FLASH_DELAY);
  Blsp.running = false;
}

/**
 * Remove the menu if it is there.
 */
function BLOG_clearMenu() {
  if (Blsp.menu != null) {
    Blsp.menu.parentNode.removeChild(Blsp.menu);
    Blsp.menu = null;
  }
}

/**
 * Create a div popup menu from a list of words.
 * @param mistake    the array associated with the misspelled word
 *                   [misspelled word, offset, suggestion1,...].
 * @param target     highlight span for the misspelled word.
 */
function BLOG_showMenu(mistake, target) {
  Blsp.menu = document.createElement('div');
  Blsp.menu.id = 'spell_menu';
  if (mistake.length > Blsp.SUGGESTION_OFFSET) {
    for (var i = Blsp.SUGGESTION_OFFSET; i < mistake.length; i++) {
      BLOG_addMenuItem(Blsp.menu, mistake[i], target);
    }
  } else {
    BLOG_addMenuItem(Blsp.menu, no_suggestions, null);
  }

  var menuContainer;
  if (RichEdit.mode == RichEdit.DESIGN_MODE) {
    var iframe = BLOG_getRichEditIFrame();
    var iframedoc = getIFrameDocument(iframe);

    menuContainer = d('RichEdit');
    Blsp.menu.style.top = (GetPageOffsetTop(target) + target.offsetHeight
        + GetPageOffsetTop(d('richeditorframe'))
        - iframedoc.body.scrollTop) + 'px';
    Blsp.menu.style.left = (GetPageOffsetLeft(target)
                           + GetPageOffsetLeft(menuContainer)) + 'px';
  } else if (RichEdit.mode == RichEdit.HTML_MODE) {
    menuContainer = d('body');
    Blsp.menu.style.top = (GetPageOffsetTop(target) + target.offsetHeight
                           - Blsp.spellarea.scrollTop) + 'px';
    Blsp.menu.style.left = GetPageOffsetLeft(target) + 'px';
  }

  menuContainer.appendChild(Blsp.menu);

  return Blsp.menu;
}

/**
 * Add a suggestion and target to the menu.
 */
// TODO (thuang): break the dom/js cycle
function BLOG_addMenuItem(menu, word, target) {
  var item = document.createElement('span');
  item.innerHTML = word;
  item.className = 'spell_menu_item';
  if (target != null) {
    item.onclick = function() {
        target.innerHTML = word;
        BLOG_clearNodeHighlight(target);
      };
  }
  menu.appendChild(item);
  return item;
}


/**
 * Run the spell checker.
 * Get the text to check.
 * Send it to the server.
 * Get the data back.
 * Highlight the errors.
 */
function BLOG_spellcheck() {
  // Check to see if we are already in the middle of a spell check.
  // We don't want to allow the user to call the spellcheck twice on
  // accident as it leads to a really ugly flash.  There will be a
  // separate button in the UI to "recheck spelling".
  // If we are rechecking, we need to call finishSpellcheck first
  // before we recheck to clean out encoding problems.
  if (!Blsp.running) {
    BLOG_clearMenu();
    BLOG_checkText();
    Blsp.running = true;
  }
}

/**
 * Grab the text, submit it for spellchecking update the display.<p>
 * The callback from the server contains either a call to BLOG_selectErrors()
 * with an array of arrays where the inner arrays each represent a mistake
 * and correction suggestions.  Something like this:
 * <pre>
 *    BLOG_selectErrors([['foo',0,'a','b','c','d']]);
 * </pre>
 * or -- if there are no errors -- a simple call to show the no-errors message.
 * <pre>
 *    BLOG_noErrors();
 * </pre>
 */
function BLOG_checkText() {
  if (RichEdit.mode == RichEdit.DESIGN_MODE && Blsp.editorBody) {
    Blsp.text = Blsp.editorBody.innerHTML;
  } else if (RichEdit.mode == RichEdit.HTML_MODE) {
    // We HtmlEscape since we are putting the text into a div and want
    // to display the HTML instead of have it interpreted.
    Blsp.text = HtmlEscape(getElement(Preview.TEXTAREA).value);
    Blsp.text = HtmlifyNewlines(Blsp.text);
  }

  // NOTE(johanges): If we want to hook up a notification like 'Checking...'
  // while the server is working, this is the place to do it. Then cancel
  // the 'Checking...' display inside the callback below.
  // TODO (thuang): lock the editing while spellchecking.

  Goo_SendMessage(Blsp.SPELLCHECK_URI, null, Blsp.text, null);
}

/**
 * Highlights spelling errors in the text. The list of words to be checked is
 * passed as an array of mistakes (where mistakes are in turn
 * represented as an array). It highlights each error with a span and
 * attachers an onclick listener to it.
 */
function BLOG_selectErrors(errors) {
  BLOG_setKeypressHandler();
  for (var i = errors.length - 1; i >= 0; --i) {
    var err = errors[i];
    var word = err[0];
    var offset = err[1];
    var mistakeID = 'SPELLING_ERROR_' + i;

    // TODO (thuang): optimize such that we only tear up the text area once.
    // The text before the change
    var before = Blsp.text.substring(0, offset);
    // The text after the change
    var after = Blsp.text.substring(offset + word.length);
    // The suspect text itself
    var suspect = Blsp.text.substring(offset, offset + word.length);

    // Splice it back together
    Blsp.text = before + '<span class="'
                       + Blsp.SPELLING_ERROR_CLASS
                       + '" id="' + mistakeID + '">'
                       + suspect
                       + '</span>'
                       + after;
    Blsp.errors[mistakeID] = err;
  }
  if (RichEdit.mode == RichEdit.DESIGN_MODE) {
    Blsp.editorBody.innerHTML = Blsp.text;
  } else if (RichEdit.mode == RichEdit.HTML_MODE) {
    if (!Blsp.spellarea) {
      Blsp.spellarea = document.createElement('div');
      Blsp.spellarea.id = 'spellarea';
      Blsp.spellarea.style.display = 'none';
      RichEdit.textarea.parentNode.insertBefore(Blsp.spellarea,
          RichEdit.textarea);
      Blsp.spellarea.style.position = 'static';
      Blsp.spellarea.onclick = BLOG_clickEventHandler;
    }

    Blsp.spellarea.style.height = d('textarea').style.height;
    Blsp.spellarea.innerHTML = Blsp.text;
    showElement(Blsp.spellarea);
    hideElement(RichEdit.textarea);
  }

  // Display the done button.
  Blsp.spellcheckMessage.innerHTML =
    (RichEdit.mode == RichEdit.DESIGN_MODE) ? done_spellchecking
                                             : resume_editing;
  Blsp.spellcheckMessage.onclick = function() {
    BLOG_finishSpellcheck();
  }
}

/**
 * Handler for clicking on a spelling error.
 * Should display the spelling correction menu.
 */
function BLOG_spellClickHandler(target) {
  BLOG_clearMenu();
  if (HasClass(target, Blsp.SPELLING_ERROR_CLASS)) {
    var err = Blsp.errors[target.id];
    if (err != null) {
      BLOG_showMenu(err, target);
    }
  }
}

/**
 * If you start typing while spellchecking in the compose
 * mode, make the menu disappear.
 */
function BLOG_keypressHandler(event) {
  BLOG_clearMenu();
}

/**
 * Clear the highlight off a node when it changes
 */
function BLOG_clearNodeHighlight(node) {
  BLOG_clearMenu();
  node.className = Blsp.SPELLING_CORRECTED_CLASS;
  node.onclick = null;
}

/**
 * Close the menu, clean out highlight spans, and
 * revert the html compose mode back to normal.
 */
function BLOG_finishSpellcheck() {
  if (Blsp.running) {
    BLOG_clearMenu();
    BLOG_setKeypressHandler();
    Blsp.spellcheckMessage.innerHTML = '';
    Blsp.spellcheckMessage.onclick = '';
    if (RichEdit.mode == RichEdit.DESIGN_MODE && Blsp.editorBody) {
      BLOG_removeHighlights(Blsp.editorBody);
    } else if (RichEdit.mode == RichEdit.HTML_MODE) {
      if (Blsp.spellarea) {
        BLOG_removeHighlights(Blsp.spellarea);
        var textareaValue = HtmlUnescape(Blsp.spellarea.innerHTML);
        textareaValue = textareaValue.replace(/<br>/gi, '\n');

        RichEdit.textarea.value = textareaValue;
        // Flip back to showing the HTML editor
        showElement(RichEdit.textarea);
        hideElement(Blsp.spellarea);
      }
    }
    Blsp.running = false;
  }
}

/**
 * Remove all highlight spans.
 */
function BLOG_removeHighlights(parent) {
  if (parent != null) {
    var nodes = GetElementsBySelector(parent, Blsp.highlightSelector);
    for (i = 0; i < nodes.length; i++) {
      var n = nodes[i];
      for (var child = n.firstChild; child != null; child = n.firstChild) {
        n.parentNode.insertBefore(child, n);
      }
      n.parentNode.removeChild(n);
    }
  }
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/post.video.js
/**
 * post.video.js
 *
 * Handles blogger uploaded video objects inside the post editor.  When the
 * posting form is first loaded, we extract out all the video objects and
 * store them in an associative array based on the contentId.  This is the
 * object that is shown in the Edit HTML mode as well as what is written out
 * in the post.
 *
 * The video can be in two states when in Compose mode.  The first is a
 * thumbnail that when clicked, turns into an iframe pointing to an embedded
 * video player to preview the movie.  When the user clicks outside of this
 * iframe, the iframe turns back into the thumbnail preview, which can be
 * dragged around in the posting form.
 *
 * Copyright 2007 Google. All rights reserved.
 *
 * @author thuang (Tina Huang)
 */

var BLOG_THUMBNAIL_CLASS = 'BLOG_thumbnail_class';
var BLOG_VIDEO_ID_PREFIX = 'BLOG_video-';
var BLOG_VIDEO_CLASS = 'BLOG_video_class';
var BLOG_PREVIEW_CLASS = 'BLOG_video_preview';

/**
 * When in compose mode, if the user clicks on the preview video logo,
 * substitute out the video thumbnail with an iframe containing the video
 * flash player.
 */
function BLOG_videoThumbnailClickHandler(el, e) {
  if (BLOG_currentVideoUploadStatus != BLOG_VideoUploadStatus.NONE) {
    // Don't do anything if we are uploading or processing a video.
    return;
  }
  if (hasClass(el, BLOG_THUMBNAIL_CLASS)) {
    // check to see if you are clicking the bottom left corner
    // of the image.

    var elVertOffset = GetPageOffsetTop(el) - RichEdit.frameDoc.body.scrollTop;
    var elHorzOffset = GetPageOffsetLeft(el);

    // The following variables represent the position of the center arrow that
    // the user needs to click to preview.
    // TODO (thuang): input real values here once we finalize the art work.
    var buttonTop = elVertOffset + (.3 * el.height);
    var buttonBottom = elVertOffset + (.7 * el.height);
    var buttonLeft = elHorzOffset + (.4 * el.width);
    var buttonRight = elHorzOffset + (.7 * el.width);
    if ((e.clientY > buttonTop) && (e.clientY < buttonBottom)
       && (e.clientX > buttonLeft) && (e.clientX < buttonRight)) {
       BLOG_replaceObjectWithIframe(el);
    }
  } else {
    BLOG_removePreviewIframes(RichEdit.frameDoc);
  }
}

/**
 * Replace all blogger video expansion objects with iframe previews
 */
function BLOG_replaceAllExpansionsWithIframes(doc) {
  var expansions = doc.getElementsByTagName('object');
  for (var i = 0; i < expansions.length; i++) {
    var contentId = expansions[i].id.replace(BLOG_VIDEO_ID_PREFIX, '');
    if (hasClass(expansions[i], BLOG_VIDEO_CLASS) &&
        (contentId != 'UPLOADING') &&
        (contentId != 'FAILED')) {
      BLOG_replaceObjectWithIframe(expansions[i]);
      // We need to decrement the counter since getElementsByTagName
      // returns a live array and we are removing one of the elements.
      i--;
    }
  }
}

function BLOG_replaceObjectWithIframe(obj) {
  var doc = obj.ownerDocument;
  if (doc == RichEdit.frameDoc) setDesignMode('off');
  var preview = doc.createElement('iframe');
  var contentId = obj.id.replace(BLOG_VIDEO_ID_PREFIX, '');
  var objType = obj.nodeName;
  var height;
  var width;
  if (objType.toLowerCase() == 'img') {
    height = obj.clientHeight;
    width = obj.clientWidth;
  } else {
    height = obj.getAttribute('height');
    width = obj.getAttribute('width');
  }
  preview.src = '/video-preview.g?contentId=' + contentId;
  preview.src += '&width=' + width
              + '&height=' + height;
  preview.style.height = height + 'px';
  preview.style.width = width + 'px';
  preview.id = obj.id;
  preview.style.display = obj.style.display;
  preview.style.margin = obj.style.margin;
  preview.style.padding = obj.style.padding;
  preview.frameBorder = '0';
  preview.scrolling = 'no';
  preview.className = BLOG_PREVIEW_CLASS;
  obj.parentNode.replaceChild(preview, obj);
  if (doc == RichEdit.frameDoc) setDesignMode('off');
}

/**
 * Strip out any video preview iframes and replace with the correct thumbnail
 * preview.
 */
function BLOG_removePreviewIframes(doc) {
  var iframes = doc.getElementsByTagName('iframe');
  for (var i = 0; i < iframes.length; i++) {
    if (hasClass(iframes[i], BLOG_PREVIEW_CLASS)) {
      BLOG_replacePreviewWithImg(iframes[i]);
      // We need to decrement the counter since getElementsByTagName
      // returns a live array and we are removing one of the elements.
      i--;
    }
  }
}

/**
 * Replace the video thumbnails with the source object to be shown
 * in editHTML mode.
 */
function BLOG_replaceAllImgsWithVideos(doc) {
  // first remove any preview iframes that we might have.
  BLOG_removePreviewIframes(doc);
  var imgs = doc.getElementsByTagName('img');
  for (var i = 0; i < imgs.length; i++) {
    var imgElement = imgs[i];
    if (hasClass(imgElement, BLOG_THUMBNAIL_CLASS)) {
      var contentId = imgElement.id.replace(BLOG_VIDEO_ID_PREFIX, '');
      var video = BLOG_getVideoObj(contentId);
      if (video) {
        var height = imgElement.style.height.replace(/px/, '');
        var width = imgElement.style.width.replace(/px/, '');
        video.setAttribute('height', height);
        video.setAttribute('width', width);
        imgElement.parentNode.replaceChild(video, imgElement);
        // We need to decrement the counter since getElementsByTagName
        // returns a live array and we are removing one of the elements.
        i--;
      }
    }
  }
}

/**
 * Replace all occurences of Blogger videos with thumbnail previews that can
 * be better handled by the compose mode.
 */
function BLOG_replaceAllVideosWithImgs() {
  var doc = RichEdit.frameDoc;
  var objs = doc.getElementsByTagName('object');
  for (var i = 0; i < objs.length; i++) {
    if (hasClass(objs[i], BLOG_VIDEO_CLASS)) {
      BLOG_replaceVideoWithImg(objs[i], doc);
      // We need to decrement the counter since getElementsByTagName
      // returns a live array and we are removing one of the elements.
      i--;
    }
  }
}

function BLOG_replaceVideoWithImg(vid, doc) {
  var preview = doc.createElement('img');
  var contentId = vid.id.replace(BLOG_VIDEO_ID_PREFIX, '');

  // The mouseup event is only used for IE since in FF, the onclick
  // event on the iframe traps the event before this is called.
  preview.onmouseup = function() {
    var event = getAvailableEventForIE();
    var target = event.srcElement;
    BLOG_videoThumbnailClickHandler(target, event);
  };

  // We add this random number at the end in case the browser
  // is caching the processing image.
  preview.src = '/video-thumbnail.g?contentId=' + contentId
      + '&zx=' + Math.random();
  var vidType = vid.nodeName;
  if (vidType.toLowerCase() == 'iframe') {
    preview.style.height = vid.style.height;
    preview.style.width = vid.style.width;
  } else {
    preview.style.height = vid.getAttribute('height') + 'px';
    preview.style.width = vid.getAttribute('width') + 'px';
  }
  preview.id = vid.id;
  preview.style.display = vid.style.display;
  preview.style.margin = vid.style.margin;
  preview.className = BLOG_THUMBNAIL_CLASS;
  vid.parentNode.replaceChild(preview, vid);
}

/**
 * Replace the embedded flash player with the thumbnail preview.
 */
function BLOG_replacePreviewWithImg(preview) {
  setDesignMode('off');
  BLOG_replaceVideoWithImg(preview, RichEdit.frameDoc);
  setDesignMode('on');
}

function setDesignMode(val) {
  // We need to toggle the design mode when adding/removing iframes from the
  // editor to work around some FF bug where iframes aren't drawn when
  // designMode is on.
  // setDesignMode("on") will fail in FF if we're not in DESIGN_MODE.
  if (!Detect.IE() && (RichEdit.mode == RichEdit.DESIGN_MODE)) {
    RichEdit.frameDoc.designMode = val;
  }
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/video.js
// Copyright 2007 Google Inc.
// All Rights Reserved.

/**
 * @fileoverview Handles the uploading of a video in the Blogger post editor.
 * This includes validating the upload form, inserting the preview stub, and
 * checking the status of a video while it is processing.  There are two
 * states that a video can be in.  One is "uploading", where the file is
 * actually being sent to Blogger, and the other is "processing", where the
 * video is being transcoded and replicated. Requires the use of
 * modaldialog.js, dom.common.js, and post.video.js.
 * @author thuang (Tina Huang)
 * @supported Firefox, IE/Win 5+.
 */

/**
 * The various states a video upload can be in.
 * @enum {number}
 */
var BLOG_VideoUploadStatus = {

  /**
   * No video upload action is happening.
   */
  NONE: 0,

  /**
   * The video is being uploaded to our servers.
   */
  UPLOADING: 1,

  /**
   * The video is being processed by CDB.  At this pont, it is
   * safe for the user to leave blogger.
   */
  PROCESSING: 2
};

/**
 * Current status of video uploading or processing in the editor.
 * @type BLOG_VideoUploadStatus
 */
var BLOG_currentVideoUploadStatus = BLOG_VideoUploadStatus.NONE;

/**
 * The contentId of the video currently being uploaded or processed.  If no
 * video is uploading/processing, the value should be null.
 * @type String
 */
var BLOG_currentProcessingContentId = null;

/**
 * The url for the video status servlet.  Used to periodically check the
 * status of a video with a given contentId.
 * @type String
 */
var VIDEO_STATUS_URL = '/video-status.g';

/**
 * The url for serving up the thumbnails of videos used in the compose mode.
 * @type String
 */
var VIDEO_PREVIEW_URL = '/video-thumbnail.g';

/**
 * Timer used to periodically check the status of a video upload.
 * @type Object
 */
var BLOG_videoStatusTimer = null;

/**
 * Default width of a video upload.
 * @type Number
 */
var BLOG_defaultVideoWidth = 320;

/**
 * Default height of a video upload.
 * @type Number
 */
var BLOG_defaultVideoHeight = 266;

/**
 * Image and tooltip information for the video upload icon in the toolbar.
 * add_video_tt and unable_add_video_tt are defined JsConstants.gxp, fall back
 * to '' if they aren't present. For example, this info might be referenced from
 * within an iframe (such as the rich text editor).
 * @enum {@enum {String}}
 */
var BLOG_VideoUploadIcon = {
  ENABLED: {
    IMGSRC: 'http://img1.blogblog.com/img/gl.video.gif',
    TOOLTIP: typeof(add_video_tt) != 'undefined' ? add_video_tt : ''
  },
  DISABLED: {
    IMGSRC: 'http://img2.blogblog.com/img/gl.video.disabled.gif',
    TOOLTIP: typeof(unable_add_video_tt) != 'undefined' ?
        unable_add_video_tt : ''
  }
};

/**
 * Ids of various elements used for video upload.
 * @enum {String}
 */
var BLOG_VideoUploadElements = {
  IFRAME: 'uploaderiframeform',
  DIALOG: 'dialogBox',
  STATUS: 'videoStatus',
  UPLOADING_TEXT: 'videoUploadingText',
  PROCESSING_TEXT: 'videoProcessingText',
  HTML_VIDEO_BTN: 'htmlbar_Add_Video',
  RICHEDIT_VIDEO_BTN: 'formatbar_Add_Video',
  POST_LINK: 'postListLink',  // Link to return to the posts page.
  PUBLISH_BTN: 'publishButton',
  SAVE_BTN: 'saveButton',
  PROGRESS_TEXT: 'videoUploadProgress',
  TITLE_ERROR: 'video-no-title',
  FILE_ERROR: 'video-no-file',
  TOS_ERROR: 'video-no-tos',
  VIDEO_FORM_TITLE: 'videoUploadTitle',
  VIDEO_FORM_TOS: 'videoUploadTos'
};

/**
 * Used for checking progress of the upload.
 */
var BLOG_videoUploadId = null;


/**
 * The video-thumbnail.g servlet knows to render the stub video thumbnail
 * if the contentId is UPLOADING.  Otherwise, it fetches the correct thumbnail
 * from Google Video.
 */
var BLOG_videoUploadingPlaceholderContentId = 'UPLOADING';


/**
 * This method gets called when the user hits the upload button in the video
 * upload dialog.  It validates the form elements, and if everything is valid,
 * it copies over the input elements into the iframe and submits the iframe
 * that contains the actual video file.
 */
function BLOG_uploadVideo() {
  BLOG_clearOldFormErrors();

  if (!BLOG_validateVideoForm()) {
    return;
  }

  var dialogBox = document.getElementById(BLOG_VideoUploadElements.DIALOG);
  var iframe = dialogBox.getElementsByTagName('iframe')[0];
  var iframeDoc = iframe.contentDocument ? iframe.contentDocument :
                  iframe.contentWindow.document;
  var iframeForm = iframeDoc.getElementById(BLOG_VideoUploadElements.IFRAME);
  var inputs = dialogBox.getElementsByTagName('input');
  var length = inputs.length;

  // So that we can perform the actual file upload asynchronously, once we
  // have validated the form inputs, we create hidden form elements in the
  // iframe that the file input element is in before submitting that form.
  // We then hide the div that contains the iframe, allowing the user to
  // continue editing their post.
  for (var i = 0; i < length; i++) {
    var newInput = iframeDoc.createElement('input');
    newInput.name = inputs[i].name;

    // Creating a checkbox via javascript doesn't seem to work properly
    // in the form input, so we just create a hidden form element since
    // this is never actually manipulated by the user.
    if (inputs[i].type == 'checkbox') {
      newInput.type = 'hidden';
      newInput.value = 'true';
    } else {
      newInput.value = inputs[i].value;
      newInput.type = inputs[i].type;
    }

    iframeForm.appendChild(newInput);
  }

  // Put in the upload tracking param as a query parameter.
  // We append the current system time to the blogId to unique the tracking id.
  var date = new Date();
  BLOG_videoUploadId = blogId + '-' + date.getTime();
  iframeForm.action = iframeForm.action + '?progressId=' + BLOG_videoUploadId;

  iframeForm.submit();

  BLOG_startVideoStatusTimer();

  // Remove the click trapping div and hide the modal dialog.
  // We pass in false so that the iframe isn't removed, which would kill
  // the upload in progress.
  BLOG_cancelDialog(false);

  // show the video upload status
  var status = document.getElementById(BLOG_VideoUploadElements.STATUS);
  var uploadingText =
      document.getElementById(BLOG_VideoUploadElements.UPLOADING_TEXT);
  var processingText =
      document.getElementById(BLOG_VideoUploadElements.PROCESSING_TEXT);
  uploadingText.style.display = 'block';
  processingText.style.display = 'none';
  status.style.display = 'block';

  BLOG_insertVideo(BLOG_videoUploadingPlaceholderContentId);

  BLOG_setVideoUploadInProgress(BLOG_VideoUploadStatus.UPLOADING);
}

/**
 * Enables or disables UI elements that are not valid while video uploading
 * is in progress.
 * @param {BLOG_VideoUploadStatus} uploadStatus Current video uploading status.
 */
function BLOG_setVideoUploadInProgress(uploadStatus) {
  BLOG_currentVideoUploadStatus = uploadStatus;

  // Disable the video upload icon until video uploading and processing are
  // done.  We only allow one video upload at a time to simplify things.
  var icon = uploadStatus > BLOG_VideoUploadStatus.NONE ?
             BLOG_VideoUploadIcon.DISABLED : BLOG_VideoUploadIcon.ENABLED;
  var videoButton =
      document.getElementById(BLOG_VideoUploadElements.RICHEDIT_VIDEO_BTN);
  var htmlVideoButton =
      document.getElementById(BLOG_VideoUploadElements.HTML_VIDEO_BTN);
  videoButton.childNodes[0].src = icon.IMGSRC;
  videoButton.childNodes[0].title = icon.TOOLTIP;
  htmlVideoButton.childNodes[0].src = icon.IMGSRC;
  htmlVideoButton.childNodes[0].title = icon.TOOLTIP;

  // Disable the return to post link/save button for uploading state only.
  var link = document.getElementById(BLOG_VideoUploadElements.POST_LINK);
  var saveButton = document.getElementById(BLOG_VideoUploadElements.SAVE_BTN);
  if (uploadStatus == BLOG_VideoUploadStatus.UPLOADING && link) {
    // If uploading a video, don't allow the user to return to posts page.
    link.style.display = 'none';
  } else if (uploadStatus == BLOG_VideoUploadStatus.UPLOADING && saveButton) {
    // If uploading a video, don't allow the user to return to save as draft.
    addClass(saveButton, 'ubtn-disabled');
  } else if (link) {
    // If we are done uploading, show the posts link.
    link.style.display = 'block';
  } else if (saveButton) {
    // If we are done uploading, allow the user to save as draft.
    removeClass(saveButton, 'ubtn-disabled');
  }

  // Disable the publish button until video uploading and processing are done.
  var publishButton =
      document.getElementById(BLOG_VideoUploadElements.PUBLISH_BTN);
  if (uploadStatus > BLOG_VideoUploadStatus.NONE) {
    addClass(publishButton, 'ubtn-disabled');
  } else {
    removeClass(publishButton, 'ubtn-disabled');
  }
}

/**
 * Clear out old form errors
 */
function BLOG_clearOldFormErrors() {
  hideElement(document.getElementById(BLOG_VideoUploadElements.TOS_ERROR));
  hideElement(document.getElementById(BLOG_VideoUploadElements.FILE_ERROR));
  hideElement(document.getElementById(BLOG_VideoUploadElements.TITLE_ERROR));
}

/**
 * This method is called either if the form fails javascript validation or if
 * there is a validation error when the form is submitted, in which case, the
 * method is called from the VideoUploadDone.gxp.
 */
function BLOG_showFormErrors() {
  BLOG_clearOldFormErrors();
  BLOG_replaceVideo(BLOG_videoUploadingPlaceholderContentId, undefined);
  BLOG_clearVideoUpload();
  BLOG_redisplayDialog();
}

/**
 * Called by VideoUploadDone.gxp to display the error message if no file is
 * selected.
 */
function BLOG_showFileError() {
  showElement(document.getElementById(BLOG_VideoUploadElements.FILE_ERROR));
}

/**
 * Called by VideoUploadDone.gxp to display the error message if no title is
 * selected.  This should have been caught by BLOG_validateVideoForm(), but
 * in case it is not, it is validated by the server.
 */
function BLOG_showTitleError() {
  showElement(document.getElementById(BLOG_VideoUploadElements.TITLE_ERROR));
}

/**
 * Called by VideoUploadDone.gxp to display the error message if tos is
 * accepted.  This should have been caught by BLOG_validateVideoForm(), but
 * in case it is not, it is validated by the server.
 */
function BLOG_showTosError() {
  showElement(document.getElementById(BLOG_VideoUploadElements.TOS_ERROR));
}

/**
 * Return the custom blogger video object html for a video with a given
 * content id.  This object is non-standard, but will be replaced by Post.java
 * with the correct object and embed tags at render time.
 */
function BLOG_getVideoObj(contentId) {
  var frameDoc = RichEdit.frameDoc;
  var videoObjWrapper = frameDoc.createElement('div');
  // We do this set innerHTML stuff b/c IE doesn't like to create objects
  // using the regular document.createElement() stuff.
  videoObjWrapper.innerHTML =
    '<object id="' + BLOG_VIDEO_ID_PREFIX + contentId + '"class="'
    + BLOG_VIDEO_CLASS + '" width="' + BLOG_defaultVideoWidth
    + '" height="' + BLOG_defaultVideoHeight
    + '" contentId="' + contentId + '"/>';
  return videoObj = videoObjWrapper.getElementsByTagName('object')[0];
}

/**
 * Grab the html for the embedded video.
 */
function BLOG_insertVideo(contentId) {
  var frameDoc = RichEdit.frameDoc;
  var videoObj = BLOG_getVideoObj(contentId);

  // If we are in compose mode, insert a preview thumbnail into the text area.
  // Otherwise, if we are in HTML mode, then append the blogger video object
  // html into the textarea.
  if (RichEdit.mode == RichEdit.DESIGN_MODE) {
    var preview = frameDoc.createElement('img');
    preview.src =
        VIDEO_PREVIEW_URL + '?contentId=' + contentId;
    preview.style.height = BLOG_defaultVideoHeight + 'px';
    preview.style.width = BLOG_defaultVideoWidth + 'px';
    preview.id = 'BLOG_video-' + contentId;
    preview.className = BLOG_THUMBNAIL_CLASS;
    frameDoc.body.appendChild(preview);
  } else {
    RichEdit.textarea.value =
        RichEdit.textarea.value + videoObj.parentNode.innerHTML;
  }

  BLOG_enlargeEditorWindowForVideo();

  // Restart autosave.
  RichEdit.dirty();
}

/**
 * Resize the edit window to the larger size to accomodate videos.
 */
function BLOG_enlargeEditorWindowForVideo() {
  RichEdit.frame.style.height = '400px';
  RichEdit.textarea.style.height = '400px';
}

/**
 * Removes whitespace surrounding a string.
 */
function BLOG_trimString(str) {
  return str.replace(/^\s*|\s*$/g, '');
}

/**
 * If there is a new element, replace the old element with the new.  Otherwise,
 * remove it.
 */
function BLOG_removeOrReplace(oldEl, opt_newEl) {
  if (opt_newEl) {
    oldEl.parentNode.replaceChild(opt_newEl, oldEl);
  } else {
    oldEl.parentNode.removeChild(oldEl);
  }
}

/**
 * Cancels a video upload.  Removes the status text, preview stub, and the
 * upload iFrame if its still in the upload phase.
 */
function BLOG_cancelVideoUpload(contentId) {
  BLOG_removeVideoUploadDialog();
  BLOG_replaceVideo(contentId, undefined);
  // Remove status text, preview stub, etc.
  BLOG_clearVideoUpload();
}

/**
 * When we finish uploading a video, we first create the new video by grabbing
 * the embedded source for the new video.  Then we make the size of the new
 * video the same as the old one and replace the old video with the new one.
 *
 * If no new video is given, then it just removes the old video (used for
 * cancelling a video upload).
 */
function BLOG_replaceVideo(oldContentId, newContentId) {
  var newVideo;
  var oldVideo;
  if (RichEdit.mode == RichEdit.DESIGN_MODE) {
    var doc = RichEdit.frameDoc;
    newVideo = doc.getElementById(BLOG_VIDEO_ID_PREFIX + newContentId);
    oldVideo = doc.getElementById(BLOG_VIDEO_ID_PREFIX + oldContentId);
  } else {
    var tmp = document.createElement('div');
    tmp.innerHTML = '<div style="display:none;">' +
                    RichEdit.textarea.value + '</div>';
    tmp = tmp.childNodes[0];
    document.body.appendChild(tmp);

    oldVideo = document.getElementById(BLOG_VIDEO_ID_PREFIX + oldContentId);
    newVideo = document.getElementById(BLOG_VIDEO_ID_PREFIX + newContentId);
  }

  if (newVideo) {
    newVideo.style.height = oldVideo.style.height;
    newVideo.style.width = oldVideo.style.width;
  }

  BLOG_removeOrReplace(oldVideo, newVideo);

  if (RichEdit.mode == RichEdit.HTML_MODE) {
    RichEdit.textarea.value = tmp.innerHTML;
    tmp.parentNode.removeChild(tmp);
  }
}

/**
 * Validate the video upload form and display the approprate form error
 * messages.
 *
 * @return true if the form is valid, false if not.
 */
function BLOG_validateVideoForm() {
  var videoTitle = document.getElementById(
      BLOG_VideoUploadElements.VIDEO_FORM_TITLE).value;
  var videoTos = document.getElementById(
      BLOG_VideoUploadElements.VIDEO_FORM_TOS).checked;
  var valid = true;

  if (BLOG_trimString(videoTitle).length == 0) {
    BLOG_showTitleError();
    valid = false;
  }

  if (!videoTos) {
    BLOG_showTosError();
    valid = false;
  }

  return valid;
}

/**
 * This method gets called as soon as the file is done uploading.  At this
 * point, we go ahead and write out the content ID so that it is preserved and
 * the post can be saved as a draft.
 */
function BLOG_finishVideoUpload(contentId, enclosureUrl) {
  // Insert the video pointing to the new content id and then replace the
  // preview one with the new one.
  BLOG_videoUploadId = null;
  BLOG_insertVideo(contentId);
  BLOG_replaceVideo(BLOG_videoUploadingPlaceholderContentId, contentId);
  BLOG_startProcessing(contentId);

  // add enclosure
  // defined in enclosures.js
  if (window.BLOG_newEnclosure && enclosureUrl != null) {
    var inputs = window.BLOG_newEnclosure();
    inputs[0].value = enclosureUrl;
    inputs[1].value = window.MIME_TYPES.mp4;

    RichEdit.dirty();

    window.BLOG_showEnclosures();
  }
}

/**
 * This method gets called if there was a VideoUploadException during the
 * upload.  It removes the video uploading place holder, etc. and displays
 * the failure message.
 */
function BLOG_failedVideoUpload(contentId) {
  if (BLOG_currentProcessingContentId == null) {
    BLOG_replaceVideo(BLOG_videoUploadingPlaceholderContentId, undefined);
  } else {
    BLOG_replaceVideo(contentId, undefined);
  }

  BLOG_clearVideoUpload();

  // Display the video failed dialog.
  var params = ['contentId', contentId, 'blogID', blogId];
  BLOG_retrieveDialog('/video-failed.g', params, RichEdit.editarea, true);

  BLOG_currentProcessingContentId = null;
}

/**
 * Start the timer to check on the video upload status.  This method deponds
 * on BLOG_currentProcessingContentId being set to the currently processing
 * video content id.
 *
 * TODO(thuang): we should be able to take an array of processing videos at
 * some point.
 */
function BLOG_startVideoStatusTimer() {
  BLOG_videoStatusTimer = window.setInterval(BLOG_checkUploadStatus, 10000);
}

/**
 * Send a request for the video status of a given content id.
 */
function BLOG_checkUploadStatus() {
  // TODO(thuang): We should also add a poll here to make sure that the preview
  // stub has not been deleted by the user.  We may want to prompt the user to
  // re-insert the video in case it was deleted by accident..
  var params;
  if (BLOG_currentProcessingContentId == null) {
    params = ['progressTrackerId', BLOG_videoUploadId];
  } else {
    params = ['contentId', BLOG_currentProcessingContentId];
  }

  // We need to add a random parameter here because IE overaggressively
  // caches the respose to this request, which will mean that we will
  // repeatedbly get the same status response back unless we trick IE
  // into thinking that this is a different request.
  params[2] = 'zx';
  params[3] = Math.random();

  try {
    Goo_SendMessage(VIDEO_STATUS_URL, params, undefined, BLOG_statusCallback);
  } catch (e) {
    // Ok to ignore if there was a problem getting the status because it
    // will try again.
  }
}

/**
 * Parse the status and if it the video is done processing, remove the status
 * dialog.
 */
function BLOG_statusCallback(req) {
  if (req.status != 200) {
    BLOG_failedVideoUpload(BLOG_currentProcessingContentId);
    return;
  }

  var doc = req.responseXML;
  if (!doc) {
    BLOG_failedVideoUpload(BLOG_currentProcessingContentId);
    return;
  }

  var root = doc.documentElement;
  var status = root.getAttribute('class');
  if (status == 'videoStatus-success') {
    // TODO(thuang): once we have the delayed publishing mechanism, we can move
    // this to the finishVideoUpload() method.
    BLOG_clearVideoUpload();
    BLOG_removeVideoUploadDialog();

    // We add this random number at the end in case the browser
    // is caching the processing image.  IE overaggressively caches things so
    // it won't reload the image and grab the actual video thumbnail now that
    // the video is done processing.  The VideoUploadThumbnailServlet knows to
    // check the video's status from CDB and fetch the actual thumbnail once
    // processing is successful.
    if (RichEdit.mode == RichEdit.DESIGN_MODE) {
      var thumb = RichEdit.frameDoc.getElementById(
          BLOG_VIDEO_ID_PREFIX + BLOG_currentProcessingContentId);
      thumb.src = thumb.src + '&zx=' + Math.random();
    }
    BLOG_currentProcessingContentId = null;
  } else if (status == 'videoStatus-failed') {
    BLOG_failedVideoUpload(BLOG_currentProcessingContentId);
  }

  // return false to prevent the response from being JS eval'd by
  // Goo_SendMessage's response handler
  return false;
}

/**
 * Stops the uploading of the video by removing the iframe containing
 * the upload.
 */
function BLOG_removeVideoUploadDialog() {
  var dialogBox = document.getElementById(BLOG_VideoUploadElements.DIALOG);
  if (dialogBox) {
    dialogBox.parentNode.removeChild(dialogBox);
  }
}

/**
 * Clears a video upload that is in progress.
 * Unsets the videoInProgress flag, removes the status box,
 * and re-enables publishing buttons.
 */
function BLOG_clearVideoUpload() {
  var statusBox = document.getElementById(BLOG_VideoUploadElements.STATUS);
  statusBox.style.display = 'none';

  BLOG_setVideoUploadInProgress(BLOG_VideoUploadStatus.NONE);
  window.clearInterval(BLOG_videoStatusTimer);
}

/**
 * If the user goes back and edits a post that has a video that is not
 * completed, resume the processing state.
 */
function BLOG_startProcessing(contentId) {
  BLOG_setVideoUploadInProgress(BLOG_VideoUploadStatus.PROCESSING);

  // Show the video upload status
  var status = document.getElementById(BLOG_VideoUploadElements.STATUS);
  status.style.display = 'block';

  // Change the video processing status
  var uploadingText =
      document.getElementById(BLOG_VideoUploadElements.UPLOADING_TEXT);
  var processingText =
      document.getElementById(BLOG_VideoUploadElements.PROCESSING_TEXT);
  uploadingText.style.display = 'none';
  processingText.style.display = 'block';
  processingText.getElementsByTagName('a')[0].onclick =
    function() {
      BLOG_cancelVideoUpload(contentId); return false;
    };

  BLOG_currentProcessingContentId = contentId;

  // Restart the status timer if it's not already running.  Means that this
  // method was called when going back to edit a post that is still processing.
  if (!BLOG_videoStatusTimer) {
    BLOG_startVideoStatusTimer();
  }
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/status.js
/**
 * status.js
 *
 * Object to dynamically update the output from the BloggerStatusHtml template
 * using XMLHttpRequest. Can also be used to trigger callbacks for when Blogger
 * appears to be unreachable due to the network.
 *
 * Copyright 2005 Google. All rights reserved.
 *
 * @author phopkins
 */


function BLOG_Status() {
  this.STATUS_HOLDER_ID = 'bloggerStatusMessageHolder';
  this.STATUS_URL = '/status.g';
  // one hour in ms
  this.CHECK_DELAY = 60 * 60 * 1000;

  this._timer = false;
  this._request = false;

  // This method will be called from callbacks and handlers, so store the
  // object in the callee's "obj" field
  this.checkStatus.obj = this;
}

BLOG_Status.prototype.start = function() {
  this._timer = window.setInterval(this.checkStatus, this.CHECK_DELAY);
};


BLOG_Status.prototype.checkStatus = function() {
  var obj = arguments.callee.obj;

  if (obj._request) {
    obj._request.abort();
    obj._request = false;
  }

  var req;

  if (window.XMLHttpRequest) {
    try { req = new XMLHttpRequest(); }
    catch (e) {}
  } else if (window.ActiveXObject) {
    try { req = new ActiveXObject('Msxml2.XMLHTTP'); }
    catch (e) {
      try { req = new AciveXObject('Microsoft.XMLHTTP'); }
      catch (e) {}
    }
  }

  if (req) {
    obj._request = req;

    req.open('GET', obj.STATUS_URL + '?zx=' +
        Math.round(Math.random() * 1000000000), true);
    req.onreadystatechange = obj._makeReadyStateChangeHandler(req);
    req.send('');
  }
};


BLOG_Status.prototype._makeReadyStateChangeHandler = function(req) {
  var obj = this;

  return function() {
    // if there's a new pending request, don't deal with this one
    if (obj._request != req) return;

    if (req.readyState == 4) {
      // Firefox likes to throw an exception when the server is unreachable
      try {
        if (req.status && req.status == 200) {
          obj._processResponse(req);
        } else {
          obj.showError();
        }
      } catch (e) {
        obj.showError();
      }

      // the request has finished, so remove the record of it
      obj._request = false;
    }
  }
};


/**
 * Given an XMLHttpRequest, make sure that it contains a Blogger status message,
 * rather than a network's interstitial or something.
 */
BLOG_Status.prototype._processResponse = function(req) {
  var txt = req.responseText;

  if (!txt) {
    this.showError();
    return;
  }

  var newMessageHolder = document.createElement('DIV');
  newMessageHolder.innerHTML = txt;

  var newMessage = newMessageHolder.childNodes[0];

  if (newMessage.getAttribute('id') == 'bloggerStatusMessage') {
    var holder = document.getElementById(this.STATUS_HOLDER_ID);

    while (holder.childNodes.length > 0) {
      holder.removeChild(holder.childNodes[0]);
    }

    holder.appendChild(newMessage);

    this.hideError();
  } else {
    this.showError();
  }
};

/**
 * Redefine this function to do something when an error occurs
 */
BLOG_Status.prototype.showError = function() {
};

/**
 * Redefine this function to do something when there is no error
 */
BLOG_Status.prototype.hideError = function() {
};

//java/com/google/blogger/b2/staticresources/html/app/scripts/richedit.common.js
/*
 * Copyright, Google.com - 2004
 * Author: Chris Wetherell
 *
 * Note: The data arrays are based on a WYSIWSYG example by Bay-Wei Chang.
 */


var SHOW_DIRECTIONALITY_BUTTONS = blogRTL || userRTL;

/*
 * RichEditor (object)
 *
 */
function RichEditor()
{

  // ===================================================================
  // Settings
  // ===================================================================

  this.IMAGES_LOCATION = '/img/';
  this.DEFAULT_ALIGNMENT = (blogRTL) ? 'right' : 'left';
  this.EDIT_SOURCE = true; // if false, the mode tabs are never exposed
  this.ENABLE_IFRAME = true; // if false, the IFRAME is never appended
                             // this field is also used to detect if we
                             // in richedit or html mode (true) or
                             // preview only mode (false)
  this.ENABLE_KEYBOARD_CONTROLS = true;
  this.ALLOW_HTML_ENTRY = false;
  this.ALLOW_FULL_PASTE = true;
  this.ALLOW_LINK_ONLY_PASTE = false;
  this.DEBUG = false;
  this.frameBodyStyle = 'border:0;margin:0;padding:3px;width:auto;'
    + 'font:normal 100%/120% Georgia, serif;';
  //above line adds CSS notation to the body of the IFRAME



  // ===================================================================
  // Design Mode Toolbar Data
  // ===================================================================
  // First of each pair is the display name, second is the value.
  // If name ends in asterisk, then it is the default (otherwise first one is
  // default.)
  // If value is null, then it is same as display name.
  // Keep values lowercase for compatibility between IE and Moz.
  this.FONTS = [
    [font, ''],
    ['Arial', 'arial'],
    ['Courier', 'courier new'],
    ['Georgia', 'georgia'],
    ['Lucida Grande', 'lucida grande'],
    ['Times', 'times new roman'],
    ['Trebuchet', 'trebuchet ms'],
    ['Verdana', 'verdana'],
    ['Webdings', 'webdings']
  ];

  this.FONT_SIZES = [
    [huge, '5'],
    [large, '4'],
    [normal_size + '*', '3'],
    [small, '2'],
    [tiny, '1']
  ];

  this.BACKCOLOR = Detect.IE() ? 'BackColor' : 'HiliteColor';

  this.CONTROLS = [];

  this.HTML_CONTROLS = [
    ['Textbar.Bold();', insert_bold, 'gl.bold'],
    ['Textbar.Italic();', insert_italic, 'gl.italic'],
    ['|'],

    ['Textbar.Link();', insert_link, 'gl.link'],
    ['|'],

    ['Textbar.Blockquote();', insert_blockquote, 'gl.quote'],
    ['|']
    ];

  this.MODE_TABS = [];
  if (this.EDIT_SOURCE) {
    this.MODE_TABS.push(['ShowRichEditor', 'RichEdit.ShowRichEditor()',
      compose, 'this.START_MODE == this.DESIGN_MODE']);
    this.MODE_TABS.push(['ShowSourceEditor', 'RichEdit.ShowSourceEditor()',
      edit_html, 'this.START_MODE == this.HTML_MODE']);
  }

  this.DEPRESSABLE = [
    'Bold', 'Italic', 'JustifyLeft', 'ForeColor', this.BACKCOLOR,
    'JustifyCenter', 'JustifyRight', 'InsertOrderedList',
    'InsertUnorderedList', 'CreateLink', 'Indent', 'Outdent', 'Blockquote',
    'BlockDirRTL', 'BlockDirLTR'
  ];

  this.KEY_COMMANDS = [];
  this.KEY_COMMANDS.push(['CTRL_SHFT_A', 'CreateLink', 'CreateLink()']);
  this.KEY_COMMANDS.push(['CTRL_B', 'Bold',
    'RichEdit.frameDoc.execCommand("Bold", false, "")']);
  this.KEY_COMMANDS.push(['CTRL_I', 'Italic',
    'RichEdit.frameDoc.execCommand("Italic", false, "")']);
  this.KEY_COMMANDS.push(['CTRL_Z', null,
    'RichEdit.frameDoc.execCommand("Undo", false, "")']);
  this.KEY_COMMANDS.push(['CTRL_Y', null,
    'RichEdit.frameDoc.execCommand("Redo", false, "")']);
  // support this common key mapping of redo as well
  // TODO: doesn't work for IE
  this.KEY_COMMANDS.push(['CTRL_SHFT_Z', null,
    'RichEdit.frameDoc.execCommand("Redo", false, "")']);


  // ===================================================================
  // Properties
  // ===================================================================
  this.id = 'RichEdit'; //the name of this object within the window
  this.divId = 'RichEdit'; //the name of the container surrounding the textarea
  this.frameId = 'richeditorframe';
  this.debugField;
  this.UNSUPPORTED_MODE = 0;
  this.DESIGN_MODE = 1;
  this.HTML_MODE = 2;
  this.START_MODE = this.DESIGN_MODE;  // start in WYSIWYG mode by default
  this.mode;
  this.showKeyCommands = true;

  // ===================================================================
  // The make() method
  // ===================================================================
  this.make = function() {

    this.div = document.getElementById(this.divId);
    if (!this.div) return;

    // store this user-defined object in a globally accessible
    // variable that is unique to the rich editor object
    eval('window.'+ this.id + ' = this');

    // get the textarea within the rich editor

    this.textarea = this.div.getElementsByTagName('textarea')[0];

    // create palette container

    this.palette = createElementandAppend('div', 'palette');

    // create style container for the bars and editable areas, copy the
    // textarea into this container, and delete the original textarea

    this.editarea = createElementandAppend('div', 'editarea', this.div);
    appendClearObj(this.editarea);


    this.editarea.appendChild(this.textarea);

    // create toolbars container

    this.richbars = createElementandInsertBefore('div', 'richbars',
        this.editarea, this.textarea);
    this.richbars.setAttribute('unselectable', 'on');
    appendClearObj(this.richbars);

    // create Design Mode toolbar

    this.formatbar = this.createToolbar('formatbar',
      this.getRichBarButtonHTML());

    // create HTML Mode toolbar
    if (this.EDIT_SOURCE) {
      this.htmlbar = this.createToolbar('htmlbar', this.getHtmlBarButtonHTML());

      // hide the html bar if started in design mode
      if (this.START_MODE == this.DESIGN_MODE) {
        this.htmlbar.style.display = 'none';
      }

      // set listener for the HTML Source mode
      if (this.ENABLE_KEYBOARD_CONTROLS) {
        if (Detect.IE()) {
          // the key codes aren't captured correctly for IE if called during
          // keypress
          this.textarea.onkeydown = Textbar.activateKeyCommands;
        } else {
          this.textarea.onkeypress = Textbar.activateKeyCommands;
        }
      }

      // Set the textarea to be used for keyboard event capture
      Textbar.Element = this.textarea;
    }

    // hide the rich bar if not started in design mode
    if (this.START_MODE != this.DESIGN_MODE) {
      this.formatbar.style.display = 'none';
    }

    appendClearObj(this.richbars);

    // hide textarea, if started in design mode
    if (this.START_MODE != this.HTML_MODE) {
      this.textarea.style.display = 'none';
    }

    // add editor frame to page
    if (this.ENABLE_IFRAME) {
      var iframe = createElementandAppend('iframe', 'richeditorframe',
        this.editarea);
      if (Detect.OPERA()) iframe.src = 'serverid';

      // get all objects needed to transform the frame

      this.frame = getIFrame(this.frameId);
      this.frameWin = this.frame.contentWindow;
      this.frameDoc = getIFrameDocument(this.frame);

      // turn on rich-text editing for the frame ... for IE
      // (which has to be BEFORE its body is set)

      if (Detect.IE()) this.frameDoc.designMode = 'On';

      // write the body, or else the frame's Document object can't be acted
      // upon.

      setIFrameBody(this.frame, this.frameBodyStyle + 'text-align:'
        + this.DEFAULT_ALIGNMENT);

      // Now, IE needs a different traversal method to affect the IFRAME and
      // BODY and one HTML container to prevent <p> tags from being inserted
      // after each carriage return.
      if (Detect.IE()) {
        this.frame = getElement(this.frameId);
        this.frameDoc = this.frame.contentWindow.document;
        this.frameDoc.body.innerHTML = '<div></div>';
      }

      // set dir if necessary
      if (blogRTL) {
        var htmlElement = this.frameDoc.getElementsByTagName('html')[0];
        htmlElement.setAttribute('dir', 'rtl');
      }

      // add event handling

      //Now, IE needs a different traversal method to set IFRAME events
      var iframe = (Detect.IE()) ? getIFrame(this.frameId) : this.frame;
      setIFrameEvent(iframe, 'click', BLOG_clickEventHandler);

      if (this.ENABLE_KEYBOARD_CONTROLS) {
        if (Detect.IE()) {
          // the key codes aren't captured correctly for IE if called during
          // keypress
          setIFrameEvent(iframe, 'keydown', this.activateKeyCommands);
        } else {
          setIFrameEvent(iframe, 'keypress', this.activateKeyCommands);
        }
      }
      if (trOK) {
        // Adds a dropdown button beside the transliteration button, and all
        // its event listeners.
        _TR_addTranslitStateController();
      }

      if (!this.ALLOW_FULL_PASTE && Detect.IE()) {
        this.frameDoc.body.onbeforepaste = cleanPaste;
      }
      if (this.ALLOW_LINK_ONLY_PASTE && Detect.IE()) {
        this.frameDoc.body.onbeforepaste = smartPaste;
      }

      if (this.APPEND_TO_ACTIONS) {
        for (var x = 0; x < this.APPEND_TO_ACTIONS.length; x++) {
          var prepend = false;
          if (this.APPEND_TO_ACTIONS[x][2]) prepend = true;
          this.appendToAction(this.APPEND_TO_ACTIONS[x][0],
                              this.APPEND_TO_ACTIONS[x][1],
                              prepend);
        }

      }


      // turn on rich-text editing for the frame ... for Mozilla
      // (which has to be AFTER its body is set, and any time after the display
      //  style property is changed.)

      if (!Detect.IE()) this.frameDoc.designMode = 'On';


      // hide iframe, if started in html mode
      if (this.START_MODE != this.DESIGN_MODE) {
        this.frame.style.display = 'none';
      }

    } // end of ENABLE_IFRAME block

    this.makeModeBar();

    // For Debug Mode
    if (this.DEBUG) {
      var debugtitle = createElementandAppend;
        ('div', 'debugtitle', this.editarea);
      debugtitle.innerHTML = 'Debug output:';
      this.debugField = createElementandAppend;
        ('textarea', 'debug', this.editarea);
    }

    //move content from textarea to IFRAME
    if (this.START_MODE == this.DESIGN_MODE) {
      this.mode = this.DESIGN_MODE;
      this.ShowRichEditor();
      // remember to fill IE with a div to prevent double-spacing
      if (Detect.IE() && this.frameDoc.body.innerHTML == '') {
        this.frameDoc.body.innerHTML = '<div></div>';
      }
    } else {
      this.mode = this.HTML_MODE;
    }

    document.close();
  }

  /**
   * Checks to see if autosave is enabled, and then starts up the autosave
   * process.  Starting up the autosave process includes reenabling save button
   * and recurring autosaves.  This method should be called whenever features
   * such as video and image uploading, modify the contents of the posting form.
   */
  this.dirty = function() {
    if (isAutosaveEnabled()) {
      BLOG_autosave.toggleAutosaveButton(true);
      BLOG_autosave.start();
    }
  }

  /**
   * Called as the page is unloading. Removes any form elements that were
   * created with JS, since they will disrupt Firefox's re-populating of the
   * page if the user clicks "Back."
   */
  this.removeGeneratedFormElements = function() {
    var fontMenu = d('FontName');
    if (fontMenu) {
      fontMenu.parentNode.removeChild(fontMenu);
    }

    var inputs = document.getElementsByTagName('INPUT');
    var generatedInputs = [];

    for (var i = 0; i < inputs.length; ++i) {
      if (HasClass(inputs[i], 'generated')) {
        generatedInputs[generatedInputs.length] = inputs[i];
      }
    }

    for (var i = 0; i < generatedInputs.length; ++i) {
      generatedInputs[i].parentNode.removeChild(generatedInputs[i]);
    }
  }

}

/**
 * Returns whether or not autosave is enabled by checking for existence of a
 * BLOG_autosave object.
 * @return {Boolean} Whether or not autosave is enabled.
 */
function isAutosaveEnabled() {
    return window.BLOG_autosave != undefined;
}

/**
 * Returns true if the window has a valid RichEdit object.
 */
function EditorAvailable() {
  // IE 5.01 requires both checks: RichEdit is defined but not
  // valid (and therefore RichEdit.mode is not defined).

  return typeof RichEdit != 'undefined'
         && typeof RichEdit.mode != 'undefined';
}

function CorrectLinkAutoCompletionInImages() {

  if (!Detect.IE()) return; // only IE currently has hyperlink auto-completion

  var parent = getSelectedParentNode();

  // if typed into the WYSIWYG edit area, auto-complete the end of the certain
  // tags and bypass IE's auto-complete feature, so that a link doesn't wind up
  // in the href or src attributes
  // (i.e. <img src="<a href="http://p/">http://p/</a>">)
  try {
    var httpImgPattern = new RegExp('&lt;img[^>]+src="[^"]+"$', 'gi');
    var httpAPattern = new RegExp('&lt;a[^>]+href="[^"]+"$', 'gi');
    var isImg = httpImgPattern.test(parent.innerHTML);
    var isAnchor = httpAPattern.test(parent.innerHTML);
    if (isImg || isAnchor) {
      var event = getRichEditorEventForIE();
      var shiftPressed = isShiftKeyPressed(event);
      var key = getKey(event);

      // auto-complete the end of the tag if the user enters a space after
      // the src attribute
      if (key == 32) {
        var endTag = (isImg) ? ' \/>' : '>';
        parent.innerHTML += endTag;
      }
    }
  } catch (e) {
    RichEdit.addDebugMsg('CorrectLinkAutoCompletionInImages() failed. \n');
    return true;
  }

}

function RemoveLinksWithinTags(html) {
  var httpPattern = new RegExp(
      '&lt;img([^>]+)src=["]*(<a href="[^"]+">)([^<]+)</a>["]*', 'gi');
  if (httpPattern.test(html)) {
    html = html.replace(httpPattern, '&lt;img$1src="$3"');
  }
  var httpPattern = new RegExp(
      '&lt;a([^>]+)href=["]*(<a href="[^"]+">)([^<]+)</a>["]*', 'gi');
  if (httpPattern.test(html)) {
    html = html.replace(httpPattern, '&lt;a$1href="$3"');
  }
  return html;
}

RichEditor.prototype.appendToAction = function(methodName, func, prepend) {
  var strFunc = eval('this.' + methodName).toString();
  if (prepend) {
    var funcPattern = new RegExp('function[ ]?\\(\\) \\{', 'gi');
    var newFunc = strFunc.replace(funcPattern, 'function () {\n' + func + '\n');
  } else {
    var newFunc = strFunc.replace(/}[\n\t\s]*$/g, func + '\n}');
  }
  eval('RichEditor.prototype.' + methodName + ' = ' + newFunc);
};

RichEditor.prototype.makeModeBar = function() {

  if (this.MODE_TABS.length == 0) return;

  this.moderow = createElementandInsertBefore('div', 'modebar', this.div,
    this.editarea);

  if (Detect.IE()) {
    var min = createElementandAppend('div', 'minwidth-mode', this.moderow);
    min.className = 'minwidth';
    min.id = 'minwidth';
  }

  if (!Detect.IE()) appendClearObj(this.moderow);

  for (var i = 0; i < this.MODE_TABS.length; i++) {
    var el = createElementandAppend('span', this.MODE_TABS[i][0], this.moderow);

    eval('el.onclick = function() { if (this.className != "on") ' +
         this.MODE_TABS[i][1] + '}');

    // runs a test to see which mode is active
    if (eval(this.MODE_TABS[i][3])) el.className = 'on';

    el.onmouseover = function() {
      if (this.className != 'on') this.style.backgroundColor = '#fff';
    }

    el.onmouseout = function() {this.style.backgroundColor = '';}

    //prevent the text from being selected
    el.onselectstart = function() {return false;}
    el.onmousedown = function() {return false;}

    el.innerHTML = this.MODE_TABS[i][2];
  }

  if (!Detect.IE()) appendClearObj(this.moderow);

};

/*
  Get all textarea data where it should be for form submission and Firefox form
  caching.

  @param disableTextarea If true, this function will temporarily disable the
  Edit HTML text area. Used to prevent this text area from being included in the
  form POST, where it will only waste bandwidth.
*/
RichEditor.prototype.updateTextareas = function(disableTextarea) {
  if (this.mode == this.DESIGN_MODE) {
    RichEdit.movePostBodyToTextarea();
  }

  return checkMaxChars(this.textarea);
};


RichEditor.prototype.clearDebugMsg = function() {
  if (this.DEBUG) {
    this.debugField.value = '';
  }
};

RichEditor.prototype.addDebugMsg = function(s) {
  if (this.DEBUG) {
    this.debugField.value += (s + '\n\n');
  }
};

RichEditor.prototype.createToolbar = function(strId, html) {
  var bar = createElementandAppend('div', strId, this.richbars);
  bar.setAttribute('unselectable', 'on');
  appendClearObj(bar);
  bar.innerHTML = html;
  appendClearObj(bar);
  return bar;
};

function appendClearObj(obj) {
  var div = document.createElement('div');
  var divStyle = div.style;
  divStyle.clear = 'both';
  obj.appendChild(div);
}


// ===================================================================
// Copy-n-paste
// ===================================================================

var hiddenPaste, currentEditorSelection;

function smartPaste() {
  currentEditorSelection = GetRange();
  hiddenPaste = createElementandAppend('div', 'smartPaste');
  hiddenPaste.style.height = 1;
  hiddenPaste.style.width = 1;
  hiddenPaste.style.overflow = 'hidden';
  hiddenPaste.onpaste = waitToPaste;
  hiddenPaste.contentEditable = true;
  hiddenPaste.focus();
}

function waitToPaste() {
  setTimeout('convertPaste()', 100);
}

function convertPaste() {
  var html = hiddenPaste.innerHTML;

  // strip very bad HTML that IE may add after paste.  most of this occurs when
  // someone is pasting hand-coded HTML and IE performs its auto-completion for
  // URLs so that the result is something very ugly like:
  // &lt;img src="<A href='http://p/">http://p/</a'>&gt;foo&lt;/a</A>
  html = RemoveLinksWithinTags(html);
  var horridAnchorStartPattern = new RegExp("<A href=[^<]+<[\\/]?a'>", 'gi');
  var horridAnchorEndPattern = new RegExp('\\&lt;\\/a<\\/A>', 'gi');
  var html = html.replace(horridAnchorStartPattern, '');
  var html = html.replace(horridAnchorEndPattern, '&lt;/a');
  html = StripHTMLExceptLinks(html);

  // get rid of line feeds
  html = html.replace(/\r/g, '');
  // compress repeated line breaks
  html = html.replace(/\n{2,}/g, '\n');
  // convert line breaks
  html = html.replace(/\n/g, '<br />');

  // paste into the editor at the last cursor position
  currentEditorSelection.pasteHTML(html);
  currentEditorSelection.select();
}

function cleanPaste() {
  var content = window.clipboardData.getData('Text');
  if (content) content = content.replace(/\&nbsp;/gi, ' ');
  window.clipboardData.clearData();
  window.clipboardData.setData('Text', content);
}

function StripHTMLExceptLinks(html) {
  // make a private encoding pattern for tag delimiters
  var strTag = '~~~!#';
  var endTag = '#!~~~';
  var strTagPattern = new RegExp(strTag, 'gi');
  var endTagPattern = new RegExp(endTag, 'gi');

  // save the anchor tags
  html = html.replace(/<a ([^>]*)>/gi, strTag + 'a $1' + endTag);
  html = html.replace(/<\/a>/gi, strTag + '\/a' + endTag);

  // erase all other HTML
  html = html.replace(/(<([^>]+)>)/gi, '');

  // restore the anchor tags
  html = html.replace(strTagPattern, '<');
  html = html.replace(endTagPattern, '>');

  return html;
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/richedit.toolbars.js
/*
 * Copyright, Google.com - 2004
 * Author: Chris Wetherell
 *
 * Note: The CONTROLS management functions on this page
 * are based on a WYSIWYG example by Bay-Wei Chang.
 *
 * Array iteration uses the long form syntax in the hopes that this
 * script will eventually support IE 5 for the Mac.  This may be considered
 * out-of-scope later and may be modified.
 */


/*
 * getRichBarButtonHTML()
 *
 * Returns the HTML needed for the toolbar when the editor is in WYSIWYG mode.
 * This function uses an array from the RichEditor object that represents a map
 * of each button to its label and event handling.
 */
RichEditor.prototype.getRichBarButtonHTML = function()
{
  var CONTROLS = RichEdit.CONTROLS;
  var html = [];
  var barname = 'formatbar';
  if (userRTL) {
    html.push(this.makePreview(barname));
  }
  // The preview button has to be explicitly floated in the opposite direction
  // from the rest of the controls.  So we group them in a separate span
  html.push('<span id="' + barname + '_Buttons">');
  var i = this.firstControl(CONTROLS);
  while (true) {
    if (this.controlsDone(CONTROLS, i)) {
      break;
    }
    var ctrl = CONTROLS[i];
    var cmd = ctrl[0];

    if (cmd == '|') {
      html.push(getBarSeparator());
    } else if (ctrl.length == 5) {
      // menu
      html.push('<select id="' + cmd +
      '" onclick="HidePalette()" onchange="FormatbarMenu(this, ' + i + ')">');

      var menuitems = ctrl[4];

      for (var j = 0; j < menuitems.length; j++) {
        var optionname = menuitems[j][0];
        var optionvalue = menuitems[j][1];
        var selected = '';
        if (optionname.charAt(optionname.length - 1) == '*') {
          optionname = optionname.substring(0, optionname.length - 1);
          selected = ' selected';
        }
        html.push('<option value="' + optionvalue + '"' + selected + '>'
          + optionname + '</option>');
      }

      html.push('</select>\n\n');
    } else {
      if (hasColorPalette(cmd)) {
        var onclick = "SelectColor(this,'" + cmd + "')";
      } else {
        var onclick =
            "FormatbarButton('" + this.frameId + "', this, " + i + ')';
      }

      if (ctrl.length >= 6) onclick = ctrl[5];
      var onmouseup = ''; if (ctrl.length >= 7) onmouseup = ctrl[6];

      var img = ctrl[2] || '';
      html.push(this.makeToolbarButton(ctrl[1], onclick + ';',
        img, barname + '_' + cmd, onmouseup));
    }
    i = this.nextControl(i);
  }
  html.push('</span>');
  if (!userRTL) {
    html.push(this.makePreview(barname));
  }

  return html.join('');
};

RichEditor.prototype.makePreview = function(prefix) {
  return this.makeToolbarButton(preview, 'toggle();', null,
      prefix + '_PreviewAction', '');
};

// These methods allow the buttons to be drawn in the opposite order
// for RTL languages

// userRTL is set inside the GXP based on the user's locale.
RichEditor.prototype.firstControl = function(controls) {
  return (userRTL) ? controls.length - 1 : 0;
};

RichEditor.prototype.nextControl = function(i) {
  return (userRTL) ? i - 1 : i + 1;
};

RichEditor.prototype.controlsDone = function(controls, i) {
  return (userRTL) ? (i < 0) : (i >= controls.length);
};

/*
 * getHtmlBarButtonHTML()
 *
 * Returns the HTML needed for the toolbar when the editor is in "edit HTML
 * source" mode. This function uses an array from the RichEditor object that
 * represents a map of each button to its label and event handling.
 */
RichEditor.prototype.getHtmlBarButtonHTML = function()
{
  var CONTROLS = RichEdit.HTML_CONTROLS;
  var barname = 'htmlbar';
  var html = [];
  if (userRTL) {
    html.push(this.makePreview(barname));
  }
  html.push('<span id="' + barname + '_Buttons">');
  var i = this.firstControl(CONTROLS);
  while (true) {
    if (this.controlsDone(CONTROLS, i)) {
      break;
    }
    var ctrl = CONTROLS[i];
    if (ctrl[0] == '|') {
      html.push(getBarSeparator());
    } else {
      var img = ctrl[2] || '';
      var onmouseup = ''; if (ctrl.length >= 5) onmouseup = ctrl[4];
      var button = this.makeToolbarButton(ctrl[1], ctrl[0],
        img, barname + '_' + ctrl[3], onmouseup);
      html.push(button);
    }
    i = this.nextControl(i);
  }
  html.push('</span>');
  if (!userRTL) {
    html.push(this.makePreview(barname));
  }
  return html.join('');
};

/*
 * Returns the HTML needed to display a standalone (non-sprited) image.
 */
RichEditor.prototype.getStandaloneImageHtml = function(title, image) {
  var imageLocation = this.IMAGES_LOCATION + image + '.gif';
  return '<img src="' + imageLocation + '"'
      + ' alt="' + title + '" border="0" />';
};

/*
 * Returns the HTML needed to display a sprited image.
 */
RichEditor.prototype.getSpritedImageHtml = function(title, image) {
  // Apply a class to the image that is its base file name (no extension),
  // with each '.' converted to a '_'. For example, 'gl.align.left' maps to
  // CSS class gl_align_left.
  var imageClass = image.replace(/\./g, '_');
  var blankImagePath = this.IMAGES_LOCATION + 'blank.gif';
  imageHtml = [
      '<img src="', blankImagePath, '"', ' alt="', title, '" border="0" ',
      'class="', imageClass, '"', ' />'].join('');

  return imageHtml;
};

/*
 * Returns the HTML needed to display a sprited image; knows whether to show
 * it from a sprite or a standalone image.
 */
RichEditor.prototype.getImageHtml = function(title, image, strId) {
  // Transliteration image isn't shown from the sprite; it's set by the
  // transliteration code to an icon which matches the chosen language.
  if (strId == 'formatbar_Transliterate') {
    return this.getStandaloneImageHtml(title, image);
  } else {
    return this.getSpritedImageHtml(title, image);
  }
};

/*
 * makeToolbarButton()
 *
 * Returns the HTML needed for buttons for the RichEditor() toolbar.
 */
RichEditor.prototype.makeToolbarButton = function(
    title, onclick, image, strId, onmouseup) {
  var imageHtml = image ? this.getImageHtml(title, image, strId) : title;

  return '<span' +
         ' id="' + strId + '"' +
        ' title="' + title + '"' +
        ' onmouseover="ButtonHoverOn(this);"' +
        ' onmouseout="ButtonHoverOff(this);"' +
        ' onmouseup="' + onmouseup + '"' +
        ' onmousedown="CheckFormatting(event);' + onclick +
        'ButtonMouseDown(this);"' +
        '>' +
        imageHtml +
        '</span>\n';
};


/*
 * getBarSeparator()
 *
 * Returns the HTML needed for the separator for any RichEditor toolbar.
 */
function getBarSeparator() {
  return '<div class="vertbar">'
    + '<span class="g">&nbsp;</span><span class="w">&nbsp;</span>'
    + '</div>\n';
}



// ===================================================================
// Toolbar Event Handling (General)
// ===================================================================

/*
 * hasColorPalette()
 *
 * Determines if an object's ID indicates that its involved with palettes.
 */
function hasColorPalette(strId) {
  if (strId == null) return false;
  return (strId.indexOf('Color') > 0);
}
function ButtonHoverOn(obj) {
  if (!hasClass(obj, 'down')) addClass(obj, 'on');
}
function ButtonHoverOff(obj) {
  if (!hasClass(obj, 'down')) removeClass(obj, 'on');
}
function ButtonMouseDown(obj) {
  var ctrl;
  // retrieve the control name
  if (obj.id) ctrl = obj.id.replace(/formatbar\_/g, '');
  var DEPRESSABLE = RichEdit.DEPRESSABLE;

  // is the button part of the DEPRESSABLE array?
  for (var i = 0; i < DEPRESSABLE.length; i++) {
    if (DEPRESSABLE[i] == ctrl) {
      // make the button look depressed
      toggleClass(obj, 'down');
      RichEdit.addDebugMsg(
          'toggle button -- [' + obj.className + ', ' + ctrl + ']\n');

      // only one justify button can be depressed at any time
      ClearOtherJustify(ctrl);

      // and only one dir button
      ClearOtherDir(ctrl);

      // return the focus to the page, but pause for ol' Mozilla, who'll
      // generate an error if we move too fast
      setTimeout('setRichEditFocus()', '100');
      return true;
    }
  }
}


// ===================================================================
// Toolbar Event Handling (Design Mode)
// ===================================================================

var FORMATTING_FAILED_MSG = 'An attempt to modify formatting failed ' +
    'unexpectedly. A possible solution may be to save your post as a draft ' +
    'and reopen this post and apply formatting again.';

function FormatbarMenu(el, ctrlid) {
  var ctrl = RichEdit.CONTROLS[ctrlid][0];
  var val = el.options[el.selectedIndex].value;
  try {
    RichEdit.frameDoc.execCommand(ctrl, false, val);
  } catch (e) {
    alert(FORMATTING_FAILED_MSG + '\n' + e);
  }

  setRichEditFocus();
}

function FormatbarButton(frameId, button, ctrlid) {
  var cmd = RichEdit.CONTROLS[ctrlid][0];
  var idoc = RichEdit.frameDoc;
  try {
    if (cmd == 'CreateLink') {
      CreateLink();
    } else if (cmd == 'Blockquote') {
      RichEdit.Blockquote();
    } else if (cmd == 'Strikethrough') {
      RichEdit.Strikethrough();
    } else if (cmd == 'RemoveFormat') {
      RichEdit.RemoveFormat();
    // for IE the dir buttons are implemented by execCommand
    // for FF we need custom code
    } else if (cmd == 'BlockDirRTL' && Detect.MOZILLA()) {
      RichEdit.FirefoxSetDirectionality('rtl');
    } else if (cmd == 'BlockDirLTR' && Detect.MOZILLA()) {
      RichEdit.FirefoxSetDirectionality('ltr');
    } else {
      idoc.execCommand(cmd, false, '');
    }
    RichEdit.addDebugMsg('-- RichEdit Formatting Button pressed: ' + cmd);
  } catch (e) {
    alert(FORMATTING_FAILED_MSG + '\n' + e);
  }
}


/*
 * Blockquote()
 *
 * Surrounds the current selection with a BLOCKQUOTE tag.
 */
RichEditor.prototype.Blockquote = function() {
  this.formatSelection('blockquote');
};


/*
 * Strikethrough()
 *
 * Surrounds the current selection with a STRIKE tag.
 */
RichEditor.prototype.Strikethrough = function() {
  this.formatSelection('strike');
};


/*
 * RemoveFormat()
 *
 * Attempts to remove formatting from a selection while preserving line breaks.
 */
RichEditor.prototype.RemoveFormat = function() {
  var rangeNode = getRangeAsDocumentFragment(GetRange());
  if (!rangeNode) return;

  //remove the non-line-break nodes and collect them into an array
  var arrStrippedNodes = [];
  this.stripFormatNodesFromSelection(rangeNode, arrStrippedNodes);

  // put the new node collection into an inline-level element
  var strippedFragment = document.createElement('span');
  for (var x = 0; x < arrStrippedNodes.length; x++) {
    // IE starts with an extra <br> tag we can ignore
    if (Detect.IE() && x == 0 && arrStrippedNodes[x].nodeName == 'BR') continue;
    strippedFragment.appendChild(arrStrippedNodes[x]);
  }

  // replace the current IFRAME selection with the new node collection
  var frameWin = document.getElementById(this.frameId).contentWindow;
  insertNodeAtSelection(frameWin, 'span', strippedFragment);

  // let the WYSIWYG library remove whatever this function missed
  this.frameDoc.execCommand('RemoveFormat', false, '');
};



/*
 * formatSelection()
 *
 * Surround the selection in the WYSIWYG IFrame with the specified tag.
 */
RichEditor.prototype.formatSelection = function(tagName) {
  var parent = getSelectedParentNode();

  // if it's already there, remove it.
  if (parent) {
    var hasTagAsParent = parent.nodeName.toUpperCase() == tagName.toUpperCase();
    if (hasTagAsParent) {
      adoptGrandchildrenFromParent(this.frameDoc, parent);
      return;
    }
  }

  // or add the tag if it's not there already.
  var frame = document.getElementById(this.frameId);
  surroundFrameSelection(frame, tagName);
};


/*
 * stripFormatNodesFromSelection()
 *
 * Removes formatting nodes from a selection but preserves their values and
 * line breaks in the supplied array.
 */
RichEditor.prototype.stripFormatNodesFromSelection = function(parent, 
                                                              arrReturnNodes) {
  var doc = document;
  var children = parent.childNodes;
  // iterate through all of the child nodes
  for (var x = 0; x < children.length; x++) {
    var child = children[x];
    // null? leave.
    if (!child) continue;

    var type = child.nodeType;

    // if it's a text node, add its value to the array
    if (type == TEXT_NODE) {
      arrReturnNodes.push(child);
      continue;
    }

    // however, if it's an element node...
    if (type == ELEMENT_NODE) {
      var childName = child.nodeName.toUpperCase();

      // ...and a regular carriage return, add a <br> node to the array
      if (childName == 'BR') {
        var br = doc.createElement('br');
        arrReturnNodes.push(br);
        continue;
      }

      // if it's a carriage return managed as a DIV (as IE does for our editor)
      // then add a <br> node to the array and change the DIV to a SPAN so that
      // the user doesn't see two carriage returns where they'd expect only one.
      if (Detect.IE() && childName == 'DIV') {
        var br = doc.createElement('br');
        arrReturnNodes.push(br);
        var span = doc.createElement('span');
        span.innerHTML = child.innerHTML;
        this.stripFormatNodesFromSelection(span, arrReturnNodes);
        continue;
      }

      // otherwise continue recursively through the tree
      this.stripFormatNodesFromSelection(child, arrReturnNodes);
    }
  }
};

/*
 * adoptGrandchildrenFromParent()
 *
 * Removes the children of the supplied node and appends them to their
 * grandparent then removes the supplied node.
 */
function adoptGrandchildrenFromParent(doc, parent) {
  if (!parent) return;
  var grandparent = parent.parentNode;
  if (!grandparent) return;
  var grandchildren = parent.childNodes;
  if (!grandchildren) return;

  var nodes = [];
  for (var x = 0; x < grandchildren.length; x++) {
    var type = grandchildren[x].nodeType;
    // if it's a text node, add the grandchild to the array
    if (type == 3) {
      nodes.push(grandchildren[x]);
      continue;
    }

    // if it's an element node...
    if (type == 1) {
      // preserve the line breaks by adding a <br> node, else add the
      // grandchild
      if (grandchildren[x].nodeName.toUpperCase() == 'BR') {
        // create a new <br> node, because, mysteriously, it's difficult to
        // append the originals as nodes later.  needs looking into.
        var br = doc.createElement('br');
        nodes.push(br);
      } else {
        nodes.push(grandchildren[x]);
      }
    }
  }
  for (var x = 0; x < nodes.length; x++) {
    grandparent.insertBefore(nodes[x], parent);
  }
  grandparent.removeChild(parent);
}


function CreateLink() {
  var idoc = RichEdit.frameDoc;
  if (Detect.IE()) {
    idoc.execCommand('CreateLink', true);  // true == show ui
    // stop IE from overwriting the content when a key combination is pressed
    // i.e. CTRL+SHFT+A
    var evt = getAvailableEventForIE();
    evt.returnValue = false;

  } else {
    if (GetSelection().isCollapsed) {
      alert(select_link_text_msg);
    } else {
      var url = prompt(enter_url_msg, 'http://');
      if (url != null) {  // url == null means dialog was cancelled
        url = Trim(url);
        if ((url != '') && (url != 'http://')) {
          idoc.execCommand('CreateLink', false, url);
        } else {
          idoc.execCommand('Unlink', false, '');
        }
      }
    }
  }
}


// ===================================================================
// Color palettes
// ===================================================================

RichEditor.prototype.COLORS = [
  // blacks
  ['ffffff', 'cccccc', 'c0c0c0', '999999', '666666', '333333', '000000'],
  // reds
  ['ffcccc', 'ff6666', 'ff0000', 'cc0000', '990000', '660000', '330000'],
  // oranges
  ['ffcc99', 'ff9966', 'ff9900', 'ff6600', 'cc6600', '993300', '663300'],
  // yellows
  ['ffff99', 'ffff66', 'ffcc66', 'ffcc33', 'cc9933', '996633', '663333'],
  // olives
  ['ffffcc', 'ffff33', 'ffff00', 'ffcc00', '999900', '666600', '333300'],
  // greens
  ['99ff99', '66ff99', '33ff33', '33cc00', '009900', '006600', '003300'],
  // turquoises
  ['99ffff', '33ffff', '66cccc', '00cccc', '339999', '336666', '003333'],
  // blues
  ['ccffff', '66ffff', '33ccff', '3366ff', '3333ff', '000099', '000066'],
  // purples
  ['ccccff', '9999ff', '6666cc', '6633ff', '6600cc', '333399', '330099'],
  // violets
  ['ffccff', 'ff99ff', 'cc66cc', 'cc33cc', '993399', '663366', '330033']
];

function Palette() {}
Palette.cell = null;
Palette.obj = null;
Palette.colorSelectedValue = null;
Palette.colorSelectedButton = null;

function PaletteOver(e) {
  e.style.border = '1px solid #fff';
  Palette.cell = e;
}

function PaletteOut(e) {
  e.style.border = '1px solid #bbb';
  Palette.cell = null;
}

function ShowPalette(button) {
  // hide the fontSize menu here because it's already wired in
  hideFontSizeMenu();

  if (!Palette.obj) {
    var COLORS = RichEdit.COLORS;
    var html = [];
    html.push('<table id="xpalettetable" style="width:130px;" ' +
              'cellspacing="0" cellpadding="0">');
    for (var i = 0; i < COLORS.length; i++) {
      html.push('<tr>');
      for (var j = 0; j < COLORS[i].length; j++) {
        html.push('<td bgcolor="#' + COLORS[i][j] + '"'
        + ' unselectable="on" onmouseover="PaletteOver(this)"'
        + ' onmouseout=PaletteOut(this) '
        + "onclick=\"PaletteClick('#" + COLORS[i][j] + "')\">"
        + '<img width="1" height="1"></td>');
      }
    }
    setInnerHTML('palette', html.join(''));
    Palette.obj = getElement('palette');
  }
  var pos = getXY(button);
  var palette = Palette.obj;
  palette.style.left = pos.x + 'px';
  palette.style.top = (pos.y + button.offsetHeight) + 'px';
  showElement(palette);
}

function SelectColor(button, cmd) {
  Palette.colorSelectedValue = cmd;
  Palette.colorSelectedButton = button;
  RichEdit.addDebugMsg('-- RichEdit Formatting Button pressed: ' + cmd);
  ShowPalette(button);
}

function HidePalette(opt_event) {
  if (Palette.obj) {
    if (Palette.cell) {
      // Moz doesn't call onmouseout on the palette
      // cell when the palette is hidden, so we
      // track it manually and do it ourselves.
      PaletteOut(Palette.cell);
    }
    hideElement(Palette.obj);
    if (Palette.colorSelectedButton) {
      Palette.colorSelectedButton.className = '';
    }
  }

  // hide the fontSize menu here because this method is already called
  // when an HTML-based menu needs to be hidden
  hideFontSizeMenu(opt_event);
}

function PaletteClick(color) {
  HidePalette();
  if (Palette.colorSelectedValue) {
    RichEdit.frameDoc.execCommand(Palette.colorSelectedValue, false, color);
    removeClass(Palette.colorSelectedButton.parentNode, 'depressed');
    Palette.colorSelectedValue = null;
    Palette.colorSelectedButton = null;
  }
  setRichEditFocus(); // for moz
}



// ===================================================================
// Setting Cursor-Position-Based Button State
// ===================================================================


var IFRAME_INIT_MESSAGE = '';

function getAvailableEventForIE() {
  if (!event) {
    return getRichEditorEventForIE();
  } else {
    return event;
  }
}

function getRichEditorEventForIE() {
  return getElement(RichEdit.frameId).contentWindow.event;
}

function CheckFormatting(e) {
  if (RichEdit.mode == RichEdit.HTML_MODE) return;  //how's it getting called?

  e = (Detect.IE()) ? getAvailableEventForIE() : e;

  if (Detect.MOZILLA() && e.type == 'keydown') return; //we'll check on keyup

  ShowHtml();
  CheckIfEmpty();  //Everyone, please thank Bay.

  // hide the palette except when the palette button is being clicked
  if (getEventSource(e).parentNode
      && !hasColorPalette(getEventSource(e).parentNode.id)) {
    HidePalette(e);
  }

  // get all of the HTML nodes surrounding the current selection
  var nodes = getAncestors();
  var on = [];
  for (var i = 0; i < nodes.length; i++) {
    if (nodes) setButtonFromNode(nodes[i], on);
  }
  var baseNodeTotal = (Detect.IE()) ? 1 : 0;
  if (nodes.length == baseNodeTotal) {
    SetFontNameMenu('');
  }

  // depress those buttons whose nodes are ancestors of the selection
  var BUTTONS = {};
  var DEPRESSABLE = RichEdit.DEPRESSABLE;
  for (var i = 0; i < DEPRESSABLE.length; i++) {
    BUTTONS[DEPRESSABLE[i]] = true;
  }

  var SELECTED = {};
  for (var i = 0; i < on.length; i++) {
    SELECTED[on[i]] = true;
  }

  var DEBUG_CHECK_FORMATTING = false;
  if (RichEdit.DEBUG_CHECK_FORMATTING) {
    DEBUG_CHECK_FORMATTING = true;
    RichEdit.clearDebugMsg();
    RichEdit.addDebugMsg('CheckFormatting() -- [event: ' + e.type + ']\n');
  }

  for (var ctrl in BUTTONS) {
    if (BUTTONS[ctrl] == SELECTED[ctrl]) {
      // don't react to the toolbar button click, we need that to be in
      // some other abstraction to manage palette-like buttons
      // The state of the transliteration button does not depend on the
      // position of the text cursor
      if (e.type != 'mousedown' && ctrl != 'Transliterate') {
        Depress(ctrl);
        if (DEBUG_CHECK_FORMATTING) {
          RichEdit.addDebugMsg('Depress() -- [press: ' + ctrl + ']\n');
        }
      }
    } else {
      // don't react to the toolbar button click
      // The state of the transliteration button does not depend on the
      // position of the text cursor
      if (e.type != 'mousedown' && ctrl != 'Transliterate') {
        if (DEBUG_CHECK_FORMATTING) {
          RichEdit.addDebugMsg(
              'UnDepress() -- [' + e.type + ', ' + ctrl + ']\n');
        }
        UnDepress(ctrl);
      }
    }
  }
}

function BLOG_clickEventHandler(event) {
  CheckFormatting(event);
  event = Detect.IE() ? getAvailableEventForIE() : event;

  // The rich editor eats the onclicks from the span, so we
  // have an event listener on the iframe document and thus we
  // need to pull the target out of the event.
  var target =
    Detect.IE() ? event.srcElement : event.target;

  // defined in spellcheck.js
  BLOG_spellClickHandler(target);
  BLOG_videoThumbnailClickHandler(target, event);
}

function ClearButton(ctrl, cmd) {
  if (ctrl != cmd) {
    UnDepress(cmd);
    RichEdit.addDebugMsg('UnDepress() -- [' + cmd + ', ' + ctrl + ']\n');
  }
}

function ClearOtherJustify(ctrl) {
  if (ctrl.indexOf('Justify') != -1) {
    ClearButton(ctrl, 'JustifyLeft');
    ClearButton(ctrl, 'JustifyCenter');
    ClearButton(ctrl, 'JustifyRight');
  }
}

function ClearOtherDir(ctrl) {
  if (SHOW_DIRECTIONALITY_BUTTONS && ctrl.indexOf('BlockDir') != -1) {
    ClearButton(ctrl, 'BlockDirLTR');
    ClearButton(ctrl, 'BlockDirRTL');
  }
}

function setButtonFromNode(node, on) {
  if (isBoldMarkup(node)) { on.push('Bold');}
  if (isItalicMarkup(node)) {on.push('Italic');}
  if (isColorMarkup(node)) {on.push('ForeColor');}
  if (isNode(node, 'blockquote')) {on.push('Blockquote');}
  if (isNode(node, 'ul')) {on.push('InsertUnorderedList');}
  if (isNode(node, 'ol')) {on.push('InsertOrderedList');}
  if (isBkgColorMarkup(node)) {on.push(RichEdit.BACKCOLOR);}
  if (isAlignMarkup(node, 'left')) {
    on.push('JustifyLeft');
  } else if (isAlignMarkup(node, 'center')) {
    on.push('JustifyCenter');
  } else if (isAlignMarkup(node, 'right')) {
    on.push('JustifyRight');
  }

  if (SHOW_DIRECTIONALITY_BUTTONS) {
    if (isDirMarkup(node, 'ltr')) {
      on.push('BlockDirLTR');
    } else if (isAlignMarkup(node, 'rtl')) {
      on.push('BlockDirRTL');
    }
  }

  if (hasSomeMargin(node, 'left')) {on.push('Indent');}
  var font = new Object();
  if (isFontMarkup(node, font)) {
    var fontFamily = (!font.FAMILY) ? '' : font.FAMILY;
    var fontSize = (!font.SIZE) ? 3 : font.SIZE;
    SetFontNameMenu(font.FAMILY);
  }
}

function Depress(ctrl) {
  var el = d('formatbar_' + ctrl);
  if (el) {
    el.className = 'down';
  } else {
    RichEdit.addDebugMsg('Depress() failed for "' + ctrl + '"');
  }
}

function UnDepress(ctrl) {
  var el = d('formatbar_' + ctrl);
  if (el) {
    el.className = '';
  } else {
    RichEdit.addDebugMsg('UnDepress() failed for "' + ctrl + '"');
  }
}

function isFontMarkup(node, fontObj) {
  var nm = node.nodeName.toUpperCase();
  var fontFamily = getStyle(node, 'font-family');
  var fontSize = getStyle(node, 'font-size');
  var face = node.getAttribute('face');
  var size = node.getAttribute('size');
  var font = getStyle(node, 'font');
  if (font || fontFamily || fontSize || size || face) {
    if (font) fontObj.FONT = font;
    if (fontFamily) fontObj.FAMILY = fontFamily;
    if (fontSize) fontObj.SIZE = fontSize;
    if (face) {fontObj.FACE_ATTR = true; fontObj.FAMILY = face; }
    if (size) {fontObj.SIZE_ATTR = true; fontObj.SIZE = size; }
    return true;
  } else {
    return false;
  }
}

function isBoldMarkup(node) {
  return (matchesStyledMarkup(node, 'font-weight', 'bold') ||
          isNode(node, 'B') || isNode(node, 'STRONG'));
}

function isItalicMarkup(node) {
  return (matchesStyledMarkup(node, 'font-style', 'italic') ||
          isNode(node, 'I') || isNode(node, 'EM'));
}

function isColorMarkup(node) {
  if (Detect.IE()) {
    return node.getAttribute('color');
  } else {
    var style = node.getAttribute('style');
    if (!style) style = '';
    var bkgPattern = new RegExp('background-color:[^;"]*');
    style = style.replace(bkgPattern, '');
    return (style.indexOf('color') != -1);
  }
}

function isBkgColorMarkup(node) {
  if (Detect.IE()) {
    return node.style.backgroundColor;
  } else {
    var style = node.getAttribute('style');
    if (!style) style = '';
    return (style.indexOf('background-color') != -1);
  }
}


function isAlignMarkup(node, direction) {
  return (matchesStyledMarkup(node, 'text-align', direction) ||
          hasAttributeValue(node, 'align', direction));
}

function isDirMarkup(node, direction) {
  return (hasAttributeValue(node, 'dir', direction));
}

function hasSomeMargin(node, direction) {
  var style = getStyle(node, 'margin-' + direction);
  return (style && style != 0 && style != '0px');
}

function hasAttributeValue(node, attr, value) {
  var attr = node.getAttribute(attr);
  if (attr) {
    return (attr == value);
  } else {
    return false;
  }
}

function isNode(node, tagName) {
  return (node.nodeName == tagName.toUpperCase());
}

function matchesStyledMarkup(node, attribute, styleValue) {
  var nm = node.nodeName.toUpperCase();
  var style = getStyle(node, attribute);
  var font = getStyle(node, 'font');
  if (style || font) {
    return (style.toLowerCase() == styleValue ||
            font.toLowerCase().indexOf(styleValue.toLowerCase()) != -1) ?
           true : false;
  } else {
    return false;
  }
}

function getAncestors() {
  var ancestors = [];
  var parent = getSelectedParentNode();
  while (parent && (parent.nodeType == ELEMENT_NODE)
         && (parent.tagName != 'BODY')) {
    ancestors.push(parent);
    parent = parent.parentNode;
  }
  return ancestors;
}

function getSelectedParentNode() {
  if (Detect.IE()) {
    var sel = GetRange();
    try {
      sel.collapse();
      return sel.parentElement();
    } catch (e) {
      // Images within MSHTML editing don't have parent elements and
      // should avoid performing this function.
      return;
    }
  } else {
    var range = GetRange();
    if (!range) return;
    parent = range.commonAncestorContainer;
    if (parent.nodeType == TEXT_NODE) {
      parent = parent.parentNode;
    }
    return parent;
  }
}

function SetFontNameMenu(font) {
  try {
    SetMenu('FontName', font.toLowerCase());
  } catch (e) {
    RichEdit.addDebugMsg(
        '-- SetFontNameMenu() failed when parsing: "' + font + '"');
  }
}

function SetMenu(menuid, value) {
  var menu = document.getElementById(menuid);
  for (var i = 0; i < menu.options.length; i++) {
    if (menu.options[i].value == value) {
      menu.options.selectedIndex = i;
      break;
    }
  }
}

// In IE, editing must occur within <div></div> in order
// to have line breaks look like <br>'s rather than <p>'s.
// The div can be deleted if ctrl-A is used to select all
// and then everything is deleted.
function CheckIfEmpty() {
  var idoc = RichEdit.frameDoc;
  if (idoc.body.innerHTML == '<P>&nbsp;</P>') {
    resetIFrameBody();
  }
}

function resetIFrameBody() {
  var idoc = RichEdit.frameDoc;
  idoc.body.innerHTML = '<div></div>';
  // now put the input caret into the div
  var range = idoc.body.createTextRange();
  range.collapse();
  range.select();
}


function GetSelection() {
  if (Detect.IE()) {
    return RichEdit.frameDoc.selection;
  } else {
    return RichEdit.frameWin.getSelection();
  }
}

function GetRange() {
  var idoc = RichEdit.frameDoc;
  if (Detect.IE()) {
    return idoc.selection.createRange();
  } else {
    try {
      var range = idoc.createRange();
      var sel = GetSelection();
      range = sel.getRangeAt(sel.rangeCount - 1).cloneRange();
      return range;
    } catch (e) {
      RichEdit.addDebugMsg('--create/set range failed');
      return null;
    }
  }
}


function setRichEditFocus() {
  document.getElementById(RichEdit.frameId).contentWindow.focus();
}


// ===================================================================
// Keyboard commands
// ===================================================================


/*
 * activateKeyCommands()
 *
 * Based on a combination of keystrokes and keyholds, activate
 * a particular formatting method.
 *
 * Warning: In Safari the text formatting keyboard commands do not work
 * since there is no selection management in textareas yet for that browser.
 */
RichEditor.prototype.activateKeyCommands = function(e) {
  if (Detect.IE()) e = RichEdit.frame.contentWindow.event;

  setKeysetByEvent(e);

  if (RichEdit.DEBUG_FRAME_EVENTS) {
    RichEdit.clearDebugMsg();
    RichEdit.addDebugMsg('-- RichEdit event: ' + e);
    RichEdit.addDebugMsg('-- RichEdit event type: ' + e.type);
    RichEdit.addDebugMsg('-- RichEdit key: ' + getKey(e));
  }

  KEY_COMMANDS = RichEdit.KEY_COMMANDS;

  for (x = 0; x < KEY_COMMANDS.length; x++) {
    var ifKeyPressed = eval(KEY_COMMANDS[x][0]);
    if (ifKeyPressed) {
      if (KEY_COMMANDS[x][1] != null) {
        toggleButtonDisplay(KEY_COMMANDS[x][1]);
      }
      if (KEY_COMMANDS[x][2]) {
        // stop IE from duplicating a supported MSHTML action, like making
        // text bold or italic.
        if (!Detect.IE()
            || (Detect.IE() && !(CTRL_B || CTRL_I))) {

          eval(KEY_COMMANDS[x][2]);

          // stop IE from duplicating the post
          if (Detect.IE() && CTRL_SHFT_S) break;

        }
      }
    }
  }
  // prevent bookmarks sidebar from opening in Mozilla on Windows
  var preventConditions = (CTRL_B || CTRL_I || CTRL_S || CTRL_D);

  if (trOK) {
    // if transliteration is enabled, Ctrl+G will be the shortcut for it.
    preventConditions = preventConditions || CTRL_G;
  }

  if (Detect.MOZILLA() && preventConditions) {
    e.preventDefault();
  }

  // attempt to defeat IE's link auto-completion...it ruins image sources.
  if (Detect.IE()) CorrectLinkAutoCompletionInImages();

  /*
   * stop IE from inserting a paragraph tag when a user is trying to delete
   * the entire selection
   * check against space code is because in ASCII space is lowest visible
   * character. We don't want to delete all because of shift key press e.g.
   */
  var keyCode = getKey(e);
  if (Detect.IE()
      && !isCtrlKeyPressed(e)
      && !isAltKeyPressed(e)
      && (keyCode == TAB_KEYCODE
          || keyCode == ENTER_KEYCODE
          || keyCode == DELETE_KEYCODE
          || keyCode == BACKSPACE_KEYCODE
          || keyCode >= SPACE_KEYCODE)) {
    /*
     * get the selection... and see if it's "select all" by testing if its
     * length is larger than the innerHTML length of the IFRAME
     */
    var rangeNode = getRangeAsDocumentFragment(GetRange());
    var range = rangeNode.innerHTML;
    var body = RichEdit.frameDoc.body.innerHTML;
    if (range.length >= body.length) {
      resetIFrameBody();
    }
  }

  // cleanup extraneous markup after delete operations are performed
  if (BACKSPACE || DELETE || RETURN) {
    setTimeout('RichEdit.cleanupDeletion()', 80);
  }

  return true;
};


/*
 * toggleButtonDisplay()
 *
 * Changes a specific toolbar button to look selected or un-selected.
 */
function toggleButtonDisplay(ctrl) {
  try {
    var obj = getElement('formatbar_' + ctrl);
    obj.className = (obj.className == 'down') ? '' : 'down';
  } catch (e) {
    RichEdit.addDebugMsg('toggleButtonDisplay() failed for "' + ctrl + '"');
  }
}

// Function that is redefined in transliteration code if necessary
function checkForTrAndSubmitForm() {
  return setFormAndSubmit();
}

/**
 * A function that's called to set the form to 'save as draft' mode. Called
 * from the button's onsubmit(). This is required beause if in transliteration
 * mode, we need to know whether to process the form as 'draft' or 'publish'.
 */
function setPostAsSubmitDraft() {
  if (trOK) {
    _TR_setPostAsSubmitDraft();
  }
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/richedit.textutils.js
/*
 * Copyright, Google.com - 2004
 * Author: Chris Wetherell
 */


// ===================================================================
// HTML utilities
// ===================================================================


function removeNewlinesFromTags(s) {
  s = s.replace(/<[^>]+>/g, function(ref) {
    ref = ref.replace(/\r/g, '');
    ref = ref.replace(/\n/g, ' ');
    return ref;
  });
  return s;
}

function replaceEmptyDIVsWithBRs(s) {
  // Remove line feeds to avoid weird spacing in Edit HTML mode
  s = s.replace(/[\r]/g, '');

  /*
   * sometimes IE's pasteHTML method of the range object will insert
   * troublesome newlines that aren't useful
   */
  if (Detect.IE()) {
    var blockElementsPattern = new RegExp(
      '\n<(' +
       'BLOCKQUOTE' + '|' +
       'LI' + '|' +
       'OBJECT' + '|' +
       'H[\d]' + '|' +
       'FORM' + '|' +
       'TR' + '|' +
       'TD' + '|' +
       'TH' + '|' +
       'TBODY' + '|' +
       'THEAD' + '|' +
       'TFOOT' + '|' +
       'TABLE' + '|' +
       'DL' + '|' +
       'DD' + '|' +
       'DT' + '|' +
       'DIV' + '|' +
       ')([^>]*)>', 'gi'
    );
    s = s.replace(blockElementsPattern, '<$1$2>');
  }

  // remove newlines from inside of tags
  s = removeNewlinesFromTags(s);

  // convert newlines to HTML line breaks
  s = s.replace(/\n/g, '<BR>');

  // Attempt to persist relative paths
  if (Detect.IE()) {
    s = addRelativePathPlaceholders(s);
  }

  /*
   * Append html string to element in order to remove DIVs as nodes
   * rather than using string manipulation
   */
  var tmp = document.createElement('div');
  tmp.style.display = 'none';

  // we have to wrap the string inside of a div b/c of this weird IE bug
  // that causes the entire innerHTML to become empty if the first element
  // is an object tag.
  tmp.innerHTML = '<div>' + s + '</div>';
  tmp = tmp.childNodes[0];

  // determine which DIVs have NO attributes
  var plainDIVs = [];
  var divs = tmp.getElementsByTagName('div');
  for (var i = 0; i < divs.length; i++) {
    var hasAttributes = false;
    var attrs = divs[i].attributes;
    for (var x = 0; x < attrs.length; x++) {
      var val = attrs[x].specified;
      if (val) {
        hasAttributes = true;
        break;
      }
    }
    if (!hasAttributes) plainDIVs.push(divs[i]);
  }

  // strip DIVs with no attributes, but keep their content!
  for (var i = 0; i < plainDIVs.length; i++) {
    var grandparent = plainDIVs[i].parentNode;
    if (grandparent) {
      plainDIVs[i].innerHTML += '<BR>';
      adoptGrandchildrenFromParent(document, plainDIVs[i]);
    }
  }

  // remove newlines
  var html = tmp.innerHTML;
  html = html.replace(/[\r|\n]/g, '');

  // Remove dummyURL in order to make links and images with relative links
  // appear as requested and defeat IE's auto-complete
  if (Detect.IE()) {
    var NON_EXISTENT_URLPattern = new RegExp(NON_EXISTENT_URL, 'gi');
    html = html.replace(NON_EXISTENT_URLPattern, '');
  }

  return html;
}

function cleanMSHTML(s) {
  // Remove IE's inserted comments when text is pasted into the IFRAME
  s = s.replace(/<!--StartFragment -->&nbsp;/g, '');

  // Try to transform into well-formed markup
  s = cleanHTML(s);

  s = s.replace(/<br \/>/gi, '\n');

  // Remove IE's blockquote styling
  s = s.replace(/<blockquote dir=\"ltr" style=\"MARGIN-RIGHT: 0px\">/g,
    '<blockquote style="margin-top:0;margin-bottom:0;">');

  return s;
}

/*
 * cleanHTML()
 *
 * Attempts to transform ill-formed HTML into well-formed markup.
 */
function cleanHTML(s) {
  // make node names lower-case and add quotes to attributes
  s = cleanNodesAndAttributes(s);
  // Midas adds colors to <br> tags!  TODO: take newline conversion elsewhere
  if (Detect.MOZILLA()) s = s.replace(/<br [^>]+>/gi, '\n');
  // make single nodes XHTML compatible
  s = s.replace(/<(hr|br)>/gi, '<$1 \/>');
  // make img nodes XHTML compatible
  s = s.replace(/<(img [^>]+)>/gi, '<$1 \/>');
  s = addClosingEmbedTags(s);
  return s;
}

/*
 * addClosingEmbedTags()
 *
 * Adds a closing embed tag if there isn't already one.
 */

function addClosingEmbedTags(s) {
  // first strip away any closing embed tags if they exist.  This should only
  // happen if the HTML code was pasted into the compose mode.
  s = s.replace(/<\/embed>/gi, '');
  // Then add the closing tag.
  s = s.replace(/<(embed [^>]+)>/gi, '<$1></embed>');
  return s;
}

/*
 * cleanNodesAndAttributes()
 *
 * Attempts to transform node names to lower-case and add double-quotes to
 * HTML element node attributes.
 */
function cleanNodesAndAttributes(s) {
  // Get all of the start tags
  var htmlPattern = new RegExp('<[ ]*([\\w]+).*?>', 'gi');
  s = s.replace(htmlPattern, function(ref) {
    var cleanStartTag = ''; // for storing the result

    // Separate the tag name from its attributes
    var ref = ref.replace('^<[ ]*', '<'); // remove beginning whitespace
    var ndx = ref.search(/\s/);  // returns index of first match of whitespace
    var tagname = ref.substring(0 , ndx);
    var attributes = ref.substring(ndx, ref.length);

    // Make tag name lower case
    if (ndx == -1) return ref.toLowerCase(); // no attr/value pairs (i.e. <p>)
    cleanStartTag += tagname.toLowerCase();

    // Clean up attribute/value pairs
    var pairs = attributes.match(/[\w]+\s*=\s*("[^"]*"|[^">\s]*)/gi);
    if (pairs) {
      for (var t = 0; t < pairs.length; t++) {
        var pair = pairs[t];
        var ndx = pair.search(/=/);  // index of first match of equals (=)

        // Make attribute names lower case
        var attrname = pair.substring(0, ndx).toLowerCase();

        // Put double-quotes around values that don't have them
        var attrval = pair.substring(ndx, pair.length);
        var wellFormed = new RegExp('=[ ]*"[^"]*"', 'g');
        if (!wellFormed.test(attrval)) {
          var attrvalPattern = new RegExp('=(.*?)', 'g');
          attrval = attrval.replace(attrvalPattern, '=\"$1');
          // there's an IE bug that prevent this endquote from being appended
          // after the backreference.  no, seriously.
          attrval += '"';
        }
        // join the attribute parts
        var attr = attrname + attrval;
        cleanStartTag += ' ' + attr;
      }
    }
    cleanStartTag += '>';

    return cleanStartTag;
  });

  // Makes all of the end tags lower case
  s = s.replace(/<\/\s*[\w]*\s*>/g, function(ref) {return ref.toLowerCase();});

  return s;
}


/*
 * convertAllFontsToSpans()
 *
 * Attempts to transform deprecated FONT nodes into well-formed XHTML-compliant
 * markup.
 */
function convertAllFontsToSpans(s) {
  startTagPattern = RegExp('<[^/]*font [^<>]*>', 'gi');
  var StartTags = s.match(startTagPattern);
  if (StartTags) {
    for (var i = 0; i < StartTags.length; i++) {
      // adjacent tags get lost in some regexp searches in some browsers, so
      // we'll catch 'em here
      if (StartTags[i].indexOf('>') > 1) innerStartTags =
          StartTags[i].split('>');
      for (var x = 0; x < innerStartTags.length; x++) {
        if (innerStartTags[x] == '') continue;
        var thisTag = innerStartTags[x] + '>';
        modifiedStartTag = convertTagAttributeToStyleValue(thisTag,
                                                           'face',
                                                           'font-family');
        modifiedStartTag = convertTagAttributeToStyleValue(modifiedStartTag,
                                                          'size',
                                                          'font-size');
        modifiedStartTag = convertTagAttributeToStyleValue(modifiedStartTag,
                                                          'color',
                                                          'color');
        s = s.replace(thisTag, modifiedStartTag);

      }
    }
  }
  s = s.replace(/<font>/gi, '<span>');
  s = s.replace(/<font ([^>]*)>/gi, '<span $1>');
  s = s.replace(/<\/font>/gi, '</span>');

  // clean up extra spaces
  s = s.replace(/<span[ ]+style/gi, '<span style');
  return s;
}


/*
 * convertTagAttributeToStyleValue()
 *
 * Attempts to transfer specified HTML attributes into the 'style' attribute for
 * the supplied start tag.
 */
function convertTagAttributeToStyleValue(s, attrName, styleAttrName) {
  // Get the style attribute value to convert
  attributePattern = new RegExp(attrName + '="([^"]*)"', 'gi');
  var matched = s.match(attributePattern);
  if (!matched) return s;
  var attrValue = RegExp.$1;

  // remove the old attribute
  s = s.replace(attributePattern, '');

  // add value as new style attribute value
  if (attrValue) {
    if (attrName == 'size') attrValue = convertFontSizeToSpan(attrValue);
    stylePattern = new RegExp('(<[^>]*style="[^"]*)("[^>]*>)', 'gi');
    if (stylePattern.test(s)) {
      var style = RegExp.$1;
      if (style.indexOf(';') == -1) style += ';';
      s = s.replace(stylePattern, style + styleAttrName + ':' + attrValue +
                    ';$2');
    } else {
      tagPattern = new RegExp('(<[^\/][^>]*)(>)', 'gi');
      s = s.replace(tagPattern, '$1 style="' + styleAttrName + ':' + attrValue +
                    ';"$2');
    }

    //prevent colors with RGB values from aggregating with keyword / hex colors
    var colorPattern = new RegExp('(color\\:[\\s]*rgb[^;]*;)color\\:[^;]*;',
                                  'gi');
    if (colorPattern.test(s)) {
      s = s.replace(colorPattern, '$1');
    }
  }

  return s;
}


/*
 * convertAllSpansToFonts()
 *
 * Attempts to transform well-formed SPAN nodes into WYSIWYG-acceptable formats.
 */
function convertAllSpansToFonts(s) {
  startTagPattern = RegExp('<[^\/]*span [^>]*>', 'gi');
  var StartTags = s.match(startTagPattern);
  if (StartTags) {
    for (var i = 0; i < StartTags.length; i++) {
      // adjacent tags get lost in some regexp searches in some browsers, so
      // we'll catch 'em here
      if (StartTags[i].indexOf('>') > 1) {
        innerStartTags = StartTags[i].split('>');
      }
      for (x = 0; x < innerStartTags.length; x++) {
        if (innerStartTags[x] == '') continue;
        var thisTag = innerStartTags[x] + '>';
        modifiedStartTag = convertTagStyleValueToAttribute(thisTag,
                                                           'font-family',
                                                           'face');
        modifiedStartTag = convertTagStyleValueToAttribute(modifiedStartTag,
                                                           'font-size',
                                                           'size');
        modifiedStartTag = convertTagStyleValueToAttribute(modifiedStartTag,
                                                           'color',
                                                           'color');

        var lastTwoCharsPattern = new RegExp(' >$', 'gim');
        modifiedStartTag = modifiedStartTag.replace(lastTwoCharsPattern, '>');
        modifiedStartTag = modifiedStartTag.replace(/<span  /gi, '<span ');

        s = s.replace(thisTag, modifiedStartTag);
      }
    }
  }
  s = s.replace(/<span ([^>]*)>/gi, '<font $1>');
  s = s.replace(/<\/span>/gi, '</font>');
  s = s.replace(/<span>/gi, '<font>');

  return s;
}


/*
 * convertTagStyleValueToAttribute()
 *
 * Attempts to transfer specified values within the 'style' attribute to single
 * HTML attributes for the supplied start tag.
 */
function convertTagStyleValueToAttribute(s, styleVal, attrName) {
  // Get the style attribute value to convert
  stylePattern = new RegExp('style="[^"]*' + styleVal + ':([^;]*)[^"]*"', 'gi');
  var matched = s.match(stylePattern);
  if (!matched) return s;
  attrValue = RegExp.$1;

  if (attrValue) {

    attrValue = Trim(attrValue);  // extra spaces will cause problems in IE

    if (styleVal == 'color') {
      var rgbPattern = new RegExp(
          'rgb\\([ ]*[\\d]*[ ]*,[ ]*[\\d]*[ ]*,[ ]*[\\d]*[ ]*\\)', 'gi');
      if (rgbPattern.test(attrValue)) {
        return s;  //TODO: add RGB to Hex conversion later
      }
    }
    if (styleVal == 'font-size') {
      attrValue = convertSpanSizeToFont(attrValue);
    }
    // remove the old style attribute
    valuePattern = new RegExp(
        '(style="[^"]*)(' + styleVal + ':[^;]*)[;]*([^"]*")', 'gi');
    s = s.replace(valuePattern, '$1$3');

    // add value as new attribute
    stylePattern = new RegExp('(<[^>]*)(style="[^>]*>)', 'gi');
    s = s.replace(stylePattern, '$1' + attrName + '="' + attrValue + '" $2');
  }

  //remove empty style pairs
  s = s.replace(/style=""/gi, '');

  return s;
}

var FONT_SIZE_CONVERSIONS = [
  ['5', '180%'],
  ['4', '130%'],
  ['3', '100%'],
  ['2', '85%'],
  ['1', '78%']
];

function convertFontSizeToSpan(size) {
  return convertFontandSpanSizes(0, 1, size);
}

function convertSpanSizeToFont(size) {
  return convertFontandSpanSizes(1, 0, size);
}

function convertFontandSpanSizes(beforeIndex, afterIndex, size) {
  var conv = FONT_SIZE_CONVERSIONS;
  size = Trim(size);
  for (z = 0; z < conv.length; z++) {
    if (size == conv[z][beforeIndex]) {
      size = conv[z][afterIndex];
      break;
    }
  }
  return size;
}

function Trim(s) {
  return s.replace(/^\s+/, '').replace(/\s+$/, '');
}


/**
 * Escape any attributes found in the given html source that could be an XSS
 * attack vector.
 * @param {string} s HTML source.
 * @return {string} Escaped HTML source.
 */
function escapeXssVectors(s) {
  var htmlDiv = document.createElement('DIV');
  htmlDiv.innerHTML = s;
  escapeXssVectorsFromChildren_(htmlDiv);
  return htmlDiv.innerHTML;
}

/**
 * Recursive helper for escapeXssVectors. Escapes attributes of any children of
 * the given element that are also elements, then recurses on that child.
 * @param {Element} elem Element whose children should be escaped.
 */
function escapeXssVectorsFromChildren_(elem) {
  for (var node = elem.firstChild; node != null; node = node.nextSibling) {
    if (node.nodeType == 1) { // 1 == ELEMENT

      if (node.nodeName.toLowerCase() == 'iframe') {
        // If src attribute contains scripting, it could be an XSS attack
        // (see {@bug 3256288}), so rename it to blogger_src so it won't get
        // executed when the html is rendered inside the editor. To be extra
        // safe, we just whitelist values starting with http(s):, so that it
        // catches javascript:, vbscript:, data:, and any other tricks that
        // may come up in the future.
        var xssSafeRegExp = /^\s*https?:/i;
        var srcValue = node.getAttribute('src');
        if (srcValue && !xssSafeRegExp.test(srcValue)) {
          node.removeAttribute('src');
          node.setAttribute('blogger_src', srcValue);
        }
      }

      // Rename any attributes starting with "on" to "blogger_on"+whatever to
      // prevent the event handler code from ever executing, as that could be an
      // XSS attack.
      var eventAttrs = [];
      for (var i = 0; i < node.attributes.length; i++) {
        var attrNode = node.attributes[i];
        if (!attrNode.specified) {
          continue;
        }
        var attrName = attrNode.name.toLowerCase();
        if (/^on/.test(attrName)) {
          eventAttrs.push(attrName);
        }
      }
      for (var j = 0; j < eventAttrs.length; j++) {
        var attrValue = node.getAttribute(eventAttrs[j]);
        node.removeAttribute(eventAttrs[j]);
        node.setAttribute('blogger_' + eventAttrs[j], attrValue);
      }

      escapeXssVectorsFromChildren_(node);

    }
  }
}

/**
 * Restore any attributes found in the given html source that were escaped via
 * escapeXssVectors.
 * @param {string} s Escaped HTML source.
 * @return {string} Restored HTML source.
 */
function restoreXssVectors(s) {
  var htmlDiv = document.createElement('DIV');
  htmlDiv.innerHTML = s;
  restoreXssVectorsFromChildren_(htmlDiv);
  return htmlDiv.innerHTML;
}

/**
 * Recursive helper for restoreXssVectors. Restores attributes of any children
 * of the given element that are also elements, then recurses on that child.
 * @param {Element} elem Element whose children should be restored.
 */
function restoreXssVectorsFromChildren_(elem) {
  for (var node = elem.firstChild; node != null; node = node.nextSibling) {
    if (node.nodeType == 1) { // 1 == ELEMENT

      if (node.nodeName.toLowerCase() == 'iframe') {
        var srcValue = node.getAttribute('blogger_src');
        if (srcValue) {
          node.removeAttribute('blogger_src');
          node.setAttribute('src', srcValue);
        }
      }

      var eventAttrs = [];
      for (var i = 0; i < node.attributes.length; i++) {
        var attrNode = node.attributes[i];
        if (!attrNode.specified) {
          continue;
        }
        var attrName = attrNode.name.toLowerCase();
        if (/^blogger_on/.test(attrName)) {
          eventAttrs.push(attrName);
        }
      }
      for (var j = 0; j < eventAttrs.length; j++) {
        var attrValue = node.getAttribute(eventAttrs[j]);
        node.removeAttribute(eventAttrs[j]);
        node.setAttribute(eventAttrs[j].substr(8), attrValue);
      }

      restoreXssVectorsFromChildren_(node);

    }
  }
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/richedit.editsource.js

// ===================================================================
// Mode Toggling
// ===================================================================

RichEditor.prototype.ShowSourceEditor = function() {
  BLOG_replaceAllImgsWithVideos(RichEdit.frameDoc);

  if (BLOG_finishSpellcheck) {
    BLOG_finishSpellcheck();
  }
  if (trOK) {
    // Strip spans introduced by translitertion which will make the content
    // unreadable to the user in Edit Html mode.
    _TR_stripAllSpans();
  }
  showElement(this.textarea);
  hideElement(this.frame);
  hideElement(this.formatbar);
  showElement(this.htmlbar);
  var ShowHtmlSource = d('ShowSourceEditor');
  if (ShowHtmlSource) {
    ShowHtmlSource.className = 'on';
    ShowHtmlSource.style.backgroundColor = '';
    getElement('ShowRichEditor').className = '';
  }

  this.movePostBodyToTextarea();
  HidePalette();

  // preview needs to know which link to hide or show
  Textbar.PREVIEW_BUTTON = Preview.HTML_PREVIEW_BUTTON;

  this.mode = this.HTML_MODE;
};



var NON_EXISTENT_URL = 'http://not-a-real-namespace/'; //dummy URL
var REMOVAL_PLACEHOLDER = '~~SPECIAL_REMOVE!#~~'; //for entity escaping

function removeRelativePathPlaceholders(doc) {
  try {
    var images = doc.images;
    var links = doc.links;
    var NON_EXISTENT_URLPattern = new RegExp(NON_EXISTENT_URL, 'gi');
    for (var i = 0; i < images.length; i++) {
      images[i].src = images[i].src.replace(NON_EXISTENT_URLPattern, '');
    }
    for (var i = 0; i < links.length; i++) {
      links[i].href = links[i].href.replace(NON_EXISTENT_URLPattern, '');
    }
  } catch (e) {}
}

function addRelativePathPlaceholders(strBody) {

  /*
   * Replace links and images that have relative links to have a dummy URL
   * in order to have them appear as requested and defeat IE's auto-complete
   */
  if (Detect.IE()) {
    var quot = "'" + '"'; // mixed-syntax easier on the eyes in code editors

    /*
     * this reg exp is pre-pending a dummy URL because IE will try and
     * auto-complete certain attributes that start with "http://"
     */
    var tagPattern
      = new RegExp('<(a|img)(([^>\\s]*[\\s]+)+)(href|src)[\\s]*=[\\s]*'
                   + '[' + quot + ']?([^' + quot + '\\s>]+)[' + quot + ']?',
                  'gi');
    strBody = strBody.replace(tagPattern, '<$1$2$4="' + NON_EXISTENT_URL
      + '$5"');
    }

    return strBody;
}


RichEditor.prototype.ShowRichEditor = function() {
  if (BLOG_finishSpellcheck) {
    BLOG_finishSpellcheck();
  }
  showElement(this.frame);
  hideElement(this.textarea);
  showElement(this.formatbar);
  hideElement(this.htmlbar);
  var ShowHtmlSource = d('ShowSourceEditor');
  if (ShowHtmlSource) {ShowHtmlSource.className = '';}
  var ShowDesignMode = d('ShowRichEditor');
  if (ShowDesignMode) {
    ShowDesignMode.className = 'on';
    ShowDesignMode.style.backgroundColor = '';
  }

  // Replace text line breaks with HTML link breaks
  var strBody = this.textarea.value;

  // Newlines within tags could present problems in WYSIWYG
  strBody = removeNewlinesFromTags(strBody);

  strBody = strBody.replace(/\&lt;/g, '&' + REMOVAL_PLACEHOLDER + 'lt;');
  strBody = strBody.replace(/\&gt;/g, '&' + REMOVAL_PLACEHOLDER + 'gt;');

  // This lets you type "&amp;apos;" into Edit HTML mode such that flipping
  // back and forth between Edit HTML and Compose won't normalize it down to "'"
  strBody = strBody.replace(/\&amp;([#a-zA-Z0-9]+;)/gi, '&amp;amp;$1');

  // Attempt to persist relative paths
  if (Detect.IE()) {
    strBody = addRelativePathPlaceholders(strBody);
  }

  if (Detect.IE()) {
    if (strBody == '<p>&nbsp;</p>') strBody = '';
    strBody = '<div>' + strBody + '</div>';
  }

  // getDesignModeHtml() converts this back to newlines, and there may be
  // be issues when copying into designMode without a newline after the
  // <br> tag for Firefox 0.8
  strBody = strBody.replace(/\n/g, '<br>');

  strBody = escapeXssVectors(strBody);
  this.frameDoc.body.innerHTML = strBody;

  if (Detect.IE()) {
    //don't let DIVs or carriage returns accumulate on each toggle
    var repeatedDivPattern = new RegExp('^(<div>\r\n){1,}', 'gi');
    var repeatedDivLinePattern = new RegExp('(<div></div>)$', 'gi');
    var repeatedDivEndPattern = new RegExp('<br>(<\/div>){1,}$', 'gi');
    this.frameDoc.body.innerHTML
      = this.frameDoc.body.innerHTML.replace(repeatedDivPattern, '');
    this.frameDoc.body.innerHTML
      = this.frameDoc.body.innerHTML.replace(repeatedDivLinePattern, '');
    this.frameDoc.body.innerHTML
      = this.frameDoc.body.innerHTML.replace(repeatedDivEndPattern, '</div>');
  }

  this.frameDoc.body.innerHTML
    = cleanHTML(this.frameDoc.body.innerHTML);
  this.frameDoc.body.innerHTML
    = convertAllSpansToFonts(this.frameDoc.body.innerHTML);

  // Remove all encoding placeholders
  var removalPattern = new RegExp(REMOVAL_PLACEHOLDER, 'g');
  this.frameDoc.body.innerHTML
    = this.frameDoc.body.innerHTML.replace(removalPattern, '');

  // Remove dummyURL in order to make links and images with relative links
  // appear as requested and defeat IE's auto-complete
  removeRelativePathPlaceholders(this.frameDoc);

  // must remind moz that designmode is on after its display style is changed
  if (!Detect.IE()) this.frameDoc.designMode = 'On';

  // preview needs to know which link to hide or show
  Textbar.PREVIEW_BUTTON = Preview.PREVIEW_BUTTON;

  this.mode = this.DESIGN_MODE;

  if (trOK) {
    // Insert back spans required by translitertion while switching back from
    // Edit Html mode to Compose mode.
    _TR_insertBackSpans();
  }

  BLOG_replaceAllVideosWithImgs();
};

// ===================================================================
// Mode-to-Mode HTML Transfer
// ===================================================================

RichEditor.prototype.movePostBodyToTextarea = function() {
  this.textarea.value = getDesignModeHtml();
  this.textarea.value = cleanHTML(this.textarea.value);
  this.textarea.value = convertAllFontsToSpans(this.textarea.value);
  var val = this.textarea.value;
  /*
   * fix for showing a single newline when toggling from Compose
   * to Edit HTML
   */
  if (val == '\r\n' || val == '\n') this.textarea.value = '';

  // An IE fix for removing sometimes-inserted empty lines
  if (Detect.IE()) {
    if (val == '<p>&nbsp;</p>' || val == '<div></div>') {
      this.textarea.value = '';
    }
  }
};

function ShowHtml() {
  RichEdit.textarea.value = getDesignModeHtml();
}

function getDesignModeHtmlWithPreprocessor(preprocessor) {
  // Replace HTML line breaks with text line breaks
  var tmpBody = RichEdit.frameDoc.createElement('div');
  tmpBody.innerHTML = RichEdit.frameDoc.body.innerHTML;

  // Replace any video stubs with object/embed tags.
  BLOG_replaceAllImgsWithVideos(tmpBody);

  var strBody = tmpBody.innerHTML;
  strBody = preprocessor(strBody);
  if (Detect.IE()) {

    // Do our best to make the MSHTML markup valid.
    strBody = cleanMSHTML(strBody);

  } else {

    // Midas can crash the browser if enough Windows line feeds accumulate
    strBody = strBody.replace(/\r/g, '');

    /*
     * Mozilla needs the space replacement or it will shove words at end-of-line
     * boundaries together
     */
    strBody = strBody.replace(/\n/g, ' ');

    // get rid of trailing spaces after <br>
    strBody = strBody.replace(/<br> /gi, '<br>');

    // convert HTML line breaks to newlines
    strBody = strBody.replace(/<br>/gi, '\n');
  }

  if (RichEdit.ALLOW_HTML_ENTRY) {
    strBody = strBody.replace(/\&lt;/g, '<');
    strBody = strBody.replace(/\&gt;/g, '>');
    strBody = strBody.replace(/\&amp;lt;/g, '&lt;');
    strBody = strBody.replace(/\&amp;gt;/g, '&gt;');

    // strBody is HTML-escaped, such that if an author typed a literal '&' in
    // compose mode, we'd see '&amp;' here. We want to detect entities written
    // literally in compose mode (e.g. '&apos;') and unencode them here by
    // converting the '&amp;' down to a '&.' We try to limit this conversion
    // only to '&amp;' strings that are part of literally-written entities.
    strBody = strBody.replace(/\&amp;([#a-zA-Z0-9]+;)/gi, '&$1');
  }

  // convert space entities
  strBody = strBody.replace(/\&nbsp;/g, ' ');
  strBody = strBody.replace(/ \n/g, '\n');

  strBody = restoreXssVectors(strBody);

  return strBody;
}

/*
  The distinction between getDesignModeHtml and
  getDesignModeHtmlWithoutDOMSideEffects is that the former runs
  replaceEmptyDIVsWithBRs, while the latter does not. That function modifies
  the innerHTML of an element, which has the side effect of dumping IE's undo
  history for the compose window.

  getDesignModeHtmlWithoutDOMSideEffects should be called by periodic,
  background loops that do not wish to interfere with the undo history. For
  example, the function that saves the post for recover post. See bug 134099.
*/

function getDesignModeHtml()
{
  var ieDivCleaner = function(str) {
    if (Detect.IE()) {
      str = replaceEmptyDIVsWithBRs(str);
    }

    return str;
  }

  return getDesignModeHtmlWithPreprocessor(ieDivCleaner);
}

function getDesignModeHtmlWithoutDOMSideEffects() {
  return getDesignModeHtmlWithPreprocessor(function(s) {return s;});
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/richedit.blogger.js
/*
 * Copyright, Google.com - 2004
 * Author: Chris Wetherell
 */

function BloggerRichEditor() {
  this.ALLOW_HTML_ENTRY = true;
  this.ALLOW_LINK_ONLY_PASTE = true;
  this.PREVIEW_IS_HIDDEN = true;

  if (editorModeDefault) {
    this.START_MODE = editorModeDefault; // set in EditPost.gxp
  }

  this.frameBodyStyle = 'border:0;margin:0;padding:3px;width:auto;' +
      'font:normal 100% Georgia, serif;';

  this.KEY_COMMANDS.push(
    ['CTRL_SHFT_P', null, 'toggle(e);'],
    ['CTRL_SHFT_D', null, 'd(Posting.DRAFT_BUTTON).click();'],
    ['CTRL_P', null, 'd(Posting.PUBLISH_BUTTON).click(); preventDefault(e);'],
    ['CTRL_D', null, 'd(Posting.DRAFT_BUTTON).click();']
  );

  // TODO(tnicholas): Ctrl+Shift+X should toggle directionality for FF

  if (isAutosaveEnabled()) {
    this.KEY_COMMANDS.push(
        ['CTRL_SHFT_S', null, 'd(Posting.AUTOSAVE_BUTTON).click();'],
        ['CTRL_S', null, 'd(Posting.AUTOSAVE_BUTTON).click();']
    );
  }

  this.CONTROLS = [
      ['FontName', null, null, false, this.FONTS],
      ['FontSize', font_size_tt, 'gl.size', true, null, 'toggleFontSizeMenu()'],
      ['|'],

      ['Bold', bold_tt, 'gl.bold', true],
      ['Italic', italic_tt, 'gl.italic', true],
      ['|'],

      ['ForeColor', text_color_tt, 'gl.color.fg', true],
      ['|'],

      ['CreateLink', link_tt, 'gl.link', true],
      ['|']];

  // Buttons need to be in opposite order for RTL
  if (userRTL) {
    this.CONTROLS.push(
        ['JustifyRight', align_right_tt, 'gl.align.right', true],
        ['JustifyCenter', align_center_tt, 'gl.align.center', true],
        ['JustifyLeft', align_left_tt, 'gl.align.left', true]);
  } else {
    this.CONTROLS.push(
        ['JustifyLeft', align_left_tt, 'gl.align.left', true],
        ['JustifyCenter', align_center_tt, 'gl.align.center', true],
        ['JustifyRight', align_right_tt, 'gl.align.right', true]);
  }

  this.CONTROLS.push(
      ['JustifyFull', justify_full_tt, 'gl.align.full', true],
      ['|'],

      ['InsertOrderedList', numbered_list_tt, 'gl.list.num', true],
      ['InsertUnorderedList', bulleted_list_tt, 'gl.list.bullet', true],
      ['Blockquote', blockquote_tt, 'gl.quote', true],
      ['|']);

  if (userCanSpellcheck) {
    this.CONTROLS.push(['SpellCheck', check_spelling_tt, 'gl.spell', true, null,
                        'BLOG_spellcheck();'],
                       ['|']);
  }

  if (userCanUploadPhotos) {
    this.CONTROLS.push(
        ['Add_Image', add_image_tt, 'gl.photo', true, null, '', 'addImage();'],
        ['|']);
  }

  if (userCanUploadVideo) {
    this.CONTROLS.push(
        ['Add_Video', add_video_tt, 'gl.video', true, null, '', 'addVideo();'],
        ['|']);
  }

  if (videoUploadDistress) {
    this.CONTROLS.push(
        ['Add_Video', unable_add_video_distress_tt, 'gl.video.disabled', true,
         null, '', 'showVideoUploadDistress();'],
        ['|']);
  }

  if (userCanUploadFiles) {
    this.CONTROLS.push(
        ['Upload_File', upload_file_tt, 'gl.file', true, null, '',
         'uploadFile();'],
        ['|']);
  }

  this.CONTROLS.push(['RemoveFormat', remove_format_tt, 'gl.clean', true],
                     ['|']);

  if (SHOW_DIRECTIONALITY_BUTTONS) {
    // opposite order for LTR
    if (userRTL) {
      this.CONTROLS.push(['BlockDirRTL', dir_rtl_tt, 'gl.rtl', true],
                         ['BlockDirLTR', dir_ltr_tt, 'gl.ltr', true]);
    } else {
      this.CONTROLS.push(['BlockDirLTR', dir_ltr_tt, 'gl.ltr', true],
                         ['BlockDirRTL', dir_rtl_tt, 'gl.rtl', true]);
    }
  }

  if (trOK) {
    // Add the transliteration button after all other buttons.
    _TR_registerButtonControls(this.CONTROLS);
    _TR_makeButtonDepressible(this.DEPRESSABLE);
  }

  if (userCanSpellcheck) {
    this.HTML_CONTROLS.push(
        ['BLOG_spellcheck();', check_spelling_tt, 'gl.spell'],
        ['|']);
    }

  if (userCanUploadPhotos) {
    this.HTML_CONTROLS.push(['', add_image_tt, 'gl.photo', '', 'addImage();'],
                            ['|']);
  }

  if (userCanUploadVideo) {
    this.HTML_CONTROLS.push(
        ['', add_video_tt, 'gl.video', 'Add_Video', 'addVideo();'],
        ['|']);
  }

  if (videoUploadDistress) {
    this.HTML_CONTROLS.push(
        ['', unable_add_video_distress_tt, 'gl.video.disabled', 'Add_Video',
         'showVideoUploadDistress();'],
        ['|']);
  }

  if (userCanUploadFiles) {
    this.HTML_CONTROLS.push(
        ['', upload_file_tt, 'gl.file', '', 'uploadFile();'],
        ['|']);
  }

  this.APPEND_TO_ACTIONS = [
    ['ShowSourceEditor', 'hidePreview();\n restorePostOptions();', true],
    ['ShowRichEditor', 'hidePreview();\n restorePostOptions();', true]
  ];

  this.DEBUG = false;
  this.DEBUG_FRAME_KEY_EVENTS = false;
  this.DEBUG_CHECK_FORMATTING = false;
}

BloggerRichEditor.prototype = new RichEditor();


function BloggerPreviewOnlyEditor() {
  this.MODE_TABS = [];
  this.ENABLE_IFRAME = false;
  this.PREVIEW_IS_HIDDEN = true;
  this.START_MODE = this.HTML_MODE;

  this.HTML_CONTROLS = [];

  this.HTML_CONTROLS.push(['BLOG_spellcheck();', check_spelling_tt, 'gl.spell'],
                          ['|']);

  if (userCanUploadPhotos) {
    this.HTML_CONTROLS.push(['addImage();', add_image_tt, 'gl.photo'], ['|']);
  }
  if (userCanUploadFiles) {
    this.HTML_CONTROLS.push(['uploadFile();', upload_file_tt, 'gl.file'],
                            ['|']);
  }
  this.HTML_CONTROLS.push(['toggle();', preview, null, 'PreviewAction']);

  this.ENABLE_KEYBOARD_CONTROLS = false;
  this.DEBUG = false;
}
BloggerPreviewOnlyEditor.prototype = new RichEditor();

/*
 * setBloggerEditor()
 *
 * Sets the editor based on what browser supports which feature set.
 * Requires: Detect() object from detect.js
 */
function setBloggerEditor() {
  if (Detect.IE_5_5_newer() || Detect.MOZILLA()) {
    setRichTextEditor();
    /*
     * Mozilla needs the frame body to have some value before certain
     * operations can take place.
     */
    if (RichEdit.frameDoc.body.innerHTML == '' && Detect.MOZILLA()) {
      RichEdit.frameDoc.body.innerHTML = IFRAME_INIT_MESSAGE;
    }

    // Hide the post options div for browsers that support
    // RichTextEditor
    hidePostOptions();
    if (hasDateTimeErrors()) {
      showPostOptions();
    }
  } else if (Detect.SAFARI() || Detect.OPERA()) {
    setPreviewOnlyEditor();
  }
}

/*
 * setRichTextEditor()
 *
 * Creates the Blogger WYSIWYG editor interface.
 */
function setRichTextEditor() {
  new BloggerRichEditor().make();
  NestlingFormFields();
  // Move the labels/options zippy inside the bottom beige bar.
  var elt = d('labels-container');
  if (elt) RichEdit.editarea.appendChild(elt);
}

/*
 * setPreviewOnlyEditor()
 *
 * Creates an editor interface that has a Preview function on the
 * mode bar that works with Safari 1.2- and Opera.  Ideally, it should be the
 * "Edit HTML" textarea interface including all of its formatting functions
 * including key commands.
 */
function setPreviewOnlyEditor() {
  new BloggerPreviewOnlyEditor().make();
  d('RichEdit').style.marginTop = '1em';
}

/*
 * NestlingFormFields()
 *
 * Adjusts the form fields' positions to align more snugly with the tabbed
 * WYSIWYG editor. Don't move around when we have enclosures box, because the
 * layout breaks (at least in IE). Also don't move stuff around in bidi
 * because it makes the form overlap.
 */
function NestlingFormFields() {
  // userRTL is defined in the calling page
  if (!d('showEnclosuresLink') && !userRTL) {
    var titles = d('titles');
    var editor = d('RichEdit');

    // Reset everything in case we are getting called multiple times.
    if (titles) {
      titles.style.position = 'relative';
      titles.style.top = '0px';
      editor.style.paddingTop = '0px';
    }

    // re-position the title fields, since IE requires this area to be
    // absolutely positioned in order for them to be placed as a layer
    // above the editor
    var pos = getXY(editor);
    if (titles) {
      titles.style.position = 'absolute';
      titles.style.zIndex = '1';
      // We can't use left-based absolute positioning in RTL,
      // and it seems to work without it
      if (!userRTL) {
        titles.style.left = pos.x + 'px';
      }
      titles.style.top = (pos.y - titles.offsetHeight + 5) + 'px';

      // the titles table can have a varying number of rows, and thus vary in
      // height.  The editor needs to be set accordingly.
      titleHeight = titles.offsetHeight;
      if (titleHeight > 40) {
        titleHeight -= 20;
      } else {
        titleHeight = 15;
      }
      editor.style.paddingTop = titleHeight + 'px';
    }
  }
}


// ===================================================================
// Keyboard commands for the document area OUTSIDE of the WYSIWYG editor
// ===================================================================

document.onkeypress = function(e) {
  var e = getEvent(e);

  setKeysetByEvent(e);

  if (CTRL_SHFT_P) toggle();

  if (CTRL_P) {
    d(Posting.PUBLISH_BUTTON).click();
    preventDefault(e);
  }

  if (CTRL_D || CTRL_SHFT_D) d(Posting.DRAFT_BUTTON).click();

  if (isAutosaveEnabled()) {
    if (CTRL_S || CTRL_SHFT_S) d(Posting.AUTOSAVE_BUTTON).click();
  }

  return true;
};

// ===================================================================
// Form utilites (Posting page only)
// ===================================================================

/**
 * setFocus()
 *
 * Sets the focus to the first available form field of the editor.
 */
function setFocus() {
  // ensure that keypresses don't submit form or prevent tabbing
  setPostingFormEvents();

  var title = d(Posting.TITLE);
  var url = d(Posting.URL);
  var editor = d(RichEdit.frameId);

  if (title && (title.style.display != '')) {
    if (Detect.IE_5_5_newer()) d(RichEdit.frameId).tabIndex = 2;
    title.focus();
  }
  else if (url && (url.style.display != '')) {
    if (Detect.IE_5_5_newer()) d(RichEdit.frameId).tabIndex = 3;
    url.focus();
  }
  else if (editor) {
    if (Detect.IE_5_5_newer()) editor.tabIndex = 1;
    RichEdit.frame.contentWindow.focus();
  }
}

function setPostingFormEvents() {
  if (d(Posting.URL)) {
    d(Posting.URL).onkeypress = OnPostingFormKeypress;
  }
  if (d(Posting.TITLE)) {
    d(Posting.TITLE).onkeypress = OnPostingFormKeypress;
  }
}

function OnPostingFormKeypress(e) {
  var evt = getEvent(e);
  // prevent pressing Enter on a single-text-field form from submitting
  if (getKey(evt) == RETURN) {
    if (Detect.IE()) evt.returnValue = false;
    return false;
  }
  // Mozilla has a different way of focusing within designMode window objects.
  if (Detect.MOZILLA() && RichEdit != undefined) {
    var src = getEventSource(evt);
    if (!isShiftKeyPressed(evt)) { // allow for back tabbing
      if (src.id == Posting.URL) {
        MozillaTabToFrameFix(evt);
      } else if ((src.id == Posting.TITLE) && !d(Posting.URL)) {
        MozillaTabToFrameFix(evt);
      }
    }
  }
}

// So that Firefox 0.7+ can tab to the IFRAME...
function MozillaTabToFrameFix(evt) {
  setKeysetByEvent(evt);
  if (TAB && RichEdit.mode == RichEdit.DESIGN_MODE) {
    var html = RichEdit.frameDoc.body.innerHTML;
    /*
     * Mozilla needs the frame body to have some value before certain
     * operations can take place.
     */
    if (html = '' || html == '<br>') {
      RichEdit.frameDoc.body.innerHTML = IFRAME_INIT_MESSAGE;
    }
    setRichEditFocus();
    evt.preventDefault();
  }
}

// Get the post body.
function getPost() {
  if (RichEdit.mode == RichEdit.DESIGN_MODE) {
    if (Detect.IE()) {
      return RemoveLinksWithinTags(getDesignModeHtmlWithoutDOMSideEffects());
    } else {
      return getDesignModeHtml();
    }
  }
  if (RichEdit.mode == RichEdit.HTML_MODE) {
    return getElement(Preview.TEXTAREA).value;
  }
}

// Set the editor text.
function setPost(data) {
  if (RichEdit) {
    getElement(Preview.TEXTAREA).value = data;
  }
  if (RichEdit.mode == RichEdit.DESIGN_MODE) {
    RichEdit.ShowRichEditor();
  }
}

function savePostToWYSIMWYG(html) {
  if (html) setPost(html + getPost());
}

// open the add video dialog
function addVideo() {
  if (BLOG_currentVideoUploadStatus == BLOG_VideoUploadStatus.NONE) {
    // If you don't remove the iframes, you may cover the video dialog.
    if (RichEdit.mode == RichEdit.DESIGN_MODE) {
      BLOG_removePreviewIframes(RichEdit.frameDoc);
    }

    var locationDiv = document.getElementById('editarea');
    var params = new Array(2);
    params[0] = 'blogID';
    params[1] = blogId;
    BLOG_retrieveDialog('/video-upload.g', params, locationDiv, true);
  }
}

// add an image to a post
function addImage() {
  if (!Blsp.running) {
    if (isAutosaveEnabled()) {
      BLOG_autosave.saveCallback();
    }
    prefs = 'toolbar=0,scrollbars=1,location=0,statusbar=1,menubar=0,' +
            'resizable=1,width=770,height=425,top=150';

    // Note: imagesHostUrl is a global variable that needs to be set
    // before calling this method
    window.open(imagesHostUrl + 'upload-image.g?blogID='
        + document.stuffform.blogID.value, 'bloggerPopup', prefs);
  }
}

/*
 * Popup function that will close the window if it's already
 * open so that the new window is guaranteed to come out on top.
 */
function popUp(URL, preferences) {
  if (window.popup) {
    try {
      window.popup.close();
    } catch (e) {}
  }

  if (!preferences) {
    preferences = 'toolbar=0,scrollbars=1,location=0,statusbar=1,' +
    'menubar=0,resizable=1,width=490,height=400,top=150';
  }

  window.popup = window.open(URL, 'bloggerPopup', preferences);
}

/*
 * cleanupDeletion()
 *
 * Since we can't get the selection of certain elements with certainty, we
 * have to cleanup extraneous markup downstream on occasion.
 */
BloggerRichEditor.prototype.cleanupDeletion = function() {
  var tags = this.frameDoc.body.getElementsByTagName('font');
  for (var i = 0; i < tags.length; i++) {
    var el = tags[i];
    // cleanup Blogger Images markup
    this.removeEmptyImageElements(el);
  }
};

/*
 * removeEmptyImageElements()
 *
 * If the given element is a Blogger Image container, then the node is
 * removed while its content is retained.
 */
BloggerRichEditor.prototype.removeEmptyImageElements = function(obj) {
  if (!obj) return;
  var imgTagPattern = new RegExp('<img', 'i');
  if (obj.className == 'blogger-image' && !obj.innerHTML.match(imgTagPattern)) {

    // remove the last node if it's the carriage return that was inserted for
    // Firefox
    if (Detect.MOZILLA()) {
      var last_node = obj.childNodes[obj.childNodes.length - 1];
      obj.removeChild(last_node);
    }

    // if there's content within the old container, insert it to a new node
    // in order to cleanup the styles before removing the container
    var DELETE_ID = 'TEMP-RICHEDIT-DELETE';
    if (obj.innerHTML.length > 0) {

      // make a temporary new node in the IFRAME
      var new_node = this.frameDoc.createElement('font');
      new_node.id = DELETE_ID;

      // transfer the content of the image container to a new container
      new_node.innerHTML = obj.innerHTML;
      obj.parentNode.insertBefore(new_node, obj);

    }

    // remove the old, empty image container
    obj.parentNode.removeChild(obj);

    // cleanup unneeded elements
    if (this.frameDoc.getElementById(DELETE_ID)) {

      // remove the temp element but keep its content
      var deletePattern = new RegExp('<font id="' + DELETE_ID + '">' +
        new_node.innerHTML + '</font>', 'i');

      this.frameDoc.body.innerHTML =
        this.frameDoc.body.innerHTML.replace(deletePattern, new_node.innerHTML);

      // occasionally a stray empty temp element can remain (especially in IE)
      // and should be removed
      var deletePattern2 = new RegExp('<font id="' + DELETE_ID +
        '"></font>', 'i');

      this.frameDoc.body.innerHTML =
        this.frameDoc.body.innerHTML.replace(deletePattern2, '');

    }
  }
};


/*
 * Fix persistent image selection bug in Mozilla
 */
function deselectBloggerImageGracefully() {
  try {
    var re = RichEdit;
    hideElement(re.frame);
    showElement(re.frame);
    re.frameDoc.designMode = 'On';
    var html = re.frameDoc.body.innerHTML;
    setRichEditFocus();
    re.frameDoc.body.innerHTML = html;
  } catch (e) {}
}


/**
 * Puts the options triangle in the open position
 */
function openOptionsTriangle() {
  var triangleImg = document.getElementById('optionsTriangle');
  triangleImg.src = '/img/triangle_open.gif';
}

/**
 * Puts the options triangle in the closed position
 */
function closeOptionsTriangle() {
  var triangleImg = document.getElementById('optionsTriangle');
  if (userRTL) {
    triangleImg.src = '/img/triangle_rtl.gif';
  } else {
    triangleImg.src = '/img/triangle_ltr.gif';
  }
}

/**
 * True if the post-options triangle is open
 */
function isTriangleOpen() {
  var triangleImg = document.getElementById('optionsTriangle');
  return triangleImg.src == '/img/triangle_open.gif';
}

/**
 * Toggles the visibility of the postoptions div
 */
function togglePostOptions() {
  var div = document.getElementById('postoptions');
  if (div.style.display == 'none') {
    div.style.display = 'block';
    openOptionsTriangle();
  } else {
    div.style.display = 'none';
    closeOptionsTriangle();
  }
}

/**
 * Restores the visibility of the postoptions div based on
 * the state (src) of the optionsTriangle image.
 */
function restorePostOptions() {
  var div = document.getElementById('postoptions');
  var triangleImg = document.getElementById('optionsTriangle');

  if (div == null) {
    return;
  }

  // triangleImg will be null when u have a BloggerPreviewOnlyEditor
  // In that case restorePostOptions should make the div visible
  if (triangleImg == null || isTriangleOpen()) {
    div.style.display = 'block';
  } else {
    div.style.display = 'none';
  }
}

/**
 * Hides the postoptions div in the rich editor
 */
function hidePostOptions() {
  var div = document.getElementById('postoptions');
  if (div != null) {
    div.style.display = 'none';
  }
}

/**
 * Opens the postoptions div of the rich editor
 */
function showPostOptions() {
  var div = document.getElementById('postoptions');
  if (div != null) {
    div.style.display = 'block';
    openOptionsTriangle();
  }
}

/** Initialize the autocomplete label for the label input. */
function initAutocomplete() {
  if ((typeof BLOG_allLabelsList) != 'undefined') {
    _ac_install();
    var store = new _AC_SimpleStore(BLOG_allLabelsList);
    _ac_register(function(node, keyEvent) {
      if (node.id == 'post-labels') {
        return store;
      }

      return null;
    });
  }
}

/**
 * Responds to a click on a label by adding that label to the input box. */
function BLOG_selectLabel(label) {
  var labelInput = document.getElementById('post-labels');
  if (!labelInput) return;
  var curVal = Trim(labelInput.value);

  if (curVal == '') {
    labelInput.value = label.innerHTML;
  } else {
    // Remove excess whitespace
    var newLabel = Trim(label.innerHTML);
    var labels = curVal.split(',');
    var found = false;
    // See if the label already is in the text box
    for (var i = 0; i < labels.length; i++) {
      labels[i] = Trim(labels[i]);
      if (labels[i] == newLabel) found = true;
    }
    // If not, add it.
    if (!found) {
      labels[labels.length] = newLabel;
    }
    // Remove any whitespace-only elements from the array.
    var newLabels = new Array();
    for (var i = 0; i < labels.length; i++) {
      if (labels[i] != '') {
        newLabels[newLabels.length] = labels[i];
      }
    }
    // Put it back together.
    labelInput.value = newLabels.join(', ') + ', ';
  }
}

/** Show the list of the blog's labels. */
function BLOG_showLabels() {
  showElement(d('all-labels'));
  showElement(d('hide-labels-link'));
  hideElement(d('show-labels-link'));
}

/** Hide the list of the blog's labels. */
function BLOG_hideLabels() {
  hideElement(d('all-labels'));
  hideElement(d('hide-labels-link'));
  showElement(d('show-labels-link'));
}

function toggleFontSizeMenu() {
  var button = d('formatbar_FontSize');
  var menu = d('FontSize_menu');

  if (menu.style.display == 'none') {
    menu.style.display = 'block';
    addClass(button, 'menu-top');
  } else {
    menu.style.display = 'none';
    removeClass(button, 'menu-top');
  }
}

/**
 * Hides the font size menu. If opt_event is specified, doesn't hide if the
 * click was for the menu, since the menu would get hidden by
 * toggleFontSizeMenu() anyway.
 */
function hideFontSizeMenu(opt_event) {
  if (!opt_event
      || getEventSource(opt_event).parentNode.id != 'formatbar_FontSize') {
    var button = d('formatbar_FontSize');
    var menu = d('FontSize_menu');

    if (menu) {
      menu.style.display = 'none';
    }

    if (button) {
      removeClass(button, 'menu-top');
    }
  }
}

function buildFontSizeMenu() {
  var button = d('formatbar_FontSize');

  if (!button) {
    return;
  }

  var menu = document.createElement('UL');
  menu.id = 'FontSize_menu';
  menu.className = 'menu';
  menu.style.display = 'none';

  addFontSizeMenuItem(menu, 'Smallest', '1');
  addFontSizeMenuItem(menu, 'Small', '2');
  addFontSizeMenuItem(menu, 'Normal', '3');
  addFontSizeMenuItem(menu, 'Large', '4');
  addFontSizeMenuItem(menu, 'Largest', '5');

  var holder = document.createElement('DIV');
  holder.className = 'menu-holder';
  holder.appendChild(menu);

  button.parentNode.insertBefore(holder, button);
}

function addFontSizeMenuItem(menu, text, size) {
  var item = document.createElement('LI');
  var link = document.createElement('A');
  var font = document.createElement('FONT');
  font.setAttribute('size', size);
  font.appendChild(document.createTextNode(text));

  link.appendChild(font);
  link.setAttribute('unselectable', 'on');
  link.onmousedown = function() {
    RichEdit.frameDoc.execCommand('FontSize', false, size);
    toggleFontSizeMenu();
    setTimeout('setRichEditFocus()', '100');
    return false;
  }
  link.onmouseover = function() {
    addClass(link, 'hover');
  }
  link.onmouseout = function() {
    removeClass(link, 'hover');
  }

  item.appendChild(link);
  menu.appendChild(item);
}



/**
* Prepare and set up rich editor.
*/
function loadEditor() {
  setBloggerEditor();
  setFocus();
  editorCleanup();
  cleanForm();
  highlightErrorWrapper();
  initAutocomplete();

  if (EditorAvailable()) {
    buildFontSizeMenu();

    hidePostOptions();
    if (hasDateTimeErrors()) {
      showPostOptions();
    }
    BLOG_addSpellcheckHtml();
    if (trOK) {
      // Initializes transliteration objects, and enables transliteration
      // in the 3 input areas (richedit, title and labels boxes)
      _TR_initializeIndicTransliteration();
      // Insert spans required by transliteration when we come to the
      // Compose window through "Edit Post".
      _TR_insertBackSpans();
    }

    // forceRight is defined in RichEditVars.gxp
    if (!Detect.SAFARI() && forceRight) {
      var initialMode = RichEdit.mode;
      if (initialMode == RichEdit.HTML_MODE) {
        RichEdit.ShowRichEditor();
      }

      // Turn on right-justify
      RichEdit.frameDoc.execCommand('JustifyRight', false, '');
      // Show the button, if present, as clicked
      if (d('formatbar')) {
        ButtonMouseDown(d('formatbar_JustifyRight'));
      }
      if (SHOW_DIRECTIONALITY_BUTTONS) {
        // Turn on dir-rtl
        if (Detect.IE()) {
          RichEdit.frameDoc.execCommand('BlockDirRTL', false, '');
        } else if (Detect.MOZILLA()) {
          RichEdit.FirefoxSetDirectionality('rtl');
        }
        if (d('formatbar')) {
          ButtonMouseDown(d('formatbar_BlockDirRTL'));
        }
      }

      if (initialMode == RichEdit.HTML_MODE) {
        RichEdit.ShowSourceEditor();
      }
    }

    // Set tabIndex for the rich text editor.
    var iframe = getIFrame(RichEdit.frameId);
    if (iframe) {
      iframe.tabIndex = 5;

      var transliterationSpan = document.getElementById('language_menu');
      if (transliterationSpan) {
        // Transliteration is enabled and sets its tabIndex to 1.  Increase
        // it so that it doesn't interfere with tabbing from subject to body.
        var transliterationDiv = transliterationSpan.firstChild;
        transliterationDiv.tabIndex = 100;
      }
    }
  }

  if (window.BLOG_Status) {
    var status = new BLOG_Status();
    status.start();
  }
}

/**
 * Determine if there is a date time error message
 */
function hasDateTimeErrors() {
  var postDateTimeMsgDiv = document.getElementById('postDateTimeMsgDiv');
  return (postDateTimeMsgDiv != null) &&
      (postDateTimeMsgDiv.getElementsByTagName('div').length > 0);
}

/**
 * Cancels the event if its cancelable, and stops propagation.
 */
function preventDefault(e) {
  if (Detect.IE()) {
    // Prevent ctrl-p keyboard shortcut from printing, by setting keycode to 0.
    if (e.ctrlKey && e.keyCode == 80) {
      e.keyCode = 0;
    }

    e.cancelBubble = true;
    e.returnValue = false;
  } else {
    e.stopPropagation();
    e.preventDefault();
  }
}

//java/com/google/blogger/b2/staticresources/html/app/scripts/ff.directionality.js
/* Copyright 2007 Google, Inc.  All rights reserved.
 *
 * Firefox-specific directionality functions for Blogger.
 *
 * @author tnicholas@google.com (Tyrone Nicholas)
 */

/**
 * from mozilla, determines if element is block or not
 */
function IsBlockElement(node) {
  // Try to locate the closest ancestor with display:block
  var v = node.ownerDocument.defaultView;
  if (node.nodeType == ELEMENT_NODE) {
    var display = v.getComputedStyle(node, '').getPropertyValue('display');
    if (display == 'block' || display == 'table-cell' ||
        display == 'table-caption' || display == 'list-item')
      return true;
  }
  return false;
}

/**
 * To position selection (cursor) by reset range to a reasonable value. This
 * is called by MozChangeDirection before it returns. We should allow
 * user to change direction back and continue edit without any additional
 * cursor re-position operation
 *
 * @param end This var hold the container element whose dir is changed.
 *   we need to get down to child node and collapse.
 * @param range The range that need to be adjusted.
 */
function MozSetRangeToEnd(end, range) {
  while (end.hasChildNodes()) {
    end = end.lastChild;
  }
  var position = end.length || 0;
  range.setStart(end, position);
  range.setEnd(end, position);
  range.collapse(true);
}

/**
 * Code finds most closely related ancestor that is block node
 */
function FindClosestBlockElement(node) {
  // Try to locate the closest ancestor with display:block
  while (node) {
    if (IsBlockElement(node)) {
      return node;
    }
    node = node.parentNode;
  }
  return node;
}

/**
 * Method obtains selection, then iterates through all its ranges, then travers-
 * es their subtrees, setting the attr attribute of blocknodes to the
 * value val
 */
function ApplyAttributeToSelectedBlocks(attr, val) {
  // propagate throughout subtree
  // below adapted from mozilla
  var sel = GetSelection();
  for (var i = 0; i < sel.rangeCount; ++i) {
    var range = sel.getRangeAt(i);
    var start = range.startContainer;
    var end = range.endContainer;

    if (start.nodeName == 'BODY') {
      start = range.startContainer.firstChild;
      end = range.startContainer.lastChild;
    }

    var node = start;
    do {
      var closestBlockElement = FindClosestBlockElement(node);
      if (closestBlockElement) {
        closestBlockElement.setAttribute(attr, val);
      } else {
        break;
      }

      if (node == end) {
        break;
      }

      // Traverse through the tree in order
      if (node.firstChild) {
        node = node.firstChild;
      } else if (node.nextSibling) {
        node = node.nextSibling;
      } else {
        while (node = node.parentNode) {
          if (node.nextSibling) {
            node = node.nextSibling;
            break;
          }
        }
      }
    } while (node);
  } //end for
}

/*
 * FirefoxSetDirectionality()
 *
 * Applies dir= formatting to the currently selected block-level element.
 * Used for Firefox only; IE uses frameDoc.execCommand
 *
 * @param dir  should be either 'rtl' or 'ltr'
 */
RichEditor.prototype.FirefoxSetDirectionality = function(dir) {
  var range = GetRange();
  var start = range.startContainer;
  var end = range.endContainer;

  //step out of list item, we need to act on list as a whole
  while (start.parentNode.nodeName == 'LI') {
    start = start.parentNode;
  }
  while (end.parentNode.nodeName == 'LI') {
    end = end.parentNode;
  }

  // Set justification first, but no need to set for list (which will
  // follow direction)
  if (start.nodeName != 'LI' && end.nodeName != 'LI') {
    var edoc = this.frameDoc;
    if (dir == 'rtl') {
      edoc.execCommand('JustifyRight', false, '');
    } else {
      edoc.execCommand('JustifyLeft', false, '');
    }
  }

  // all this complexity is necessary for mozilla because where ie
  // encapsulates all #text nodes in block DIV elements, mozilla delimits
  // them with non-block BR's.
  try {
    //'clean' case of range not involving multiple parents
    // i.e. collapsed selection
    if (start.parentNode == end.parentNode) {
      // special handling of list block, apply dir to list as a whole.
      if (start.nodeName == 'LI' && end.nodeName == 'LI') {
        start.parentNode.setAttribute('dir', dir);
        return;
      }

      // set all leading block element without creating new one.
      while (IsBlockElement(start)) {
        start.setAttribute('dir', dir);
        if (start == end) {
          break;
        }
        start = start.nextSibling;
      }

      // set all trailing block element without creating new one.
      while (IsBlockElement(end)) {
        // nothing left?
        if (start == end) {
          return;
        }
        end.setAttribute('dir', dir);
        end = end.previousSibling;
      }

      //find the br's which delimit our area of modification
      while (start.previousSibling != null &&
        !IsBlockElement(start.previousSibling) &&
        start.nodeName != 'BR') {
        start = start.previousSibling;
      }
      while (end.nextSibling != null &&
        !IsBlockElement(end.nextSibling) &&
        end.nodeName != 'BR') {
        end = end.nextSibling;
      }

      // Don't bother with new element if start or their parent can be
      // manipulated.
      if (start == start.parentNode.firstChild &&
                 end == end.parentNode.lastChild) {
        start.parentNode.setAttribute('dir', dir);
        return;
      }

      // non-block element, wrap around with new div
      range.setStart(start, 0);
      range.setEnd(end, end.length || 0);

      var newElement = this.frameDoc.createElement('div');
      newElement.setAttribute('dir', dir);
      range.surroundContents(newElement);

      if (newElement.nextSibling != null &&
        newElement.nextSibling.nodeName == 'BR') {
        newElement.parentNode.removeChild(newElement.nextSibling);
      }

      //set end for collapse below
      end = newElement;
      MozSetRangeToEnd(end, range);
    } else { // end if(start.parentNode == end.parentNode)
      ApplyAttributeToSelectedBlocks('dir', dir);
      MozSetRangeToEnd(end, range);
    }
  } catch (e) {
    // nothing!  FF throws bogus exceptions the user won't notice
  }
};

