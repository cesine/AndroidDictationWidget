var formClean = null;
var formCollection = null;
var isSubmit = false;

/**
 * Whether to ask for confirmation onbeforeunload even if isSubmit is true.
 * E.g. used in EditPost.gxp when the user clicks the "try it now" button to
 * try the new editor.
 */
var preventDirtySubmit = false;

// Loop through form(s)... storing data in an array (cleanForm)
function cleanForm() {
  // Get all textarea data where it should be, if window has RichEdit object.
  if (isEditorAvailable()) {
    RichEdit.updateTextareas(false);
  }

  formCollection = document.body.getElementsByTagName('FORM');

  formClean = new Array(formCollection.length);
  var c = 0;

  for (var i = 0; i < formCollection.length; i++) {
    thisForm = formCollection[i];

    for (var x = 0; x < thisForm.elements.length; x++) {
      formClean[c] = getFormElementValue(thisForm, x);
      c = c + 1;
    }
  }
}


// Run through the dirty form(s), checking against the clean one for a miss-match
function dirtyForm() {
  /* If cleanForm() has not run we assume that user has not modified the
     form.  This only occurs when the user changes pages before the
     page loads. */
  if (formClean == null) {
    return true;
  }

  var c = 0;
  for (var i = 0; i < formCollection.length; i++) {
    thisForm = formCollection[i];
    for (var x = 0; x < thisForm.elements.length; x++) {
      var oldValue = formClean[c];
      var newValue = getFormElementValue(thisForm, x);
      // If the only changes are whitespace at the beginning or end of
      // the post, don't treat it as a change.
      if (thisForm.elements[x].name != 'tryNewEditor' &&
          thisForm.elements[x].name != 'returnToOldEditor' &&
          stripSpaces(oldValue) != stripSpaces(newValue)) {
            return false;
      }
      c = c + 1;
    }
  }
  return true;
}

// Strip leading and trailing spaces
function stripSpaces(str) {
  if (!str) {
    return str;
  }
  while (str.substring(0, 1) == ' ') str = str.substring(1);
  while (str.substring(str.length - 1, str.length) == ' ') {
    str = str.substring(0, str.length - 1);
  }
  return str;
}

function FlexibleSubmit(frm) {
  isSubmit = true;
  frm.submit();
}

function getFormElementValue(form, intElementIndex) {
  var el = form[intElementIndex];
  if (el.type == 'radio') {
    return getRadioElementValue(form[el.name]);
  } else if (el.type == 'checkbox') {
    return getCheckboxElementValue(form[el.name]);
  } else {
    return el.value;
  }
}

function getCheckboxElementValue(checkbox) {
  if (checkbox.checked) {
    return checkbox.value;
  } else {
    return null;
  }
}

function getRadioElementValue(radio) {
  for (var j = 0; j < radio.length; j++) {
    if (radio[j].checked) {
      return radio[j].value;
    } else {
      return null;
    }
  }
}

// onbeforeunload function. if dirty, prompt for action
function compareForm(event_) {
  if (!event_ && window.event) {
    event_ = window.event;
  }

  // need to check for BLOG_currentVideoStatus because it may not be defined
  if (window.BLOG_currentVideoUploadStatus &&
      window.BLOG_currentVideoUploadStatus ==
          window.BLOG_VideoUploadStatus.UPLOADING) {
    // video_upload_in_progress is defined in JsConstants.gxp
    return video_upload_in_progress;
  }

  // don't run if submit button was clicked
  if (!isSubmit || preventDirtySubmit) {
    var results = dirtyForm();

    if (!results) {
      // unsaved_changes is defined in JsConstants.gxp
      event_.returnValue = unsaved_changes;

      return unsaved_changes;
    }
  }
}

// Note: This assumes the predominantly 8-bit Unicode characters. If a request
// with 16-bit characters that exceeds 1MB gets through, however, it will still
// be caught on the server side and be treated as a bad request. This javascript
// check is added to be helpful, most of the time, to the user about large
// content.
function checkMaxChars(element) {
  if (element.value.length > 1000000) {
    window.alert(max_char_exceeded);
    return false;
  } else {
    return true;
  }
}

/**
 * Returns true if the window has a valid RichEdit object.
 */
function isEditorAvailable() {
  // IE 5.01 requires both checks: RichEdit is defined but not
  // valid (and therefore RichEdit.mode is not defined).

  return typeof RichEdit != 'undefined'
         && typeof RichEdit.mode != 'undefined';
}
