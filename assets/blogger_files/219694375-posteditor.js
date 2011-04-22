// Copyright 2005-2006, Google Inc.
// All Rights Reserved.

/**
 * If the editor was orginally used to create a new post that was autosaved,
 * then promote the form action to post-edit since the post now exists.
 */
function promoteCreateFormActionToEdit() {
  var form = d('stuffform');
  if (form.getAttribute('action') == '/post-create.do'
      && d('postID').value != '') {
    form.setAttribute('action', '/post-edit.do');
  }
  return true;
}

function setFormAndSubmit() {
  promoteCreateFormActionToEdit();
  isSubmit = true;

  // IE 5.01 requires both checks or else errors occur.
  if (EditorAvailable()) {
    // preserve the mode state to store in user preferences
    document.stuffform.editorModeDefault.value = RichEdit.mode;

    return RichEdit.updateTextareas(true);
  }

  return true;
}

// upload file
function uploadFile() {
  l = (screen.width / 2) - 190;
  t = (screen.height / 2) - 225;
  winOptions = window.open('/upload-file.g?blogID=' + blogId,
    'uploadFileWin',
    'scrollbars=no,status=no,width=500,height=360,left=' + l + ',top=' + t);
  winOptions.focus();
}


function RefreshModes() {
  if (EditorAvailable()
      && RichEdit.mode == RichEdit.DESIGN_MODE) {
    RichEdit.ShowRichEditor();
  }
}

/*
 * Tasks to perform if a user attempts to leave the page.
 */
window.onbeforeunload = function(e) {
  // Move the post from the IFRAME to the form, if WYSIWYG is enabled
  // and the submit handler hasn't already done so.
  if (EditorAvailable()) {
    if (!isSubmit) {
      RichEdit.updateTextareas(false);
    }
  }

  /*
   * If the form has changed, prompt the user to see if they want to
   * discard their changes.
   */
  if (!e) e = event;
  var message = compareForm(e);

  if (EditorAvailable()) {
    RichEdit.removeGeneratedFormElements();
  }

  return message;
};
