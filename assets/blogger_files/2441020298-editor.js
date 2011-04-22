/*
 * Copyright 2005 Google, Inc. All rights reserved.
 * Author: Tina Huang
 *
 * Compatible with Firefox 1.0, IE5+/Win, Safari 1.3+
 *
 * used by EditorToolbar.gxp
 */

function EditorSetRichEditFocus() {
  document.getElementById(RichEdit.frameId).contentWindow.focus();
}

function EditorButtonHoverOn(obj) {
  var parent = obj.parentNode;
  addClass(parent, 'hover');
}

function EditorButtonHoverOff(obj) {
  var parent = obj.parentNode;
  removeClass(parent, 'hover');
}

function ShowRichEditToolbar() {
  hideElement(d('previewOnlyToolbar'));
  showElement(d('richeditToolbar'));
  hideElement(d('htmlToolbar'));
  hideElement(d('richtextlink').parentNode);
  showElement(d('htmllink').parentNode);
}

function ShowSourceToolbar() {
  hideElement(d('previewOnlyToolbar'));
  hideElement(d('richeditToolbar'));
  showElement(d('htmlToolbar'));
  hideElement(d('htmllink').parentNode);
  showElement(d('richtextlink').parentNode);
}

function ShowPreviewOnlyToolbar() {
  hideElement(d('modeLinks'));
  hideElement(d('htmlToolbar'));
  hideElement(d('richeditToolbar'));
}

function EditorButtonClick(obj) {
  if ((obj.id != 'link_tt') &&
      (obj.id != 'blockquote_tt') &&
      (obj.id != 'check_spelling_tt') &&
      (obj.id != 'upload_file_tt')) {
    obj = obj.parentNode;
    if (hasClass(obj, 'depressed')) {
      removeClass(obj, 'depressed');
    } else {
      addClass(obj, 'depressed');
    }
  }

  // return the focus to the page, but pause for ol' Mozilla, who'll
  // generate an error if we move too fast
  setTimeout('EditorSetRichEditFocus()', '100');
}
